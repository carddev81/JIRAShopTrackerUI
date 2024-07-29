package com.omo.free.jira.tracker.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

import gov.doc.isu.com.util.AppUtil;

/**
 * This client class is used for searching for JIRA {@code Issue}'s instances and retrieving them from JIRA web site.  The JIRA {@code Issue}'s are modeled here using the JIRA Atlassian API.
 *
 * <p>class was created per JSTUI-14 Issue Number Search</p>
 *
 * @author Richard Salas, October 30, 2019
 */
public class SearchClient extends AbstractClient{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.client.SearchClient";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private static final SearchClient INSTANCE = new SearchClient();

    private List<Issue> issuesSearchedFor;
    private String searchStr;

    /**
     * Default constructor used to create an instance of the SearchClient class.
     */
    private SearchClient() {    }//end constructor

    /**
     * This method will return the single instance of the SearchClient class.
     *
     * @return INSTANCE the SearchClient class
     */
    public static SearchClient getInstance(){
        return INSTANCE;
    }//end method

    /**
     * This method will search for an issue based on the {@code issueKey} passed into this method.
     *
     * <p>NOTE:  this method will just add the issue to the existing {@code issuesSearchedFor} list, therefore this may need to be updated.
     *
     * @param issueKey the issue key to searh for
     * @return isTracked whether or not the issue is tracked
     */
    public void searchForAndAddIssue(String issueKey, String projectKey) {
        myLogger.entering(MY_CLASS_NAME, "searchForAndAddIssue", issueKey);

        if(issuesSearchedFor == null){
            this.issuesSearchedFor = new ArrayList<>();
        }//end if

        //clear search list...
        this.issuesSearchedFor.clear();

        //declaring some variables to use here
        String searchQuery = null;
        String inFunctionQueryStr = null;
        Iterable<Issue> iterable = null;
        JiraRestClient client = null;
        int maxKey = 0;
        //retrieve the greatest one
        try{
            searchQuery = "project = " + projectKey + " AND created < now() ORDER BY created desc";// need to explain this thoroughly...

            client = client();
            iterable = client.getSearchClient().searchJql(searchQuery, 1, 0, null).claim().getIssues();
            if(iterable != null){
                Issue issue = iterable.iterator().next();
                myLogger.info("The issue that was retrieved is: " + issue.getKey());

                myLogger.info("Going to parse the number from the key and use it for the top number.");
                maxKey = Integer.valueOf(issue.getKey().substring(issue.getKey().indexOf("-")+1));
                inFunctionQueryStr = processSearchStr(issueKey, projectKey, Integer.valueOf(issue.getKey().substring(issue.getKey().indexOf("-")+1)));
                if(AppUtil.isNullOrEmpty(inFunctionQueryStr)){
                    myLogger.warning("There was no search string built based off of the search string using the max key id of " + maxKey);
                }else{
                    searchQuery = "project = " + projectKey + " AND issueKey IN ( " + inFunctionQueryStr + ")";// need to explain this thoroughly...
                    myLogger.info("Running search JQL to retrieve issues from JIRA website.  Executing the following searchQuery: " + searchQuery);
                    iterable = client.getSearchClient().searchJql(searchQuery, 100, 0, null).claim().getIssues();
                    Iterator<Issue> it = iterable.iterator();
                    while(it.hasNext()){
                        Issue i = it.next();
                        if(i.getKey().contains(projectKey)){//if the issue does
                            this.issuesSearchedFor.add(i);
                        }//end if
                    }// end while
                }//end if
            }//end if
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception trying to get issues.  Error message is: " + e.getMessage());
            throw e;
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "searchForAndAddIssue");
    }//end method

