package com.omo.free.jira.tracker.ui.tasks;

import java.util.Collections;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.omo.free.jira.tracker.client.DownloadClient;

import javafx.concurrent.Task;

/**
 * This class is intended to run the build of the HTML string on a separate thread.
 *
 * @author Richard Salas, April 17, 2019
 */
public class BuildIssue extends Task<String> {

    private Issue jiraIssue;

    /**
     * Constructor used to build the html string using the {@code jiraIssue} and the {@code model} parameters.
     *
     * @param jiraIssue the issue containing the metadata used for building the html string
     */
    public BuildIssue(Issue jiraIssue) {
        this.jiraIssue = jiraIssue;
    }//end constructor

    /**
     * This method runs the task for downloading, packaging, and emailing of Jira Issues.
     *
     * @throws Exception if an error occurs
     */
    @Override
    protected String call() throws Exception {
        String jiraHtml = null;
        jiraHtml = DownloadClient.getInstance().createJIRAIssueHTMLFileAsStr(jiraIssue, Collections.emptyList());
        return jiraHtml;
    }//end method

}//end class
