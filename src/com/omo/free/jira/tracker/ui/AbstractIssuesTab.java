package com.omo.free.jira.tracker.ui;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.omo.free.jira.tracker.ui.tasks.BuildIssue;
import com.omo.free.jira.tracker.ui.tasks.Download;

import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.application.SFXViewBuilder;
import gov.doc.isu.simple.fx.tools.FXAlertOption;
import gov.doc.isu.simple.fx.util.Constants;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This AbstractIssuesTab class contains common features and functionality for all of the tabs that display issues within the JIRAShopTrackerUI application.
 *
 * @author Richard Salas, October 30, 2019
 */
public abstract class AbstractIssuesTab extends AbstractJIRATrackerTab{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.AbstractIssuesTab";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private GridPane gridPane;
    private ListView<Issue> issuesToSelect;
    private ListView<Issue> selectedIssues;
    private Label openIssuesLbl;
    private Button rightArrow;
    private Button leftArrow;
    private Button sendBtn;

    /**
     * Constructor used to create an instance of the AbstractIssuesTab class.
     *
     * @param parent the parent class (this is the builder class)
     * @param the name of the tab
     */
    public AbstractIssuesTab(JIRAWindowBuilder parent, String name) {
        super(parent, name);
        layoutForm();
        attachHandlers();
    }//end constructor

    /**
     * This method will layout the user interface tab element for user to view.
     */
    private void layoutForm() {
        myLogger.entering(MY_CLASS_NAME, "layoutForm");
        // open issues
        gridPane = new GridPane();

        VBox openIssueBox = new VBox(5);
        openIssuesLbl = new Label(getIssuesToSelectLabelText());
        issuesToSelect = new ListView<>();
        issuesToSelect.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        openIssueBox.getChildren().addAll(openIssuesLbl, issuesToSelect);

        // arrows
        VBox arrowBox = new VBox(20);
        rightArrow = new Button("Add >>>");
        leftArrow = new Button("<<< Remove");
        rightArrow.setPrefWidth(90);
        leftArrow.setPrefWidth(90);

        arrowBox.getChildren().addAll(rightArrow, leftArrow);
        arrowBox.setAlignment(Pos.CENTER);

        // shop issues
        VBox shopIssueBox = new VBox(5);
        Label shopIssuesLbl = new Label(getIssuesToSendLabelText());

        selectedIssues = new ListView<>();
        selectedIssues.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        shopIssueBox.getChildren().addAll(shopIssuesLbl, selectedIssues);
        sendBtn = new Button("Send");

        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(openIssueBox, 0, 1);
        gridPane.add(arrowBox, 1, 1);
        gridPane.add(shopIssueBox, 2, 1);
        gridPane.add(sendBtn, 0, 2, 3, 1);
        // gridPane.setStyle("-fx-background-color:grey");

        GridPane.setHalignment(sendBtn, HPos.CENTER);
        // allowing the elements to resize with window here:
        setGridPaneVgrowAlwaysOnAllNodes(openIssueBox, shopIssueBox, gridPane);
        setGridPaneHgrowAlwaysOnAllNodes(openIssueBox, shopIssueBox, gridPane);
        setVGrowAlwaysOnAllNodes(openIssueBox, issuesToSelect, shopIssueBox, selectedIssues);
        setHGrowAlwaysOnAllNodes(openIssueBox, issuesToSelect, shopIssueBox, selectedIssues, rightArrow, leftArrow, gridPane);
        getChildren().add(gridPane);

        myLogger.exiting(MY_CLASS_NAME, "layoutForm");
    }//end method

    /**
     * This method will add all user interface element handlers.
     */
    private void attachHandlers() {
        myLogger.entering(MY_CLASS_NAME, "attachHandlers");

        issuesToSelect.setCellFactory(thecell -> {
            return getIssueListCell();
        });
        selectedIssues.setCellFactory(thecell -> {
            return getIssueListCell();
        });

        //set up context menus here
        issuesToSelect.setOnMouseClicked(e -> contextMenu(e));
        selectedIssues.setOnMouseClicked(e -> contextMenu(e));

        rightArrow.setOnAction(e -> addIssues(e));
        leftArrow.setOnAction(e -> removeIssues(e));
        sendBtn.setOnAction(e -> sendJiraIssues(e));

        myLogger.exiting(MY_CLASS_NAME, "attachHandlers");
    }// end method

