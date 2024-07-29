/**
 *
 */
package com.omo.free.jira.tracker.constants;

/**
 * This class houses the Constants used by the JIRA Shop Tracker.
 *
 * @author Richard Salas, April 17, 2019
 */
public class JIRAConstants {

    public static final String JIRA_URL = "https://jira.url.goes.here";

    public static final String SMALL_BUG_URL = "/com/omo/free/jira/tracker/resources/bug_small.png";
    public static final String SMALL_SENT_BUG_URL = "/com/omo/free/jira/tracker/resources/bug_small_sent.png";

    public static final String LARGE_OPEN_BUG_URL = "/com/omo/free/jira/tracker/resources/bug_lg.png";
    public static final String LARGE_OPEN_BUG_WITH_ATTACHMENT_URL = "/com/omo/free/jira/tracker/resources/bug_attachment_lg.png";

    public static final String LARGE_SENT_BUG_URL = "/com/omo/free/jira/tracker/resources/bug_lg_sent.png";
    public static final String LARGE_SENT_BUG_WITH_ATTACHMENT_URL = "/com/omo/free/jira/tracker/resources/bug_attachment_lg_sent.png";

    /* jira icon */
    public static final String JIRA_ICON_URL = "/com/omo/free/jira/tracker/resources/jira.png";

    //constants set on startup
    public static String JIRA_DOWNLOAD_DIRECTORY = "";
    public static String JIRA_RESOURCES_DIRECTORY = "";
    public static String JIRA_TRUSTSTORE_DIRECTORY = "";
    public static String JIRA_ISSUES_TOO_LARGE_DIRECTORY = "";
    public static String JIRA_USER_ID = "";

    /**
     * Default constructor
     */
    private JIRAConstants() {}

}//end class
