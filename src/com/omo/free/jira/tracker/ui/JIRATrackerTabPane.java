package com.omo.free.jira.tracker.ui;

import java.util.logging.Logger;

import javafx.geometry.Side;
import javafx.scene.control.TabPane;

/**
 * This class is the custom JIRATrackerTabPane used to display the interactive tabs to the user.
 *
 * @author Richard Salas, April 17, 2019
 */
public class JIRATrackerTabPane extends TabPane{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.JIRATrackerTabPane";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private JIRAWindowBuilder parent;
    private JIRAIssuesTab issuesTab;
    private JIRASentIssuesTab sentIssuesTab;
    private JIRASearchResultsTab resultsTab;
    private JIRAReportTab reportsTab;

    /**
     * Constructor used to create an instance of the JIRATrackerTabPane.
     *
     * @param parent the SFXViewBuilder implementing instance
     */
    public JIRATrackerTabPane(JIRAWindowBuilder parent) {
        myLogger.entering(MY_CLASS_NAME, "JIRATrackerTabPane", parent);

        this.parent = parent;
        layoutForm();

        myLogger.exiting(MY_CLASS_NAME, "JIRATrackerTabPane");
    }//end constructor

    /**
     * This method will layout the user interface tab pane for user to view.
     */
    private void layoutForm() {
        myLogger.entering(MY_CLASS_NAME, "layoutForm");

        setSide(Side.TOP);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        issuesTab = new JIRAIssuesTab(parent);
        sentIssuesTab = new JIRASentIssuesTab(parent);
        reportsTab = new JIRAReportTab(parent);
        resultsTab = new JIRASearchResultsTab(parent);

        getTabs().addAll(issuesTab, sentIssuesTab, resultsTab, reportsTab);

        myLogger.exiting(MY_CLASS_NAME, "layoutForm");
    }//end method

    /**
     * This method refreshes the the listings
     */
    public void refresh() {
        myLogger.entering(MY_CLASS_NAME, "refresh");

        issuesTab.refresh();
        sentIssuesTab.refresh();
        resultsTab.refresh();

        myLogger.exiting(MY_CLASS_NAME, "refresh");
    }//end method

    /**
     * This method will clear the listings.
     */
    public void clear() {
        myLogger.entering(MY_CLASS_NAME, "clear");

        issuesTab.clear();
        sentIssuesTab.clear();
        resultsTab.clear();

        myLogger.exiting(MY_CLASS_NAME, "clear");
    }//end method

    /**
     * Sets focus on {@code sentIssuesTab}.
     */
    public void focusOnSentTab() {
        getSelectionModel().select(1);
    }//end method

    /**
     * Sets focus on {@code issuesTab}.
     */
    public void focusOnIssueTab() {
        getSelectionModel().select(0);
    }//end method

    /**
     * Sets focus on {@code JIRASearchResultsTab}.
     */
    public void focusOnSearchIssuesTab() {
        getSelectionModel().select(2);
    }//end method

    /**
     * This method is used to set the label based on status selected.
     *
     * @param statusName the name of the status
     */
    public void setIssuesListLabel(String statusName) {
        issuesTab.changeLabel(statusName);
    }// end method

    /**
     * This method will clear searched items.
     */
    public void clearSearchedItems() {
        resultsTab.clear();
    }//end method

}//end class
