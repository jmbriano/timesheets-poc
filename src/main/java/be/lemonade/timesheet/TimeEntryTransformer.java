package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ClientTimeEntry;
import be.lemonade.timesheet.model.FreshbookTimeEntry;
import be.lemonade.timesheet.util.ConfigurationReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeEntryTransformer {

    private static String MAPPER_FILE_NAME = "configuration/mapper.csv";
    private static String DATE_FORMAT = "MM/dd/yy";

    public static List<ClientTimeEntry> transform (List<FreshbookTimeEntry> timeEntries) {
        try {
            ConfigurationReader configurationReader = new ConfigurationReader();
            DATE_FORMAT = configurationReader.getValue(ConfigurationReader.FRESHBOOK_DATE_FORMAT);
        } catch (IOException e){
            // Do nothing. keep default date format
        }

        List<ClientTimeEntry> clientTimeEntryList = new ArrayList<ClientTimeEntry>();
        try {
            List<MapperEntry> mappers = readMapEntries();
            for (FreshbookTimeEntry fte: timeEntries){
                MapperEntry map = findMapper(mappers, fte.getMyClient(), fte.getMyProject(), fte.getMyTask());
                if (map != null){
                    clientTimeEntryList.add(
                            new ClientTimeEntry(
                                    fte.getMyPerson(),
                                    map.sw_project,
                                    map.sw_sc,
                                    map.sw_qtm,
                                    map.sw_ci,
                                    map.sw_wp,
                                    "TODO, note",
                                    parseDate(fte.getMyDate()),
                                    Double.parseDouble(fte.getMyHours())));

                } else {
                    throw new RuntimeException("ERROR: Could not find map entry for: \""+fte.getMyClient()+"\",\""+fte.getMyProject()+"\",\""+fte.getMyTask()+"\" in "+MAPPER_FILE_NAME+". "+
                    "Add an entry in "+MAPPER_FILE_NAME+" so the tool knows to which timesheet row to map the entries found in Freshbook");
                }
            }
        } catch (ParseException e){
            throw new RuntimeException("ERROR: Can not transform the list. Invalid date found. Date should have format: "+DATE_FORMAT);
        } catch (IOException ioe){
            throw new RuntimeException("ERROR: Can not transform the list. can not read mapper file: "+MAPPER_FILE_NAME);
        }

        return clientTimeEntryList;
    }

    private static List<MapperEntry> readMapEntries() throws IOException {
        List<MapperEntry> entries = new ArrayList<MapperEntry>();
        Reader in = new FileReader(MAPPER_FILE_NAME);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
        for (CSVRecord record : records) {
            MapperEntry m = new MapperEntry();
            m.fb_client = record.get("Freshbook Client");
            m.fb_project = record.get("Freshbook Project");
            m.fb_task = record.get("Freshbook Task");

            m.sw_project = record.get("Timesheet Project");
            m.sw_sc = record.get("Timesheet SC");
            m.sw_qtm = record.get("Timesheet QTM");
            m.sw_ci = record.get("Timesheet CI");
            m.sw_wp = record.get("Timesheet WP");

            entries.add(m);
        }
        return entries;
    }

    private static MapperEntry findMapper(List<MapperEntry> mappers, String myClient, String myProject, String myTask) {
        for (MapperEntry entry : mappers){
            if (entry.fb_client.equalsIgnoreCase(myClient) &&
                    entry.fb_project.equalsIgnoreCase(myProject) &&
                    entry.fb_task.equalsIgnoreCase(myTask))
                return entry;
        }
        return null;
    }

    private static Date parseDate(String date) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.parse(date);
    }

    static class MapperEntry {
        public String fb_client;
        public String fb_project;
        public String fb_task;
        public String sw_project;
        public String sw_sc;
        public String sw_qtm;
        public String sw_ci;
        public String sw_wp;

    }

    public static void main(String[] args) {
        List<FreshbookTimeEntry> freshbookTimeEntries = new ArrayList<FreshbookTimeEntry>();
        freshbookTimeEntries.add(new FreshbookTimeEntry("Juan","Client 2","Content API","Software development & testing", "01/20/2017","8.30"));
        freshbookTimeEntries.add(new FreshbookTimeEntry("Juan","Client 2","IDP","Maintenance", "01/20/2017","8.30"));
        freshbookTimeEntries.add(new FreshbookTimeEntry("Juan","Client 2","IDP","Maintenance", "01/20/2017","8.30"));
        try {

            List<ClientTimeEntry> transformedList = TimeEntryTransformer.transform(freshbookTimeEntries);
            System.out.println(transformedList.size());

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

}
