package com.omo.free.jira.tracker.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.util.JiraUtil;

import gov.doc.isu.com.util.AppUtil;
import gov.doc.isu.simple.fx.managers.UIPropertiesMgr;
import gov.doc.isu.simple.fx.util.Constants;
import gov.doc.isu.simple.fx.util.FileUtility;

/**
 * This client class is used for downloading JIRA resources selected by the user of the JIRA Shop Tracker UI.  The JIRA {@code Issue}'s, {@code Project}'s, and {@code Attachments}'s are modeled here using the JIRA Atlassian API.
 *
 * @author Richard Salas, October 30, 2019
 */
public class DownloadClient extends AbstractClient{

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.client.DownloadClient";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private static final DownloadClient INSTANCE = new DownloadClient();

    /* custom fields that will be used ... can add more if needed here */
    private static List<String> ALLOWED_CUSTOM_FIELD_LIST = Arrays.asList("DOC Vendor Paid", "PROMORS Affiliation", "DOC OPII Projects", "Creator", "DOC Screen", "DOC JCCC Developer", "DOC Sharepoint Issue Number", "DOC Iteration", "DOC Phase", "DOC Date moved to Production", "DOC Module", "DOC Modified By", "DOC Project", "DOC Notes", "DOC Attachment is Scrubbed ?", "Maintenance", "PAQ Number", "Project Manager", "DOC Application Name", "DOC ANT Tags", "DOC Environment");

    /**
     * Default constructor used to create an instance of the DownloadClient class.
     */
    private DownloadClient() {
        myLogger.entering(MY_CLASS_NAME, "DownloadClient");
        //copy the internal files to external location
        FileUtility.copyInternalFileToExternalDestination(DownloadClient.class, JIRAConstants.JIRA_RESOURCES_DIRECTORY, "JIRATemplate.html");
        FileUtility.copyInternalFileToExternalDestination(DownloadClient.class, JIRAConstants.JIRA_RESOURCES_DIRECTORY, "JIRAXMLTemplate.xml");
        myLogger.exiting(MY_CLASS_NAME, "DownloadClient");
    }//end constructor

    /**
     * This method will return the single instance of the DownloadClient class.
     *
     * @return INSTANCE the DownloadClient class
     */
    public static DownloadClient getInstance(){
        return INSTANCE;
    }//end method

    /**
     * This method will write the JIRA issues in a light-weight HTML and XML formatted file.
     *
     * @param issue the JIRA issue used to write the HTML file
     * @param excludedAttachments the attachments that are excluded
     * @throws IOException can occur during the writing of the file
     */
    public void writeJiraIssue(Issue issue, List<String> excludedAttachments) throws IOException {
        myLogger.entering(MY_CLASS_NAME, "writeJiraIssue", issue);

        myLogger.info("Retrieving templates to write the following issue:  " + String.valueOf(issue));
        String htmlTemplateStr = createJIRAIssueHTMLFileAsStr(issue, excludedAttachments).toString();

        Files.write(Paths.get(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY, issue.getKey(), issue.getKey() + ".html"), htmlTemplateStr.getBytes(), StandardOpenOption.CREATE);

        //TODO per new bug zilla we do not need xml documents...just commenting out these lines because they are not needed.
//        String xmlTemplateStr = createJIRAIssueXMLFileAsStr(issue, excludedAttachments).toString();
//        Files.write(Paths.get(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY, issue.getKey(), issue.getKey() + "-XML.xml"), xmlTemplateStr.getBytes(), StandardOpenOption.CREATE);

        myLogger.exiting(MY_CLASS_NAME, "writeJiraIssue");
    }// end method

