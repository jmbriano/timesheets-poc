package be.lemonade.timesheet.model;

public class ActivityKey {

    private String project;
    private String specificContract;
    private String qtm_rfa;
    private String ci;
    private String wp;

    public ActivityKey(String project, String sc, String qtm, String ci, String wp) {
        this.project = project;
        this.specificContract = sc;
        this.qtm_rfa = qtm;
        this.ci = ci;
        this.wp = wp;
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
    public String getCI() {
        return ci;
    }

    public String getWp() {
        return wp;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return (this.project +"-"+ this.specificContract +"-"+ this.qtm_rfa + "-" + this.ci +"-"+ this.wp).hashCode();
    }
}
