package com.omo.free.jira.tracker.model;

import java.sql.Timestamp;

/**
 * This class is used to map a row of data within the JIRA_TRACKED_ISSUES.
 *
 * @author Richard Salas
 */
public class JIRATrackedIssue {

    private String issueKey;//ISSUE_KEY
    private String projectKey;//PROJECT_KEY
    private String summary;//SUMMARY
    private String userId;//SENT_BY_USER_ID
    private String deleteInd;//DELETE_IND
    private Timestamp dateTimeSent;//SENT_TS
    private Timestamp lastDateTimeSent;//LAST_SENT_TS

    /**
     * Constructor used to create an instance of the JIRAIssue class.
     *
     * @param projectKey the project key
     * @param issueKey the issue key
     * @param summary the issue summary
     */
    public JIRATrackedIssue(String projectKey, String issueKey, String summary) {
        this.projectKey = projectKey;
        this.issueKey = issueKey;
        this.summary = summary;
    }//end constructor

    /**
     * Default constructor used to create an instance of the JIRAIssue class.
     */
    public JIRATrackedIssue() {
    }//end constructor

    /**
     * Constructor used to create an instance of the JIRA.
     *
     * @param issueKey the issue
     */
    public JIRATrackedIssue(String issueKey) {
        this.issueKey = issueKey;
    }//end constructor

    /**
     * @return the projectKey
     */
    public String getProjectKey() {
        return projectKey;
    }//end method

    /**
     * @param projectKey the projectKey to set
     */
    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }//end method

    /**
     * @return the issueKey
     */
    public String getIssueKey() {
        return issueKey;
    }//end method

    /**
     * @param issueKey the issueKey to set
     */
    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }//end method

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }//end method

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }//end method

    /**
     * @return the dateTimeSent
     */
    public Timestamp getDateTimeSent() {
        return dateTimeSent;
    }//end method

    /**
     * @param dateTimeSent the dateTimeSent to set
     */
    public void setDateTimeSent(Timestamp dateTimeSent) {
        this.dateTimeSent = dateTimeSent;
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TrackedIssue [projectKey=");
        builder.append(projectKey);
        builder.append(", issueKey=");
        builder.append(issueKey);
        builder.append(", summary=");
        builder.append(summary);
        builder.append(", dateTimeSent=");
        builder.append(dateTimeSent);
        builder.append("]");
        return builder.toString();
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((issueKey == null) ? 0 : issueKey.hashCode());
        return result;
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        JIRATrackedIssue other = (JIRATrackedIssue) obj;
        if(issueKey == null){
            if(other.issueKey != null) return false;
        }else if(!issueKey.equals(other.issueKey)) return false;
        return true;
    }//end method

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }//end method

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }//end method

    /**
     * @return the deleteInd
     */
    public String getDeleteInd() {
        return deleteInd;
    }//end method

    /**
     * @param deleteInd the deleteInd to set
     */
    public void setDeleteInd(String deleteInd) {
        this.deleteInd = deleteInd;
    }//end method

    /**
     * @return the lastDateTimeSent
     */
    public Timestamp getLastDateTimeSent() {
        return lastDateTimeSent;
    }//end method

    /**
     * @param lastDateTimeSent the lastDateTimeSent to set
     */
    public void setLastDateTimeSent(Timestamp lastDateTimeSent) {
        this.lastDateTimeSent = lastDateTimeSent;
    }//end method

}//end class
