package com.omo.free.jira.tracker.ui;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.omo.free.jira.tracker.client.IssueClient;
import com.omo.free.jira.tracker.client.ProjectClient;
import com.omo.free.jira.tracker.client.SearchClient;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.model.JIRAStatus;
import com.omo.free.jira.tracker.ui.tasks.JIRAClientPrimer;
import com.omo.free.jira.tracker.util.JiraUtil;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.application.SFXViewBuilder;
import gov.doc.isu.simple.fx.tools.FXAlertOption;
import gov.doc.isu.simple.fx.util.FXUtil;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

/**
 * This class contains the JIRA Project header User Interface components and functionality.
 *
 * @author Richard Salas
 */
public class JIRAProjectHeader extends HBox {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.JIRAProjectHeader";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private Button refreshButton;
    private ComboBox<String> projectComboBox;
    private ComboBox<JIRAStatus> statusComboBox;
    private TextField searchTxt;
    private Button searchButton;
    private JIRAWindowBuilder parent;

    /**
     * Constructor used to create an instance of this class.
     *
     * @param parent
     *        the main gui window
     */
    public JIRAProjectHeader(JIRAWindowBuilder parent) {
        myLogger.entering(MY_CLASS_NAME, "JIRAProjectHeader", parent);
        this.parent = parent;
        layoutForm();
        attachHandlers();
        myLogger.exiting(MY_CLASS_NAME, "JIRAProjectHeader", parent);
    }// end constructor

