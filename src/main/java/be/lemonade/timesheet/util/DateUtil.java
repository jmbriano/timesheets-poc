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

    public static int getWeekDaysInMonth(int month, int year) {

        if (month<1 || month > 12){
            throw new RuntimeException("Invalid month: "+month);
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(createDate(1,month,year));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(createDate(lastDayOfMonth(month,year),month,year));

        int workDays = 0;

        do {
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                workDays++;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (startCal.getTimeInMillis() <= endCal.getTimeInMillis());

        return workDays;
    }

    public static int lastDayOfMonth(int month, int year) {
        Date firstDay = createDate(1,month,year);
        Date date = getLastDateOfMonth(firstDay);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.DAY_OF_MONTH);

    }
}
