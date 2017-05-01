package be.lemonade.timesheet.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by nemo on 5/1/17.
 */
public class DateUtil {

    /**
     * Creates a date object
     * @param day 1 based day in them month
     * @param month 1 based month
     * @param year year
     * @return
     */
    public static Date createDate(int day, int month, int year){
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, 0, 0, 0);

        return c.getTime();

    }

    /**
     * Creates a date object with the date of the last day of the month of the given date
     * @param date
     * @return
     */
    public static Date getLastDateOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));

        return cal.getTime();

    }
}
