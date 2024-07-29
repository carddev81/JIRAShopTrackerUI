package com.omo.free.jira.tracker.ui;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.omo.free.jira.tracker.client.IssueClient;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.ui.tasks.Download;

import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.application.SFXViewBuilder;
import gov.doc.isu.simple.fx.tools.FXAlertOption;
import gov.doc.isu.simple.fx.util.Constants;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * This class is used to display a listing of tracked JIRA Issues to a user and will allow the user to select issues to send the shop via email.
 *
 * @author Richard Salas, April 17, 2019
 */
public class JIRASentIssuesTab extends AbstractIssuesTab {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.JIRASentIssuesTab";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /* elements used by this tab */
    private Button viewAllBtn;

    /**
     * Constructor used to create an instance of the JIRASentIssuesTab class.
     *
     * @param parent
     *        the SFXViewBuilder implementing instance
     */
    public JIRASentIssuesTab(JIRAWindowBuilder parent) {
        super(parent, "Sent Issues");
        myLogger.entering(MY_CLASS_NAME, "JIRASentIssuesTab", parent);

        setGraphic(new ImageView(JIRAConstants.SMALL_SENT_BUG_URL));
        layoutForm();
        attachHandler();

        myLogger.exiting(MY_CLASS_NAME, "JIRASentIssuesTab");
    }// end constructor

    /**
     * This method will attach the handler on the view all button.
     */
    private void attachHandler() {
        myLogger.entering(MY_CLASS_NAME, "attachHandler");

        viewAllBtn.setOnAction(e -> viewAllJiraIssues(e));

        myLogger.exiting(MY_CLASS_NAME, "attachHandler");
    }// end method

    /**
     * This method will place the view all button onto the sent issues tab.
     */
    private void layoutForm() {
        myLogger.entering(MY_CLASS_NAME, "layoutForm");

        HBox viewAllBox = new HBox(10);
        viewAllBtn = new Button("View All Tracked Issues");
        viewAllBox.getChildren().add(viewAllBtn);
        viewAllBox.setAlignment(Pos.CENTER);
        getGridPane().add(viewAllBox, 0, 0, 3, 1);

        myLogger.entering(MY_CLASS_NAME, "layoutForm");
    }// end method

    /***
     * This method will display all the tracked issues into the {@code ListView}
     * @param e the event
     */
    private void viewAllJiraIssues(ActionEvent e) {
        myLogger.entering(MY_CLASS_NAME, "viewAllJiraIssues", e);

        parent.setMaskerPaneVisible(true);
        parent.setMaskerPaneText("Retrieving all tracked issues from JIRA...");

        Task<Void> loadTrackedIssues = new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                IssueClient.getInstance().retrieveAllTrackedIssues();
                return null;
            }// end method
        };// end anonymous inner class

        // on successful
        loadTrackedIssues.setOnSucceeded(ev -> {
            parent.setMaskerPaneVisible(false);// turn off mask
            parent.refresh();// refresh...
        });

        // on failure
        loadTrackedIssues.setOnFailed(ev -> {
            parent.setMaskerPaneVisible(false);// turn off mask
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), AppUtil.breakUpString("Issue occurred while trying to retrieve the tracked JIRA issues.\n\nError is: " + ev.getSource().getException().getMessage(), 150), "Error Retrieving JIRA Issues", null, AlertType.ERROR);
            parent.refresh();// refresh...
        });

        parent.executeTask(loadTrackedIssues);

        myLogger.exiting(MY_CLASS_NAME, "viewAllJiraIssues");
    }// end method

    /**
     * This method will refresh the view listings for the user.
     */
    @Override
    protected void refresh() {
        myLogger.entering(MY_CLASS_NAME, "refresh");

        setMaskerPaneVisible(false);
        // clearing lists here...
        clear();
        try{
            myLogger.info("loading tracked issues into listing.");
            addIssuesToListView(IssueClient.getInstance().getTrackedIssues());
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to load tracked issues.  Message is: " + e.getMessage());
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "refresh");
    }// end method

    /**
     * This method will download/track/send all the selected JIRA issues to shop via email.
     *
     * @param e
     *        the action event
     */
    @Override
    protected void sendJiraIssues(ActionEvent e) {
        myLogger.entering(MY_CLASS_NAME, "sendJiraIssues", e);

        List<Issue> issuesToSend = getSelectedIssues();

        if(issuesToSend.isEmpty()){
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "There are no issues to send.", "What Issues?", null, AlertType.WARNING);
        }else{
            myLogger.info("sending " + issuesToSend.size() + " jira issues to shop...");

            StringBuilder message = new StringBuilder();
            message.append("You are about to re-send the following issues to the shop:");
            message.append(Constants.LINESEPERATOR);
            message.append(Constants.LINESEPERATOR);
            for(int i = 0, j = issuesToSend.size();i < j;i++){
                message.append("* ").append(issuesToSend.get(i).getKey()).append(Constants.LINESEPERATOR);
            }// end for
            message.append(Constants.LINESEPERATOR);
            message.append(Constants.LINESEPERATOR);
            message.append("Are you sure you want to re-send them?");

            // JSTUI-11 created the SendIssuesDialog to allow user to add text email.
            SendIssuesDialog dialog = new SendIssuesDialog(message);
            if(dialog.isOk()){
                Download download = new Download(issuesToSend, dialog.getOptionalEmailText(), dialog.getCommaSeparatedEmailAddresses());
                download.setIsTracked(true);
                download.setOnSucceeded(ev -> showDownloadSuccess(ev));
                download.setOnFailed(ev -> showDownloadFailure(ev));
                setMaskerPaneText("Downloading and re-sending " + getSelectedJIRAProject() + " JIRA Issues to Shop...");
                setMaskerPaneVisible(true);
                executeTask(download);
            }// end if
        }// end if...else

        myLogger.exiting(MY_CLASS_NAME, "sendJiraIssues");
    }// end method

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIssuesToSelectLabelText() {
        return "Tracked Issues:";
    }// end method

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIssuesToSendLabelText() {
        return "Issues to re-send to the ISU shop:";
    }// end method

    /**
     * {@inheritDoc}
     */
    @Override
    protected IssueListCell getIssueListCell() {
        return new IssueListCell(new ImageView(JIRAConstants.LARGE_SENT_BUG_URL), new ImageView(JIRAConstants.LARGE_SENT_BUG_WITH_ATTACHMENT_URL));
    }//end method

}// end class
