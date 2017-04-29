package be.lemonade.timesheet.model;

import be.lemonade.timesheet.ActivityKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nemo on 4/29/17.
 */
public class SwordEmployee {

    private String name;
    private List<SwordTimeEntry> timeEntries;

    public SwordEmployee(String inputName) {
        name = inputName;
        timeEntries = new ArrayList<SwordTimeEntry>();
    }

    /** This method adds the given time entry to the list of time entries of this empoyee
     * @param timeEntry the Time entry to add to the list.
     */
    public void addSwordEntry(SwordTimeEntry timeEntry) {
        timeEntries.add(timeEntry);
    }

    public String getName() {
        return name;
    }

    /** This method returns a list of unique ActivityKey
     * based on the entries existing in the employee
     * The list must be sorted by: Project, SC, QTM, WP
     * @return
     */
    public List<ActivityKey> getAllTimeEntryKeys(){
        //TODO:
        return new ArrayList<ActivityKey>();
    }

    /** This method sums and returns the total hours spent in a given day for a given activity (row)
     *
     * @param activity
     * @param date
     * @return
     */
    public double getTotalTimeForActivityOnDate(ActivityKey activity, Date date){
        return 0;
    }
}
