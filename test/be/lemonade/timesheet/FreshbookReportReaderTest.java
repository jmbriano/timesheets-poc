package be.lemonade.timesheet;

import be.lemonade.timesheet.model.FreshbookTimeEntry;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.util.List;

public class FreshbookReportReaderTest extends TestCase{

    public void testParseRecords() throws IOException {
        FreshbookReportReader frr = new FreshbookReportReader();
        List<FreshbookTimeEntry> timeEntryList = frr.parseRecords("runtime/input.csv");
        Assert.assertTrue(timeEntryList.get(0).getMyPerson().contains("LastName, Name 1"));
    }

    public void testParseRecordsSize() throws IOException{
        FreshbookReportReader frr = new FreshbookReportReader();
        List<FreshbookTimeEntry> timeEntryList = frr.parseRecords("runtime/input.csv");
        Assert.assertTrue(timeEntryList.size() == 104);
    }

}
