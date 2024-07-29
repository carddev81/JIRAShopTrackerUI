package com.omo.free.jira.tracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.junderground.jdbc.DebugLevel;
import com.junderground.jdbc.StatementFactory;
import com.omo.free.jira.tracker.constants.JIRAConstants;
import com.omo.free.jira.tracker.model.JIRATrackerEnhancement;

import gov.doc.isu.simple.fx.tools.CreateConnection;



/**
 * This class is used for basic CRUD for JIRA Tracker Enhancements and Bugs sent to the shop.
 *
 * A HSQL DB table named JIRA_TRACKER_ENHANCEMENTS is the keeper of all the bugs and enhancements that have been sent to the shop.
 *
 * @author Richard Salas, April 17, 2019
 */
public class JIRAEnhancementDAO {

    private static final String MY_CLASS_NAME = "com.omo.free.jira.tracker.dao.JIRAEnhancementDAO";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /* Basic SQL queries used by this DAO */
    private static final String CREATE = "CREATE CACHED TABLE IF NOT EXISTS JIRA_TRACKER_ENHANCEMENTS(REF_ID INTEGER GENERATED BY DEFAULT AS IDENTITY, TYPE VARCHAR(20), DESCRIPTION VARCHAR(1000), CREATED_BY VARCHAR(30), CREATE_TS TIMESTAMP(0))";
    private static final String INSERT = "INSERT INTO JIRA_TRACKER_ENHANCEMENTS(TYPE, DESCRIPTION, CREATED_BY, CREATE_TS) values (?, ?, ?, CURRENT_TIMESTAMP)";

    /**
     * Constructor used to create an instance of the JIRATrackerDAO class.
     *
     * @throws Exception can be thrown while trying to get a db connection
     */
    public JIRAEnhancementDAO() {
        myLogger.entering(MY_CLASS_NAME, "JIRAEnhancementDAO");
        create();//create the table if does not exist!...if there are any issues then error out here letting user know that issues cannot be tracked, but can currently this will allow user to still send them.
        myLogger.entering(MY_CLASS_NAME, "JIRAEnhancementDAO");
    }//end constructor

    /**
     * This method will create the JIRA_TRACKER_ENHANCEMENTS table if it does not exist.
     *
     */
    public void create() {
        myLogger.entering(MY_CLASS_NAME, "create");

        Connection conn = null;
        PreparedStatement ps = null;

        try{
            conn = CreateConnection.getHSQLConnection();
            ps = StatementFactory.getStatement(conn, CREATE, DebugLevel.ON);
            ps.execute();
        }catch(SQLException e){
            myLogger.log(Level.SEVERE, "SQLException occured while trying to create shared cached table JIRA_TRACKER_ENHANCEMENTS. sql=" + String.valueOf(CREATE) + "; Error is: " + e.getMessage(), e);
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occured while trying to create shared cached table JIRA_TRACKER_ENHANCEMENTS. sql=" + String.valueOf(CREATE) + " Error is: " + e.getMessage(), e);
        }finally{
            CreateConnection.destroyObjects(conn, ps, null);
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "create");
    }//end method

    /**
     * This method will insert a JIRATrackerEnhancement into the JIRA_TRACKER_ENHANCEMENTS table.
     *
     * @param enhancement the enhancement to insert into the JIRA_TRACKER_ENHANCEMENTS tab;e
     */
    public void insert(JIRATrackerEnhancement enhancement) {
        myLogger.entering(MY_CLASS_NAME, "insert", enhancement);
        PreparedStatement ps = null;
        Connection conn = null;
        try{
            conn = CreateConnection.getHSQLConnection();
            ps = StatementFactory.getStatement(conn, INSERT, DebugLevel.ON);

            ps.setString(1, enhancement.getType());
            ps.setString(2, truncate(enhancement.getDescription(), 1000));
            ps.setString(3, JIRAConstants.JIRA_USER_ID);

            int inserts = ps.executeUpdate();
            myLogger.info("Successsfully inserted " + String.valueOf(inserts) + " record into the JIRA_TRACKER_ENHANCEMENTS table");
        }catch(SQLException e){
            myLogger.log(Level.SEVERE, "SQLException occured while trying to insert a record into the JIRA_TRACKER_ENHANCEMENTS table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occured while trying to insert a record into the JIRA_TRACKER_ENHANCEMENTS table. ps=" + String.valueOf(ps) + " Error is: " + e.getMessage(), e);
        }finally{
            CreateConnection.destroyObjects(conn, ps, null);
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "insert");
    }//end method

    /**
     * This helper method is used to truncate strings to the length passed into this method.
     *
     * @param description the description
     * @param length the length of the description
     * @return the truncated string
     */
    private String truncate(String truncateStr, int length) {
        return truncateStr.length() > length ? truncateStr.substring(0, length) : truncateStr;
    }//end method

}//end class
