/**
 *
 */
package com.omo.free.jira.tracker.ui;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.controlsfx.control.MaskerPane;

import com.omo.free.jira.tracker.client.IssueClient;
import com.omo.free.jira.tracker.client.ProjectClient;
import com.omo.free.jira.tracker.constants.JIRAConstants;

import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.application.Credit;
import gov.doc.isu.simple.fx.application.SFXViewBuilder;
import gov.doc.isu.simple.fx.managers.PropertiesMgr;
import gov.doc.isu.simple.fx.managers.UIPropertiesMgr;
import gov.doc.isu.simple.fx.tools.FXAlertOption;
import gov.doc.isu.simple.fx.util.FXUtil;
import gov.doc.isu.simple.fx.util.FileUtility;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class is used for building the main window that will be displayed to the user.
 *
 * @author Richard Salas, April 17, 2019
 */
public class JIRAWindowBuilder extends SFXViewBuilder {

    private static final String MY_CLASS_NAME = "com.omo.free.java.view.JIRAWindowBuilder";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /* number of times to retry connection */
    private static final int RETRY_COUNT = 2;

    /* instance variables */
    private JIRAProjectHeader header;
    private JIRATrackerTabPane footer;
    private MaskerPane maskerPane = new MaskerPane();
    private ThreadPoolExecutor threadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);//handles the multiple background threads needed for this application.  only 2 set up for now.

    /**
     * This method creates and returns the {@code JIRAListingWindow} root Node which will be used for creating the Scene.
     *
     * @return root the root node of the scene graph
     */
    @Override
    protected Parent buildParent() {
        myLogger.entering(MY_CLASS_NAME, "buildParent");
        myLogger.info("java home is: " + System.getProperty("java.home", ""));
        initializeResources();
        setResizable(true);
        addStyleSheets("/com/omo/free/jira/tracker/resources/jira.css");

        Credit credit = new Credit();
        credit.addLeadDeveloper("Richard Salas");
        credit.addContributors("Don Brown", "James Kuhlmann", "David Lowe", "Addison Woody");
        MenuBar menuBar = addStandardMenuBar(credit);
        menuBar.getMenus().add(0, new ToolsMenu());
        addJIRAHelpMenuItem(menuBar);

        //main layout here...
        StackPane root = new StackPane();
        BorderPane main = new BorderPane();

        header = new JIRAProjectHeader(this);
        footer = new JIRATrackerTabPane(this);
        footer.refresh();//call to load forms

        main.setTop(header);
        main.setCenter(footer);
        main.setPrefSize(830, 600);
        //this is used for styling the ui at later time

        maskerPane.setVisible(false);

        root.getChildren().addAll(main, maskerPane);
        myLogger.exiting(MY_CLASS_NAME, "buildParent");
        return root;
    }// end method

    /**
     * This method will add the "How To Use JIRA Shop Tracker" menu item to the Help Menu.
     *
     * @param menuBar the menu bar to add the help menu item to
     */
    private void addJIRAHelpMenuItem(MenuBar menuBar) {
        myLogger.entering(MY_CLASS_NAME, "addJIRAHelpMenuItem", menuBar);
        ObservableList<Menu> menus = menuBar.getMenus();
        for(int i = 0, j = menus.size(); i < j; i++){
            if("_Help".equals(menus.get(i).getText())){
                MenuItem howTo = new MenuItem("How To Use JIRA Shop Tracker");
                howTo.setOnAction(e -> {
                    URL url = this.getClass().getProtectionDomain().getClassLoader().getResource("com/omo/free/jira/tracker/resources/index.html");
                    myLogger.info("URL to the how to use html is: " + String.valueOf(url));
                    if(url == null){
                        myLogger.warning("Could not load the html page due to it not existing. Path used is:  com/omo/free/jira/tracker/resources/index.html");
                        FXAlertOption.showAlert(getPrimaryStage(), "How To Use JIRA Shop Tracker site is currently not available.", "Not Available", null, AlertType.WARNING);
                        return;
                    }// end if

                    // nothing special here just basic stage creation!
                    Stage stage = new Stage();
                    stage.setTitle("How To Use JIRA Shop Tracker");
                    stage.getIcons().add(getPrimaryStage().getIcons().get(0));
                    stage.setResizable(true);
                    stage.initOwner(getPrimaryStage());
                    stage.initStyle(StageStyle.DECORATED);
                    stage.setAlwaysOnTop(false);
                    // create the web view
                    WebView webView = new WebView();
                    webView.getEngine().load(url.toString());

                    // create the root container
                    VBox root = new VBox(webView);
                    root.setMinWidth(1000);
                    root.setMaxHeight(550);
                    VBox.setVgrow(webView, Priority.ALWAYS);

                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.sizeToScene();
                    stage.show();
                });//end howTo
                menus.get(i).getItems().add(0, howTo);
                break;
            }//end if
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "addJIRAHelpMenuItem");
    }//end method

    /**
     * This method initializes all the resources needed by the JIRA Shop Tracker UI.
     */
    private void initializeResources() {
        myLogger.entering(MY_CLASS_NAME, "initializeResources");

        JIRAConstants.JIRA_DOWNLOAD_DIRECTORY = PropertiesMgr.getInstance().getProperties().getProperty("jira.download.directory");
        JIRAConstants.JIRA_RESOURCES_DIRECTORY =  PropertiesMgr.getInstance().getProperties().getProperty("jira.resources.directory");
        JIRAConstants.JIRA_TRUSTSTORE_DIRECTORY = PropertiesMgr.getInstance().getProperties().getProperty("jira.truststore.directory");
        JIRAConstants.JIRA_ISSUES_TOO_LARGE_DIRECTORY = PropertiesMgr.getInstance().getProperties().getProperty("jira.issuesTooLarge.directory");
        JIRAConstants.JIRA_USER_ID = AppUtil.getUserIdFromEnvVar();

        //make sure directories exist
        FileUtility.checkDirectories(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY);
        FileUtility.checkDirectories(JIRAConstants.JIRA_RESOURCES_DIRECTORY);
        FileUtility.checkDirectories(JIRAConstants.JIRA_TRUSTSTORE_DIRECTORY);

        //FIXME need to rewrite this section here for reconfiguring the truststore using the improved upon method.
        //CONFIGURING TRUSTSTORE start
        Path truststoreFile = Paths.get(JIRAConstants.JIRA_TRUSTSTORE_DIRECTORY, "cacerts");
        if(!Files.exists(truststoreFile)){
            FXUtil.createLocalTrustStore(truststoreFile.getParent(), "cacerts");
        }//end if

        FXUtil.checkWebServerCertificate(JIRAConstants.JIRA_URL.replace("https://", ""), truststoreFile, null, true);
        FXUtil.setTrustStoreLocationProperty(truststoreFile.toFile().getPath());
        //CONFIGURING TRUSTSTORE end

        checkJIRAConnection();
        //set system security

        //get the jira clients instances here...
        ProjectClient projectClient = ProjectClient.getInstance();
        IssueClient issueClient = IssueClient.getInstance();

        //BELOW code is long running, note that the splash screen will be displayed till this is finished.
        int count = 0;
        while(count < RETRY_COUNT){
            try{
                projectClient.setProjects();
                if(projectClient.getProjects().contains("MOCIS")){
                    issueClient.setIssues("MOCIS", "0");
                }else{
                    issueClient.setIssues(projectClient.getProjects().get(0), "0");
                }//end method
                issueClient.loadJiraCache();
                break;
            }catch(Exception e){
                myLogger.log(Level.SEVERE, "Problem connecting to JIRA website...please try again.");
                count++;
                //TODO add more flow here
                if(!projectClient.isAuthorized() || !issueClient.isAuthorized() && count < RETRY_COUNT){
                    FXAlertOption.showAlert(null, "User is not authorized!  Please try re-entering connection settings.", "User Not Authorized", null, AlertType.ERROR, new Image(JIRAConstants.JIRA_ICON_URL));
                    new ConnectionSettingsWindow();//show window
                }else{
                    break;
                }//end if
            }//end try...catch
        }//end while
        myLogger.entering(MY_CLASS_NAME, "initializeResources");
    }//end method

    /**
     * This method will check to see if the user has already initialized his/her credential properties.
     */
    private void checkJIRAConnection() {
        myLogger.entering(MY_CLASS_NAME, "loadConnectionProperties");

        Properties props = UIPropertiesMgr.getInstance().getProperties();

        String remember = props.getProperty("jira.remember");
        String pass = props.getProperty("jira.pass");
        String user = props.getProperty("jira.user");

        if (user == null || remember == null || "false".equals(remember)){
            new ConnectionSettingsWindow();//load user properties.
        }else if("true".equals(remember) && pass == null){
            new ConnectionSettingsWindow();//load user properties.
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "loadConnectionProperties");
    }//end method

    /**
     * Closes system resources that are open if there are any.
     */
    @Override public void close(){
        myLogger.entering(MY_CLASS_NAME, "loadConnectionProperties");

        String rem = UIPropertiesMgr.getInstance().getProperties().getProperty("jira.remember");
        if(rem != null && "false".equals(rem)){
            UIPropertiesMgr.getInstance().getProperties().remove("jira.pass");
        }//end if
        threadExecutor.shutdown();
        myLogger.exiting(MY_CLASS_NAME, "loadConnectionProperties");
    }//end method

    /**
     * Sets the masker pane text.
     * @param theText the text to set into the masker pane
     */
    public void setMaskerPaneText(String theText) {
        maskerPane.setText(theText);
    }//end method

    /**
     * Sets the visibility on the masker pane.
     * @param isVisible true or false values
     */
    public void setMaskerPaneVisible(boolean isVisible) {
        maskerPane.setVisible(isVisible);
    }//end method

    /**
     * This method will execute a task on a background thread.
     *
     * @param <V> the returning object that the task will return
     * @param aTask the task to be executed on a background thread
     */
    public <V> void executeTask(Task<V> aTask){
        myLogger.entering(MY_CLASS_NAME, "executeTask", aTask);
        threadExecutor.execute(aTask);
        myLogger.exiting(MY_CLASS_NAME, "executeTask", aTask);
    }//end method

    /**
     * This method refreshes all data within the tabpane.
     */
    public void refresh() {
        myLogger.entering(MY_CLASS_NAME, "refresh");

        footer.refresh();

        myLogger.exiting(MY_CLASS_NAME, "refresh");
    }//end method

    /**
     * This method clears all data within the tabpane.
     */
    public void clearAll() {
        myLogger.entering(MY_CLASS_NAME, "clearAll");

        footer.clear();

        myLogger.exiting(MY_CLASS_NAME, "clearAll");
    }//end method

    /**
     * This method will return the the selected JIRA project
     * @return the jira project selected
     */
    public String getSelectedJIRAProject(){
        return header.getSelectedJIRAProject();
    }//end method

    /**
     * This method returns the icon path used for attaching to main window.
     */
    @Override protected String getStageIconPath() {
        return "/com/omo/free/jira/tracker/resources/jira.png";
    }// end method

    /**
     * Puts focus on sent tab.
     */
    public void focusoOnSentIssuesTab() {
        footer.focusOnSentTab();
    }//end method

    /**
     * Puts focus on sent tab.
     */
    public void focusoOnSearchIssuesTab() {
        footer.focusOnSearchIssuesTab();
    }//end method

    /**
     * Puts focus on issues not sent to shop tab.
     */
    public void focusOnOpenIssuesTab() {
        footer.focusOnIssueTab();
    }//end method

    /**
     * This method will set the issues label
     * @param statusName the item that was selected
     */
    public void setIssuesListLabel(String statusName) {
        footer.setIssuesListLabel(statusName);
    }// end method

    /**
     * This method will clear searched items.
     */
    public void clearSearchedItems(){
        footer.clearSearchedItems();
    }//end method

}// end class
