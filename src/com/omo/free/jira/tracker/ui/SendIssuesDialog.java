package com.omo.free.jira.tracker.ui;

import java.util.logging.Logger;

import org.springframework.util.StringUtils;

import gov.doc.isu.simple.fx.application.SFXViewBuilder;
import gov.doc.isu.simple.fx.tools.FXAlertOption;
import gov.doc.isu.simple.fx.util.Constants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class represents the Send Issues window to the user displaying the issues that are going to be sent to the shop as well as allowing the option of entering email text.
 *
 * <p>Created per JSTUI-11</p>
 *
 * @author Richard Salas JCCC 09/30/2019
 */
public class SendIssuesDialog extends Stage{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.SendIssuesDialog";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private TextArea emailTextArea;
    private TextField additionalEmailTxtFld;
    private Button okay;
    private Button cancel;
    private boolean ok;

    /**
     * Constructor used to create an instance of the SendIssuesDialog.
     *
     * @param message the message used to populate the send issues dialog box
     */
    public SendIssuesDialog(StringBuilder message) {
        myLogger.entering(MY_CLASS_NAME, "SendIssuesDialog", message);

        initOwner(SFXViewBuilder.getPrimaryStage());
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.DECORATED);
        getIcons().add(SFXViewBuilder.getPrimaryStage().getIcons().get(0));
        setResizable(false);
        setTitle("Are You Sure?");

        layoutForm(message);
        attachHandlers();

        Platform.runLater(() -> center());//centering the window

