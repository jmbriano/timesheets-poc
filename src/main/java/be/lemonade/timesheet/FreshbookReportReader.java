package be.lemonade.timesheet;

import be.lemonade.timesheet.model.FreshbookTimeEntry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class FreshbookReportReader {

    public List<FreshbookTimeEntry> parseRecords(String filename) throws IOException {

        List<FreshbookTimeEntry> records = new ArrayList<FreshbookTimeEntry>();
        Reader fr = new FileReader(filename);

        for (CSVRecord currentEntry : CSVFormat.EXCEL.withHeader().parse(fr)) {
            FreshbookTimeEntry newEntry = new FreshbookTimeEntry(currentEntry.get("Team"), currentEntry.get("Client"),
                    currentEntry.get("Project"), currentEntry.get("Task"), currentEntry.get("Date"), currentEntry.get("Hours"));
            records.add(newEntry);
        }
        return records;
    }

}