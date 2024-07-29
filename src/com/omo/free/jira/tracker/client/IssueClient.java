package com.omo.free.jira.tracker.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.omo.free.jira.tracker.dao.JIRATrackerDAO;
import com.omo.free.jira.tracker.model.JIRACacheLoaderThread;
import com.omo.free.jira.tracker.model.JIRACacheManager;
import com.omo.free.jira.tracker.model.JIRAStatus;
import com.omo.free.jira.tracker.model.JIRATrackedIssue;

import gov.doc.isu.com.util.AppUtil;

/**
 * This client class is used for retrieving JIRA {@code Issue}'s instances from JIRA web site.  The JIRA {@code Issue}'s are modeled here using the JIRA Atlassian API.
 *
 * @author Richard Salas, October 30, 2019
 */
public class IssueClient extends AbstractClient{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.client.IssueClient";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private static final IssueClient INSTANCE = new IssueClient();

    /* instance variables */
    private String projectKey;
    private String status;
    private List<Issue> issues;
    private List<Issue> trackedIssues;
    private JIRATrackerDAO dao;

    /**
     * Default constructor used to create an instance of the IssueClient class.
     */
    private IssueClient() {
        myLogger.entering(MY_CLASS_NAME, "IssueClient");
        try{
            this.isAuthorized = true;
            this.dao = new JIRATrackerDAO();
        }catch(Exception e){
            errorFlag = true;
            myLogger.log(Level.WARNING, "*****************************Could not obtain a connection to the JIRA Tracker Database*****************************");
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "IssueClient");
    }//end method

    /**
     * This method will return the single instance of the IssueClient class.
     *
     * @return INSTANCE the IssueClient class
     */
    public static IssueClient getInstance(){
        return INSTANCE;
    }//end method

    /**
     * This method will return a list of untracked jira issues sorted in descending order by Creation Date.
     *
     * @return issues the untracked jira issues
     */
    public List<Issue> getIssues() {
        myLogger.entering(MY_CLASS_NAME, "getIssues");

        if(issues == null){
            issues = new ArrayList<>();
        }//end if

        if(!AppUtil.isEmpty(issues)){
            //sorting the issues that are tracked
            Collections.sort(issues, new Comparator<Issue>(){
                @Override
                public int compare(Issue o1, Issue o2) {
                    int result = 0;
                    if(o2.getCreationDate() == null && o1.getCreationDate() == null){
                        result = 0;
                    }else if(o2.getCreationDate() == null){
                        result = -1;
                    }else if(o1.getCreationDate() == null){
                        result = 1;
                    }else{
                        result = o2.getCreationDate().compareTo(o1.getCreationDate());
                    }//end if...else
                    return result;
                }//end method
            });
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "getIssues", (issues != null ? "number of untracked issues is: " + String.valueOf(issues.size()) : "empty"));
        return issues;
    }// end method

    /**
     * This method will return a list of tracked jira issues sorted in descending order by Update Date.
     *
     * @return issues the tracked jira issues
     */
    public List<Issue> getTrackedIssues() {
        myLogger.entering(MY_CLASS_NAME, "getTrackedIssues");
        if(myLogger.isLoggable(Level.FINE)){
            myLogger.fine("all issues tracked:" + String.valueOf(trackedIssues));
        }// end if

        if(trackedIssues == null){
            trackedIssues = new ArrayList<>();
        }//end if

        if(!AppUtil.isEmpty(trackedIssues)){//TODO also sort this by create date if there is no update date...?
            //sorting the issues that are tracked
            Collections.sort(trackedIssues, new Comparator<Issue>(){
                @Override
                public int compare(Issue o1, Issue o2) {
                    int result = 0;
                    if(o2.getUpdateDate() == null && o1.getUpdateDate() == null){
                        result = 0;
                    }else if(o2.getUpdateDate() == null){
                        result = -1;
                    }else if(o1.getUpdateDate() == null){
                        result = 1;
                    }else{
                        result = o2.getUpdateDate().compareTo(o1.getUpdateDate());
                    }//end if...else
                    return result;
                }//end method
            });
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "getTrackedIssues", (trackedIssues != null ? "number of untracked issues is: " + String.valueOf(trackedIssues.size()) : "empty"));
        return trackedIssues;
    }//end method

