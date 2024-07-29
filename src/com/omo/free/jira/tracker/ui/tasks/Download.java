package com.omo.free.jira.tracker.ui.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.omo.free.jira.tracker.client.DownloadClient;
import com.omo.free.jira.tracker.client.IssueClient;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.model.JIRATrackedIssue;
import com.omo.free.jira.tracker.util.JiraUtil;

import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.tools.SendMail;
import gov.doc.isu.simple.fx.util.FileUtility;
import javafx.concurrent.Task;

/**
 * This class handles all of the downloading, packaging, and emailing of Jira Issues.
 *
 * @author Richard Salas, April 17, 2019
 */
public class Download extends Task<Void> {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.ui.tasks.Download";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /* instance variables used by this class */
    private boolean isTracked;
    private List<Issue> issues;
    private boolean isFileCopyError;
    private String optionalEmailText;
    private String additionalEmailAddresses;
    /**
     * Constructor used to create an instance of the Download class.
     *
     * @param issues the jira issues to send to the shop
     * @param optionalEmailText email text that user has supplied
     */
    public Download(List<Issue> issues, String optionalEmailText) {
        myLogger.entering(MY_CLASS_NAME, "Download", new Object[]{issues, optionalEmailText});

        this.issues = issues;
        this.optionalEmailText = optionalEmailText;

        myLogger.exiting(MY_CLASS_NAME, "Download");
    }//end constructor

    /**
     * Constructor used to create an instance of the Download class.
     * 
     * @param issues the jira issues to send to the shop
     * @param optionalEmailText email text that user has supplied
     * @param commaSeparatedEmailAddresses additional email addresses to send the jira email to
     */
    public Download(List<Issue> issuesToSend, String optionalEmailText, String commaSeparatedEmailAddresses) {
        this(issuesToSend, optionalEmailText);
        this.additionalEmailAddresses = commaSeparatedEmailAddresses;
    }//end constructor

    /**
     * This method runs the task for downloading, packaging, and emailing of Jira Issues.
     *
     * @throws Exception if an error occurs
     */
    @Override
    protected Void call() throws Exception {
        myLogger.entering(MY_CLASS_NAME, "call");

        //clean directory on every call...
        FileUtility.deleteDirectory(new File(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY));
        FileUtility.checkDirectories(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY);

        //read_template into a variable
        Issue issue = null;

        //step one get issues with expandos here...this is for downloading any attachments.
        long start = System.currentTimeMillis();
        DownloadClient downloadClient = DownloadClient.getInstance();

        List<Issue> expandosList = downloadClient.getIssueWithExpandos(issues);//this list will be used for holding the issues w/attachments
        myLogger.info("time it took to retrieve changelog expandos is " + AppUtil.getTimeTookInSecMinHours(start));
        //step two loop through expandos here!!!
        Iterator<Issue> it = expandosList.iterator();
        while(it.hasNext()){
            //1 build jira issue and write it...
            issue = it.next();

            //[JSTUI-10] Attachments not included in Email
            //moved the check directories feature here...
            FileUtility.checkDirectories(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY + "/" + issue.getKey());

            List<String> excludedAttachments = downloadClient.downloadAttachments(issue);//download attachments
            myLogger.info("successful in downloading jira attachments...");

            downloadClient.writeJiraIssue(issue, excludedAttachments);//write new jira
            myLogger.info("successful in writing jira issue...");

            downloadClient.prepareIssueForEmail(issue);//prepare issue and attachments for email
            myLogger.info("successful in preparing jira attachments for email...");
        }//end while

        File downloadDirectory = new File(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY);
        if(!downloadDirectory.exists()){
            throw new Exception("Downloads do not exist!");
        }//end if

        File[] files = downloadDirectory.listFiles();

        if(files == null || files.length < 1){
            throw new Exception("No JIRA issues were able to be retrieved from " + JIRAConstants.JIRA_URL);
        }//end if

        //count up the sizes of the files
        double megabytes = 1048576;
        double size = 0;
        for(int i = 0, j = files.length; i < j; i++){
            size += files[i].length();
        }//end for

        double totalInMegaBytes = size / megabytes;
        if(totalInMegaBytes > 4.2D){
            myLogger.warning("Downloads are too large to send through to email need to select fewer issues then try again.  Size trying to send is: " + String.valueOf(totalInMegaBytes));
            //it is too large therefore all downloads will be copied to new directory...
            copyFilesToSharedShopLocation(files);
        }else{
            //send email with attachments
            sendEmailWithAttachments(files);
        }//end if...else

        myLogger.info("time it took to complete all downloads " + AppUtil.getTimeTookInSecMinHours(start));
        myLogger.exiting(MY_CLASS_NAME, "call");
        return null;
    }//end if...else

