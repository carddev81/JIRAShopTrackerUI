package com.omo.free.jira.tracker.ui;

import com.omo.free.jira.tracker.util.JiraUtil;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

public class JIRAReportTab extends AbstractJIRATrackerTab{

    public JIRAReportTab(JIRAWindowBuilder parent) {
        super(parent, "Reports");
        setGraphic(JiraUtil.createIcon(FontAwesomeIcon.OPTIN_MONSTER));
        layoutForm();
        attachHandlers();
    }

    private void attachHandlers() {
       setContent(new UnderConstructionBox("REPORTS TAB CURRENTLY UNDER CONSTRUCTION!"));
    }

    private void layoutForm() {

    }

}
