package be.lemonade.timesheet.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Employee {

    private String name;
    private List<ClientTimeEntry> timeEntries;

    public Employee(String inputName) {
        name = inputName;
        timeEntries = new ArrayList<ClientTimeEntry>();
    }

    /**
     * This method adds the given time entry to the list of time entries of this employee
     *
     * @param timeEntry the Time entry to add to the list.
     */
    public void addSwordEntry(ClientTimeEntry timeEntry) {
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

        for (ClientTimeEntry currentEntry : timeEntries) {
            if (!allTimeEntryKeys.contains(currentEntry.getActivity())) {
                allTimeEntryKeys.add(currentEntry.getActivity());
            }
        }

        Collections.sort(allTimeEntryKeys, new Comparator<ActivityKey>() {
            public int compare(ActivityKey k1, ActivityKey k2) {
                Integer k1SP;
                Integer k2SP;
                Integer k1QTM;
                Integer k2QTM;
                try {
                    k1SP = Integer.parseInt(k1.getSpecificContract());
                } catch (NumberFormatException e) {
                    k1SP = 0;
                }try {
                    k2SP = Integer.parseInt(k2.getSpecificContract());
                } catch (NumberFormatException e) {
                    k2SP = 0;
                }try {
                    k1QTM = Integer.parseInt(k1.getQtm_rfa());
                } catch (NumberFormatException e) {
                    k1QTM = 0;
                }try {
                    k2QTM = Integer.parseInt(k2.getQtm_rfa());
                } catch (NumberFormatException e) {
                    k2QTM = 0;
                }
                if (k1.getProject().equalsIgnoreCase(k2.getProject()))
                    if (k1SP.equals(k2SP))
                        if (k1QTM.equals(k2QTM))
                            if (k1.getCI().equalsIgnoreCase(k2.getCI()))
                                return k1.getWp().compareTo(k2.getWp());
                            else
                                return k1.getCI().compareTo(k2.getCI());
                        else
                            return k1QTM.compareTo(k2QTM);
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

        for (ClientTimeEntry currentEntry : timeEntries) {
            String currentDate = parseDate(currentEntry.getDate());
            if (inDate.equals(currentDate) && currentEntry.getActivity().equals(activity)) {
                activityTotal += currentEntry.getTime_hs();
            }
        }

        return activityTotal;
    }

    /**
     * This method sums and returns the total hours spent in a given activity (row)
     *
     * @param activity
     * @return
     */
    public double getTotalTimeForActivity(ActivityKey activity) {
        double activityTotal = 0;

        for (ClientTimeEntry currentEntry : timeEntries) {
            if (currentEntry.getActivity().equals(activity)) {
                activityTotal += currentEntry.getTime_hs();
            }
        }

        return activityTotal;
    }

    /**
     * This method sums and returns the total hours spent
     * @return
     */
    public double getTotalTime() {
        double activityTotal = 0;

        for (ClientTimeEntry currentEntry : timeEntries) {
            activityTotal += currentEntry.getTime_hs();
        }

        return activityTotal;
    }

    public String parseDate(Date date) {
        DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
        String output = outputFormatter.format(date);
        return output;
    }
}