    /**
     * This method must be implemented by your class.  This method when called should refresh the {@code TabPane}.
     */
    protected abstract void refresh();

    /**
     * This method returns the label string used for display on issues to select.
     *
     * @return label for issues to select
     */
    protected abstract String getIssuesToSelectLabelText();

    /**
     * This method returns the label string used for display on issues to send.
     *
     * @return label for issues to select
     */
    protected abstract String getIssuesToSendLabelText();

    /**
     * This method returns an instance of the IssueListCell used for displaying issue and graphic.
     *
     * @return the IssueListCell instance
     */
    protected abstract IssueListCell getIssueListCell();

    /**
     * This method will download/track/send all the selected JIRA issues to shop via email.
     *
     * @param e the action event
     */
    protected void sendJiraIssues(ActionEvent e) {
        myLogger.entering(MY_CLASS_NAME, "sendJiraIssues", e);

        List<Issue> issuesToSend = getSelectedIssues();
        if(issuesToSend.isEmpty()){
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "There are no issues to send.", "What Issues?", null, AlertType.WARNING);
        }else{
            myLogger.info("sending " + issuesToSend.size() + " jira issues to shop...");
            StringBuilder message = new StringBuilder();
            message.append("You are about to send the following issues to the shop:");
            message.append(Constants.LINESEPERATOR);
            message.append(Constants.LINESEPERATOR);
            for(int i = 0, j = issuesToSend.size();i < j;i++){
                message.append("* ").append(issuesToSend.get(i).getKey()).append(Constants.LINESEPERATOR);
                if(i>=9 && issuesToSend.size() > 10){//only displaying 10
                    message.append("* MORE...").append(Constants.LINESEPERATOR);
                    break;
                }//end if
            }// end for
            message.append(Constants.LINESEPERATOR);
            message.append("Are you sure you want to send them?");

            //JSTUI-11 created the SendIssuesDialog to allow user to add text email.
            SendIssuesDialog dialog = new SendIssuesDialog(message);
            if(dialog.isOk()){
                //JSTUI-18 Additional Email Recipients requested (Richard Salas)
                Download download = new Download(issuesToSend, dialog.getOptionalEmailText(), dialog.getCommaSeparatedEmailAddresses());
                download.setOnSucceeded(ev -> showDownloadSuccess(ev));
                download.setOnFailed(ev -> showDownloadFailure(ev));
                setMaskerPaneText("Downloading and sending " + getSelectedJIRAProject() + " JIRA Issues to Shop...");
                setMaskerPaneVisible(true);
                executeTask(download);
            }// end if
        }// end if...else
        myLogger.exiting(MY_CLASS_NAME, "sendJiraIssues");
    }// end method

    /**
     * Removes an issue from the issues to send to shop selection box.
     *
     * @param e
     *        event that was fired off.
     */
    private void removeIssues(ActionEvent e) {
        myLogger.entering(MY_CLASS_NAME, "removeIssues", e);

        MultipleSelectionModel<Issue> selection = selectedIssues.getSelectionModel();
        if(selection.getSelectedItems().isEmpty()){
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "You must select an issue to Remove.", "No Issue Selected", "No Issue Selected", AlertType.WARNING);
        }else{
            issuesToSelect.getItems().addAll(selection.getSelectedItems());

            // remove the issues from openIssues
            ObservableList<Issue> itemsToOpen = issuesToSelect.getItems();
            Iterator<Issue> it = selectedIssues.getItems().iterator();
            Issue openIssue = null;
            while(it.hasNext()){
                openIssue = it.next();
                for(int i = 0, j = itemsToOpen.size();i < j;i++){// remove the open issues
                    if(itemsToOpen.get(i).getKey().equals(openIssue.getKey())){
                        it.remove();
                        break;
                    }// end if
                }
            }// end while

            selectedIssues.refresh();// refresh toSendIssues
            issuesToSelect.refresh();// refresh trackedIssues
        }// end method

        myLogger.exiting(MY_CLASS_NAME, "removeIssues");
    }// end method

    /**
     * Adds an issue to the issues to send to shop selection box.
     *
     * @param e
     *        event that was fired off.
     */
    private void addIssues(ActionEvent e) {
        myLogger.entering(MY_CLASS_NAME, "addIssues", e);

        MultipleSelectionModel<Issue> selection = issuesToSelect.getSelectionModel();
        if(selection.getSelectedItems().isEmpty()){
            FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "You must select an issue to Add.", "No Issue Selected", "No Issue Selected", AlertType.WARNING);
        }else{
            // add to shop issues
            selectedIssues.getItems().addAll(selection.getSelectedItems());

            // remove the issues from openIssues
            ObservableList<Issue> itemsToSend = selectedIssues.getItems();
            Iterator<Issue> it = issuesToSelect.getItems().iterator();
            Issue openIssue = null;
            while(it.hasNext()){
                openIssue = it.next();
                for(int i = 0, j = itemsToSend.size();i < j;i++){// remove the open issues
                    if(itemsToSend.get(i).getKey().equals(openIssue.getKey())){
                        it.remove();
                        break;
                    }// end if
                }
            }// end while
            issuesToSelect.refresh();// refresh
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "addIssues");
    }// end method

    /**
     * This method will be called when the user right clicks on a list item within one of the list views.
     *
     * <p>A menu of options will be displayed to user to allow for them to fire an action off.
     *
     * @param mouseEvent the mouse event
     */
    private void contextMenu(MouseEvent mouseEvent) {
        myLogger.entering(MY_CLASS_NAME, "contextMenu", mouseEvent);
        //TODO can probably add more here later.
        if(MouseButton.SECONDARY == mouseEvent.getButton() && mouseEvent.getTarget() instanceof Text){
            ContextMenu menu = new ContextMenu();

            final List<Issue> issuesSelected;

            if (mouseEvent.getSource() == issuesToSelect){
                issuesSelected = issuesToSelect.getSelectionModel().getSelectedItems();
            }else if(mouseEvent.getSource() == selectedIssues){
                issuesSelected = selectedIssues.getSelectionModel().getSelectedItems();
            }else{
                issuesSelected = null;
            }//end else...if

            if(issuesSelected!=null && issuesSelected.size() == 1){
                //set up 2 menu items
                MenuItem viewIssues = new MenuItem("View Issue Details");
                viewIssues.setOnAction(e -> {
                    Issue issueToView = issuesSelected.get(0);
                    BuildIssue buildIssue = new BuildIssue(issueToView);
                    buildIssue.setOnSucceeded(ev -> displayHTML(ev, issueToView));
                    buildIssue.setOnFailed(ev -> displayIssueError(ev, issueToView));
                    setMaskerPaneText("Loading JIRA " + issueToView.getKey() + " details...");
                    setMaskerPaneVisible(true);
                    executeTask(buildIssue);
                });//end menu item.

                menu.getItems().add(viewIssues);

                menu.show(SFXViewBuilder.getPrimaryStage(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }//end if
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "contextMenu", mouseEvent);
    }//end method

    /**
     * This method will be called when the user has requested to view a JIRA issue's details.
     *
     * @param ev the action event
     * @param issueToView the issued used for display
     */
    private void displayHTML(WorkerStateEvent ev, Issue issueToView) {
        myLogger.entering(MY_CLASS_NAME, "displayHTML", new Object[]{ev, issueToView});

        String htmlString = (String) ev.getSource().getValue();//html string
        //load web view

        // nothing special here just basic stage creation!
        Stage stage = new Stage();
        stage.setTitle(String.valueOf(issueToView.getKey()) + " Details");
        stage.getIcons().add(SFXViewBuilder.getPrimaryStage().getIcons().get(0));
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.setResizable(true);
        stage.initOwner(SFXViewBuilder.getPrimaryStage());
        stage.initStyle(StageStyle.DECORATED);
        stage.setAlwaysOnTop(true);
        // create the web view
        WebView webView = new WebView();
        webView.getEngine().loadContent(htmlString);

        // create the root container
        VBox root = new VBox(webView);
        VBox.setVgrow(webView, Priority.ALWAYS);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        setMaskerPaneVisible(false);
        stage.showAndWait();

        myLogger.exiting(MY_CLASS_NAME, "displayIssueError");
    }//end method

    /**
     * This method will be called when there is an issue that occurs during the loading of html to display.
     *
     * @param ev the action event
     * @param issueToView the issue for display
     */
    private void displayIssueError(WorkerStateEvent ev, Issue issueToView) {
        myLogger.entering(MY_CLASS_NAME, "displayIssueError", new Object[]{ev, issueToView});

        setMaskerPaneVisible(false);
        FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "Could not load issue " + String.valueOf(issueToView.getKey()) + "\n\nError is: " + ev.getSource().getException().getMessage(), "Error Loading Issue", null, AlertType.ERROR);

        myLogger.exiting(MY_CLASS_NAME, "displayIssueError");
    }//end method

    /**
     * This method will display to users on successful sends of JIRA issues.
     *
     * @param ev the worker state event
     */
    protected void showDownloadSuccess(WorkerStateEvent ev) {
        myLogger.entering(MY_CLASS_NAME, "showDownloadSuccess", ev);

        setMaskerPaneVisible(false);
        refresh();// refresh
        //no matter what always clear issues to send list here..
        //[JSTUI-15] Values not retained in Issues to send to ISU Shop column during new search (Richard Salas)
        selectedIssues.getItems().clear();
        // move issues to other pane here.
        FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), "Successfully sent JIRA Issues to Shop.", "Successfully Sent JIRA Issues", null, AlertType.INFORMATION);

        myLogger.exiting(MY_CLASS_NAME, "showDownloadSuccess");
    }// end method

    /**
     * This method will display to users on failures of sending JIRA issues to shop.
     * @param ev the worker state event
     */
    protected void showDownloadFailure(WorkerStateEvent ev) {
        myLogger.entering(MY_CLASS_NAME, "showDownloadFailure", ev);

        setMaskerPaneVisible(false);
        FXAlertOption.showAlert(SFXViewBuilder.getPrimaryStage(), AppUtil.breakUpString("Issue occurred while downloading JIRA issues.\n\nError is: " + ev.getSource().getException().getMessage(), 150), "Error Downloading JIRA Issues", null, AlertType.ERROR);

        myLogger.exiting(MY_CLASS_NAME, "showDownloadFailure");
    }// end method

    /**
     * This method will add the list of issues that are passed into this method to the {@code issuesToSelect} {@code ListView}.
     * @param issuesToAdd
     */
    protected void addIssuesToListView(List<Issue> issuesToAdd){
        myLogger.entering(MY_CLASS_NAME, "addIssuesToListView", "issuesToAdd.size()=" + (AppUtil.isEmpty(issuesToAdd) ? "0" : issuesToAdd.size()));

        issuesToSelect.getItems().addAll(issuesToAdd);

        myLogger.exiting(MY_CLASS_NAME, "addIssuesToListView");
    }//end method

    /**
     * This method will clear the listings.
     */
    protected void clear() {
        myLogger.entering(MY_CLASS_NAME, "clear");

        issuesToSelect.getItems().clear();
        selectedIssues.getItems().clear();

        myLogger.exiting(MY_CLASS_NAME, "clear");
    }// end method

    /**
     * @return the gridPane
     */
    protected GridPane getGridPane() {
        return gridPane;
    }

    /**
     * @return the selectedIssues
     */
    public List<Issue> getSelectedIssues() {
        return selectedIssues.getItems();
    }//end method

    /**
     * added this method to account for [JSTUI-15] Values not retained in Issues to send to ISU Shop column during new search (Richard Salas)
     * @return the issuesToSelect
     */
    public List<Issue> getIssuesToSelect() {
        return issuesToSelect.getItems();
    }//end method

    /**
     * @return the openIssuesLbl
     */
    public Label getOpenIssuesLbl() {
        return openIssuesLbl;
    }//end method

}//end class
