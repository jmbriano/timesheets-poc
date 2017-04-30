package be.lemonade.timesheet.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SwordEmployee {

    private String name;
    private List<SwordTimeEntry> timeEntries;

    public SwordEmployee(String inputName) {
        name = inputName;
        timeEntries = new ArrayList<SwordTimeEntry>();
    }

    /** This method adds the given time entry to the list of time entries of this employee
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
        List<ActivityKey> allTimeEntryKeys = new ArrayList<ActivityKey>();

        for(SwordTimeEntry currentEntry : timeEntries){
            if(!allTimeEntryKeys.contains(currentEntry.getActivity())){
                allTimeEntryKeys.add(currentEntry.getActivity());
            }
        }

        return allTimeEntryKeys;
    }

    /** This method sums and returns the total hours spent in a given day for a given activity (row)
     *
     * @param activity
     * @param date
     * @return
     */
    public double getTotalTimeForActivityOnDate(ActivityKey activity, Date date){
        int activityTotal = 0;
        String inDate = parseDate(date);

        for(SwordTimeEntry currentEntry : timeEntries){
            String currentDate = parseDate(currentEntry.getDate());
            if(inDate.equals(currentDate) && currentEntry.getActivity().equals(activity)){
                activityTotal += currentEntry.getTime_hs();
            }
        }

        return activityTotal;
    }

    public String parseDate(Date date){
        DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
        String output = outputFormatter.format(date);
        return output;
    }
}
