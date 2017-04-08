package be.lemonade.timesheet

import java.util.List;

public class FreshbookTimeEntry {

    private String myPerson;
    private String myClient;
    private String myProject;
    private String myTask;
    private String myDate;
    private String myHours;

    public FreshbookTimeEntry(String person, String client, String project, String task, String date, String hours) {

        myPerson = person;
        myClient = client;
        myProject = project;
        myTask = task;
        myDate = date;
        myHours = hours;

    }

    public String getMyPerson() {
        return myPerson;
    }

    public String getMyClient() {
        return myClient;
    }

    public String getMyProject() {
        return myProject;
    }

    public String getMyTask() {
        return myTask;
    }

    public String getMyDate() {
        return myDate;
    }

    public String getMyHours() {
        return myHours;
    }

    public void setMyPerson(String myPerson) {
        this.myPerson = myPerson;
    }

    public void setMyClient(String myClient) {
        this.myClient = myClient;
    }

    public void setMyProject(String myProject) {
        this.myProject = myProject;
    }

    public void setMyTask(String myTask) {
        this.myTask = myTask;
    }

    public void setMyDate(String myDate) {
        this.myDate = myDate;
    }

    public void setMyHours(String myHours) {
        this.myHours = myHours;
    }

}
