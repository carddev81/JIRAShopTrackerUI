package com.omo.free.jira.tracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.junderground.jdbc.DebugLevel;
import com.junderground.jdbc.StatementFactory;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.model.JIRATrackedIssue;

import gov.doc.isu.simple.fx.tools.CreateConnection;


/**
 * This class is used for basic CRUD for tracking JIRA issues sent to the shop.
 *
 * A HSQL DB table named JIRA_TRACKED_ISSUES is the keeper of all the jira issues that have been sent to the shop.
 *
 * <p>NOTE:  I have added a Bug/Enhancement Tracker Table to this DAO for tracking bugs within the JIRA shop tracker that the user may come across during their use of this application.  It is called JIRA_TRACKER_ENHANCEMENTS.</p>
 *
 * @author Richard Salas, April 17, 2019
 */
public class JIRATrackerDAO {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.dao.JIRATrackerDAO";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /* Basic SQL queries used by this DAO */
    private static final String CREATE = "CREATE CACHED TABLE IF NOT EXISTS JIRA_TRACKED_ISSUES(ISSUE_KEY VARCHAR(40) PRIMARY KEY, PROJECT_KEY VARCHAR(20), SUMMARY VARCHAR(250), SENT_TS TIMESTAMP(0), LAST_SENT_TS TIMESTAMP(0) DEFAULT NULL, SENT_BY_USER_ID VARCHAR(30), DELETE_IND CHAR(1))";

    private static final String SELECT = "SELECT * FROM JIRA_TRACKED_ISSUES where PROJECT_KEY = ? AND DELETE_IND = 'N'";
    private static final String INSERT = "INSERT INTO JIRA_TRACKED_ISSUES(ISSUE_KEY, PROJECT_KEY, SUMMARY, SENT_TS, SENT_BY_USER_ID, DELETE_IND) values (?, ?, ?, CURRENT_TIMESTAMP, ?, 'N')";
    private static final String UPDATE = "UPDATE JIRA_TRACKED_ISSUES SET LAST_SENT_TS = CURRENT_TIMESTAMP WHERE ISSUE_KEY = ?";
    private static final String DELETE = "UPDATE JIRA_TRACKED_ISSUES SET DELETE_IND = 'Y', LAST_SENT_TS = CURRENT_TIMESTAMP WHERE ISSUE_KEY = ?";
    //private static final String SELECT_ISSUE = "SELECT * FROM JIRA_TRACKED_ISSUES where PROJECT_KEY = ? AND ISSUE_KEY = ?";

    /**
     * Constructor used to create an instance of the JIRATrackerDAO class.
     *
     * @throws Exception can be thrown while trying to get a db connection
     */
    public JIRATrackerDAO() throws Exception {
        myLogger.entering(MY_CLASS_NAME, "JIRATrackerDAO");
        create();//create the table if does not exist!...if there are any issues then error out here letting user know that issues cannot be tracked, but can currently this will allow user to still send them.
        myLogger.entering(MY_CLASS_NAME, "JIRATrackerDAO");
    }//end constructor

    /**
     * This method will create the JIRA_TRACKED_ISSUES table and the JIRA_TRACKER_ENHANCEMENTS table if it does not exist.
     *
     * @throws Exception can be thrown while attempting to create the JIRA_TRACKED_ISSUES table and the JIRA_TRACKER_ENHANCEMENTS table
     */
    public void create() throws Exception{
        myLogger.entering(MY_CLASS_NAME, "create");

        Connection conn = null;
        PreparedStatement ps = null;

        try{
            conn = CreateConnection.getHSQLConnection();
            ps = StatementFactory.getStatement(conn, CREATE, DebugLevel.ON);
            ps.execute();
        }catch(SQLException e){
            myLogger.log(Level.SEVERE, "SQLException occured while trying to create shared cached tables JIRA_TRACKED_ISSUES and JIRA_TRACKER_ENHANCEMENTS. sql=" + String.valueOf(CREATE) + "; Error is: " + e.getMessage(), e);
            throw new Exception("SQLException occured while trying to create shared cached tables JIRA_TRACKED_ISSUES and JIRA_TRACKER_ENHANCEMENTS.", e);
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occured while trying to create shared cached tables JIRA_TRACKED_ISSUES and JIRA_TRACKER_ENHANCEMENTS. sql=" + String.valueOf(CREATE) + " Error is: " + e.getMessage(), e);
            throw new Exception("Exception occured while trying to create shared cached tables JIRA_TRACKED_ISSUES and JIRA_TRACKER_ENHANCEMENTS.", e);
        }finally{
            CreateConnection.destroyObjects(conn, ps, null);
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "create");
    }//end method

