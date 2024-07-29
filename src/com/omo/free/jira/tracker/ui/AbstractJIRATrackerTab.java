package com.omo.free.jira.tracker.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * This AbstractJIRATrackerTab class contains common features and functionality for all of the tabs within the JIRAShopTrackerUI application.
 *
 * @author Richard Salas, April 17, 2019
 */
public abstract class AbstractJIRATrackerTab extends Tab{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.AbstractJIRATrackerTab";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /* common instance variables */
    protected JIRAWindowBuilder parent;
    protected HBox tabContent;

    /**
     * Constructor used to create an instance of the AbstractJIRATrackerTab class.
     *
     * @param parent the parent class (this is the builder class)
     * @param the name of the tab
     */
    public AbstractJIRATrackerTab(JIRAWindowBuilder parent, String name) {
        super(name);
        this.parent = parent;
        this.tabContent = new HBox(20);
        this.tabContent.setAlignment(Pos.CENTER);
        this.tabContent.setPadding(new Insets(10, 10, 10, 10));
        setContent(this.tabContent);
        //this.tabContent.setStyle("-fx-background-color:grey");
        HBox.setHgrow(this.tabContent, Priority.ALWAYS);
    }//end constructor

    /**
     * The masker pane text to set.
     *
     * @param text the text to display to the user while he/she waits
     */
    public void setMaskerPaneText(String text){
        parent.setMaskerPaneText(text);
    }//end method

    /**
     * This method will set the visible property of the masker pane.
     *
     * @param isVisible true or false value
     */
    public void setMaskerPaneVisible(boolean isVisible){
        parent.setMaskerPaneVisible(isVisible);
    }//end method

    /**
     * This method will return the nodes contained within the {@code tabConent}.
     *
     * @return {@code ObservableList<Node>} a list of nodes contained within the tab content
     */
    public ObservableList<Node> getChildren(){
        return tabContent.getChildren();
    }//end method

    /**
     * This method will execute a task on a background thread.

     * @param <V> the return value of the task
     * @param aTask the task to be executed on a background thread
     */
    protected <V> void executeTask(Task<V> aTask){
        myLogger.entering(MY_CLASS_NAME, "executeTask", aTask);
        parent.executeTask(aTask);
        myLogger.exiting(MY_CLASS_NAME, "executeTask", aTask);
    }//end method

    /**
     * This method will set the HGrow {@code Priority.ALWAYS} on the nodes array passed into this method.
     *
     * @param nodes the array of nodes to set VGrow attributes
     */
    protected void setHGrowAlwaysOnAllNodes(Node... nodes){
        myLogger.entering(MY_CLASS_NAME, "setHGrowAlwaysOnAllNodes", nodes);
        for(int i = 0, j = nodes.length; i < j; i++){
            HBox.setHgrow(nodes[i], Priority.ALWAYS);
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "setHGrowAlwaysOnAllNodes");
    }//end method

    /**
     * This method will set the VGrow {@code Priority.ALWAYS} on the nodes array passed into this method.
     *
     * @param nodes the array of nodes to set VGrow attributes
     */
    protected void setVGrowAlwaysOnAllNodes(Node... nodes){
        myLogger.entering(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes", nodes);
        for(int i = 0, j = nodes.length; i < j; i++){
            VBox.setVgrow(nodes[i], Priority.ALWAYS);
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes");
    }//end method

    /**
     * This method will set the GridPane HGrow {@code Priority.ALWAYS} on the nodes array passed into this method.
     *
     * @param nodes the array of nodes to set grid pane HGrow attributes
     */
    protected void setGridPaneHgrowAlwaysOnAllNodes(Node... nodes) {
        myLogger.entering(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes", nodes);
        for(int i = 0, j = nodes.length; i < j; i++){
            GridPane.setHgrow(nodes[i], Priority.ALWAYS);
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes");
    }//end method

    /**
     * This method will set the GridPane VGrow {@code Priority.ALWAYS} on the nodes array passed into this method.
     *
     * @param nodes the array of nodes to set the gridpane VGrow
     */
    protected void setGridPaneVgrowAlwaysOnAllNodes(Node... nodes) {
        myLogger.entering(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes", nodes);
        for(int i = 0, j = nodes.length; i < j; i++){
            GridPane.setVgrow(nodes[i], Priority.ALWAYS);
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes");
    }//end method

    /**
     * This method will set the style id on the array of nodes that you have sent into this method.
     *
     * @param styleId the style id to set
     * @param nodes the array of nodes to set style ids
     */
    protected void setStyleIdOnNodes(String styleId, Node... nodes){
        myLogger.entering(MY_CLASS_NAME, "setStyleIdOnNodes", new Object[]{styleId, nodes});
        for(int i = 0, j = nodes.length; i < j; i++){
            nodes[i].setId(styleId);
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "setStyleIdOnNodes");
    }//end method

    /**
     * The currently selected JIRA project.
     *
     * @return the selected JIRA project
     */
    protected String getSelectedJIRAProject(){
        return parent.getSelectedJIRAProject();
    }//end method

}//end class
