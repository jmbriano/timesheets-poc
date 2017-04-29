package be.lemonade.timesheet.model;

/**
 * Created by nemo on 4/8/17.
 */
public class ActivityKey {

    private String project;
    private String specificContract;
    private String qtm_rfa;
    private String wp;

    public ActivityKey(String project, String sc, String qtm, String wp) {
        this.project = project;
        this.specificContract = sc;
        this.qtm_rfa = qtm;
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

    public String getWp() {
        return wp;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return (this.project +"-"+ this.specificContract +"-"+ this.qtm_rfa +"-"+ this.wp).hashCode();
    }
}