    /**
     * This method will download any attachments associated with a JIRA issue.
     *
     * @param issue the jira issue possibly containing attachments
     * @return list of excluded attachments
     * @throws Exception can occur trying to connect to JIRA.
     */
    public List<String> downloadAttachments(Issue issue) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "downloadAttachments", issue);

        List<String> excludedAttachments = new ArrayList<>();
        int count = 0;

        try{
            //get jira client
            JiraRestClient client = client();
            try{
                //[JSTUI-10] Attachments not included in Email there are several different methods for getting attachments from jira and i have implemented 2 of them here...it will use one or the other.
                Iterable<Attachment> attachments = issue.getAttachments();
                Iterator<Attachment> it = attachments.iterator();
                Attachment attachment = null;
                while(it.hasNext()){
                    attachment = it.next();
                    myLogger.info("Attachment exists going to download it here using this uri: " + String.valueOf(String.valueOf(attachment.getContentUri())));
                    InputStream is = null;
                    //[JSTUI-10] Attachments not included in Email
                    try{//can't download all attachments because some do not exist due to them being either deleted from JIRA system or JIRA just keeps a history of docs that were uploaded hence the ChangelogGroup instances
                        is = client.getIssueClient().getAttachment(attachment.getContentUri()).claim();
                        if(is != null){//write method below will close input stream...
                            JiraUtil.writeFile(is, attachment.getFilename(), issue.getKey());
                            count++;
                        }// end if
                    }catch(Exception e){
                        myLogger.warning("Could not download attachment using attachment details " + String.valueOf(attachment));
                        excludedAttachments.add(String.valueOf(attachment.getFilename()));
                    }finally{
                        if(is!=null){
                            is.close();
                        }//end if
                    }//end try...catch...finally
                }//end while

                if(count==0){//if no attachments were retrieved from above then they will be retrieved using the below logic.
                    myLogger.info("Retrieving the changelog...");
                    Iterable<ChangelogGroup> logIterable = issue.getChangelog();
                    Iterator<ChangelogGroup> logIt = logIterable.iterator();

                    myLogger.info("Looping through the change log groups to isolate attachment data...");
                    while(logIt.hasNext()){
                        ChangelogGroup log = logIt.next();

                        Iterable<ChangelogItem> itemIterable = log.getItems();
                        Iterator<ChangelogItem> itemIt = itemIterable.iterator();
                        while(itemIt.hasNext()){
                            ChangelogItem item = itemIt.next();
                            if(item.getField().equals("Attachment") && item.getTo() != null){
                                String attachmentName = item.getToString();
                                String attachmentId = item.getTo();
                                String uriString = String.format("https://jira.url.goes.here/secure/attachment/%s/%s", attachmentId, URLEncoder.encode(attachmentName, "UTF-8"));
                                URI attachmentURI = URI.create(String.format("https://jira.url.goes.here/secure/attachment/%s/%s", attachmentId, URLEncoder.encode(attachmentName, "UTF-8")));
                                myLogger.info("Attachment exists going to download it here using this uri: " + String.valueOf(uriString));
                                InputStream is = null;
                                //[JSTUI-10] Attachments not included in Email
                                try{//can't download all attachments because some do not exist due to them being either deleted from JIRA system or JIRA just keeps a history of docs that were uploaded hence the ChangelogGroup instances
                                    is = client.getIssueClient().getAttachment(attachmentURI).claim();
                                    if(is != null){//write method below will close input stream...
                                        JiraUtil.writeFile(is, attachmentName, issue.getKey());
                                        count++;
                                    }// end if
                                }catch(Exception e){
                                    myLogger.warning("Could not download attachment using uri " + String.valueOf(uriString) + " and the ChangeLogItem is " + String.valueOf(item));
                                    excludedAttachments.add(String.valueOf(attachmentName));
                                }finally{
                                    if(is!=null){
                                        is.close();
                                    }//end if
                                }//end try...catch...finally
                            }// end if
                        }// end while
                    }// end while
                }//end if

                myLogger.info("Successful in searching changelog/issue for attachments.  The number of attachments downloaded is: " + String.valueOf(count));
            }catch(Exception e){
                myLogger.log(Level.WARNING, "Exception occurred while trying to see if attachment exists.  Error is " + e.getMessage(), e);
            }// end try...catch
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to download attachments.  Error message is: " + e.getMessage() + "; Values of interest is: issue=" + String.valueOf(issue), e);
            throw e;
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "downloadAttachments", excludedAttachments);
        return excludedAttachments;
    }// end method

    /**
     * This method will return the html JIRATemplate used for generating a HTML file.
     *
     * @param templateName the name of the template file
     * @return templateStr the JIRATemplate.html file contents as a string
     * @throws IOException can occur while trying to read file
     */
    public String getJiraTemplate(String templateName) throws IOException {
        myLogger.entering(MY_CLASS_NAME, "getJiraTemplate");

        List<String> allLines = null;
        String templateStr = null;
        try{
            allLines = Files.readAllLines(Paths.get(JIRAConstants.JIRA_RESOURCES_DIRECTORY, templateName), Charset.forName("UTF-8"));
            Iterator<String> it = allLines.iterator();
            StringBuilder jiraTemplate = new StringBuilder();
            while(it.hasNext()){
                jiraTemplate.append(it.next());
                jiraTemplate.append(Constants.LINESEPERATOR);
            }// end while
            templateStr = jiraTemplate.toString();
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to read the JIRATemplate.html and converting it into a string.  Error message is: " + e.getMessage(), e);
            throw e;
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "getJiraTemplate", templateStr);
        return templateStr;
    }// end method

    /**
     * This method will build the JIRA HTML file and return it as a string.
     *
     * @param issue the jira issue used to insert values into the html string
     * @param excludedAttachments attachments that should be excluded
     * @return jiraIssueStr the built jira issue string
     * @throws IOException can occur while trying to read file the JIRATemplate.html file
     */
    public String createJIRAIssueHTMLFileAsStr(Issue issue, List<String> excludedAttachments) throws IOException {
        myLogger.entering(MY_CLASS_NAME, "createJIRAIssueHTMLFileAsStr", issue);

        String jiraIssueStr = null;
        try{
            jiraIssueStr = getJiraTemplate("JIRATemplate.html");

            myLogger.info("Building jira issue using template...");
            jiraIssueStr = jiraIssueStr.replaceAll("@project_key@", String.valueOf(issue.getKey()));
            jiraIssueStr = jiraIssueStr.replaceAll("@summary@", Matcher.quoteReplacement(String.valueOf(issue.getSummary())));

            jiraIssueStr = jiraIssueStr.replaceAll("@creation_date@", JiraUtil.getFormattedDateOrBlank(issue.getCreationDate()));
            jiraIssueStr = jiraIssueStr.replaceAll("@update_date@", JiraUtil.getFormattedDateOrBlank(issue.getUpdateDate()));

            jiraIssueStr = jiraIssueStr.replaceAll("@status@", issue.getStatus() == null ? "" : JiraUtil.cleanStr(issue.getStatus().getName()));// or issue.getStatus().getDescription()

            jiraIssueStr = jiraIssueStr.replaceAll("@project_name@", issue.getProject() == null ? "" : JiraUtil.cleanStr(issue.getProject().getName()));

            String components = getComponents(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@components@", components == null ? "None" : Matcher.quoteReplacement(components.substring(0, components.lastIndexOf(","))));

            // FIX VERSIONS START
            String versions = getVersions(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@fix_versions@", versions == null ? "None" : Matcher.quoteReplacement(versions.substring(0, versions.lastIndexOf(","))));
            // FIX VERSIONS END

            jiraIssueStr = jiraIssueStr.replaceAll("@issue_type@", issue.getIssueType() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getIssueType().getName()));// or issue.getIssueType().getDescription()
            jiraIssueStr = jiraIssueStr.replaceAll("@priority@", issue.getPriority() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getPriority().getName()));
            jiraIssueStr = jiraIssueStr.replaceAll("@reporter@", issue.getReporter() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getReporter().getDisplayName()));// or issue.getReporter().getName()
            jiraIssueStr = jiraIssueStr.replaceAll("@assignee@", issue.getAssignee() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getAssignee().getDisplayName()));// or issue.getAssignee().getName()
            jiraIssueStr = jiraIssueStr.replaceAll("@resolution@", issue.getResolution() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getResolution().getName()));// or issue.getIssueType().getDescription()

            // LABELS START
            String labels = getLabels(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@labels@", labels == null ? "None" : Matcher.quoteReplacement(labels.substring(0, labels.lastIndexOf(","))));
            // LABELS END

            // TIME ESTIMATES START
            String remainingEstimate = null;
            String timeSpent = null;
            String originalEstimate = null;
            if(issue.getTimeTracking() != null){
                TimeTracking tt = issue.getTimeTracking();
                remainingEstimate = tt.getRemainingEstimateMinutes() == null ? "Not Specified" : String.valueOf(tt.getRemainingEstimateMinutes());
                originalEstimate = tt.getOriginalEstimateMinutes() == null ? "Not Specified" : String.valueOf(tt.getOriginalEstimateMinutes());
                timeSpent = tt.getTimeSpentMinutes() == null ? "Not Specified" : String.valueOf(tt.getTimeSpentMinutes());
            }else{
                remainingEstimate = "Not Specified";
                timeSpent = "Not Specified";
                originalEstimate = "Not Specified";
            }// end if...else
             // TIME ESTIMATES END

            jiraIssueStr = jiraIssueStr.replaceAll("@timetracking_remaining_estimate@", JiraUtil.cleanStr(remainingEstimate));
            jiraIssueStr = jiraIssueStr.replaceAll("@timetracking_time_spent@", JiraUtil.cleanStr(timeSpent));
            jiraIssueStr = jiraIssueStr.replaceAll("@timetracking_original_estimate@", JiraUtil.cleanStr(originalEstimate));

            // ATTACHMENTS START
            String attachments = getAttachments(issue, excludedAttachments);
            jiraIssueStr = jiraIssueStr.replaceAll("@attachment_filenames@", attachments == null ? "None" : attachments.substring(0, attachments.lastIndexOf(",")));
            // ATTACHMENTS END

            // ISSUE LINKS START
            String issueLinks = getIssueLinks(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@issue_links@", issueLinks);
            // ISSUE LINKS END

            // CUSTOM FIELDS START
            String customFields = getCustomFields(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@custom_fields@", Matcher.quoteReplacement(customFields));
            // CUSTOM FIELDS END

            jiraIssueStr = jiraIssueStr.replaceAll("@description@", issue.getDescription() == null ? "None" : Matcher.quoteReplacement(JiraUtil.cleanStr(issue.getDescription())).replaceAll("(\n|\r\n)", "<br>"));// or issue.getStatus().getDescription()

            // COMMENTS START
            String comments = getComments(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@comment_body@", Matcher.quoteReplacement(comments));
            // COMMENTS END

            // current time stamp

            // user from path
            jiraIssueStr = jiraIssueStr.replaceAll("@user_from_path_env@", AppUtil.getUserIdFromEnvVar());
            // app name
            jiraIssueStr = jiraIssueStr.replaceAll("@current_timestamp@", LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy")));
            // app name
            jiraIssueStr = jiraIssueStr.replaceAll("@app_name@", UIPropertiesMgr.getInstance().getProperties().getProperty("application.name", ""));

            myLogger.info("Finished building jira issue...");
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to build jira issue using the JIRATemplate.html.  Error message is: " + e.getMessage() + "; jiraIssueStr=" + String.valueOf(jiraIssueStr), e);
            throw e;
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "createJIRAIssueHTMLFileAsStr", jiraIssueStr);
        return jiraIssueStr;
    }// end method

    /**
     * This method will build the JIRA HTML file and return it as a string.
     *
     * @param issue the jira issue used to insert values into the html string
     * @param excludedAttachments attachments to exclude
     * @return jiraIssueStr the built jira issue string
     * @throws IOException can occur while trying to read file the JIRATemplate.html file
     */
    public String createJIRAIssueXMLFileAsStr(Issue issue, List<String> excludedAttachments) throws IOException {
        myLogger.entering(MY_CLASS_NAME, "createJIRAIssueHTMLFileAsStr", issue);

        String jiraIssueStr = null;
        try{
            jiraIssueStr = getJiraTemplate("JIRAXMLTemplate.xml");

            myLogger.info("Building jira issue using template...");
            jiraIssueStr = jiraIssueStr.replaceAll("@project_key@", String.valueOf(issue.getKey()));
            jiraIssueStr = jiraIssueStr.replaceAll("@summary@", Matcher.quoteReplacement(String.valueOf(issue.getSummary())));
            jiraIssueStr = jiraIssueStr.replaceAll("@project_name@", issue.getProject() == null ? "" : JiraUtil.cleanStr(issue.getProject().getName()));
            jiraIssueStr = jiraIssueStr.replaceAll("@description@", issue.getDescription() == null ? "None" : Matcher.quoteReplacement(JiraUtil.cleanStr(issue.getDescription())).replaceAll("(\n|\r\n)", "&lt;br/&gt;"));// or issue.getStatus().getDescription()
            jiraIssueStr = jiraIssueStr.replaceAll("@issue_type@", issue.getIssueType() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getIssueType().getName()));// or issue.getIssueType().getDescription()
            jiraIssueStr = jiraIssueStr.replaceAll("@priority@", issue.getPriority() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getPriority().getName()));
            jiraIssueStr = jiraIssueStr.replaceAll("@status@", issue.getStatus() == null ? "" : JiraUtil.cleanStr(issue.getStatus().getName()));// or issue.getStatus().getDescription()
            jiraIssueStr = jiraIssueStr.replaceAll("@resolution@", issue.getResolution() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getResolution().getName()));// or issue.getIssueType().getDescription()
            jiraIssueStr = jiraIssueStr.replaceAll("@assignee@", issue.getAssignee() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getAssignee().getDisplayName()));// or issue.getAssignee().getName()
            jiraIssueStr = jiraIssueStr.replaceAll("@reporter@", issue.getReporter() == null ? "Not Specified" : JiraUtil.cleanStr(issue.getReporter().getDisplayName()));// or issue.getReporter().getName()

            // LABELS START
            String labels = getLabels(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@labels@", labels == null ? "None" : Matcher.quoteReplacement(labels.substring(0, labels.lastIndexOf(","))));
            // LABELS END

            jiraIssueStr = jiraIssueStr.replaceAll("@creation_date@", JiraUtil.getFormattedDateOrBlank(issue.getCreationDate()));
            jiraIssueStr = jiraIssueStr.replaceAll("@update_date@", JiraUtil.getFormattedDateOrBlank(issue.getUpdateDate()));

            // COMMENTS START
            String comments = getXMLComments(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@comment_body@", Matcher.quoteReplacement(comments));
            // COMMENTS END

            // ATTACHMENTS START
            String attachments = getXMLAttachments(issue, excludedAttachments);
            jiraIssueStr = jiraIssueStr.replaceAll("@attachment_filenames@", attachments == null ? "" : attachments);
            // ATTACHMENTS END

            // CUSTOM FIELDS START
            String customFields = getXMLCustomFields(issue);
            jiraIssueStr = jiraIssueStr.replaceAll("@custom_fields@", Matcher.quoteReplacement(customFields));
            // CUSTOM FIELDS END
            myLogger.info("Finished building xml jira issue...");
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to build jira issue using the JIRATemplate.html.  Error message is: " + e.getMessage() + "; jiraIssueStr=" + String.valueOf(jiraIssueStr), e);
            throw e;
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "createJIRAIssueHTMLFileAsStr", jiraIssueStr);
        return jiraIssueStr;
    }// end method

    /**
     * This method is a helper method for obtaining custom fields from the issue passed into this method.
     *
     * <p>NOTE:  Custom field data is returned as either a JSONObject or a JSONArray.  I used the already available jettison API for parsing the JSON.</p>
     *
     * @param issue the issue used to build the comment
     * @return the custom fields xml string
     */
    private String getXMLCustomFields(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getXMLCustomFields", issue);

        StringBuilder customFields = new StringBuilder();
        if(issue.getFields() != null){
            myLogger.info("Custom fields exists for issue, going to start the parsing process...");
            Iterator<IssueField> it = issue.getFields().iterator();
            IssueField field = null;
            JSONObject jsonObj = null;
            JSONArray jsonArray = null;
            String self = null;

            while(it.hasNext()){
                field = it.next();
                //checking the constan containing all of the allowed fields...no need to parse all of them
                if(!ALLOWED_CUSTOM_FIELD_LIST.contains(field.getName().trim()) || field.getValue() == null){
                    continue;
                }// end if

                try{
                    String value = String.valueOf(field.getValue());
                    if(value.startsWith("{") && value.endsWith("}")){// JSON object
                        jsonObj = new JSONObject(value);
                        self = (String) jsonObj.opt("self");
                        if(self != null && self.contains("customFieldOption")){
                            customFields.append("<customfield><customfieldname>").append(String.valueOf(field.getName())).append("</customfieldname>");
                            customFields.append("<customfieldvalues><customfieldvalue><![CDATA[").append(String.valueOf(jsonObj.opt("value"))).append("]]></customfieldvalue></customfieldvalues></customfield>");
                            customFields.append(Constants.LINESEPERATOR);
                        }else if(self != null && self.contains("user")){
                            customFields.append("<customfield><customfieldname>").append(String.valueOf(field.getName())).append("</customfieldname>");
                            customFields.append("<customfieldvalues><customfieldvalue><![CDATA[").append(String.valueOf(jsonObj.opt("displayName"))).append("]]></customfieldvalue></customfieldvalues></customfield>");
                            customFields.append(Constants.LINESEPERATOR);
                        }// end if
                    }else if(value.startsWith("[") && value.endsWith("]")){// JSON array
                        jsonArray = new JSONArray(value);
                        jsonObj = (JSONObject) jsonArray.opt(0);
                        self = (String) jsonObj.opt("self");
                        if(self != null && self.contains("customFieldOption")){
                            customFields.append("<customfield><customfieldname>").append(String.valueOf(field.getName())).append("</customfieldname>");
                            customFields.append("<customfieldvalues><customfieldvalue><![CDATA[").append(String.valueOf(jsonObj.opt("value"))).append("]]></customfieldvalue></customfieldvalues></customfield>");
                            customFields.append(Constants.LINESEPERATOR);
                        }else if(self != null && self.contains("user")){
                            customFields.append("<customfield><customfieldname>").append(String.valueOf(field.getName())).append("</customfieldname>");
                            customFields.append("<customfieldvalues><customfieldvalue><![CDATA[").append(String.valueOf(jsonObj.opt("displayName"))).append("]]></customfieldvalue></customfieldvalues></customfield>");
                            customFields.append(Constants.LINESEPERATOR);
                        }// end if
                    }else{//just a plain ole String
                        customFields.append("<customfield><customfieldname>").append(String.valueOf(field.getName())).append("</customfieldname>");
                        customFields.append("<customfieldvalues><customfieldvalue><![CDATA[").append(String.valueOf(field.getValue())).append("]]></customfieldvalue></customfieldvalues></customfield>");
                        customFields.append(Constants.LINESEPERATOR);
                    }// end if
                }catch(Exception e){
                    myLogger.log(Level.SEVERE, "Exception occurred while trying to parse the custom fields.  Error is: " + e.getMessage(), e);
                }// end try...catch
            }// end while
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "getXMLCustomFields", customFields);
        return customFields.toString();
    }//end method

    /**
     * This method is a helper method for obtaining attachments from the issue passed into this method.
     *
     * @param issue the issue used to build attachments string
     * @param excludedAttachments attachments to exclude
     * @return the attachments xml string
     */
    private String getXMLAttachments(Issue issue, List<String> excludedAttachments) {
        myLogger.entering(MY_CLASS_NAME, "getXMLAttachments", String.valueOf(issue));

        String attachments = null;
        int count = 0;

        try{
            Iterable<Attachment> attachmentList = issue.getAttachments();
            Iterator<Attachment> it = attachmentList.iterator();
            Attachment attachment = null;
            while(it.hasNext()){
                attachment = it.next();
                if(!excludedAttachments.contains(attachment.getFilename())){
                    if(attachments == null){
                        attachments = "<attachment name=\"" + attachment.getFilename() + "\" />" + Constants.LINESEPERATOR;
                    }else{
                        attachments += "<attachment name=\"" + attachment.getFilename() + "\" />" + Constants.LINESEPERATOR;
                    }//end if
                    count++;
                }// end if
            }//end while

            if(count==0){//if no attachments were retrieved from above then they will be retrieved using the below logic.
                Iterable<ChangelogGroup> logIterable = issue.getChangelog();
                Iterator<ChangelogGroup> logIt = logIterable.iterator();
                while(logIt.hasNext()){
                    ChangelogGroup log = logIt.next();

                    Iterable<ChangelogItem> itemIterable = log.getItems();
                    Iterator<ChangelogItem> itemIt = itemIterable.iterator();
                    while(itemIt.hasNext()){
                        ChangelogItem item = itemIt.next();
                        if(item.getField().equals("Attachment") && item.getTo() != null && !excludedAttachments.contains(item.getToString())){
                            if(attachments == null){
                                attachments = "<attachment name=\"" + item.getToString() + "\" />" + Constants.LINESEPERATOR;
                            }else{
                                attachments += "<attachment name=\"" + item.getToString() + "\" />" + Constants.LINESEPERATOR;
                            }//end if
                        }// end if
                    }// end while
                }// end while
            }//end if
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to build comma separated string of attachments.  Error is: " + e.getMessage(), e);
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "getXMLAttachments", attachments);
        return attachments;
    }//end method

    /**
     * This method is a helper method for obtaining comments from the issue passed into this method and returning them within an xml format
     *
     * @param issue the issue used to build the comment
     * @return the comments xml string
     */
    private String getXMLComments(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getComments", issue);

        StringBuilder comments = new StringBuilder();
        if(issue.getComments() != null){
            Iterator<Comment> it = issue.getComments().iterator();
            while(it.hasNext()){
                Comment comment = it.next();
                comments.append("<comment author=\"").append(comment.getAuthor() == null ? "" : JiraUtil.cleanStr(comment.getAuthor().getDisplayName()));
                comments.append("\" created=\"").append(JiraUtil.getFormattedDateOrBlank(comment.getCreationDate())).append("\">");
                comments.append(JiraUtil.cleanStr(JiraUtil.htmlEscape(comment.getBody()))).append("</comment>").append(Constants.LINESEPERATOR);
            }// end while
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "getComments", comments);
        return comments.toString();
    }//end method

    /**
     * This method is a helper method for obtaining comments from the issue passed into this method.
     *
     * @param issue the issue used to build the comment
     * @return the comments html string
     */
    private String getComments(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getComments", issue);

        StringBuilder comments = new StringBuilder();
        if(issue.getComments() != null){
            Iterator<Comment> it = issue.getComments().iterator();
            while(it.hasNext()){
                Comment comment = it.next();
                comments.append("<TR id=\"comment-header-1502542\"><TD bgcolor=\"#f0f0f0\">Comment by<A class=\"user-hover\" href=\"#\">");
                comments.append(comment.getAuthor() == null ? "" : JiraUtil.cleanStr(comment.getAuthor().getDisplayName()));
                comments.append("</A><FONT size=\"-2\">[<FONT color=\"#336699\">");
                comments.append(JiraUtil.getFormattedDateOrBlank(comment.getCreationDate()));
                comments.append("</FONT>]</FONT></TD></TR><TR id=\"comment-body-1502542\"><TD bgcolor=\"#ffffff\">");
                comments.append(JiraUtil.cleanStr(comment.getBody())).append("</TD></TR>");
            }// end while
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "getComments", comments);
        return comments.toString();
    }// end method

    /**
     * This method is a helper method for obtaining custom fields from the issue passed into this method.
     *
     * <p>NOTE:  Custom field data is returned as either a JSONObject or a JSONArray.  I used the already available jettison API for parsing the JSON.</p>
     *
     * @param issue the issue used to build the comment
     * @return the custom fields html string
     */
    private String getCustomFields(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getCustomFields", issue);

        StringBuilder customFields = new StringBuilder();
        if(issue.getFields() != null){
            myLogger.info("Custom fields exists for issue, going to start the parsing process...");
            Iterator<IssueField> it = issue.getFields().iterator();
            IssueField field = null;
            JSONObject jsonObj = null;
            JSONArray jsonArray = null;
            String self = null;

            while(it.hasNext()){
                field = it.next();
                //checking the constan containing all of the allowed fields...no need to parse all of them
                if(!ALLOWED_CUSTOM_FIELD_LIST.contains(field.getName().trim()) || field.getValue() == null){
                    continue;
                }// end if

                try{
                    String value = String.valueOf(field.getValue());
                    if(value.startsWith("{") && value.endsWith("}")){// JSON object
                        jsonObj = new JSONObject(value);
                        self = (String) jsonObj.opt("self");
                        if(self != null && self.contains("customFieldOption")){
                            customFields.append("<TR><TD width=\"20%\" valign=\"top\" bgcolor=\"#f0f0f0\"><B>").append(String.valueOf(field.getName())).append(":</B></TD>");
                            customFields.append("<TD width=\"80%\" class=\"value\" bgcolor=\"#ffffff\"><DIV class=\"shorten\"><SPAN>").append(String.valueOf(jsonObj.opt("value"))).append("</DIV></TD>");
                            customFields.append("</TR>");
                        }else if(self != null && self.contains("user")){
                            customFields.append("<TR><TD width=\"20%\" valign=\"top\" bgcolor=\"#f0f0f0\"><B>").append(String.valueOf(field.getName())).append(":</B></TD>");
                            customFields.append("<TD width=\"80%\" class=\"value\" bgcolor=\"#ffffff\"><DIV class=\"shorten\"><SPAN>").append(String.valueOf(jsonObj.opt("displayName"))).append("</DIV></TD>");
                            customFields.append("</TR>");
                        }// end if
                    }else if(value.startsWith("[") && value.endsWith("]")){// JSON array
                        jsonArray = new JSONArray(value);
                        jsonObj = (JSONObject) jsonArray.opt(0);
                        self = (String) jsonObj.opt("self");
                        if(self != null && self.contains("customFieldOption")){
                            customFields.append("<TR><TD width=\"20%\" valign=\"top\" bgcolor=\"#f0f0f0\"><B>").append(String.valueOf(field.getName())).append(":</B></TD>");
                            customFields.append("<TD width=\"80%\" class=\"value\" bgcolor=\"#ffffff\"><DIV class=\"shorten\"><SPAN>").append(String.valueOf(jsonObj.opt("value"))).append("</DIV></TD>");
                            customFields.append("</TR>");
                        }else if(self != null && self.contains("user")){
                            customFields.append("<TR><TD width=\"20%\" valign=\"top\" bgcolor=\"#f0f0f0\"><B>").append(String.valueOf(field.getName())).append(":</B></TD>");
                            customFields.append("<TD width=\"80%\" class=\"value\" bgcolor=\"#ffffff\"><DIV class=\"shorten\"><SPAN>").append(String.valueOf(jsonObj.opt("displayName"))).append("</DIV></TD>");
                            customFields.append("</TR>");
                        }// end if
                    }else{//just a plain ole String
                        customFields.append("<TR><TD width=\"20%\" valign=\"top\" bgcolor=\"#f0f0f0\"><B>").append(String.valueOf(field.getName())).append(":</B></TD>");
                        customFields.append("<TD width=\"80%\" class=\"value\" bgcolor=\"#ffffff\"><DIV class=\"shorten\"><SPAN>").append(String.valueOf(field.getValue())).append("</DIV></TD>");
                        customFields.append("</TR>");
                    }// end if
                }catch(Exception e){
                    myLogger.log(Level.SEVERE, "Exception occurred while trying to parse the custom fields.  Error is: " + e.getMessage(), e);
                }// end try...catch
            }// end while
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "getCustomFields", customFields);
        return customFields.toString();
    }// end method

    /**
     * This method is a helper method for obtaining Issue Links from the issue passed into this method.
     *
     * @param issue the issue used to build issue links
     * @return the issue links html string
     */
    private String getIssueLinks(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getIssueLinks", issue);

        StringBuilder issueLinks = new StringBuilder();
        if(issue.getIssueLinks() != null){
            Iterator<IssueLink> it = issue.getIssueLinks().iterator();
            while(it.hasNext()){
                IssueLink link = it.next();
                issueLinks.append("<TR><TD bgcolor=\"#f0f0f0\" colspan=\"4\"><B>Includes</B><BR></TD></TR><TR><TD>Includes</TD><TD><A href=\"#\">");
                issueLinks.append(link.getTargetIssueKey() == null ? "Not Specified" : JiraUtil.cleanStr(link.getTargetIssueKey())).append("</A></TD><TD>");
                issueLinks.append(link.getIssueLinkType() == null ? "Not Specified" : JiraUtil.cleanStr(link.getIssueLinkType().getDescription())).append("</TD><TD>");
                issueLinks.append(link.getIssueLinkType() == null ? "Not Specified" : JiraUtil.cleanStr(link.getIssueLinkType().getName())).append("</TD></TR>");
            }// end while
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "getIssueLinks", issueLinks);
        return issueLinks.toString();
    }// end method

    /**
     * This method is a helper method for obtaining attachments from the issue passed into this method.
     *
     * @param issue the issue used to build attachments string
     * @param excludedAttachments the list of excluded attachments
     * @return the attachments html string
     */
    private String getAttachments(Issue issue, List<String> excludedAttachments) {
        myLogger.entering(MY_CLASS_NAME, "getAttachments", String.valueOf(issue));

        String attachments = null;
        int count = 0;

        try{//[JSTUI-10] Attachments not included in Email 2 ways attachments are retrieved.
            Iterable<Attachment> attachmentList = issue.getAttachments();
            Iterator<Attachment> it = attachmentList.iterator();
            Attachment attachment = null;
            while(it.hasNext()){
                attachment = it.next();
                attachment.getFilename();
                attachment.getContentUri();
                if(!excludedAttachments.contains(attachment.getFilename())){
                    attachments = (attachments == null) ? attachment.getFilename() + ", " : attachments + attachment.getFilename() + ", ";
                    count++;
                }// end if
            }//end while

            if(count==0){//if no attachments were retrieved from above then they will be retrieved using the below logic.
                Iterable<ChangelogGroup> logIterable = issue.getChangelog();
                Iterator<ChangelogGroup> logIt = logIterable.iterator();
                while(logIt.hasNext()){
                    ChangelogGroup log = logIt.next();

                    Iterable<ChangelogItem> itemIterable = log.getItems();
                    Iterator<ChangelogItem> itemIt = itemIterable.iterator();
                    while(itemIt.hasNext()){
                        ChangelogItem item = itemIt.next();//[JSTUI-10] Attachments not included in Email
                        if(item.getField().equals("Attachment") && item.getTo() != null && !excludedAttachments.contains(item.getToString())){
                            attachments = (attachments == null) ? item.getToString() + ", " : attachments + item.getToString() + ", ";
                        }// end if
                    }// end while
                }// end while
            }//end if
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to build comma separated string of attachments.  Error is: " + e.getMessage(), e);
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "getAttachments", attachments);
        return attachments;
    }// end method

    /**
     * This method is a helper method for obtaining labels from the issue passed into this method.
     *
     * @param issue the issue used to build labels string
     * @return the labels html string
     */
    private String getLabels(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getLabels", issue);

        String labels = null;
        if(issue.getLabels() != null){
            Iterator<String> it = issue.getLabels().iterator();
            String l = null;
            while(it.hasNext()){
                l = it.next();
                labels = l + ", ";
            }// end while
        }// end if...else

        myLogger.exiting(MY_CLASS_NAME, "getLabels", labels);
        return labels;
    }// end method

    /**
     * This method is a helper method for obtaining versions from the issue passed into this method.
     *
     * @param issue the issue used to build versions string
     * @return the versions html string
     */
    private String getVersions(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getVersions", issue);

        String versions = null;
        if(issue.getFixVersions() != null){
            Iterator<Version> it = issue.getFixVersions().iterator();
            Version v = null;
            while(it.hasNext()){
                v = it.next();
                versions = v.getName() + ", ";
            }// end while
        }// end if...else

        myLogger.exiting(MY_CLASS_NAME, "getVersions", String.valueOf(versions));
        return versions;
    }// end emthod

    /**
     * This method is a helper method for obtaining components from the issue passed into this method.
     *
     * @param issue the issue used to build components string
     * @return the components html string
     */
    private String getComponents(Issue issue) {
        myLogger.entering(MY_CLASS_NAME, "getComponents", issue);

        String components = null;
        if(issue.getComponents() != null){
            Iterator<BasicComponent> it = issue.getComponents().iterator();
            BasicComponent bc = null;
            while(it.hasNext()){
                bc = it.next();
                components = bc.getName() + ", ";
            }// end while
        }// end if

        myLogger.exiting(MY_CLASS_NAME, "getComponents", components);
        return components;
    }// end method

    /**
     * This method will prepare the passed in {@code Issue} for an email.  Basically a check is done to see if there are more than 1 file associated to an issue and if so then the files are zipped into a package to be sent in an email.
     *
     * @param issue the issue to prepare
     * @throws Exception can occur while trying to prepare issue for email
     */
    public void prepareIssueForEmail(Issue issue) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "prepareIssueForEmail", issue);

        File issueDirectory = new File(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY + "/" + issue.getKey());

        try{
            if(issueDirectory.exists()){
                File[] files = issueDirectory.listFiles();
                if(files.length > 1){
                    // zip
                    JiraUtil.zipUpFiles(files, new File(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY + "/" + issue.getKey() + ".zip"));
                }else if(files.length == 1){
                    Files.copy(files[0].toPath(), Paths.get(JIRAConstants.JIRA_DOWNLOAD_DIRECTORY, files[0].getName()), StandardCopyOption.REPLACE_EXISTING);
                }else{
                    myLogger.warning("For some strange reason there are no files associated with this issue.");
                }// end else...if
                FileUtility.deleteDirectory(issueDirectory);
            }// end if
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to prepare attachments for download.  Error message is: " + e.getMessage() + "; Values of interest is: issue=" + String.valueOf(issue) + "; issueDirectory=" + issueDirectory.getPath(), e);
            throw e;
        }// end try...catch

        myLogger.exiting(MY_CLASS_NAME, "prepareIssueForEmail");
    }// end method

    /**
     * This method is used for getting issues with changelog expandos.  This is important as this is where the attachment information is located...<b>getAttachment() does not ever return data</b>.
     *
     * @param issues the issues used for retrieving changelog expandos.
     *
     * @return expandosList the issue withs attachments
     */
    public List<Issue> getIssueWithExpandos(List<Issue> issues) {
        myLogger.exiting(MY_CLASS_NAME, "getIssueWithExpandos", issues);

        List<Issue> expandosList = new ArrayList<>();
        try{
            JiraRestClient client = client();
            Iterator<Issue> it = issues.iterator();
            Issue theIssue = null;
            while(it.hasNext()){
                theIssue = it.next();
                expandosList.add(client.getIssueClient().getIssue(theIssue.getKey(), Arrays.asList(IssueRestClient.Expandos.CHANGELOG)).claim());
                myLogger.info("call to jira webservice to get the changelog with attachments was successful for " + String.valueOf(theIssue.getKey()));
            }//end while
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to retrieve issues changlelog expanodo.  This is needed for attachment information.  Error is: " + e.getMessage(), e);
            throw e;
        }//end try...catch

        myLogger.exiting(MY_CLASS_NAME, "getIssueWithExpandos", expandosList);
        return expandosList;
    }//end method

}//end class
