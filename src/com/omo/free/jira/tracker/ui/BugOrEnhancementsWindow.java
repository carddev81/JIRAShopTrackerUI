/**
 *
 */
package com.omo.free.jira.tracker.ui;

import java.util.Arrays;
import java.util.logging.Logger;

import com.omo.free.jira.tracker.dao.JIRAEnhancementDAO;
import com.omo.free.jira.tracker.model.JIRATrackerEnhancement;
import com.omo.free.jira.tracker.util.JiraUtil;

import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.application.SFXViewBuilder;
import gov.doc.isu.simple.fx.managers.PropertiesMgr;
import gov.doc.isu.simple.fx.tools.FXAlertOption;
import gov.doc.isu.simple.fx.tools.SendMail;
import gov.doc.isu.simple.fx.util.Constants;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class is used to allow a user to write up a request to fix a bug or to implement an enhancement.
 *
 * @author Richard Salas, May 02, 2019
 */
public class BugOrEnhancementsWindow extends Stage {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.BugOrEnhancementsWindow";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private ComboBox<String> types;
    private TextArea description;
    private Button publishBug;

    /**
     * Constructor used to create an instance of the BugOrEnhancementsWindow.
     */
    public BugOrEnhancementsWindow(){
        super(StageStyle.DECORATED);
        myLogger.entering(MY_CLASS_NAME, "BugOrEnhancementsWindow");
        setTitle("Report Bug or Enhancement");
        getIcons().add(SFXViewBuilder.getPrimaryStage().getIcons().get(0));
        initOwner(SFXViewBuilder.getPrimaryStage());
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        layoutForm();
        attachHandlers();
        showAndWait();
        myLogger.exiting(MY_CLASS_NAME, "BugOrEnhancementsWindow");
    }//end constructor

    /**
     * This method will add all user interface element handlers.
     */
    private void attachHandlers() {
        myLogger.entering(MY_CLASS_NAME, "attachHandlers");
        publishBug.setOnAction(ev -> sendBug(ev));
        myLogger.exiting(MY_CLASS_NAME, "attachHandlers");
    }//end method

    /**
     * This method will send the bug or enhancement request to the shop via email.
     *
     * @param ev the event
     */
    private void sendBug(ActionEvent ev) {
        myLogger.entering(MY_CLASS_NAME, "sendBug", ev);

        if(". . .".equals(types.getSelectionModel().getSelectedItem()) || AppUtil.isNullOrEmpty(description.getText())){
            FXAlertOption.showAlert(this, "Type and Description are both required!", "Required Fields", null, AlertType.ERROR);
        }else{
            final JIRATrackerEnhancement enhancement = new JIRATrackerEnhancement();
            enhancement.setType(types.getSelectionModel().getSelectedItem());
            enhancement.setDescription(description.getText());

            //disable the fields
            types.setDisable(true);
            description.setDisable(true);

            Task<JIRATrackerEnhancement> sendEnhancement = new Task<JIRATrackerEnhancement>(){
                @Override
                protected JIRATrackerEnhancement call() throws Exception {

                    StringBuilder email = new StringBuilder("<p style=\"font-size:11pt; font-family:Calibri; color:#336699; text-align:left\">Please forward this email to the lead developer of the \"JIRA Shop Tracker\" at the shop.");
                    email.append("<h3 style=\"font-family:Calibri; color:#336699;\">The following ").append(enhancement.getType()).append(" has been requested to be worked on:</h3>");
                    email.append("<ul style=\"font-size:11pt; font-family:Calibri; color:#336699; text-align:left\"><li>").append(enhancement.getDescription());
                    email.append("</li></ul><p style=\"font-size:11pt; font-family:Calibri; color:#336699; text-align:left\">Thank you!</p>");

                    String to = null;
                    if("ISU".equals(System.getenv("USERDOMAIN"))){
                        to = PropertiesMgr.getInstance().getProperties().getProperty("email.to");
                    }else{
                        to = "ITSDJCCCSHOP@doc.mo.gov,OA.ITSD.AppDev.DOC.Tech@oa.mo.gov,OA.ITSD.AppDev.DOC.PM@oa.mo.gov";//hardcoded this!!!
                    }//end if...else

                    //send email
                    SendMail.send(to, email.toString(), enhancement.getType() + " to be worked on", null);

                    return enhancement;
                }//end method
            };//end task

            sendEnhancement.setOnSucceeded(eve -> clearForm(eve));
            sendEnhancement.setOnFailed(eve -> failedToSendBug(eve));
            new Thread(sendEnhancement).start();
        }//end if...else

        myLogger.exiting(MY_CLASS_NAME, "sendBug", ev);
    }//end method