    /**
     * This method will connect to jira web site and retrieve issues based on the project key and status id passed into this method.
     *
     * @param projectKey the project key used for searching for issues
     * @param statusId the status id used for searching for issues
     */
    public void setIssues(String projectKey, String statusId) {
        myLogger.entering(MY_CLASS_NAME, "setIssues", new Object[]{projectKey, statusId});

        if(issues == null){//initialize the issues list
            issues = new ArrayList<>();
        }else{//clear the issues list
            issues.clear();
        }// end if

        if(trackedIssues == null){//initialize the trackedIssues list
            trackedIssues = new ArrayList<>();
        }else{//clear the trackedIssues list
            trackedIssues.clear();
        }// end if...else
         // projectKey

        this.projectKey = projectKey;
        this.errorFlag = false;

        String searchQuery = null;
        Iterable<Issue> iterable = null;
        JiraRestClient client = null;
        List<Issue> allIssues = null;
        JIRACacheManager cache = JIRACacheManager.getInstance();
        try{
            //if !isRefresh && containsStatus
            if(!isRefresh() && cache.containsStatusKeyForProject(projectKey, statusId)){
                allIssues = cache.getUntrackedIssuesFromCache(projectKey, statusId);
            }else{
                if("0".equals(statusId)){
                    /*
                     * 1=OPEN
                     * 3=In Progress
                     * 10102=Production Fix
                     * 10736=In Development
                     * 10737=In Testing
                     * 10738=Ready to Migrate
                     * 10111=Research
                     * 10047=Researching Issue
                     */
                    searchQuery = "project = " + String.valueOf(projectKey) + " AND status in (1, 3, 10102, 10736, 10737, 10738, 10111, 10047)";// need to explain this thoroughly...see above descriptions...
                    this.status = "0";
                }else{
                    searchQuery = "project = " + String.valueOf(projectKey) + " AND status in (" + String.valueOf(statusId) + ")";// need to explain this thoroughly...
                    this.status = statusId;
                }//end if...else

                myLogger.info("Running search JQL to retrieve issues from JIRA website.  Executing the following searchQuery: " + searchQuery);
                client = client();

                iterable = client.getSearchClient().searchJql(searchQuery, 75, 0, null).claim().getIssues();
                allIssues = new ArrayList<Issue>();

                Iterator<Issue> it = iterable.iterator();
                while(it.hasNext()){
                    Issue i = it.next();
                    myLogger.info("attachment uri: " + String.valueOf(i.getAttachmentsUri()));//all uri's exist!
                    myLogger.info("attachments: " + String.valueOf(i.getAttachments()));//all uri's exist!
                    myLogger.info("attachments: " + String.valueOf(i.getFieldByName("Attachments")));//all uri's exist!
//                    allIssues.add(client.getIssueClient().getIssue(i.getKey(), Arrays.asList(IssueRestClient.Expandos.CHANGELOG)).claim());//this is too slow
                    allIssues.add(i);
                }// end while
            }//end if...else

            if(this.dao == null){
                myLogger.info("JIRA Tracker database not accessible going to just add all issues to issues list.");
                for(int i = 0, j = allIssues.size();i < j;i++){
                    issues.add(allIssues.get(i));
                }// end for
                cache.addUntrackedIssuesToCache(projectKey, statusId, issues);
            }else{
                Issue issue = null;
                List<JIRATrackedIssue> jiraIssues = dao.getJIRAIssuesByProjectKey(projectKey);
                JIRATrackedIssue compareIssue = null;

                if(!AppUtil.isEmpty(cache.getTrackedIssuesFromCache(projectKey, statusId)) && !isRefresh()){//add the tracked issues here when not refreshed
                    trackedIssues.addAll(cache.getTrackedIssuesFromCache(projectKey, statusId));
                }//end if

                for(int i = 0, j = allIssues.size();i < j;i++){
                    issue = allIssues.get(i);
                    compareIssue = new JIRATrackedIssue(issue.getKey());
                    if(jiraIssues.contains(compareIssue)){
                        trackedIssues.add(issue);
                    }else{
                        issues.add(issue);
                    }// end if
                }// end for

                //add to cache here
                cache.addTrackedIssuesToCache(projectKey, statusId, trackedIssues);
                cache.addUntrackedIssuesToCache(projectKey, statusId, issues);

            }// end if...else
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception trying to get issues.  Error message is: " + e.getMessage());
            errorFlag = true;
            issues.clear();
            trackedIssues.clear();
            myLogger.info("JIRA Tracker database not accessible going to just add all issues to issues list.");
            for(int i = 0, j = allIssues.size();i < j;i++){
                issues.add(allIssues.get(i));
            }// end for
            cache.addUntrackedIssuesToCache(projectKey, statusId, issues);
        }finally{
            this.setRefresh(false);//always set to false to reset value on every call of this method
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "setIssues");
    }// end method

