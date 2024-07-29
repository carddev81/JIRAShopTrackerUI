package com.omo.free.jira.tracker.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.omo.free.jira.tracker.constants.JIRAConstants;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.util.Constants;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * This class contains multiple utility helper methods (static) used by the JIRA Shop Tracker.
 *
 * @author Richard Salas, April 17, 2019
 */
public class JiraUtil {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.util.JiraUtil";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /**
     * Default Constructor
     */
    private JiraUtil() {
    }//end constructor

    /**
     * This method is used to zip up an array of files (@link java.io.File} that are passed into this method as a parameter. The name and path to the zip file is also passed into this method as well.
     *
     * @param filesToZip
     *        an array of {@link java.io.File} Files that are to be zipped up
     * @param zipFilePath
     *        The {@link java.lang.String} value of the path to the zip file that will be created
     * @throws Exception
     *         thrown during the zipping up of a file if something goes wrong.
     */
    public static void zipUpFiles(File[] filesToZip, File zipFilePath) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "zipUpFiles", new Object[]{filesToZip, zipFilePath});
        if(filesToZip == null || zipFilePath == null){
            String errorMessage = "Arguments passed into this method must not be null. Arguments are: filesToZip=null" + ", zipFilePath=" + String.valueOf(zipFilePath);
            myLogger.severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }// end if

        if(!zipFilePath.getParentFile().exists()){
            String errorMessage = "The directory " + zipFilePath.getParentFile().getAbsolutePath() + " that you want to zip the file to does not exist! You will need to make sure the directory exists before attempting to create the zip file";
            myLogger.severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }// end if

        // looping through list of files to make sure they do exist.
        for(int i = 0, j = filesToZip.length;i < j;i++){
            if(filesToZip[i] == null || !filesToZip[i].exists()){
                String errorMessage = "Argument filesToZip passed into this method contains a file that does not exist.  The file is: " + (filesToZip[i] != null ? filesToZip[i].getAbsolutePath() : "null");
                myLogger.severe(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }// end if
        }// end for

        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        int numOfBytes = 0;
        byte[] data = null;
        int byteCount = 0;
        try{
            myLogger.info("starting to create the zip file " + zipFilePath.getName());
            zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
            File file;
            ZipEntry fileEntry;
            for(int i = 0, j = filesToZip.length;i < j;i++){
                file = filesToZip[i];
                // Create a file input stream for reading in the file..
                bis = new BufferedInputStream(new FileInputStream(file));
                // Create a Zip Entry and put it into the archive (no data yet)
                fileEntry = new ZipEntry(file.getName());
                zos.putNextEntry(fileEntry);
                numOfBytes = (int) file.length();
                // Create byte array object named data and declare byte count variable;
                data = new byte[numOfBytes];
                byteCount = bis.read(data, 0, numOfBytes);
                // Create loop that reads from the buffered input stream and writes to the zip output stream until the bis has been entirely read.
                while(byteCount > -1){
                    zos.write(data, 0, byteCount);
                    byteCount = bis.read(data, 0, byteCount);
                }// end while
                zos.flush();
                bis.close();
            }// end for
            myLogger.info("successfully zipped up files within the file: " + zipFilePath.getAbsolutePath());
        }catch(FileNotFoundException e){
            myLogger.log(Level.SEVERE, "FileNotFoundException occurred while trying to zip up " + filesToZip.length + " files for file: " + zipFilePath.getPath() + ". Message is: " + e.getMessage(), e);
            throw e;
        }catch(IOException e){
            myLogger.log(Level.SEVERE, "IOException occurred while zipping up " + filesToZip.length + " files for file: " + zipFilePath.getPath() + ". Message is: " + e.getMessage(), e);
            throw e;
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while zipping up " + filesToZip.length + " files for file: " + zipFilePath.getPath() + ". Message is: " + e.getMessage(), e);
            throw e;
        }finally{
            try{
                // in case an exception was thrown just double checking to make sure the ZipOutputStream is closed.
                if(zos != null){
                    zos.close();
                }// end if
                 // in case an exception was thrown just double checking to make sure the BufferedInputStream is closed.
                if(bis != null){
                    bis.close();
                }// end if
            }catch(IOException e){
                myLogger.log(Level.SEVERE, "IOException occurred while trying to close streams. " + filesToZip.length + " files were attempted to be zipped for file: " + zipFilePath.getPath() + ". Message is: " + e.getMessage(), e);
                throw e;
            }// end try...catch
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "zipUpFiles");
    }// end zipLogFiles

    /**
     * This method will check to see if an issue exists within the issues list passed into this method.
     *
     * @param issues the issues to search
     * @param issue the issue that is being searched for
     * @return true or false on whether or not the issue exists
     */
    public static boolean checkIssuesExistsInListAlready(List<Issue> issues, Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "checkIssuesExistsInListAlready", new Object[]{issues, issue});

        boolean exists = false;
        Issue temp = null;
        for(int i = 0, j = issues.size(); i < j; i++){
            temp = issues.get(i);
            if(temp.getKey().equals(issue.getKey())){
                exists = true;
                break;
            }//end if
        }//end for

        myLogger.exiting(MY_CLASS_NAME, "checkIssuesExistsInListAlready", exists);
        return exists;
    }//end method

    /**
     * This method will write the passed in {@code InputStream} to a local file using the {@code filename} and {@code issueKey} parameters.
     *
     * @param is the input stream containing the bytes of a file
     * @param filename the filename used for creating the local file
     * @param issueKey the issue key used for creating a unique directory to place the file.
     * @throws IOException
     */
    public static void writeFile(InputStream is, String filename, String issueKey) throws IOException {
        myLogger.entering(MY_CLASS_NAME, "writeFile", new Object[]{is, filename, issueKey});

        File fileToWriteTo = null;
        FileOutputStream fos = null;

        try{
            byte[] bytes = IOUtils.toByteArray(is);
            fileToWriteTo = new File(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY + "/" + issueKey + "/" + filename);
            if(!fileToWriteTo.createNewFile()){
                throw new IOException("Could not download the file.");
            }// end if
            fos = new FileOutputStream(fileToWriteTo);
            fos.write(bytes);
            fos.flush();
            myLogger.info("successful in downloading " + String.valueOf(filename));
        }finally{
            if(is != null){
                is.close();
            }// end if
            if(fos != null){
                fos.close();
            }// end if
        }// end try...finally

        myLogger.exiting(MY_CLASS_NAME, "writeFile");
    }// end method

    /**
     * This method is used to clean the string passed in by making sure to return an empty string instead of null.
     *
     * @param stringToClean the string to clean
     * @return either an empty string or the string that was passed into this method
     */
    public static String cleanStr(String stringToClean) {
        return stringToClean == null ? "" : stringToClean;
    }//end method

    /**
     * This method is used to either format a {@code org.joda.time.DateTime} to MM/dd/yy or an empty string.
     *
     * @param dt the date and time to format as a string
     * @return the date formatted into the MM/dd/yy pattern or blank
     */
    public static String getFormattedDateOrBlank(DateTime dt) {
        myLogger.entering(MY_CLASS_NAME, "getFormattedDateOrBlank", dt);

        String formattedDate = null;
        if(dt == null){
            formattedDate = "";
        }else{
            formattedDate = dt.toString("MM/dd/yy");
        }//end if...else

        myLogger.exiting(MY_CLASS_NAME, "getFormattedDateOrBlank", formattedDate);
        return formattedDate;
    }// end method

    /**
     * This method will decrypt the passed in encryptedStr value using the common secret passphrase.
     *
     * @param encryptedStr
     * @return the decrypted value of the passed in encrypted value
     */
    public static String decrypt(String encryptedStr) {
        myLogger.entering(MY_CLASS_NAME, "decrypt", encryptedStr);
        StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
        decryptor.setPassword(Constants.SECRET_PASSWORD);
        myLogger.exiting(MY_CLASS_NAME, "decrypt", "****");
        return decryptor.decrypt(encryptedStr);
    }// end method

    /**
     * This method will encrpt the passed in value using the common secret passphrase.
     *
     * @param humanReadableStr
     *        the string value to encrypt
     * @return the decrypted string value
     */
    public static String encrypt(String humanReadableStr) {
        myLogger.entering(MY_CLASS_NAME, "encrypt");
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(gov.doc.isu.simple.fx.util.Constants.SECRET_PASSWORD);
        myLogger.entering(MY_CLASS_NAME, "encrypt");
        return encryptor.encrypt(humanReadableStr);
    }// end method

    /**
     * This method will set the HGrow {@code Priority.ALWAYS} on the nodes array passed into this method.
     *
     * @param nodes the array of nodes to set VGrow attributes
     */
    public static void setHGrowAlwaysOnAllNodes(Node... nodes){
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
    public static void setVGrowAlwaysOnAllNodes(Node... nodes){
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
    public static void setGridPaneHgrowAlwaysOnAllNodes(Node... nodes) {
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
    public static void setGridPaneVgrowAlwaysOnAllNodes(Node... nodes) {
        myLogger.entering(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes", nodes);
        for(int i = 0, j = nodes.length; i < j; i++){
            GridPane.setVgrow(nodes[i], Priority.ALWAYS);
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "setVGrowAlwaysOnAllNodes");
    }//end method

    /**
     * This method will create a Text {@code Node} icon colored with the jira-purple css value
     * @param fontAwesomeIcon the icon to use
     * @return theIcon the Text node that was created
     */
    public static Text createIcon(FontAwesomeIcon fontAwesomeIcon){
        myLogger.entering(MY_CLASS_NAME, "createIcon", fontAwesomeIcon);
        Text theIcon = FontAwesomeIconFactory.get().createIcon(fontAwesomeIcon);
        theIcon.setId("jira-purple");
        myLogger.exiting(MY_CLASS_NAME, "createIcon", fontAwesomeIcon);
        return theIcon;
    }//end method

    /**
     * This method will create a Text {@code Node} icon colored with the css id value...therefore you must provide the value within the css file.
     *
     * @param fontAwesomeIcon the icon to use
     * @return theIcon the Text node that was created
     */
    public static Text createIcon(FontAwesomeIcon fontAwesomeIcon, String cssId){
        myLogger.entering(MY_CLASS_NAME, "createIcon", fontAwesomeIcon);
        Text theIcon = FontAwesomeIconFactory.get().createIcon(fontAwesomeIcon);
        theIcon.setId(cssId);
        myLogger.exiting(MY_CLASS_NAME, "createIcon", fontAwesomeIcon);
        return theIcon;
    }//end method

    /**
     * htmlEscape(String) escapes the eight html characters and replaces them with entity equivalents.
     * <p>
     * Added additional for tab character to be replaced by 4 &nbsp;
     * </p>
     *
     * @param s
     *        String to be modified
     * @return String
     */
    public static String htmlEscape(String s) {
        // give them back what they gave me if they gave me a null instead of a string
        // avoids null pointer error below
        if (s == null) return s;

        StringBuilder sb = new StringBuilder();

        // loop through each character of the string and look at it
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // replace html-special characters with html-escaped characters
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '\"':
                    sb.append("&quot;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\'':
                    sb.append("&#039;");
                    break;
                case '\n':
                    sb.append("&#010;");
                    break;
                case '\r':
                    sb.append("&#013;");
                    break;
                case '\t':
                    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                    break;
                default:
                    sb.append(c);
            }// end switch
        }//end for
        return sb.toString();
    }//end method

    /**
     * This method will search the passed in list of string for a word that starts with the letter passed into this method.  The word that is found will be returned.
     * @param items the list of string
     * @param letter the letter to search for
     * @return item the word found
     */
    public static String searchStartsWithLetterInList(ObservableList<String> items, String letter) {
        String item = null;
        for(int i = 0, j = items.size(); i < j; i++){
            if(items.get(i).toUpperCase().startsWith(letter)){
                item = items.get(i);
                break;
            }//end if
        }//end for
        return item;
    }//end method

    /**
     * This method will return the email address by domain user id and empty if not found.
     *
     * @return address the email address
     */
    public static String getEmailAddressByDomainUserId(){
        myLogger.entering(MY_CLASS_NAME, "getEmailAddressByDomainUserId");

        Map<String, String> domainMap = new HashMap<>();
        domainMap.put("AMW000IS", "Amy.Bell@oa.mo.gov");
        domainMap.put("PENNM", "Marcus.Penn@oa.mo.gov");
        domainMap.put("BACKUR", "Robert.Backus@oa.mo.gov");
        domainMap.put("GJB000IS", "Gloria.Burt@oa.mo.gov");
        domainMap.put("GIRLIR", "Richard.Gerling@oa.mo.gov");
        domainMap.put("GRANTC1", "Chris.Grant@oa.mo.gov");
        domainMap.put("SRB000IS", "Sandy.Mealy@oa.mo.gov");
        domainMap.put("JPM000IS", "Jim.Meili@oa.mo.gov");
        domainMap.put("MSW000IS", "Melody.Rush@oa.mo.gov");
        domainMap.put("SHEPES", "Stephanie.Sheperd@oa.mo.gov");
        domainMap.put("GDD000IS", "Glynne.Strube@oa.mo.gov");
        domainMap.put("GBT000IS", "Gay.Thomas@oa.mo.gov ");
        domainMap.put("DTW000IS", "Dwayne.Walker@oa.mo.gov");
        domainMap.put("LOWED", "David.Lowe@oa.mo.gov");
        domainMap.put("WOODYA", "Addison.Woody@oa.mo.gov");
        domainMap.put("BUZARA1", "Arbizene.Buzard@oa.mo.gov");
        domainMap.put("CHIPLJ1", "Jennifer.Chipley@oa.mo.gov");
        domainMap.put("CHIPLEJ1", "Jennifer.Chipley@oa.mo.gov");
        domainMap.put("JAHRG", "Gabrielle.Jahr@oa.mo.gov");
        domainMap.put("JAHRG1", "Gabrielle.Jahr@oa.mo.gov");

        String address = domainMap.get(JIRAConstants.JIRA_USER_ID.toUpperCase());

        myLogger.exiting(MY_CLASS_NAME, "getEmailAddressByDomainUserId");
        return AppUtil.isNullOrEmpty(address) ? "" : address;
    }//end method

    /**
     * This method will return the user name by domain user id and empty if not found.
     *
     * @return address the email address
     */
    public static String getUserNameByDomainUserId(){
        myLogger.entering(MY_CLASS_NAME, "getUserNameByDomainUserId");

        Map<String, String> domainMap = new HashMap<>();
        domainMap.put("AMW000IS", "Amy Bell");
        domainMap.put("PENNM", "Marcus Penn");
        domainMap.put("BACKUR", "Robert Backus");
        domainMap.put("GJB000IS", "Gloria Burt");
        domainMap.put("GIRLIR", "Richard Gerling");
        domainMap.put("GRANTC1", "Chris Grant");
        domainMap.put("SRB000IS", "Sandy Mealy");
        domainMap.put("JPM000IS", "Jim Meili");
        domainMap.put("MSW000IS", "Melody Rush");
        domainMap.put("SHEPES", "Stephanie Sheperd");
        domainMap.put("GDD000IS", "Glynne Dee Strube");
        domainMap.put("GBT000IS", "Gay Thomas");
        domainMap.put("DTW000IS", "Dwayne Walker");
        domainMap.put("LOWED", "David Lowe");
        domainMap.put("WOODYA", "Addison Woody");
        domainMap.put("BUZARA1", "Arbizene Buzard");
        domainMap.put("CHIPLJ1", "Jennifer Chipley");
        domainMap.put("CHIPLEJ1", "Jennifer Chipley");
        domainMap.put("JAHRG", "Gabrielle Jahr");
        domainMap.put("JAHRG1", "Gabrielle Jahr");

        String userName = domainMap.get(JIRAConstants.JIRA_USER_ID.toUpperCase());

        myLogger.exiting(MY_CLASS_NAME, "getUserNameByDomainUserId");
        return AppUtil.isNullOrEmpty(userName) ? JIRAConstants.JIRA_USER_ID.toUpperCase() : userName;
    }//end method





}// end class
