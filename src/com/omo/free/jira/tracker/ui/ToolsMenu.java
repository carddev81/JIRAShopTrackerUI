package com.omo.free.jira.tracker.ui;

import java.util.logging.Logger;

import com.omo.free.jira.tracker.util.JiraUtil;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * This class is used to contain the Tools menu items.
 *
 * @author Richard Salas, April 17, 2019
 */
public class ToolsMenu extends Menu{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.ToolsMenu";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private MenuItem setConnectionSettings;
    private MenuItem publishBug;

    /**
     * Constructor used to create an instance of the ToolsMenu class.
     */
    public ToolsMenu() {
        super("_Tools");
        myLogger.entering(MY_CLASS_NAME, "ToolsMenu");
        initItems();
        myLogger.exiting(MY_CLASS_NAME, "ToolsMenu");
    }//end method

    /**
     * This method will initialize the menu items within the Tools Menu.
     */
    private void initItems() {
        myLogger.entering(MY_CLASS_NAME, "initItems");
        setConnectionSettings = new MenuItem("Connection Settings",  JiraUtil.createIcon(FontAwesomeIcon.GEAR));
        publishBug = new MenuItem("Report Bug or Enhancement", JiraUtil.createIcon(FontAwesomeIcon.BUG));
        publishBug.setOnAction(e -> new BugOrEnhancementsWindow());
        setConnectionSettings.setOnAction((e) -> new ConnectionSettingsWindow());
        getItems().addAll(publishBug, setConnectionSettings);

        myLogger.exiting(MY_CLASS_NAME, "initItems");
    }//end method

}//end class
