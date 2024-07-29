package com.omo.free.jira.tracker.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.omo.free.jira.tracker.client.SearchClient;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.util.JiraUtil;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import gov.doc.isu.com.util.AppUtil;
import javafx.scene.image.ImageView;

/**
 * This class is used to display a listing of JIRA Issues that a user searched for and will allow the user to select issues to send the shop via email.
 *
 * @author Richard Salas, October 30, 2019
 */
public class JIRASearchResultsTab extends AbstractIssuesTab{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.JIRASearchResultsTab";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /**
     * Constructor used to create an instance of the JIRASearchResultsTab class.
     *
     * @param parent
     *        the SFXViewBuilder implementing instance
     */
    public JIRASearchResultsTab(JIRAWindowBuilder parent) {
        super(parent, "Search Results");
        myLogger.entering(MY_CLASS_NAME, "JIRASearchResultsTab", parent);
        setGraphic(JiraUtil.createIcon(FontAwesomeIcon.YELP, "jira-darkblue"));
        myLogger.exiting(MY_CLASS_NAME, "JIRASearchResultsTab");
    }//end constructor

    /**
     * This method is used to change the label of the open issues
     *
     * @param label the label to change
     */
    public void changeLabel(){
        myLogger.entering(MY_CLASS_NAME, "changeLabel");
        String searchLbl = SearchClient.getInstance().getSearchStr();
        getOpenIssuesLbl().setText(searchLbl == null ? getIssuesToSelectLabelText() : "Search results for " + searchLbl + ":");
        myLogger.exiting(MY_CLASS_NAME, "changeLabel");
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
            myLogger.info("loading searched for issues into listing.");
            if(!AppUtil.isEmpty(SearchClient.getInstance().getIssuesSearchedFor())){
                addIssuesToListView(SearchClient.getInstance().getIssuesSearchedFor());
            }//end if
            changeLabel();
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to load untracked issues.  Message is: " + e.getMessage(), e);
        }//end try...catch

        myLogger.exiting(MY_CLASS_NAME, "refresh");
    }// end method

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIssuesToSelectLabelText() {
        return "Search Results:";
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

    /**
     * This method will clear the listings.
     * Override of this method per [JSTUI-15] Values not retained in Issues to send to ISU Shop column during new search (Richard Salas)
     */
    @Override
    protected void clear() {
        myLogger.entering(MY_CLASS_NAME, "clear");

        getIssuesToSelect().clear();

        myLogger.exiting(MY_CLASS_NAME, "clear");
    }// end method

}//end method
