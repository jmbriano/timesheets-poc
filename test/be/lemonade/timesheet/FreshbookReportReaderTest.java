package be.lemonade.timesheet;

import be.lemonade.timesheet.model.FreshbookTimeEntry;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.util.List;

public class FreshbookReportReaderTest extends TestCase{

    public void testParseRecords() throws IOException {
        FreshbookCSVReader frr = new FreshbookCSVReader();
        List<FreshbookTimeEntry> timeEntryList = frr.parseRecords("runtime/input.csv");
        Assert.assertEquals("AVE, 999",timeEntryList.get(0).getMyPerson());
    }

    public void testParseRecordsSize() throws IOException{
        FreshbookCSVReader frr = new FreshbookCSVReader();
        List<FreshbookTimeEntry> timeEntryList = frr.parseRecords("runtime/input.csv");
        Assert.assertTrue(timeEntryList.size() == 104);
    }

}
