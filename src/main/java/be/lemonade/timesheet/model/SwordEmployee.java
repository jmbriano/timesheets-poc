package be.lemonade.timesheet.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SwordEmployee {

    private String name;
    private List<SwordTimeEntry> timeEntries;

    public SwordEmployee(String inputName) {
        name = inputName;
        timeEntries = new ArrayList<SwordTimeEntry>();
    }

    /**
     * This method adds the given time entry to the list of time entries of this employee
     *
     * @param timeEntry the Time entry to add to the list.
     */
    public void addSwordEntry(SwordTimeEntry timeEntry) {
        timeEntries.add(timeEntry);
    }

    public String getName() {
        return name;
    }

    /**
     * This method returns a list of unique ActivityKey
     * based on the entries existing in the employee
     * The list must be sorted by: Project, SC, QTM, CI, WP
     *
     * @return
     */
    public List<ActivityKey> getAllTimeEntryKeys() {
        List<ActivityKey> allTimeEntryKeys = new ArrayList<ActivityKey>();

        for (SwordTimeEntry currentEntry : timeEntries) {
            if (!allTimeEntryKeys.contains(currentEntry.getActivity())) {
                allTimeEntryKeys.add(currentEntry.getActivity());
            }
        }

        Collections.sort(allTimeEntryKeys, new Comparator<ActivityKey>() {
            public int compare(ActivityKey k1, ActivityKey k2) {
                Integer k1SP;
                Integer k2SP;
                try {
                    k1SP = Integer.parseInt(k1.getSpecificContract());
                    k2SP = Integer.parseInt(k2.getSpecificContract());
                } catch (NumberFormatException e) {
                    k1SP = k2SP = 0;
                }
                if (k1.getProject().equalsIgnoreCase(k2.getProject()))
                    if (k1SP.equals(k2SP))
                        if (k1.getQtm_rfa().equalsIgnoreCase(k2.getQtm_rfa()))
                            if (k1.getCI().equalsIgnoreCase(k2.getCI()))
                                return k1.getWp().compareTo(k2.getWp());
                            else
                                return k1.getCI().compareTo(k2.getCI());
                        else
                            return k1.getQtm_rfa().compareTo(k2.getQtm_rfa());
                    else
                        return k1SP.compareTo(k2SP);
                else
                    return k1.getProject().compareTo(k2.getProject());
            }
        });

        return allTimeEntryKeys;
    }

    /**
     * This method sums and returns the total hours spent in a given day for a given activity (row)
     *
     * @param activity
     * @param date
     * @return
     */
    public double getTotalTimeForActivityOnDate(ActivityKey activity, Date date) {
        double activityTotal = 0;
        String inDate = parseDate(date);

        for (SwordTimeEntry currentEntry : timeEntries) {
            String currentDate = parseDate(currentEntry.getDate());
            if (inDate.equals(currentDate) && currentEntry.getActivity().equals(activity)) {
                activityTotal += currentEntry.getTime_hs();
            }
        }

        return activityTotal;
    }

    public String parseDate(Date date) {
        DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
        String output = outputFormatter.format(date);
        return output;
    }
}
