package be.lemonade.timesheet.model;

import java.util.Date;

/**
 * This class works as a placeholder for elements
 */
public class ClientTimeEntry {

    private Date date;
    private String employee;
    private ActivityKey activity;
    private String task;
    private double time_hs;

    public ClientTimeEntry(String employee, String project, String sc, String qtm, String ci, String wp, String task, Date date, double hs){
        this.employee = employee;
        this.activity = new ActivityKey(project,sc,qtm,ci,wp);
        this.task = task;
        this.date = date;
        this.time_hs = hs;
    }

    public Date getDate() {
        return date;
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
