package be.lemonade.timesheet;

import org.junit.Assert;
import org.junit.Test;

public class TimesheetWriterTest {

    @Test
    public void testGetWorkingDaysInMonth() {
        Assert.assertEquals(23, TimesheetWriter.getWeekDaysInMonth(1, 2019));

        Assert.assertEquals(20, TimesheetWriter.getWeekDaysInMonth(2, 2019));

    }

    @Test(expected = RuntimeException.class)
    public void testGetWorkingDaysInWrongMonth() {
        Assert.assertEquals(23,TimesheetWriter.getWeekDaysInMonth(0,2019));
    }

    @Test(expected = RuntimeException.class)
    public void testGetWorkingDaysInWrongMonth2() {
        Assert.assertEquals(23,TimesheetWriter.getWeekDaysInMonth(13,2019));
    }

}