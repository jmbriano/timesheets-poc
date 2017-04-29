package be.lemonade.timesheet.model;

import java.util.Date;

/**
 * This class works as a placeholder for elements
 */
public class SwordTimeEntry {

    private Date date;
    private String employee;
    private ActivityKey activity;
    private String task;
    private double time_hs;

    public SwordTimeEntry(String employee, String project, String sc, String qtm, String wp, String task, Date date, double hs){
        this.employee = employee;
        this.activity = new ActivityKey(project,sc,qtm,wp);
        this.task = task;
        this.date = date;
        this.time_hs = hs;
    }

    public ActivityKey getActivity() {
        return activity;
    }

    public String getTask() {
        return task;
    }

    public String getEmployee() {
        return employee;
    }

    public double getTime_hs() {
        return time_hs;
    }



}
