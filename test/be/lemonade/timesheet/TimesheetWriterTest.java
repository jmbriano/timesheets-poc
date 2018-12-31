package be.lemonade.timesheet;

import be.lemonade.timesheet.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

public class TimesheetWriterTest {

    @Test
    public void testGetWorkingDaysInMonth() {
        Assert.assertEquals(23, DateUtil.getWeekDaysInMonth(1, 2019));

        Assert.assertEquals(20, DateUtil.getWeekDaysInMonth(2, 2019));

    }

    @Test(expected = RuntimeException.class)
    public void testGetWorkingDaysInWrongMonth() {
        Assert.assertEquals(23, DateUtil.getWeekDaysInMonth(0,2019));
    }

    @Test(expected = RuntimeException.class)
    public void testGetWorkingDaysInWrongMonth2() {
        Assert.assertEquals(23, DateUtil.getWeekDaysInMonth(13,2019));
    }

}