    /**
     * This method will retrieve all of the tracked issues that were logged within the shared database file.
     *
     * <p>NOTE:  maybe load this into the cache at a later time
     */
    public void retrieveAllTrackedIssues() throws Exception{
        myLogger.entering(MY_CLASS_NAME, "retrieveAllTrackedIssues");

        //clear lists...note that these lists will be created already no need to check for null.

        String searchQuery = null;
        Iterable<Issue> iterable = null;
        JiraRestClient client = null;

        try{
            if(this.dao == null){
                myLogger.warning("JIRA Tracker database not accessible, therefore could not load the listing of tracked issues.");
                throw new Exception("Could not access the listing of tracked issues.");
            }else{
                Issue issue = null;
                searchQuery = "issueKey = %s";// need to explain this thoroughly...
                List<JIRATrackedIssue> jiraIssues = dao.getJIRAIssuesByProjectKey(projectKey);
                client = client();

                //clear lists here
                this.issues.clear();
                this.trackedIssues.clear();
                for(int i = 0, j = jiraIssues.size(); i < j; i++){
                    iterable = client.getSearchClient().searchJql(String.format(searchQuery, String.valueOf(jiraIssues.get(i).getIssueKey())), 2, 0, null).claim().getIssues();

                    Iterator<Issue> it = iterable.iterator();
                    while(it.hasNext()){
                        issue = it.next();
                        this.trackedIssues.add(issue);
                    }//end while
                }//end for
            }// end if...else
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to retrieve the tracked issues.  Error message is: " + e.getMessage(), e);
            trackedIssues.clear();
            throw new Exception("Could not access the listing of tracked issues.");
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "retrieveAllTrackedIssues");
    }//end method

    /**
     * This method will load the jira issues cache sorted by project|status.
     */
    public void loadJiraCache(){
        ThreadPoolExecutor threadExecutor = null;

        try{
            // spawn some threads here to see if this will work as intended.
            threadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);// handles the multiple background threads needed for this application. setting it to 4 here.

            //mocis only for now!!!  have to do a load test sometime just to see.
            JiraRestClient client = client();
            List<JIRAStatus> statusList = JIRAStatus.getJIRAStatuses();

            myLogger.info("Attempting to load some cache.");
            for(int i = 1, j = statusList.size(); i < j; i++){
                threadExecutor.execute(new JIRACacheLoaderThread(client, statusList.get(i), "MOCIS"));
            }//end for
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to load jira cache", e);
        }finally{
            if(threadExecutor != null){
                threadExecutor.shutdown();
            }//end if
        }//end try...catch

    }//end method

    /**
     * This method will update Tracked JIRA Issues.
     *
     * @param jiraIssues the issues to update
     */
    public void updateTrackedIssue(List<JIRATrackedIssue> jiraIssues) {
        myLogger.entering(MY_CLASS_NAME, "saveTrackedIssue", jiraIssues);

        if(dao == null){
            myLogger.warning("DAO is null therefore cannot update tracked issue.");
        }else{
            try{
                dao.update(jiraIssues);
            }catch(Exception e){
                myLogger.log(Level.SEVERE, "Exception occurred trying to update tracked issues.  Error is: " + e.getMessage(), e);
            }// end try...catch
        }// end if
        myLogger.exiting(MY_CLASS_NAME, "saveTrackedIssue");
    }// end method

    /**
     * This method will save Tracked JIRA Issues
     * @param jiraIssues the issues to save
     */
    public void saveTrackedIssue(List<JIRATrackedIssue> jiraIssues) {
        myLogger.entering(MY_CLASS_NAME, "saveTrackedIssue", jiraIssues);

        if(dao == null){
            myLogger.warning("DAO is null therefore cannot save tracked issue.");
        }else{
            try{
                dao.insert(jiraIssues);
            }catch(Exception e){
                myLogger.log(Level.SEVERE, "Exception occurred trying to save new tracked issues.  Error is: " + e.getMessage(), e);
            }// end try...catch
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "saveTrackedIssue");
    }// end method

    /**
     * @return the dao
     */
    public JIRATrackerDAO getJIRATrackerDAO() {
        return dao;
    }// end method

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }// end method

    /**
     * @return the projectKey
     */
    public String getProjectKey() {
        return projectKey;
    }// end method

    /**
     * This method will return a list of untracked jira issues sorted in descending order by Creation Date.
     *
     * @return issues the untracked jira issues
     */
    public void clearIssues() {
        myLogger.entering(MY_CLASS_NAME, "getIssues");
        issues.clear();
        myLogger.exiting(MY_CLASS_NAME, "getIssues", (issues != null ? "number of untracked issues is: " + String.valueOf(issues.size()) : "empty"));
    }// end method

}//end class
