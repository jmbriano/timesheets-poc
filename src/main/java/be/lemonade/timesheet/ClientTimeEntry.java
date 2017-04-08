package be.lemonade.timesheet;
/**
 * This class works as a placeholder for elements
 */
public class ClientTimeEntry {

    private String employee;
    private String project;
    private String specificContract;
    private String qtm_rfa;
    private String wp;
    private String task;
    private double time_hs;

    public ClientTimeEntry(String employee, String project, String sc, String qtm, String wp, String task, double hs){
        this.employee = employee;
        this.project = project;
        this.specificContract = sc;
        this.qtm_rfa = qtm;
        this.wp = wp;
        this.task = task;
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