    /**
     * Clears the form after a successful send of bug or enhancement.
     *
     * @param eve the event
     */
    private void clearForm(WorkerStateEvent eve) {
        myLogger.entering(MY_CLASS_NAME, "clearForm", eve);

        //disable the fields
        types.setDisable(false);
        description.setDisable(false);
        types.getSelectionModel().select(0);
        description.setText("");

        StringBuilder sb = new StringBuilder("Successfully send bug or enhancement to the following email addresses:");
        sb.append(Constants.LINESEPERATOR).append(Constants.LINESEPERATOR);

        if("ISU".equals(System.getenv("USERDOMAIN"))){
            sb.append(PropertiesMgr.getInstance().getProperties().get("email.to"));
        }else{//hardcoded these!!!
            sb.append("* ITSDJCCCSHOP@doc.mo.gov");
            sb.append(Constants.LINESEPERATOR);
            sb.append("* OA.ITSD.AppDev.DOC.Tech@oa.mo.gov");
        }//end if...else

        //start thread for saving record here...
        final JIRATrackerEnhancement en = (JIRATrackerEnhancement) eve.getSource().getValue();
        Task<Void> saveEnhancement = new Task<Void>(){
            @Override protected Void call() throws Exception {
                myLogger.info("saving bug here!!!");
                JIRAEnhancementDAO dao = new JIRAEnhancementDAO();
                dao.insert(en);
                return null;
            }//end method
        };//end task

        new Thread(saveEnhancement).start();
        FXAlertOption.showAlert(this, sb.toString(), "Successfully sent bug or enhancement to shop!", null, AlertType.INFORMATION);

        myLogger.exiting(MY_CLASS_NAME, "clearForm");
    }//end method

    /**
     * This method is called when there is a failure to send an email concerning an enhancement or a bug.
     *
     * @param ev the event
     */
    private void failedToSendBug(WorkerStateEvent ev) {
        myLogger.entering(MY_CLASS_NAME, "failedToSendBug", ev);

        types.setDisable(false);
        description.setDisable(false);
        FXAlertOption.showAlert(this, "Failed to send email to shop. The error message is:\n\n" + ev.getSource().getException().getMessage(), "Error sending bug or enhancement to shop!", null, AlertType.ERROR);

        myLogger.exiting(MY_CLASS_NAME, "failedToSendBug");
    }//end method

    /**
     * This method will layout the user interface tab element for user to view.
     */
    private void layoutForm() {
        myLogger.entering(MY_CLASS_NAME, "layoutForm");

        GridPane mainPane = new GridPane();
        mainPane.setAlignment(Pos.TOP_LEFT);
        mainPane.setPrefSize(600, 300);
        mainPane.setPadding(new Insets(15));
        mainPane.setHgap(15);
        mainPane.setVgap(15);

        Label typeLbl = new Label("Type:");
        types = new ComboBox<>();
        types.getItems().addAll(Arrays.asList(". . .", "EHANCEMENT", "BUG"));
        types.getSelectionModel().select(0);

        mainPane.add(typeLbl, 0, 0);
        mainPane.add(types, 1, 0);

        Label descLbl = new Label("Description:");
        description = new TextArea();

        mainPane.add(descLbl, 0, 1);
        mainPane.add(description, 1, 1);

        publishBug = new Button("Send");

        mainPane.add(publishBug, 0, 2, 2, 1);

        GridPane.setHalignment(typeLbl, HPos.RIGHT);
        GridPane.setValignment(descLbl, VPos.TOP);
        GridPane.setHalignment(publishBug, HPos.CENTER);

        JiraUtil.setGridPaneVgrowAlwaysOnAllNodes(description);
        JiraUtil.setGridPaneHgrowAlwaysOnAllNodes(description);

        Scene scene = new Scene(mainPane);
        setScene(scene);
        sizeToScene();
        myLogger.exiting(MY_CLASS_NAME, "layoutForm");
    }//end method

}//end class
