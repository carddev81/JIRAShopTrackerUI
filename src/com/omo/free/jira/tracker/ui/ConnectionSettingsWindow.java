package com.omo.free.jira.tracker.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.util.JiraUtil;

import gov.doc.isu.simple.fx.application.SFXViewBuilder;
import gov.doc.isu.simple.fx.managers.UIPropertiesMgr;
import gov.doc.isu.simple.fx.tools.FXAlertOption;
import gov.doc.isu.simple.fx.util.FXUtil;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class is used to for setting the connection settings used by the JIRAShopTracker UI by displaying a small window to users to enter user id and password.
 *
 * @author Richard Salas, April 17, 2019
 */
public class ConnectionSettingsWindow extends Stage {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.ConnectionSettingsWindow";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private PasswordField passwordField;
    private TextField userField;
    private CheckBox rememberChbx;

    private Button okBtn;
    private Button cancelBtn;

    /**
     * Constructor used to create an instance of the ConnectionSettingsWindow.
     */
    public ConnectionSettingsWindow() {
        super(StageStyle.DECORATED);
        myLogger.entering(MY_CLASS_NAME, "ConnectionSettingsWindow");
        setTitle("JIRA User Connection Settings");
        getIcons().add(new Image("/com/omo/free/jira/tracker/resources/jira.png"));
        initOwner(SFXViewBuilder.getPrimaryStage());
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        //1 main hbox
        GridPane mainPane = getLayout();
        attachAllListeners();
        loadSavedSettings();

        Scene scene = new Scene(mainPane);
        setScene(scene);
        sizeToScene();
        showAndWait();
        myLogger.exiting(MY_CLASS_NAME, "ConnectionSettingsWindow");
    }//end constructor

    /**
     * This method will load the saved settings if they exist.
     */
    private void loadSavedSettings() {
        myLogger.entering(MY_CLASS_NAME, "loadSavedSettings");

        String user = UIPropertiesMgr.getInstance().getProperties().getProperty("jira.user");
        String password = UIPropertiesMgr.getInstance().getProperties().getProperty("jira.pass");
        String remember = UIPropertiesMgr.getInstance().getProperties().getProperty("jira.remember");

        if(user!=null){
            userField.setText(JiraUtil.decrypt(user));
        }//end if

        if(password!=null && remember!=null && Boolean.parseBoolean(remember)){
            passwordField.setText(JiraUtil.decrypt(password));
        }//end if

        if(remember!=null){
            rememberChbx.setSelected(Boolean.parseBoolean(remember));
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "loadSavedSettings");
    }//end method

    /**
     * This method will add all handlers to the user interface elements.
     */
    private void attachAllListeners() {
        myLogger.entering(MY_CLASS_NAME, "attachAllListeners");

        okBtn.setOnAction(e -> saveConnectionSettings());
        cancelBtn.setOnAction(e -> this.close());
        passwordField.setOnKeyReleased(value -> {
            if(KeyCode.ENTER.equals(value.getCode())){
                saveConnectionSettings();
            }//end if
        });
        FXUtil.makeEnterKeyFireButtonAction(okBtn);
        FXUtil.makeEnterKeyFireButtonAction(cancelBtn);

        myLogger.entering(MY_CLASS_NAME, "attachAllListeners");
    }//end method

    /**
     * This method will save the connection settings set by the user.
     *
     * @param e
     */
    private void saveConnectionSettings() {
        myLogger.entering(MY_CLASS_NAME, "saveConnectionSettings");

        String user = userField.getText();
        String pass = passwordField.getText();

        if(user == null || "".equals(user.trim()) || pass == null || "".equals(pass.trim())){
            FXAlertOption.showAlert(this, "User or Password cannot be blank or empty.", "No Blanks Allowed", null, AlertType.ERROR);
        }else{
            UIPropertiesMgr.getInstance().getProperties().setProperty("jira.user", JiraUtil.encrypt(user));
            UIPropertiesMgr.getInstance().getProperties().setProperty("jira.remember", Boolean.toString(rememberChbx.isSelected()));

            try{
                if(rememberChbx.isSelected()){
                    UIPropertiesMgr.getInstance().getProperties().setProperty("jira.pass", JiraUtil.encrypt(pass));
                    UIPropertiesMgr.getInstance().save();
                }else{
                    UIPropertiesMgr.getInstance().save();
                    UIPropertiesMgr.getInstance().getProperties().setProperty("jira.pass", JiraUtil.encrypt(pass));
                }//end if...else
            }catch(Exception e1){
                myLogger.log(Level.SEVERE, "Exception occurred while trying to save the properties.  Error message is:  " + e1.getMessage(), e1);
            }//end try...catch
            this.close();
        }//end if...else

        myLogger.exiting(MY_CLASS_NAME, "saveConnectionSettings");
    }//end method

    /**
     * This method will return the GridPane layout used for displaying the textfields to user for entry of information.
     *
     * @return the {@code GridPane} containing the user interface elements
     */
    private GridPane getLayout() {
        myLogger.entering(MY_CLASS_NAME, "getLayout");

        GridPane mainPane = new GridPane();
        mainPane.setPrefSize(300, 100);
        mainPane.setPadding(new Insets(10));
        mainPane.setHgap(10);
        mainPane.setVgap(10);

        userField = new TextField();
        passwordField = new PasswordField();
        rememberChbx = new CheckBox("Save password?");
        rememberChbx.setSelected(true);
        okBtn = new Button("OK");
        cancelBtn = new Button("Cancel");

        mainPane.add(new Label("JIRA URL:"), 0, 0);
        mainPane.add(new Label(JIRAConstants.JIRA_URL), 1, 0);

        mainPane.add(new Label("User:"), 0, 1);
        mainPane.add(userField, 1, 1);

        mainPane.add(new Label("Password:"), 0, 2);
        mainPane.add(passwordField, 1, 2);

        mainPane.add(rememberChbx, 1, 3);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(okBtn, cancelBtn);
        GridPane.setHalignment(buttonBox, HPos.CENTER);
        mainPane.add(buttonBox, 0, 4, 2, 1);

        GridPane.setHgrow(buttonBox, Priority.ALWAYS);
        GridPane.setHgrow(userField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        GridPane.setHgrow(mainPane, Priority.ALWAYS);

        myLogger.exiting(MY_CLASS_NAME, "getLayout", mainPane);
        return mainPane;
    }//end method

}//end class
