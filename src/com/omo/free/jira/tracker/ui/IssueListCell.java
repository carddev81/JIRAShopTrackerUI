package com.omo.free.jira.tracker.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.Issue;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

/**
 * This is a custom JIRA Issue ListCell to add some customization a list view for the JIRAShopTrackerUI.
 *
 * @author Richard Salas, April 17, 2019
 */
public class IssueListCell extends ListCell<Issue>{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.IssueListCell";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private ImageView bugWithAttachment;
    private ImageView bug;

    /**
     * Constructor used to create an instance of the IssueListCell class.
     *
     * @param bug the bug image
     * @param bugWithAttachmentView the bug image with attachment
     */
    public IssueListCell(ImageView bug, ImageView bugWithAttachmentView) {
        myLogger.entering(MY_CLASS_NAME, "IssueListCell", bug);
        this.bug = bug;
        this.bugWithAttachment = bugWithAttachmentView;
        myLogger.exiting(MY_CLASS_NAME, "IssueListCell");
    }//end method

    /**
     * This method will make sure that the listed item is correctly presented to the user.
     *
     * @param item The new item for the cell.
     * @param empty whether or not this cell represents data from the list. If it
     *        is empty, then it does not represent any domain data, but is a cell
     *        being used to render an "empty" row.
     */
    @Override public void updateItem(Issue item, boolean empty) {
        myLogger.entering(MY_CLASS_NAME, "updateItem", new Object[]{item, empty});

        super.updateItem(item, empty);
        this.setGraphic(null);
        this.setText(null);
        if(!empty){
            if(attachmentsExist(item)){
//                //add paperclip image
                this.setGraphic(bugWithAttachment);
            }else{
//                //add regular image
                this.setGraphic(bug);
            }
            this.setText(item.getKey() + "\n" + item.getSummary());
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "updateItem");
    }//end method

    /**
     * This method will check to see if there are any attachments associated to the JIRA issue.
     *
     * @param issue the issue to check for attachments
     * @return true or false on whether or not an issue is associated with an attachment
     */
    private boolean attachmentsExist(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "attachmentsExist", issue);
        boolean exists = false;

        try{
            Iterable<Attachment> list = issue.getAttachments();
            if(list != null){
                exists = list.iterator().hasNext();
            }//end if
        }catch(Exception e){
            myLogger.log(Level.WARNING, "Exception occurred while trying to see if attachment exists.  Error is " + e.getMessage(), e);
        }//end try...catch

        myLogger.exiting(MY_CLASS_NAME, "attachmentsExist", issue);
        return exists;
    }//end method

}//end class
