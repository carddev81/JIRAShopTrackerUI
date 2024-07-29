package com.omo.free.jira.tracker.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;

import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.managers.UIPropertiesMgr;

/**
 * This client class is used for retrieving JIRA {@code Project}'s instances from JIRA web site.  The JIRA {@code Project}'s are modeled here using the JIRA Atlassian API.
 *
 * @author Richard Salas, October 30, 2019
 */
public class ProjectClient extends AbstractClient{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.client.ProjectClient";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private static final ProjectClient INSTANCE = new ProjectClient();

    /* project fields that will be always be used ... can add more if needed here */
    private static List<String> KNOWN_PROJECT_KEY_LIST = Arrays.asList("COIH", "DOCARB", "DOCLENS", "DOCMSHP", "DOCOIM", "OPII", "DOCTMS", "IRISBATCH", "JSTUI", "MOCIS", "MODOCFEES", "PANDA", "TABEBATCH");

    private List<String> projects;

    /**
     * Default constructor used to create an instance of the ProjectClient class.
     */
    private ProjectClient() {    }

    /**
     * This method will return the single instance of the ProjectClient class.
     *
     * @return INSTANCE the ProjectClient class
     */
    public static ProjectClient getInstance(){
        return INSTANCE;
    }//end method

    /**
     * This method will connect to the JIRA web site to retrieve all projects and set {@code projects} variable.
     */
    public void setProjects() {
        myLogger.entering(MY_CLASS_NAME, "setProjects");

        if(projects == null){
            projects = new ArrayList<>();
        }else{
            projects.clear();
        }// end if

        JiraRestClient client = null;

        myLogger.info("Making call to jira client to retrieve projects");
        try{
            client = client();
            Iterator<BasicProject> it = client.getProjectClient().getAllProjects().claim().iterator();
            Project proj = null;
            String key = null;
            while(it.hasNext()){
                //JSTUI-7 select project field...Richard Salas - added functionality in hopes of helping to drop off some of the selection options
                key = String.valueOf(it.next().getKey());
                if(KNOWN_PROJECT_KEY_LIST.contains(key.trim())){
                    projects.add(key);
                }else{
                    //do a check to see if there are any more projects based on issue types...
                    proj = client.getProjectClient().getProject(key).claim();
                    if(String.valueOf(proj.getIssueTypes()).contains("DOC ")) {
                        projects.add(key);
                        myLogger.info("size of project list is: " + projects.size());
                        myLogger.info("adding following project: " + String.valueOf(proj));
                    }//end if
                }//end if...else
            }// end while
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to retrieve list of jira projects.  Error is: " + e.getMessage(), e);
            if(e.getMessage() != null && e.getMessage().contains("Unauthorized (401)")){
                myLogger.warning("User is not authorized!  User is: " + String.valueOf(UIPropertiesMgr.getInstance().getProperties().getProperty("jira.user")));
                this.isAuthorized = false;
            }//end if
            throw e;
        }//end try...catch

        myLogger.exiting(MY_CLASS_NAME, "setProjects");
    }// end method

    /**
     * This method will return a list of JIRA projects (known as project key).
     *
     * @return projects the list of project names (keys)
     */
    public List<String> getProjects() {
        myLogger.info("all available projects:" + String.valueOf(projects));

        if(!AppUtil.isEmpty(projects)){//JIRA ISSUE JSTUI 2 Sort the Select Project List
            Collections.sort(projects);
        }//end if

        return projects;
    }// end method

}//end class
