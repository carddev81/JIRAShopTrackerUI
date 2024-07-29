package com.omo.free.jira.tracker.model;

import java.sql.Timestamp;

/**
 * This class is used to model a Bug reported by a user of the JIRA Shop Tracker UI.
 *
 * @author Richard Salas, May 02, 2019
 */
public class JIRATrackerEnhancement {

    //Columns TYPE|DESCRIPTION|CREATED_BY|CREATE_TS|
    private int refId;
    private String type;
    private String description;
    private String createdBy;
    private Timestamp createTs;

    /**
     * Default Constructor
     */
    public JIRATrackerEnhancement(){

    }//end constructor

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }//end method

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }//end method

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }//end method

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }//end method

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }//end method

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }//end method

    /**
     * @return the createTs
     */
    public Timestamp getCreateTs() {
        return createTs;
    }//end method

    /**
     * @param createTs the createTs to set
     */
    public void setCreateTs(Timestamp createTs) {
        this.createTs = createTs;
    }//end method

    /**
     * @return the refId
     */
    public int getRefId() {
        return refId;
    }//end method

    /**
     * @param refId the refId to set
     */
    public void setRefId(int refId) {
        this.refId = refId;
    }//end method

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Bug [refId=");
        builder.append(refId);
        builder.append(", type=");
        builder.append(type);
        builder.append(", description=");
        builder.append(description);
        builder.append(", createdBy=");
        builder.append(createdBy);
        builder.append(", createTs=");
        builder.append(createTs);
        builder.append("]");
        return builder.toString();
    }//end method

}//end class
