package be.lemonade.timesheet.model;

import java.util.Date;

/**
 * This class works as a placeholder for elements
 */
public class SwordTimeEntry {

    private Date date;
    private String employee;
    private String project;
    private String specificContract;
    private String qtm_rfa;
    private String wp;
    private String task;
    private double time_hs;

    public SwordTimeEntry(String employee, String project, String sc, String qtm, String wp, String task, Date date, double hs){
        this.employee = employee;
        this.project = project;
        this.specificContract = sc;
        this.qtm_rfa = qtm;
        this.wp = wp;
        this.task = task;
        this.date = date;
        this.time_hs = hs;
    }

    public String getProject() {
        return project;
    }

    public String getSpecificContract() {
        return specificContract;
    }

    public String getQtm_rfa() {
        return qtm_rfa;
    }

    public String getWp() {
        return wp;
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
