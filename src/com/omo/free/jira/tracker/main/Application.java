/**
 *
 */
package com.omo.free.jira.tracker.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.security.KeyStoreException;
import java.util.concurrent.TimeUnit;

import com.omo.free.jira.tracker.ui.JIRAWindowBuilder;

import gov.doc.isu.simple.fx.application.SFXApplication;
import gov.doc.isu.simple.fx.application.SFXViewBuilder;


/**
 * This class is used to start the JIRA Shop Tracker UI Application.
 *
 * @author Richard Salas, April 17, 2019
 */
public class Application extends SFXApplication {

    /**
     * @param args the arguments used to start this application (if any)
     * @param sfxViewClass the class that extends the {@code SFXViewBuilder}
     */
    public Application(String[] args, Class<? extends SFXViewBuilder> sfxViewClass) {
        super(args, sfxViewClass);
    }//end constructor

    /**
     * This method is used to start the application.
     *
     * @param args the arguments used to start this application (if any)
     * @throws KeyStoreException
     */
    public static void main(String[] args) throws Exception {
        new Application(args, JIRAWindowBuilder.class);
    }//end method

    /**
     * This method will return the parent Resources directory name. This method is used during the creating of the resources directory structure.
     * @return the resources directory name
     */
    @Override
    public String getResourcesParentDirectoryName() {
        return "JIRAShopTrackerUI";
    }//end method

    /**
     * This method will update the splash screen with some visual effects
     */
    @Override public void updateSplashScreenFunctionality() {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if(splash == null){
            return;
        }//end if

        final Graphics2D graphics = splash.createGraphics();
        if (graphics == null) {
            return;
        }//end if

        //animate the splash screen.
        Thread animate = new Thread(){
            @Override public void run() {
                String[] stepsarray = {"JIRA Shop Tracker is starting to load ", "Establishing whether connection to JIRA is secured ", "Connecting to https://jira.url.goes.here to retrieve issues ", "Populating JIRA Shop Tracker with issues "};
                String[] dots = {".", ". .", ". . .", ". . . .", ". . . . .", ". . . . . .", ". . . . . . .", ". . . . . . . .", ". . . . . . . . .", ". . . . . . . . . ."};
                int pos = 0;
                int counter = 0;
                while(splash.isVisible()){
                    try{
                        splash.update();
                        if(pos == dots.length){
                            pos = 0;
                        }//end if
                        graphics.setComposite(AlphaComposite.Clear);
                        graphics.fillRect(48,387,500,20);//this clears the text everytime.
                        graphics.setPaintMode();
                        //graphics.setColor(new Color(0, 140, 255));
                        graphics.setColor(Color.WHITE);
                        graphics.setFont(new Font("Arial", Font.BOLD, 11));

                        if(counter < 15){
                            graphics.drawString(stepsarray[0] + dots[pos++], 48, 397);
                        }else if(counter < 30){

                            graphics.drawString(stepsarray[1] + dots[pos++], 48, 397);
                        }else if(counter < 45){

                            graphics.drawString(stepsarray[2] + dots[pos++], 48, 397);
                        }else{
                            graphics.drawString(stepsarray[3] + dots[pos++], 48, 397);
                        }//end if...else
                        TimeUnit.MILLISECONDS.sleep(550);
                        counter++;
                    }catch(InterruptedException e){
                        System.err.println("InterruptedException occurred during the animation of the splash screen. Error message is: " + e.getMessage());
                    }catch(Exception e){
                        System.err.println("Exception occurred during the animation of the splash screen. Error message is: " + e.getMessage());
                    }//end try...catch
                }//end while
            }//end run
        };//end class
        animate.start();
    }//end method

}//end class
