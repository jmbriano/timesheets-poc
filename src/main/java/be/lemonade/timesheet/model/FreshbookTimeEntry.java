package be.lemonade.timesheet.model;

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

}
