package be.lemonade.timesheet;

import be.lemonade.timesheet.model.FreshbookTimeEntry;
import be.lemonade.timesheet.model.SwordEmployee;
import be.lemonade.timesheet.model.SwordTimeEntry;
import be.lemonade.timesheet.util.ConfigurationReader;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.*;

/**
 * Created by nemo on 5/1/17.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        ConfigurationReader config = new ConfigurationReader();

        // Read input file
        System.out.println("Reading freshbook export:");
        List<FreshbookTimeEntry> freshbookTimeEntries = FreshbookReportReader.parseRecords(config.getValue(ConfigurationReader.FRESHBOOK_EXPORT_FILENAME));
        System.out.println(" - Total entries found: "+freshbookTimeEntries.size());

        // filter sword entries
        List<FreshbookTimeEntry> filteredFreshbookTimeEntries = filterClients(freshbookTimeEntries, config.getValue(ConfigurationReader.RELEVANT_PROJECTS));
        System.out.println(" - Relevant entries found ("+config.getValue(ConfigurationReader.RELEVANT_PROJECTS)+"): "+filteredFreshbookTimeEntries.size());

        // Map FreshbookTimeEntry to SwordTimeEntry
        List<SwordTimeEntry> swordTimeEntries = null;
        try {
            swordTimeEntries = TimeEntryTransformer.transform(filteredFreshbookTimeEntries);
        } catch (RuntimeException e){
            System.out.println(e.getMessage());
            return;
        }


        // Get distinct employees
        System.out.println("Reading employees:");
        Map<String, List<SwordTimeEntry>> employees = getEmployeesMap(swordTimeEntries);
        System.out.println(" - Total employees found: "+employees.keySet().size());

        for (String name : employees.keySet()){
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println("Creating timesheet for: "+ name.toUpperCase());
            SwordEmployee employee = new SwordEmployee(name);
            for (SwordTimeEntry timeEntry : employees.get(name)){
                employee.addSwordEntry(timeEntry);
            }

            try {
                // Write timesheets
                TimesheetWriter.write(employee);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
        }

    }

    private static Map<String, List<SwordTimeEntry>> getEmployeesMap(List<SwordTimeEntry> swordTimeEntries) {
        Map<String, List<SwordTimeEntry>> map = new HashMap<String, List<SwordTimeEntry>>();

        for (SwordTimeEntry te: swordTimeEntries){
            if (!map.containsKey(te.getEmployee())){
                map.put(te.getEmployee(),new ArrayList<SwordTimeEntry>());
            }
            map.get(te.getEmployee()).add(te);
        }
        return map;
    }

    private static List<FreshbookTimeEntry> filterClients(List<FreshbookTimeEntry> allEntries, String value) {
        List<String> filters = Arrays.asList(value.split(","));
        List<FreshbookTimeEntry> filtered = new ArrayList<FreshbookTimeEntry>();
        for (FreshbookTimeEntry te: allEntries){
            if (filters.contains(te.getMyClient())){
                filtered.add(te);
            }
        }
        return filtered;
    }
}