    /**
     * This method will send email with the jira issues as an attachments.  The process within this method will also make the necessary calls to track the issues sent.
     *
     * @param files the files to send as attachments
     * @throws Exception if there is any issues sending the email or tracking the issues.
     */
    private void sendEmailWithAttachments(File[] files) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "sendEmailWithAttachments", files);

        myLogger.info("building small email for users...");

        //JSTUI-11 Allowing Free Text Field for email (added the below method call to complete the request)
        StringBuilder email = buildOpeningEmailStatement();

        email.append("<h3 style=\"font-family:Calibri; color:#336699;\">Issues:</h3>");
        email.append("<ul style=\"font-size:11pt; font-family:Calibri; color:#336699;\">");

        Issue issued = null;
        for(int i = 0, j = issues.size(); i < j; i++){
            issued = issues.get(i);
            email.append("<li>").append(issued.getKey()).append("</li>");
        }//end for

        email.append("</ul>");

        if(isTracked){
            email.append("<p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">Please review and work on the most recent ");
            email.append(issues.size()).append(" attached JIRA issue(s).");
        }else{
            email.append("<p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">Please review the attached JIRA issue(s) and advise who will be working on ");
            email.append(issues.size() > 1 ? "these issues." : "the issue.");
        }//end if...else

        email.append(" Thank you!</p><p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">").append(JiraUtil.getUserNameByDomainUserId()).append(" (ITSD-DOC)</p>");

        try{
            sendEmailAndRetryOnError(email.toString(), buildEmailSubject(issues), files);

            //track the issues here
            List<JIRATrackedIssue> jiraIssues = new ArrayList<>();
            IssueClient issueClient = IssueClient.getInstance();

            for(int i = 0, j = issues.size(); i < j; i++){
                Issue issueToTrack = issues.get(i);
                jiraIssues.add(new JIRATrackedIssue(issueClient.getProjectKey(), issueToTrack.getKey(), issueToTrack.getSummary()));
            }//end for

            myLogger.info("Number of jiraissues to save is: " + jiraIssues.size());
            if(isTracked){
                issueClient.updateTrackedIssue(jiraIssues);
            }else{
                issueClient.saveTrackedIssue(jiraIssues);
            }//end if
            issueClient.setIssues(issueClient.getProjectKey(), issueClient.getStatus());//refresh the model
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to send JIRA issues in email.  Error message is: " + e.getMessage(), e);
            throw e;
        }//end try...catch

        myLogger.exiting(MY_CLASS_NAME, "sendEmailWithAttachments");
    }//end method

    /**
     * This method is called to send email with or without attachments.  This method will also retry sending an email on error.
     *
     * @param toEmail the email to list
     * @param subject the subject
     * @param attachments the attachments
     * @throws Exception the exception that was thrown
     */
    private void sendEmailAndRetryOnError(String toEmail, String subject, File[] attachments) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "sendEmailAndRetryOnError", new Object[]{toEmail, subject, attachments});

        int maxErrorCount = 3;
        int numOfRetries = 0;

        //JSTUI-18 Additional Email Recipients requested (Richard Salas)
        String cc = null;
        if(AppUtil.isNullOrEmpty(this.additionalEmailAddresses)){
            cc = JiraUtil.getEmailAddressByDomainUserId();
        }else{
            cc = JiraUtil.getEmailAddressByDomainUserId() + "," + this.additionalEmailAddresses;
        }//end if...else

        while(numOfRetries < maxErrorCount){
            try{
                SendMail.send(toEmail, subject, attachments, cc);
                break;//break out of loop
            }catch(Exception e){
                numOfRetries++;
                if(numOfRetries >= maxErrorCount){
                    myLogger.log(Level.SEVERE, "Exception occurred while trying to send an email and maximum number of retries have been met.  Error message is: " + e.getMessage(), e);
                    throw e;
                } //end if
                myLogger.log(Level.WARNING, "Exception occurred while trying to send an email therefore going to retry again.  The max number of retries is " + String.valueOf(maxErrorCount) + ".  The number of retries attempted is " + String.valueOf(numOfRetries) + ".  Error message is: " + e.getMessage(), e);
                TimeUnit.SECONDS.sleep(1L);
            } //end try...catch
        } //end while

        myLogger.exiting(MY_CLASS_NAME, "sendEmailAndRetryOnError");
    }//end method

    /**
     * This method will build and return the email subject in the following format:
     *
     * <p><b>JSTUI: Issue number, Issue number, Issue number, MORE</b></p>
     *
     * <p>This method was created per JIRA JSTUI-12 Subject Line Text</p>
     *
     * @param issues the list of issues that are being sent.
     * @return emailSubject the email subject in the format specified above
     */
    private String buildEmailSubject(List<Issue> issues) {
        myLogger.entering(MY_CLASS_NAME, "buildEmailSubject", issues);
        StringBuilder emailSubject = new StringBuilder("JSTUI: ");

        for(int i = 0, j = issues.size();i < j;i++){
            if(i > 2){
                emailSubject.append("MORE");
                break;
            }else{
                emailSubject.append(issues.get(i).getKey()).append(", ");
            }// end if/else
        }// end for

        if(emailSubject.toString().endsWith(", ")){
            emailSubject.deleteCharAt(emailSubject.lastIndexOf(","));
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "buildEmailSubject", issues);
        return emailSubject.toString().trim();
    }//end method

    /**
     * This method will copy files to the shared shop location due to the attachments not being able to be sent in an email.  The process within this method will also make the necessary calls to track the issues sent.
     *
     * @param files the files to copy to the shared location
     * @throws Exception if an error occurs during the copy process of the files
     */
    private void copyFilesToSharedShopLocation(File[] files) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "copyFilesToSharedShopLocation", files);
        //prep shared directory
        File directory = new File(JIRAConstants.JIRA_ISSUES_TOO_LARGE_DIRECTORY);
        myLogger.info("Did I need to create the shared issues directory for copying files?: " + directory.mkdirs());

        myLogger.info("creating the unique directory for sending issues to the shop.");
        String uniqueDirNm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
        File uniqueDir = new File(directory.getPath() + "/" + uniqueDirNm);
        myLogger.info("Created the unique shared issues directory for copying files?: " + uniqueDir.mkdir());

        List<File> filesToCopy = Arrays.asList(files);

        filesToCopy.parallelStream().forEach(file -> {
            try{
                Files.copy(file.toPath(), Paths.get(JIRAConstants.JIRA_ISSUES_TOO_LARGE_DIRECTORY, uniqueDirNm, file.getName()), StandardCopyOption.REPLACE_EXISTING);
            }catch(IOException e){
                myLogger.log(Level.SEVERE, "IOException occurred during the copy process of the file " + file.getName() + " to the shared jira issues directory " + String.valueOf(uniqueDir.getPath()) + ".  Error message is:  " + e.getMessage(), e);
                isFileCopyError = true;
            }//end method
        });//end selected files

        if(isFileCopyError){
            //clean up and then throw exception
            FileUtility.deleteDirectory(uniqueDir);
            throw new Exception("Could not copy issues to the shared jira issues location.");
        }//end if

        //send email and then succeed...
        myLogger.info("building small email for users...");
        //JSTUI-11 Allowing Free Text Field for email (added the below method call to complete the request)
        StringBuilder email = buildOpeningEmailStatement();
        email.append("<h3 style=\"font-family:Calibri; color:#336699;\">Issues:</h3>");
        email.append("<ul style=\"font-size:11pt; font-family:Calibri; color:#336699;\">");

        Issue issued = null;
        for(int i = 0, j = issues.size(); i < j; i++){
            issued = issues.get(i);
            email.append("<li>").append(issued.getKey()).append("</li>");
        }//end for

        email.append("</ul>");
        email.append("<p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">Due to the issues being too large to be attached to this email they were placed within the shared network directory <a href=\"");
        email.append(uniqueDir.toURI().toString()).append("\">");
        email.append(uniqueDir.toURI().getPath()).append("</a>.  Please cut these issues out and send them to the ISU shop.");

        if(isTracked){
            email.append("<p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">Please review and work on the most recent");
            email.append(issues.size()).append(" JIRA issue(s) listed above.");
        }else{
            email.append("<p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">Please review the JIRA issue(s) and advise who will be working on ");
            email.append(issues.size() > 1 ? "these issues." : "the issue.");
        }//end if...else

        email.append(" Thank you!</p><p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">").append(JiraUtil.getUserNameByDomainUserId()).append(" (ITSD-DOC)</p>");

        try{
            sendEmailAndRetryOnError(email.toString(), buildEmailSubject(issues), null);

            //track the issues here
            List<JIRATrackedIssue> jiraIssues = new ArrayList<>();
            IssueClient issueClient = IssueClient.getInstance();

            for(int i = 0, j = issues.size(); i < j; i++){
                Issue issueToTrack = issues.get(i);
                jiraIssues.add(new JIRATrackedIssue(issueClient.getProjectKey(), issueToTrack.getKey(), issueToTrack.getSummary()));
            }//end for

            myLogger.info("Number of jiraissues to save is: " + jiraIssues.size());
            if(isTracked){
                issueClient.updateTrackedIssue(jiraIssues);
            }else{
                issueClient.saveTrackedIssue(jiraIssues);
            }//end if
            issueClient.setIssues(issueClient.getProjectKey(), issueClient.getStatus());//refresh the model
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to send email or while trying to insert or update jira issues.  Error message is: " + e.getMessage(), e);
            throw e;
        }//end try...catch

        myLogger.exiting(MY_CLASS_NAME, "copyFilesToSharedShopLocation");
    }//end method

    /**
     * This helper method is used to build the opening statement.
     *
     * <p>Added this method per JSTUI-11 (Allowing Free Text Field for email)</p>
     * @return email the opening statement in the email
     */
    private StringBuilder buildOpeningEmailStatement() {
        myLogger.entering(MY_CLASS_NAME, "buildOpeningEmailStatement");

        StringBuilder email = new StringBuilder("<p style=\"font-size:11pt; font-family:Calibri; color:#336699;\">");
        if(optionalEmailText==null || "".equals(optionalEmailText.trim())){
            email = email.append("Please forward this email to the ");
            if(issues.get(0).getProject() == null){
                email.append("Triage group.</p>");
            }else if ("MOCIS".equals(issues.get(0).getProject().getKey())){
                email.append(issues.get(0).getProject().getKey());
                email.append(" Triage group.</p>");
            }else {
                email.append("users responsible for working on ");
                email.append(issues.get(0).getProject().getKey());
                email.append(".</p>");
            }//end if...else
        }else{
            email.append(optionalEmailText);
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "buildOpeningEmailStatement", email);
        return email;
    }//end method

    /**
     * @param isTracked is tracked
     */
    public void setIsTracked(boolean isTracked) {
        this.isTracked = isTracked;
    }//end method

}//end method