    /**
     * This method will return a list of {@code JIRAIssue}'s based on the {@code projectKey} passed into this method.
     *
     * @param projectKey the project key passed into this method
     * @return jiraIssues the list of JIRAIssues
     * @throws Exception can be thrown while trying to select records from the JIRA_TRACKED_ISSUES table
     */
    public List<JIRATrackedIssue> getJIRAIssuesByProjectKey(String projectKey) throws Exception{
        myLogger.entering(MY_CLASS_NAME, "getJIRAIssuesByProjectKey", projectKey);

        List<JIRATrackedIssue> jiraIssues = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JIRATrackedIssue issue = null;
        try{
            conn = CreateConnection.getHSQLConnection();
            ps = StatementFactory.getStatement(conn, SELECT, DebugLevel.ON, ResultSet.CONCUR_READ_ONLY, ResultSet.TYPE_FORWARD_ONLY);
            ps.setString(1, projectKey);
            rs = ps.executeQuery();
            while(rs.next()){
                issue = new JIRATrackedIssue();
                //issue.setProjectKey(rs.getString("PROJECT_KEY"));only need issue_key value here
                issue.setIssueKey(rs.getString("ISSUE_KEY"));
                jiraIssues.add(issue);
            }//end if
        }catch(SQLException e){
            myLogger.log(Level.SEVERE, "SQLException occured while trying to select records from the JIRA_TRACKED_ISSUES  table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("SQLException occured while trying to select records from the JIRA_TRACKED_ISSUES table.", e.getCause());
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occured while trying to select records from the JIRA_TRACKED_ISSUES  table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("Exception occured while trying to select records from the JIRA_TRACKED_ISSUES table.", e.getCause());
        }finally{
            CreateConnection.destroyObjects(conn, ps, rs);
        }// end try...catch

        myLogger.info("selected records are: " + String.valueOf(jiraIssues));
        myLogger.exiting(MY_CLASS_NAME, "getJIRAIssuesByProjectKey", jiraIssues);
        return jiraIssues;
    }//end method

    /**
     * This method will insert the list of JIRA issues into the JIRA_TRACKED_ISSUES table.
     *
     * @param issues the issues to insert into the JIRA_TRACKED_ISSUES table
     * @throws Exception can be thrown while trying to insert a record into the JIRA_TRACKED_ISSUES table
     */
    public void insert(List<JIRATrackedIssue> issues) throws Exception{
        myLogger.entering(MY_CLASS_NAME, "insert", issues);
        PreparedStatement ps = null;
        Connection conn = null;
        JIRATrackedIssue issue = null;
        int counter = 0;
        int[] inserts = null;
        try{
            conn = CreateConnection.getHSQLConnection();
            ps = StatementFactory.getStatement(conn, INSERT, DebugLevel.ON);

            for(int i = 0, j = issues.size(); i < j; i++){
                if(counter >= 50){
                    inserts = ps.executeBatch();
                    myLogger.info(logRowUpdateNumbersFromBatchStatements(inserts, INSERT));
                    counter = 0;
                }//end if

                issue = issues.get(i);
                ps.setString(1, issue.getIssueKey());
                ps.setString(2, issue.getProjectKey());
                ps.setString(3, issue.getSummary());
                ps.setString(4, JIRAConstants.JIRA_USER_ID);
                ps.addBatch();
                counter++;
            }//end for

            if(counter > 0){
                inserts = ps.executeBatch();
                myLogger.info(logRowUpdateNumbersFromBatchStatements(inserts, INSERT));
            }//end if

        }catch(SQLException e){
            myLogger.log(Level.SEVERE, "SQLException occured while trying to insert records into the JIRA_TRACKED_ISSUES table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("SQLException occured while trying to insert records into the JIRA_TRACKED_ISSUES table.", e);
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occured while trying to insert records into the JIRA_TRACKED_ISSUES table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("Exception occured while trying to insert records into the JIRA_TRACKED_ISSUES table.", e);
        }finally{
            CreateConnection.destroyObjects(conn, ps, null);
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "insert");
    }//end method

    /**
     * This method will update the list of JIRA issues passed into this method within the JIRA_TRACKED_ISSUES table.
     *
     * @param issues the issues to update into the JIRA_TRACKED_ISSUES table
     * @throws Exception can be thrown while trying to update a record into the JIRA_TRACKED_ISSUES table
     */
    public void update(List<JIRATrackedIssue> issues) throws Exception{
        myLogger.entering(MY_CLASS_NAME, "update", issues);
        PreparedStatement ps = null;
        Connection conn = null;
        JIRATrackedIssue issue = null;
        int counter = 0;
        int[] updates = null;
        try{
            conn = CreateConnection.getHSQLConnection();
            ps = StatementFactory.getStatement(conn, UPDATE, DebugLevel.ON);

            for(int i = 0,j = issues.size(); i < j; i++){
                if(counter >= 50){
                    updates = ps.executeBatch();
                    myLogger.info(logRowUpdateNumbersFromBatchStatements(updates, UPDATE));
                    counter = 0;
                }//end if

                issue = issues.get(i);
                ps.setString(1, issue.getIssueKey());
                ps.addBatch();
                counter++;
            }//end for

            if(counter > 0){
                updates = ps.executeBatch();
                myLogger.info(logRowUpdateNumbersFromBatchStatements(updates, UPDATE));
            }//end if
        }catch(SQLException e){
            myLogger.log(Level.SEVERE, "SQLException occured while trying to update records into the JIRA_TRACKED_ISSUES table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("SQLException occured while trying to update records into the JIRA_TRACKED_ISSUES table.", e);
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occured while trying to update records into the JIRA_TRACKED_ISSUES table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("Exception occured while trying to insert update into the JIRA_TRACKED_ISSUES table.", e);
        }finally{
            CreateConnection.destroyObjects(conn, ps, null);
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "update");
    }//end method

    /**
     * This method will delete the list of JIRA issues passed into this method within the JIRA_TRACKED_ISSUES table.
     *
     * @param issues the issues to delete into the JIRA_TRACKED_ISSUES table
     * @throws Exception can be thrown while trying to delete a record into the JIRA_TRACKED_ISSUES table
     */
    public void delete(List<JIRATrackedIssue> issues) throws Exception{
        myLogger.entering(MY_CLASS_NAME, "delete", issues);
        PreparedStatement ps = null;
        Connection conn = null;
        JIRATrackedIssue issue = null;
        int counter = 0;
        int[] deletes = null;
        try{
            conn = CreateConnection.getHSQLConnection();
            ps = StatementFactory.getStatement(conn, DELETE, DebugLevel.ON);

            for(int i = 0,j = issues.size(); i < j; i++){
                if(counter >= 50){
                    deletes = ps.executeBatch();
                    myLogger.info(logRowUpdateNumbersFromBatchStatements(deletes, DELETE));
                    counter = 0;
                }//end if

                issue = issues.get(i);
                ps.setString(1, issue.getIssueKey());
                ps.addBatch();
                counter++;
            }//end for

            if(counter > 0){
                deletes = ps.executeBatch();
                myLogger.info(logRowUpdateNumbersFromBatchStatements(deletes, DELETE));
            }//end if
        }catch(SQLException e){
            myLogger.log(Level.SEVERE, "SQLException occured while trying to delete records into the JIRA_TRACKED_ISSUES table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("SQLException occured while trying to delete records into the JIRA_TRACKED_ISSUES table.", e);
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occured while trying to delete records into the JIRA_TRACKED_ISSUES table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
            throw new Exception("Exception occured while trying to insert delete into the JIRA_TRACKED_ISSUES table.", e);
        }finally{
            CreateConnection.destroyObjects(conn, ps, null);
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "delete");
    }//end method

    /**
     * Helper method to log number of rows affected in database when executeBatch was called.
     *
     * @param numberOfUpdatedRowsArray
     *        array that holds number of rows affected in database per executed statement.
     * @param sql
     *        the sql statement
     * @return log statement that was built
     */
    private String logRowUpdateNumbersFromBatchStatements(int[] numberOfUpdatedRowsArray, String sql) {
        myLogger.entering(MY_CLASS_NAME, "logRowUpdateNumbersFromBatchStatements", numberOfUpdatedRowsArray != null ? "numberOfUpdatedRowsArray.length=" + numberOfUpdatedRowsArray.length : "null");
        StringBuilder sb = null;
        if(numberOfUpdatedRowsArray != null && numberOfUpdatedRowsArray.length > 0){
            sb = new StringBuilder("Number of statements executed in the calling executeBatch() method: ").append(numberOfUpdatedRowsArray.length);
        }else{
            sb = new StringBuilder("No Rows were updated by sql batch of statements!").append("; SQL batch statement that was executed is: ").append(sql);
        }// end if
        myLogger.exiting(MY_CLASS_NAME, "logRowUpdateNumbersFromBatchStatements", String.valueOf(sb));
        return sb.toString();
    }//end method

}//end class
