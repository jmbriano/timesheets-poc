package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ActivityKey;
import be.lemonade.timesheet.model.SwordEmployee;

import java.util.Date;
import java.util.List;

/**
 * Created by nemo on 4/30/17.
 */
public class TimesheetWriter {

    public static void write(SwordEmployee employee){

        // Open template XLS

        // Write employee name

        // Get unique Activity list
        List<ActivityKey> allKeys = employee.getAllTimeEntryKeys();
        // Iterate the list of Activity
        for (ActivityKey key: allKeys){
            // Iterate each day and ask number of hours
            for (int i=1; i <= lastDayOfMonth(4,2017); i++){
                Date date = null;
                double hours = employee.getTotalTimeForActivityOnDate(key,date);
                // Write value in cell (only if bigger than 0)

            }
        }

        // Recalculate formulas

        // Write output file




    }

    private static int lastDayOfMonth(int i, int i1) {
        return 0;
    }
}
