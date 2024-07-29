package com.omo.free.jira.tracker.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

/**
 * The JIRACacheLoaderThread handles making a call to the JIRA website to retrieve issues to for loading into the cache.
 *
 * @author Richard Salas May 05 2019
 */
public class JIRACacheLoaderThread implements Runnable{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.model.JIRACacheLoaderThread";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private String project;
    private JIRAStatus status;
    private JiraRestClient client;

    /**
     * Constructor used to create an instance of the JIRACacheLoaderThread.
     *
     * @param client the jira client used to make webservice calls
     * @param status the jira status to select issues for
     * @param project the project name
     */
    public JIRACacheLoaderThread(JiraRestClient client, JIRAStatus status, String project) {
        myLogger.entering(MY_CLASS_NAME, "JIRACacheLoaderThread", new Object[]{client, status, project});

        this.status = status;
        this.project = project;
        this.client = client;

        myLogger.exiting(MY_CLASS_NAME, "JIRACacheLoaderThread");
    }//end constructor

    /**
     * This method will run the logic for retrieving jira issues per status for loading the cache.
     */
    @Override
    public void run() {
        myLogger.entering(MY_CLASS_NAME, "run");

        List<Issue> allIssues = new ArrayList<Issue>();
        try{
            myLogger.info("Loading issues with a status of " + status.getValue() + " for cache.");

            String searchQuery = "project = " + String.valueOf(project) + " AND status in (" + String.valueOf(status.getId()) + ")";// need to explain this thoroughly...
            myLogger.info("Running search JQL to retrieve issues from JIRA website.  Executing the following searchQuery: " + searchQuery);

            Iterable<Issue> iterable = client.getSearchClient().searchJql(searchQuery, 75, 0, null).claim().getIssues();
            Iterator<Issue> it = iterable.iterator();
            while(it.hasNext()){
                Issue i = it.next();
                allIssues.add(i);
                //myLogger.info("attachment uri: " + String.valueOf(i.getAttachmentsUri()));
//                allIssues.add(client.getIssueClient().getIssue(i.getKey(), Arrays.asList(IssueRestClient.Expandos.CHANGELOG)).claim());
            }// end while
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to load cache.  Exception is: " + e.getMessage(), e);
        }//end try...catcch

        myLogger.info("number of issues retrieved for status of " + status.getValue() + " is " + allIssues.size());
        JIRACacheManager.getInstance().addUntrackedIssuesToCache(project, status.getId(), allIssues);

        myLogger.exiting(MY_CLASS_NAME, "run");
    }//end method

}//end method
