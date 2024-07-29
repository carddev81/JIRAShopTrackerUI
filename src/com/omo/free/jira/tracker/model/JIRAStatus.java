package com.omo.free.jira.tracker.model;

import java.util.Arrays;
import java.util.List;

/**
 * This class is used to contain the JIRA status name and id.
 *
 * @author Richard Salas
 */
public class JIRAStatus implements Comparable<JIRAStatus>{

    private String id;
    private String value;

    /**
     * Constructor used to create an instance of the JIRAStatus class.
     *
     * @param id the id of the status
     * @param value the status name
     */
    public JIRAStatus(String id, String value) {
        this.id = id;
        this.value = value;
    }//end constructor

    /**
     * These are the hardcoded JIRA statuses that will be currently used.  If more JIRA statuses are needed add it to this list here.
     *
     * @return statuses the list of jira statuses
     */
    public static List<JIRAStatus> getJIRAStatuses(){
        List<JIRAStatus> statuses = Arrays.asList(new JIRAStatus("0", ". . ."), new JIRAStatus("1", "Open"), new JIRAStatus("3", "In Progress"), new JIRAStatus("10102", "Production Fix"), new JIRAStatus("10111", "Research"), new JIRAStatus("10047", "Researching Issue"), new JIRAStatus("5", "Closed"), new JIRAStatus("10736", "In Development"), new JIRAStatus("10737", "In Testing"), new JIRAStatus("10738", "Ready to Migrate"), new JIRAStatus("4", "Reopened"), new JIRAStatus("6", "Resolved"), new JIRAStatus("10005", "Accepted"), new JIRAStatus("10026", "System Testing"), new JIRAStatus("10028", "Coding"), new JIRAStatus("10032", "Status Needs Updated"), new JIRAStatus("10100", "Code Migrated"), new JIRAStatus("10177", "In Production"), new JIRAStatus("10202", "Migrate"), new JIRAStatus("10217", "Migration Complete"), new JIRAStatus("10219", "Approved"), new JIRAStatus("10222", "Pending"), new JIRAStatus("10226", "Development"), new JIRAStatus("10234", "Testing"), new JIRAStatus("10339", "Ready for Production"), new JIRAStatus("10340", "Ready for Development"), new JIRAStatus("10342", "Ready for Deployment"), new JIRAStatus("10633", "Closed - Procedural"), new JIRAStatus("10638", "Ready for Test"));
        return statuses;
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(JIRAStatus o) {
        return value.compareToIgnoreCase(o.value);
    }//end method

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }//end method

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }//end method

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }//end method

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }//end method

}//end class