    /**
     * This method will parse the passed in issueSearchKey into a listing of issues to search for using the projectKey.
     *
     * @param issueSearchKey the string that will be parsed for example (*886)
     * @param projectKey the key used to build the search string
     * @param maxNum the maximum key number
     * @return the search string
     */
    private String processSearchStr(String issueSearchKey, String projectKey, int maxNum){
        myLogger.entering(MY_CLASS_NAME, "processSearchStr", new Object[]{issueSearchKey, projectKey});

        myLogger.info("issueSearchKey=" + issueSearchKey + "; projectKey=" + projectKey);
        int wildCards = StringUtils.countMatches(issueSearchKey, "*");
        String cleanedFilterStr = null;

        boolean startsWith = false;
        boolean endsWith = false;
        boolean contains = false;

        StringBuilder searchBuilder = new StringBuilder();
        if(wildCards == 2 && (!issueSearchKey.endsWith("*") || !issueSearchKey.startsWith("*"))){
            //correct it here....
            //cleanedFilterStr = searchStr.replace("*", "");
            this.searchStr = issueSearchKey.replace("*", "");
            this.searchStr = "*" + this.searchStr + "*";
            contains = true;
        }else if(wildCards == 2){
            contains = true;
            this.searchStr = issueSearchKey;
        }else if(wildCards == 1 && (issueSearchKey.endsWith("*") || issueSearchKey.startsWith("*"))){
            //everything is good here just set text
            startsWith = issueSearchKey.endsWith("*");
            endsWith = issueSearchKey.startsWith("*");
            this.searchStr = issueSearchKey;
        }else if(wildCards == 1){
            //correct it the one here
            this.searchStr = issueSearchKey.replace("*", "");
            this.searchStr = "*" + this.searchStr;
            startsWith = true;
        }else{
            //good one here
            this.searchStr = issueSearchKey;
            cleanedFilterStr = issueSearchKey;
        }//end if...else

        cleanedFilterStr = issueSearchKey.replace("*", "");

        myLogger.info("cleanedFilterStr=" + cleanedFilterStr);

        List<String> searchKeys = new ArrayList<>();
        for(int i = 1; i <= maxNum; i++){
            if(contains && String.valueOf(i).contains(cleanedFilterStr)){
                searchKeys.add(projectKey + "-" + i);
            }else if(endsWith && String.valueOf(i).endsWith(cleanedFilterStr)){
                searchKeys.add(projectKey + "-" + i);
            }else if(startsWith && String.valueOf(i).startsWith(cleanedFilterStr)){
                searchKeys.add(projectKey + "-" + i);
            }else if(String.valueOf(i).equals(cleanedFilterStr)){
                searchKeys.add(projectKey + "-" + i);
                break;
            }//end if...else
        }//end for

        myLogger.info("searchKeys.size()="+ searchKeys.size());
        //build the search string here
        Iterator<String> keys = searchKeys.iterator();
        while(keys.hasNext()){
            searchBuilder.append(keys.next());
            if(keys.hasNext()){
                searchBuilder.append(",");
            }//end if
        }//end while

        myLogger.exiting(MY_CLASS_NAME, "processSearchStr", searchBuilder);
        return searchBuilder.toString();
    }//end method

    /**
     * This method will return the list of issues in ascending order that were retrieved from a search.
     *
     * @return list of issues that were searched for
     */
    public List<Issue> getIssuesSearchedFor(){
        myLogger.entering(MY_CLASS_NAME, "getIssuesSearchedFor");

        if(!AppUtil.isEmpty(issuesSearchedFor)){//sort per JSTUI-14 Issue Number Search
            //sorting the issues that are tracked
            Collections.sort(issuesSearchedFor, new Comparator<Issue>(){
                @Override
                public int compare(Issue o1, Issue o2) {
                    int result = 0;
                    try{
                        Integer key1 = Integer.valueOf(o1.getKey().substring(o1.getKey().indexOf("-") + 1));
                        Integer key2 = Integer.valueOf(o2.getKey().substring(o2.getKey().indexOf("-") + 1));
                        result = key1.compareTo(key2);
                    }catch(Exception e){
                        myLogger.warning("Exception occurred while trying to parse issue key. o1 key is:  " + String.valueOf(o1.getKey()) + "; o2 key is: " + String.valueOf(o2.getKey()) + ".  Error message is: " + e.getMessage());
                    }//end try...catch
                    return result;
                }//end method
            });
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "getIssuesSearchedFor", issuesSearchedFor);
        return issuesSearchedFor;
    }//end method

    /**
     * Search for issues...not used at this time...maybe it will later This will be a custom search.....
     *
     * @param query
     */
    public void executeSearchQuery(String query) {
      //XXXXXXXXXXXXXXXXXXXXXXX
        //        if(issues == null){
        //            issues = new ArrayList<>();
        //        }else{
        //            issues.clear();
        //        }// end if
      //XXXXXXXXXXXXXXXXXXXXXXX

        try{
            myLogger.info("Executing the following searchQuery: " + query);
            Iterator<Issue> it = client().getSearchClient().searchJql(query).claim().getIssues().iterator();
            while(it.hasNext()){
              //XXXXXXXXXXXXXXXXXXXXXXX
//                issues.add(it.next());
              //XXXXXXXXXXXXXXXXXXXXXXX
            }// end while
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception trying to get issues.  Error message is: " + e.getMessage());
        }// end try...catch
    }// end method

    /**
     * @return the searchStr
     */
    public String getSearchStr() {
        return searchStr;
    }//end method

}//end class
