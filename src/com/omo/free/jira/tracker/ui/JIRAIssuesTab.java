package com.omo.free.jira.tracker.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.omo.free.jira.tracker.client.IssueClient;
import com.omo.free.jira.tracker.constants.JIRAConstants;

import javafx.scene.image.ImageView;

/**
 * This class is used to display a listing of JIRA Issues to a user and will allow the user to select issues to send the shop via email.
 *
 * @author Richard Salas, October 30, 2019
 */
public class JIRAIssuesTab extends AbstractIssuesTab {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.JIRAIssuesTab";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /**
     * Constructor used to create an instance of the JIRAIssuesTab class.
     *
     * @param parent
     *        the SFXViewBuilder implementing instance
     */
    public JIRAIssuesTab(JIRAWindowBuilder parent) {
        super(parent, "Issues");
        myLogger.entering(MY_CLASS_NAME, "JIRAIssuesTab", parent);
        setGraphic(new ImageView(JIRAConstants.SMALL_BUG_URL));
        myLogger.exiting(MY_CLASS_NAME, "JIRAIssuesTab");
    }// end constructor

    /**
     * This method is used to change the label of the open issues
     *
     * @param label the label to change
     */
    public void changeLabel(String label){
        if(". . .".equals(label)){
            getOpenIssuesLbl().setText("Issues:");
        }else{
            getOpenIssuesLbl().setText(label + ":");
        }// end if...else
    }//end method

    /**
     * This method will refresh the view listings for the user.
     */
    @Override
    protected void refresh() {
        myLogger.entering(MY_CLASS_NAME, "refresh");

        setMaskerPaneVisible(false);
        clear();
        // checking the jira model to see if it is null
        try{
            myLogger.info("loading open issues into listing.");
            addIssuesToListView(IssueClient.getInstance().getIssues());
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to load untracked issues.  Message is: " + e.getMessage());
        }//end try...catch

        myLogger.exiting(MY_CLASS_NAME, "refresh");
    }// end method

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIssuesToSelectLabelText() {
        return "Issues:";
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIssuesToSendLabelText() {
        return "Issues to send to ISU shop:";
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    protected IssueListCell getIssueListCell() {
        return new IssueListCell(new ImageView(JIRAConstants.LARGE_OPEN_BUG_URL), new ImageView(JIRAConstants.LARGE_OPEN_BUG_WITH_ATTACHMENT_URL));
    }//end method

}// end class
