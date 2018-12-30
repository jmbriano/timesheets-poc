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
import java.util.*;

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
            Map<String, Map> missingMapperEntries = new HashMap<String, Map>();

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

                    recordMissingMapperEntryAndHours(missingMapperEntries, fte);

                }
            }

            if (!missingMapperEntries.keySet().isEmpty()){

                throw new RuntimeException(getStringError(missingMapperEntries));
            }
        } catch (ParseException e){
            throw new RuntimeException("ERROR: Can not transform the list. Invalid date found. Date should have format: "+DATE_FORMAT);
        } catch (IOException ioe){
            throw new RuntimeException("ERROR: Can not transform the list. can not read mapper file: "+MAPPER_FILE_NAME);
        }

        return clientTimeEntryList;
    }

    private static void recordMissingMapperEntryAndHours(Map<String, Map> missingMapperEntries, FreshbookTimeEntry fte) {

        String missingEntry = "\""+fte.getMyClient()+"\",\""+fte.getMyProject()+"\",\""+fte.getMyTask()+"\",";

        if (!missingMapperEntries.keySet().contains(missingEntry)){

            Map<String,Double> employees = new HashMap<String, Double>();
            employees.put(fte.getMyPerson(),Double.parseDouble(fte.getMyHours()));
            missingMapperEntries.put(missingEntry, employees);

        } else {

            Map<String,Double> employees =  missingMapperEntries.get(missingEntry);
            if (employees.keySet().contains(fte.getMyPerson())){
                employees.put(fte.getMyPerson(),employees.get(fte.getMyPerson())+Double.parseDouble(fte.getMyHours()));
            } else {
                employees.put(fte.getMyPerson(),Double.parseDouble(fte.getMyHours()));
            }
        }
    }

    private static String getStringError(Map<String, Map> missingMapperEntries) {
        StringBuilder errorMessage = new StringBuilder();

        errorMessage.append("ERROR: The following entries are missing in the mapper: \n\n");

        errorMessage.append(buildMissingEntriesList(missingMapperEntries,true));

        errorMessage.append("\nTo fix this, add the following lines in "+MAPPER_FILE_NAME+":\n\n");

        errorMessage.append(buildMissingEntriesList(missingMapperEntries,false));
        return errorMessage.toString();
    }

    private static String buildMissingEntriesList(Map<String, Map> missingMapperEntriesWithEmployees, boolean withHours){

        StringBuilder message = new StringBuilder();
        for (String s: missingMapperEntriesWithEmployees.keySet()){
            message.append(s);
            if (withHours) {
                message.append(" <--- ");
                Map<String, Double> missingEntries = missingMapperEntriesWithEmployees.get(s);
                message.append(" (");
                boolean first = true;
                for (String employee : missingEntries.keySet()) {
                    if (!first)
                        message.append(" || ");
                    Double hours = missingEntries.get(employee);
                    message.append(employee + ": " + Math.round(hours) + "hr");
                    first = false;
                }
                message.append(")");
            }
            message.append("\n");
        }

        return message.toString();

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