    /**
     * This method will build the user interface elements and then place them into view for the user.
     */
    private void layoutForm() {
        myLogger.entering(MY_CLASS_NAME, "layoutForm");
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10));

        Label comboBoxLbl = new Label("Project:");
        Label statusLbl = new Label("Status:");

        // project combobox
        // status comboBox
        statusComboBox = new ComboBox<JIRAStatus>();
        projectComboBox = new ComboBox<String>();
        IssueClient issueClient = IssueClient.getInstance();
        ProjectClient projectClient = ProjectClient.getInstance();

        if(!AppUtil.isEmpty(projectClient.getProjects())){
            projectComboBox.getItems().addAll(projectClient.getProjects());
            projectComboBox.getSelectionModel().select(issueClient.getProjectKey());
            statusComboBox.getItems().addAll(JIRAStatus.getJIRAStatuses());
            statusComboBox.getSelectionModel().select(0);
        }// end if

        refreshButton = new Button(null, JiraUtil.createIcon(FontAwesomeIcon.REFRESH));
        refreshButton.setTooltip(new Tooltip("Refresh"));

        HBox search = new HBox(0);
        searchTxt = new TextField();
        searchTxt.setPromptText("Search issue number  (\"*\") wild character ");
        searchTxt.setPrefWidth(230D);
        searchButton = new Button(null, JiraUtil.createIcon(FontAwesomeIcon.SEARCH, "jira-darkblue"));
        searchButton.setTooltip(new Tooltip("Search for an issue"));
        search.getChildren().addAll(searchTxt, searchButton);
        // refreshButton = FontAwesomeIconFactory.get().createIconButton(FontAwesomeIcon.REFRESH);

        getChildren().addAll(comboBoxLbl, projectComboBox, statusLbl, statusComboBox, search, refreshButton);

        // TODO error flag handler ... maybe add logic for the jira model
        if(issueClient.isErrorFlag()){
        }else{
        }// end if...else

        myLogger.exiting(MY_CLASS_NAME, "layoutForm");
    }// end method

    /**
     * This method will attach the combo box and refresh button handlers.
     */
    @SuppressWarnings("unchecked")
    private void attachHandlers() {
        myLogger.entering(MY_CLASS_NAME, "attachHandlers");
        if(projectComboBox.getItems().isEmpty()){
            myLogger.warning("Not adding the action handler to the project combobox");
        }else{
            projectComboBox.setOnAction(event -> {
                parent.focusOnOpenIssuesTab();
                parent.clearSearchedItems();
                loadIssues(event);
            });

            // JSTUI-7 select project field...Richard Salas - added functionality for correcting combo box
            projectComboBox.setOnKeyPressed(event -> {
                if(event.getCode().isLetterKey()){
                    String projectKey = JiraUtil.searchStartsWithLetterInList(projectComboBox.getItems(), String.valueOf(event.getText()).toUpperCase());
                    if(projectKey != null){
                        projectComboBox.getSelectionModel().select(projectKey);
                        // JSTUI-7 select project field...Richard Salas
                        ComboBoxListViewSkin<String> skin = (ComboBoxListViewSkin<String>) projectComboBox.getSkin();
                        skin.getListView().scrollTo(projectKey);
                    }// end if
                }// end if
            });// end anonymous inner class

            projectComboBox.setVisibleRowCount(10);
            statusComboBox.setOnAction(event -> {// JSTUI-5 issue fix for changing label upon selection change.
                parent.setIssuesListLabel(statusComboBox.getSelectionModel().getSelectedItem().getValue());
                loadIssues(event);
            });
            searchButton.setOnAction(event -> searchForIssues());
        }// end if
        refreshButton.setOnAction(event -> refreshAndLoad(event));
        // only allow numbers
        searchTxt.setTextFormatter(new TextFormatter<>(change -> {
            // added the logic below per JSTUI-14 ... probably will update at later time
            String theText = change.getText();
            int wildCards = StringUtils.countMatches(searchTxt.getText(), "*");

            if(("*".equals(theText) && wildCards >= 2) || ("*".equals(searchTxt.getText()) && "*".equals(theText))){
                return null;
            }// end if

            return StringUtils.isNumeric(change.getText()) || "*".equals(change.getText()) ? change : null;
        }));
        searchTxt.setOnKeyReleased(event -> {
            if(KeyCode.ENTER.equals(event.getCode())){
                searchForIssues();
            }// end if
        });

        // just added this for testing this process in ISU
        if("ISU".equals(System.getenv("USERDOMAIN"))){
            statusComboBox.getItems().addAll(JIRAStatus.getJIRAStatuses());
            statusComboBox.getSelectionModel().select(0);
            statusComboBox.setOnAction(event -> {
                parent.setIssuesListLabel(statusComboBox.getSelectionModel().getSelectedItem().getValue());
            });

            projectComboBox.setVisibleRowCount(10);
            projectComboBox.getItems().addAll(Arrays.asList("COIH", "DOCARB", "DOCLENS", "DOCMSHP", "DOCOIM", "OPII", "DOCTMS", "IRISBATCH", "JSTUI", "MOCIS", "MODOCFEES", "PANDA", "TABEBATCH"));
            projectComboBox.getSelectionModel().select(0);
            projectComboBox.setOnAction(event -> System.out.println("tester ... tester ... blah ... "));
            // JSTUI-7 select project field...Richard Salas - added functionality for correcting combo box
            projectComboBox.setOnKeyPressed(event -> {
                if(event.getCode().isLetterKey()){
                    String projectKey = JiraUtil.searchStartsWithLetterInList(projectComboBox.getItems(), String.valueOf(event.getText()).toUpperCase());
                    if(projectKey != null){
                        projectComboBox.getSelectionModel().select(projectKey);
                        ComboBoxListViewSkin<String> skin = (ComboBoxListViewSkin<String>) projectComboBox.getSkin();
                        skin.getListView().scrollTo(projectKey);
                    }// end if
                }// end if
            });// end anonymous inner class
        }// end if

        FXUtil.makeEnterKeyFireButtonAction(searchButton);
        FXUtil.makeEnterKeyFireButtonAction(refreshButton);
        myLogger.exiting(MY_CLASS_NAME, "attachHandlers");
    }// end method

    /**
     * This method will execute a search for a JIRA issue.
     *
     * @param event
     *        the event that was fired by the user
     */
    private void searchForIssues() {
        myLogger.entering(MY_CLASS_NAME, "searchForIssues");
        if(AppUtil.isNullOrEmpty(searchTxt.getText()) || "*".equals(searchTxt.getText())){
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "Must enter an issue number to search for.", "Missing Issue Number", null, AlertType.ERROR);
        }else{
            SearchClient searchClient = SearchClient.getInstance();

            final String projectKey = projectComboBox.getSelectionModel().getSelectedItem();
            final String searchStr = searchTxt.getText();

            parent.setMaskerPaneText("Searching for issues using " + String.valueOf(searchStr) + " in JIRA website...");
            parent.setMaskerPaneVisible(true);
            Task<Void> doWork = new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    searchClient.searchForAndAddIssue(searchStr, projectKey);
                    return null;
                }// end method
            };// end anonymous inner class impl

            doWork.setOnSucceeded(e -> {
                refresh(e);
                parent.focusoOnSearchIssuesTab();
            });
            doWork.setOnFailed(e -> {
                Throwable ex = e.getSource().getException();
                myLogger.severe("Error occurred searching jira site....Error is: " + ex.getMessage());
                parent.setMaskerPaneVisible(false);
                // parent.clearAll();// clear the listings
                FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "There was an issue trying to search for issues on the JIRA website.\nError message recieved is: " + ex.getMessage(), "JIRA Search Error", null, AlertType.ERROR);
            });
            parent.executeTask(doWork);
        }// end method

        myLogger.exiting(MY_CLASS_NAME, "searchForIssues");
    }// end method

    /**
     * This method will execute tasks which will make calls to the jira website to retrieve issues.
     *
     * @param event
     *        the event (button click)
     */
    private void refreshAndLoad(ActionEvent event) {
        myLogger.entering(MY_CLASS_NAME, "refreshAndLoad", event);
        if(projectComboBox.getItems().isEmpty()){// combo box is empty
            myLogger.info("combo box is empty going to reconnect and retrieve issues");
            parent.setMaskerPaneText("Refreshing Project Tracker from JIRA website...");
            parent.setMaskerPaneVisible(true);
            // have to make sure that MOCIS always exists here...
            Task<Void> refresherTask = new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    ProjectClient projectClient = ProjectClient.getInstance();
                    IssueClient issueClient = IssueClient.getInstance();
                    issueClient.setRefresh(true);
                    projectClient.setProjects();
                    if(projectClient.getProjects().contains("MOCIS")){
                        issueClient.setIssues("MOCIS", "0");
                    }else{
                        issueClient.setIssues(projectClient.getProjects().get(0), "0");
                    }// end method
                    return null;
                }// end method
            };
            refresherTask.setOnSucceeded(e -> {
                myLogger.info("populating combobox and then setting the selection...");
                projectComboBox.getItems().addAll(ProjectClient.getInstance().getProjects());
                projectComboBox.getSelectionModel().select(IssueClient.getInstance().getProjectKey());

                statusComboBox.getItems().addAll(JIRAStatus.getJIRAStatuses());
                statusComboBox.getSelectionModel().select(0);

                projectComboBox.setOnAction(theEvent -> loadIssues(theEvent));
                statusComboBox.setOnAction(theEvent -> loadIssues(theEvent));
                searchButton.setOnAction(theEvent -> searchForIssues());

                myLogger.info("refreshing view for user...");
                refresh(e);
            });
            refresherTask.setOnFailed(e -> refreshFailed(e));
            parent.executeTask(refresherTask);
        }else{
            loadIssues(event);
        }// end if

        // JSTUI-3 refresh not clearing issue number field
        searchTxt.setText("");

        myLogger.exiting(MY_CLASS_NAME, "refreshAndLoad");
    }// end method

    /**
     * This method will display error message to user.
     *
     * @param e
     *        the event
     */
    private void refreshFailed(WorkerStateEvent e) {
        myLogger.entering(MY_CLASS_NAME, "refreshFailed", e);
        parent.setMaskerPaneVisible(false);

        if(!IssueClient.getInstance().isAuthorized()){
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "User is not authorized! Please try re-entering connection settings and then clicking Refresh button.", "User Not Authorized", null, AlertType.ERROR, new Image(JIRAConstants.JIRA_ICON_URL));
            new ConnectionSettingsWindow();// show window
        }else{
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "Could not refresh JIRA Project Tracker.  Error message is: \n" + e.getSource().getException().getMessage(), "Refresh Failed", null, AlertType.ERROR);
        }// end if

        myLogger.entering(MY_CLASS_NAME, "refreshFailed");
    }// end method

    /**
     * This method will execute the {@code JIRAClientPrimer} task which is used to make calls to jira website to retrieve issues.
     *
     * @param event
     *        the event
     */
    private void loadIssues(ActionEvent event) {
        myLogger.entering(MY_CLASS_NAME, "loadIssues", event);

        String project = projectComboBox.getSelectionModel().getSelectedItem();
        String id = statusComboBox.getSelectionModel().getSelectedItem().getId();

        myLogger.info("Starting the jira retrieval process for " + String.valueOf(project));
        parent.setMaskerPaneText("Retrieving issues for project named " + String.valueOf(project) + " from JIRA website...");
        parent.setMaskerPaneVisible(true);
        JIRAClientPrimer primer = new JIRAClientPrimer(project, id);
        primer.setOnSucceeded(e -> refresh(e));
        primer.setOnFailed(e -> displayError(e));
        parent.executeTask(primer);
        myLogger.exiting(MY_CLASS_NAME, "loadIssues");
    }// end method

    /**
     * This method is called when a refresh of the user interface is needed. This will be called by a successful execution of the {@code JIRAClientPrimer} task.
     *
     * @param event
     *        the event
     */
    private void refresh(WorkerStateEvent event) {
        myLogger.entering(MY_CLASS_NAME, "refresh", event);

        myLogger.info("Successful in priming the JIRA Model...");
        parent.refresh();
        parent.setMaskerPaneVisible(false);
        // Boolean isTracked = (Boolean) event.getSource().getValue();

        // if(isTracked){
        // parent.focusoOnSentIssuesTab();
        // }else{
        // parent.focusOnOpenIssuesTab();
        // }//end if...else

        myLogger.exiting(MY_CLASS_NAME, "refresh", event);
    }// end method

    private void displayError(WorkerStateEvent event) {
        myLogger.entering(MY_CLASS_NAME, "displayError", event);

        Throwable e = event.getSource().getException();
        myLogger.severe("Error occurred priming the JIRA Model...Error is: " + e.getMessage());
        parent.setMaskerPaneVisible(false);
        parent.clearAll();// clear the listings
        FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "There was an issue trying to connect to the JIRA website.\nError message recieved is: " + event.getSource().getException().getMessage(), "JIRA Connection Error", null, AlertType.ERROR);

        myLogger.entering(MY_CLASS_NAME, "displayError", event);
    }// end method

    /**
     * This method will return the selected JIRA project.
     *
     * @return the selected jira project
     */
    public String getSelectedJIRAProject() {
        return projectComboBox.getSelectionModel().getSelectedItem();
    }// end method

}// end class
