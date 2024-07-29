package com.omo.free.jira.tracker.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * This class is
 * @author rts000is
 *
 */
public class UnderConstructionBox extends VBox {

    /**
     * Constructor used to create an instance of the underconstruction box.
     *
     * @param message the message that is used for displaying a message to the user.
     */
    public UnderConstructionBox(String message){
        super(20);
        setStyle("-fx-border-width:8;-fx-border-radius:5;-fx-border-color:rgb(137, 94, 204);");
        setAlignment(Pos.CENTER);
        Text theText = new Text(message);
        getChildren().add(theText);
    }//end constructor

}//end class
