package be.lemonade.timesheet.model;

import be.lemonade.timesheet.FreshbookReportReader;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.util.List;

public class ActivityKeyTest extends TestCase{

    public void testEquals() throws IOException {
        ActivityKey a1 = new ActivityKey("p1","sc1","qtm1","wp1");
        ActivityKey a2 = new ActivityKey("p1","sc1","qtm1","wp1");
        Assert.assertEquals(a1,a2);
        Assert.assertEquals(a1.hashCode(),a2.hashCode());

    }

    public void testDifferent() throws IOException{
        ActivityKey a1 = new ActivityKey("p1","sc1","qtm1","wp1");
        ActivityKey a2 = new ActivityKey("p2","sc1","qtm1","wp1");
        ActivityKey a3 = new ActivityKey("p1","sc2","qtm1","wp1");
        ActivityKey a4 = new ActivityKey("p1","sc1","qtm2","wp1");
        ActivityKey a5 = new ActivityKey("p1","sc1","qtm1","wp2");
        ActivityKey a6 = new ActivityKey("p2","sc2","qtm2","wp2");
        ActivityKey a7 = new ActivityKey("p","1sc1","qtm1","wp1");

        Assert.assertNotEquals(a1.hashCode(),a2.hashCode());
        Assert.assertNotEquals(a1.hashCode(),a3.hashCode());
        Assert.assertNotEquals(a1.hashCode(),a4.hashCode());
        Assert.assertNotEquals(a1.hashCode(),a5.hashCode());
        Assert.assertNotEquals(a1.hashCode(),a6.hashCode());
        Assert.assertNotEquals(a1.hashCode(),a7.hashCode());

        Assert.assertNotEquals(a1,a2);
        Assert.assertNotEquals(a1,a3);
        Assert.assertNotEquals(a1,a4);
        Assert.assertNotEquals(a1,a5);
        Assert.assertNotEquals(a1,a6);
        Assert.assertNotEquals(a1,a7);

    }

}
