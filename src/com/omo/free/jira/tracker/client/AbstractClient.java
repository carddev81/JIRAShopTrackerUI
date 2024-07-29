package com.omo.free.jira.tracker.client;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.util.JiraUtil;

import gov.doc.isu.simple.fx.managers.UIPropertiesMgr;

/**
 * This abstract class should be extended by client classes to connect to JIRA resources for obtaining data for the JIRA Shop Tracker UI.  The JIRA {@code Issue}'s, {@code Project}'s, and {@code Attachments}'s are modeled here using the JIRA Atlassian API.
 *
 * @author Richard Salas, October 30, 2019
 */
public abstract class AbstractClient {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.client.AbstractClient";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    protected boolean errorFlag;
    protected boolean isAuthorized;
    private boolean isRefresh;

    /**
     * Default constructor used to create an instance of the AbstractClient class.
     */
    public AbstractClient() {    }//end constructor

    /**
     * This method will attempt to make a connection return an instance of {@code JiraRestClient}.
     *
     * @return jiraClient the restClient
     */
    public JiraRestClient client() {
        myLogger.entering(MY_CLASS_NAME, "client");

        // resources here
        URI jiraServerUri = null;
        AsynchronousJiraRestClientFactory jiraClientfactory = null;
        JiraRestClient jiraClient = null;
        String user = null;
        String pass = null;
        try{
            // initialize user and pass word
            user = JiraUtil.decrypt(UIPropertiesMgr.getInstance().getProperties().getProperty("jira.user"));
            pass = JiraUtil.decrypt(UIPropertiesMgr.getInstance().getProperties().getProperty("jira.pass"));

            jiraServerUri = URI.create(JIRAConstants.JIRA_URL);
            myLogger.info("created URI " + JIRAConstants.JIRA_URL);

            jiraClientfactory = new AsynchronousJiraRestClientFactory();
            jiraClient = jiraClientfactory.createWithBasicHttpAuthentication(jiraServerUri, user, pass);

            if(myLogger.isLoggable(Level.FINE)){
                int buildNumber = jiraClient.getMetadataClient().getServerInfo().claim().getBuildNumber();
                myLogger.fine("version of jira being used is: " + String.valueOf(buildNumber));
            }//end if

            myLogger.info("successful connection to jira...(created jiraClient");
            this.isAuthorized = true;
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception while trying to connect to jira.  Error message is: " + e.getMessage() + ".  Values of interest are: JIRAConstants.JIRA_URL=" + String.valueOf(JIRAConstants.JIRA_URL) + "; jiraClientfactory=" + String.valueOf(jiraClientfactory) + "; jiraClient=" + String.valueOf(jiraClient), e);

            //check to see if message contains the authorized
            if(e.getMessage() != null && e.getMessage().contains("Not Authorized")){
                myLogger.warning("User is not authorized!  User is: " + String.valueOf(user));
                this.isAuthorized = false;
            }//end if
            throw e;
        }// end method

        myLogger.exiting(MY_CLASS_NAME, "client", jiraClient);
        return jiraClient;
    }// end method

    /**
     * @return the errorFlag
     */
    public boolean isErrorFlag() {
        return errorFlag;
    }// end method

    /**
     * @return the isAuthorized
     */
    public boolean isAuthorized() {
        return isAuthorized;
    }//end method

    /**
     * @return the isRefresh
     */
    public boolean isRefresh() {
        return isRefresh;
    }

    /**
     * @param isRefresh the isRefresh to set
     */
    public void setRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }//end method

}//end class
