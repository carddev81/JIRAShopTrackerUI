package com.omo.free.jira.tracker.ui.tasks;

import java.util.logging.Logger;

import com.omo.free.jira.tracker.client.IssueClient;
import com.omo.free.jira.tracker.client.ProjectClient;

import javafx.concurrent.Task;

/**
 * This class is used to load the JIRA Client instances by calling their methods to initialize it's state.
 *
 * @author Richard Salas, October 30, 2019
 */
public class JIRAClientPrimer extends Task<Void> {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.tasks.JIRAClientPrimer";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private String project;
    private String statusId;

    /**
     * Constructor used to create an instance of the JIRAClientPrimer class.
     *
     * @param project the project key used for retrieving issues
     * @param statusId the status id number of issues to retrieve.
     */
    public JIRAClientPrimer(String project, String statusId) {
        myLogger.entering(MY_CLASS_NAME, "JIRAClientPrimer", new Object[]{project, statusId});

        this.project = project;
        this.statusId = statusId;

        myLogger.exiting(MY_CLASS_NAME, "JIRAClientPrimer");
    }//end constructor

    /**
     * This method runs the task for executing the client methods to initialize client instances state.
     *
     * @throws Exception if an error occurs
     */
    @Override
    protected Void call() throws Exception {
        myLogger.entering(MY_CLASS_NAME, "call");

        ProjectClient projectClient = ProjectClient.getInstance();

        if(projectClient.getProjects() == null){//check to see if the projects is null
            projectClient.setProjects();
        }//end if
        IssueClient.getInstance().setIssues(this.project, statusId);

        myLogger.exiting(MY_CLASS_NAME, "call");
        return null;
    }//end method

}//end class