        showAndWait();
        myLogger.exiting(MY_CLASS_NAME, "SendIssuesDialog");
    }//end constructor



    /**
     * This method will center the dialog window to the main Stage window.
     */
    private void center() {
        myLogger.entering(MY_CLASS_NAME, "center");
        Stage mainStage = SFXViewBuilder.getPrimaryStage();

        double x = (mainStage.getX() + mainStage.getWidth() / 2) - (getWidth() / 2);
        double y = (mainStage.getY() + mainStage.getHeight() / 2) - (getHeight() / 2);

        setX(x);
        setY(y);
        myLogger.exiting(MY_CLASS_NAME, "center");
    }//end method

    /**
     * This method will layout the form to the user.
     *
     * @param message the message that will be populated in the message box
     */
    private void layoutForm(StringBuilder message) {
        myLogger.entering(MY_CLASS_NAME, "layoutForm", message);

        VBox root = new VBox(20);
        root.setPadding(new Insets(15));

        HBox labeling = new HBox(20);

        //message for ISU testing...
//        StringBuilder label = new StringBuilder("You are about to re-send the following issues to the shop:");
//        label.append(Constants.LINESEPERATOR).append(Constants.LINESEPERATOR);
//        label.append(" * MOCIS-3829").append(Constants.LINESEPERATOR);
//        label.append(" * MOCIS-4829").append(Constants.LINESEPERATOR);
//        label.append(" * MOCIS-5829").append(Constants.LINESEPERATOR);
//        label.append(Constants.LINESEPERATOR);
//        label.append("Are you sure you want to send them?");

        ImageView questionMark = new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-confirm.png");
        Label lbl = new Label(message.toString());
        labeling.getChildren().addAll(questionMark, lbl);

        //text area
        emailTextArea = new TextArea();
        emailTextArea.setFont(Font.font("Tahoma", 12D));
        emailTextArea.setWrapText(true);
        emailTextArea.setPromptText("Enter your custom email message here ...");
        emailTextArea.setPrefWidth(250);

        //textfield for additional email addresses (JSTUI-18 Additional Email Recipients requested [Richard Salas])
        Label additionEmaillbl = new Label("Additional Email Recipients (separated by , or ;)");
        additionalEmailTxtFld = new TextField();
        additionalEmailTxtFld.setPromptText("Enter email addresses separated by commas or semi-colons...");
        additionalEmailTxtFld.setTooltip(new Tooltip("If others should recieve this email other than YOU or ISU SHOP then enter them here."));

        VBox emailAddrBox = new VBox(5);
        emailAddrBox.getChildren().addAll(additionEmaillbl, additionalEmailTxtFld);

        //buttons
        cancel = new Button("Cancel");
        cancel.setPrefWidth(75);

        okay = new Button("OK");
        okay.setPrefWidth(75);

        HBox buttonHB = new HBox(20);
        buttonHB.setAlignment(Pos.CENTER);
        buttonHB.getChildren().addAll(okay, cancel);

        //add stuff to the root
        root.getChildren().addAll(labeling, emailTextArea, emailAddrBox, buttonHB);
        Scene scene = new Scene(root);
        setScene(scene);

        sizeToScene();
        okay.requestFocus();
    }//end method

    /**
     * This method will attach the event handlers to the buttons.
     */
    private void attachHandlers() {
        cancel.setOnAction(e -> close());
        okay.setOnAction(e -> {
            if(isFormValid()){
                setOk(true);
                close();
            }//end if
        });//end anonymous class
    }//end method

    /**
     * Returns whether or not the SendIssuesDialog Form contains any bad data.
     *
     * @return valid true or false based on whether or not the form was correctly filled out.
     */
    private boolean isFormValid() {
        myLogger.entering(MY_CLASS_NAME, "isFormValid");

        boolean valid = true;
        String emailRegEx = "^[\\w][\\w\\.#]*[\\w]@[\\w][\\w\\.]*[\\w]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";

        //only need to check email addresses if they exist [JSTUI-18 Additional Email Recipients requested (Richard Salas)]
        String emailAddresses = StringUtils.trimWhitespace(this.additionalEmailTxtFld.getText());
        if(!"".equals(emailAddresses)){
            String[] emailArray = StringUtils.trimArrayElements(emailAddresses.split("(,|;)"));//removing all the whitespace characters
            for(int i=0,j=emailArray.length;i<j;i++){//going through the array looking for badly formatted email addresses
                if(!emailArray[i].matches(emailRegEx)){
                    StringBuilder errorMsg = new StringBuilder("Please correct the following problems...");
                    errorMsg.append(Constants.LINESEPERATOR).append(Constants.LINESEPERATOR);
                    errorMsg.append("- Additional Email Recipients contains an invalid email address.");
                    errorMsg.append(Constants.LINESEPERATOR).append(Constants.LINESEPERATOR);
                    errorMsg.append("Invalid email address:  ").append(emailArray[i]);
                    FXAlertOption.showAlert(this, errorMsg.toString(), "Invalid Email Address Found", "Invalid Email Address", AlertType.ERROR);
                    this.additionalEmailTxtFld.requestFocus();
                    this.additionalEmailTxtFld.positionCaret(0);
                    valid = false;
                    break;
                }//end if
            }//end for
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "isFormValid", valid);
        return valid;
    }//end method

    /**
     * @return the ok
     */
    public boolean isOk() {
        return ok;
    }//end method

    /**
     * @param ok the ok to set
     */
    public void setOk(boolean ok) {
        this.ok = ok;
    }//end method

    /**
     * This method will return the optional email text.
     *
     * @return optionalText the optional email text
     */
    public String getOptionalEmailText() {
        myLogger.entering(MY_CLASS_NAME, "getOptionalEmailText");

        String optionalText = null;

        if(emailTextArea.getText() != null){
            optionalText = emailTextArea.getText().replace("\n", "<br/>");
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "getOptionalEmailText", optionalText);
        return optionalText;
    }//end method

    /**
     * This method will return the optional email text.
     *
     * @return optionalText the optional email text
     */
    public String getCommaSeparatedEmailAddresses() {
        myLogger.entering(MY_CLASS_NAME, "getCommaSeparatedEmailAddresses");

       //JSTUI-18 Additional Email Recipients requested (Richard Salas)
        String commaSeparatedAddresses = "";
        String emailAddresses = StringUtils.trimWhitespace(this.additionalEmailTxtFld.getText());
        if(!"".equals(emailAddresses)){
            commaSeparatedAddresses = String.join(",", StringUtils.trimArrayElements(emailAddresses.split("(,|;)")));
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "getCommaSeparatedEmailAddresses", commaSeparatedAddresses);
        return commaSeparatedAddresses;
    }//end method

}//end class
