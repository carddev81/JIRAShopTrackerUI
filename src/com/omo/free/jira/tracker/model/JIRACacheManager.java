package com.omo.free.jira.tracker.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.Issue;

/**
 * This JIRACacheManager class is used to contain a cache of the most common data that was retrieved from the JIRA website.
 *
 * This is to give user a better experience during there viewing of the data.
 *
 * <p>Note that this needs to be tested before using</p>
 */
public class JIRACacheManager {

    private static volatile JIRACacheManager SINGLETON;

    private Map<String, List<Issue>> untrackedIssuesMap;
    private Map<String, List<Issue>> trackedIssuesMap;

    /**
     * Default constructor used to create an instance of the JIRACacheManager.
     */
    private JIRACacheManager() {
        untrackedIssuesMap = new HashMap<>();
        trackedIssuesMap = new HashMap<>();
    }//end constructor

    /**
     * This method will return the single instance of the {@code JIRACacheManager}.
     *
     * @return SINGLETON single instance of this class
     */
    public static JIRACacheManager getInstance(){
        if(SINGLETON == null){
            synchronized(JIRACacheManager.class){//double-checked locking
                if(SINGLETON == null){
                    SINGLETON = new JIRACacheManager();
                }//end if
            }//end synchronized
        }//end if
        return SINGLETON;
    }//end method

    public boolean containsStatusKeyForProject(String project, String statusKey){
        //validateProject(project);
        return untrackedIssuesMap.containsKey(project + "|" + statusKey);
    }//end method

    /**
     * This method will add a list of untracked {@code Issue}'s to the cache.
     *
     * @param statusKey
     * @param untrackedIssues list of untracked {@code Issue} instance
     */
    public synchronized void addUntrackedIssuesToCache(String project, String statusKey, List<Issue> untrackedIssues){
        //validateProject(project);
        untrackedIssuesMap.put(project + "|" + statusKey, new ArrayList<Issue>(untrackedIssues));
    }//end method

    /**
     * This method will add a list of untracked {@code Issue}'s to the cache.
     *
     * @param projectAndStatusKey the system name
     * @param trackedIssues list of tracked {@code Issue}'s
     */
    public synchronized void addTrackedIssuesToCache(String project, String statusKey, List<Issue> trackedIssues){
        //validateProject(project);
        trackedIssuesMap.put(project + "|" + statusKey, new ArrayList<Issue>(trackedIssues));
    }//end method

    /**
     * This method will add a list of untracked {@code Issue}'s to the cache.
     *
     * @param statusKey
     * @param untrackedIssues list of untracked {@code Issue} instance
     */
    public List<Issue> getUntrackedIssuesFromCache(String project, String statusKey){
        //validateProject(project);
        return untrackedIssuesMap.get(project + "|" + statusKey);
    }//end method

    /**
     * This method will add a list of untracked {@code Issue}'s to the cache.
     *
     * @param projectAndStatusKey the system name
     * @param trackedIssues list of tracked {@code Issue}'s
     */
    public List<Issue> getTrackedIssuesFromCache(String project, String statusKey){
        //validateProject(project);
        return trackedIssuesMap.get(project + "|" + statusKey);
    }//end method

//    /**
//     * This will validate the project here and if the cache manager has no issues relating to this project the the cache will be cleared.
//     *
//     * @param project the project to validate against
//     */
//    private void validateProject(String project) {
//        if(this.project != project){
//            clearCache();//clear the cache here
//            this.project = project;//set new project name here
//        }//end if
//    }//end if

    /**
     * Clear the cache.
     */
    public synchronized void clearCache(){
        untrackedIssuesMap.clear();
        trackedIssuesMap.clear();
    }//end method

}//end class
