package be.lemonade.timesheet;

import java.util.ArrayList;
import java.util.List;

public class Employee {

    private String name;
    private List<SwordTimeEntry> timeEntries;

    public Employee(String inputName) {
        name = inputName;
        timeEntries = new ArrayList<SwordTimeEntry>();
    }

    public void addSwordEntry(SwordTimeEntry timeEntry) {
        timeEntries.add(timeEntry);
    }

    public String getName() {
        return name;
    }

    public List<SwordTimeEntry> getEntries() {
        return timeEntries;
    }
}
