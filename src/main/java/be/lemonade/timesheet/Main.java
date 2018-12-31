package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ClientTimeEntry;
import be.lemonade.timesheet.model.FreshbookTimeEntry;
import be.lemonade.timesheet.model.Employee;
import be.lemonade.timesheet.model.exceptions.MissingMapperEntryException;
import be.lemonade.timesheet.util.ConfigurationReader;
import be.lemonade.timesheet.util.DateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {

        ConfigurationReader config = new ConfigurationReader();
        List<String> warningMoreHours = new ArrayList<String>();
        List<String> warningLessHours = new ArrayList<String>();

        try {

            List<FreshbookTimeEntry> freshbookTimeEntries;

            String source = config.getValue(ConfigurationReader.DATA_SOURCE);
            if ("ONLINE".equalsIgnoreCase(source)){
                println("Reading freshbook directly from: " + config.getValue(ConfigurationReader.FRESHBOOK_API_URL));
                FreshbookOnlineReader onlineLoader = new FreshbookOnlineReader(
                        config.getValue(ConfigurationReader.FRESHBOOK_API_URL),
                        config.getValue(ConfigurationReader.FRESHBOOK_API_TOKEN),
                        true,
                        config.getValue(ConfigurationReader.OUTPUT_DIR)+config.getValue(ConfigurationReader.FRESHBOOK_OUTPUT_CSV_NAME));

                Integer month = Integer.parseInt(config.getValue(ConfigurationReader.MONTH));
                Integer year = Integer.parseInt(config.getValue(ConfigurationReader.YEAR));
                Date from = DateUtil.createDate(1,month, year);
                Date to = DateUtil.getLastDateOfMonth(from);

                freshbookTimeEntries = onlineLoader.parseRecords(from,to);

            } else if ("CSV".equalsIgnoreCase(source)){
                println("Reading freshbook records from CSV file: "+ config.getValue(ConfigurationReader.FRESHBOOK_EXPORT_FILENAME));
                freshbookTimeEntries = FreshbookCSVReader.parseRecords(config.getValue(ConfigurationReader.FRESHBOOK_EXPORT_FILENAME));

            } else {
                System.out.println("ERROR: DATA_SOURCE should be either ONLINE or CSV");
                return;
            }
            println(" - Total entries found: "+freshbookTimeEntries.size());

            // filter relevant entries
            List<FreshbookTimeEntry> filteredFreshbookTimeEntries = filterClients(freshbookTimeEntries, config.getValue(ConfigurationReader.RELEVANT_PROJECTS));
            println(" - Relevant entries found ("+config.getValue(ConfigurationReader.RELEVANT_PROJECTS)+"): "+filteredFreshbookTimeEntries.size());
            println();

            List<ClientTimeEntry> swordTimeEntries;

            swordTimeEntries = TimeEntryTransformer.transform(filteredFreshbookTimeEntries);

            createTimesheets(config, swordTimeEntries, warningLessHours, warningMoreHours);

            printWarnings(warningLessHours, warningMoreHours);

            generateBudgetCardsSummary(config, swordTimeEntries);

        } catch (MissingMapperEntryException e){

            String outputFileName = buildMissingMapperOutputFilename(config);

            println(e.getMessage(), outputFileName);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (RuntimeException e){
            println(e.getMessage());
        }

    }

    private static void printWarnings(List<String> warningLessHours, List<String> warningMoreHours) {

        println();

        if (warningLessHours.size()>0 || warningMoreHours.size()>0){
            println("WARNINGs:\n");
        }
        for (String warning: warningLessHours){
            println("WARNING: " + warning);
        }

        println();

        for (String warning: warningMoreHours){
            println("WARNING: " + warning);
        }
        println();
    }

    private static void generateBudgetCardsSummary(ConfigurationReader config, List<ClientTimeEntry> swordTimeEntries) {
        String outputFileName = buildBudgetCardOutputFilename(config);

        println("Reading QTMs:", outputFileName);
        List<String> qtms = getQTMs(swordTimeEntries);
        Collections.sort(qtms);
        qtms.remove(""); // remove horizontal activities
        println(" - Total QTMs found: "+ qtms.size(), outputFileName);

        for (String qtm : qtms){
            if (qtm!=null && !"".equals(qtm) ) {
                println("-------------------------------------------------------------------------------------", outputFileName);
                println("Budget card info for: " + qtm, outputFileName);
                Map<String, Double> tpp = BudgetCardReporter.qtmTimePerPerson(qtm, swordTimeEntries);

                double total = 0;
                List<String> persons = new ArrayList<String>(tpp.keySet());
                Collections.sort(persons);
                NumberFormat formatter = new DecimalFormat("#0.00");
                for (String person : persons) {

                    println("   - " + String.format("%1$30s", person) + " - " + formatter.format(tpp.get(person)) + " hr - " + formatter.format(tpp.get(person) / 8) + " mdays", outputFileName);
                    total += tpp.get(person);
                }
                println("     " + String.format("%1$30s", "TOTAL") + " - " + formatter.format(total) + " hr - " + formatter.format(total / 8) + " mdays", outputFileName);
            }
        }
    }

    private static void createTimesheets(ConfigurationReader conf, List<ClientTimeEntry> swordTimeEntries, List<String> warningsLess, List<String> warningsMore) throws IOException {

        int month = Integer.parseInt(conf.getValue(ConfigurationReader.MONTH));
        int year = Integer.parseInt(conf.getValue(ConfigurationReader.YEAR));
        int warningLimit = Integer.parseInt(conf.getValue(ConfigurationReader.WARNING_LIMIT_HR));

        int weekDays = DateUtil.getWeekDaysInMonth(month,year);
        int targetHours = weekDays * 8;

        // Get distinct employees
        println("Reading employees:");
        Map<String, List<ClientTimeEntry>> employees = getEmployeesMap(swordTimeEntries);
        println(" - Total employees found: "+employees.keySet().size());

        for (String name : employees.keySet()){
            println("-------------------------------------------------------------------------------------");
            println("Creating timesheet for: "+ name.toUpperCase());
            Employee employee = new Employee(name);
            for (ClientTimeEntry timeEntry : employees.get(name)){
                employee.addSwordEntry(timeEntry);
            }

            try {
                // Write timesheets
                TimesheetWriter.write(employee);

                int total = (int)employee.getTotalTime();
                if ((total-targetHours)>warningLimit){
                    warningsMore.add(employee.getName() + " worked " + total +" hr which is "+(total-targetHours)+" MORE than the month target ("+targetHours+")");
                }
                if ((targetHours-total)>warningLimit){
                    warningsLess.add(employee.getName() + " worked " + total +" hr which is "+(targetHours-total)+" LESS than the month target ("+targetHours+")");
                }

            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private static String buildBudgetCardOutputFilename(ConfigurationReader config) {
        String outputFileName;
        String outputDir = config.getValue(ConfigurationReader.OUTPUT_DIR);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String timestamp = df.format(new Date());
        outputFileName = outputDir+"BudgetCardOutput-"+timestamp+".txt";
        return outputFileName;
    }

    private static String buildMissingMapperOutputFilename(ConfigurationReader config) {
        String outputFileName;
        String outputDir = config.getValue(ConfigurationReader.OUTPUT_DIR);
        outputFileName = outputDir+"MapperErrors.txt";
        return outputFileName;
    }



    private static List<String> getQTMs(List<ClientTimeEntry> swordTimeEntries) {
        List<String> qtms = new ArrayList<String>();

        for (ClientTimeEntry te: swordTimeEntries){
            if (!qtms.contains(te.getActivity().getQtm_rfa())){
                qtms.add(te.getActivity().getQtm_rfa());
            }
        }
        return qtms;
    }

    private static Map<String, List<ClientTimeEntry>> getEmployeesMap(List<ClientTimeEntry> swordTimeEntries) {
        Map<String, List<ClientTimeEntry>> map = new HashMap<String, List<ClientTimeEntry>>();

        for (ClientTimeEntry te: swordTimeEntries){
            if (!map.containsKey(te.getEmployee())){
                map.put(te.getEmployee(),new ArrayList<ClientTimeEntry>());
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

    private static void println(){
        println("", null);
    }

    private static void println(String line){
        println(line, null);
    }

    private static void println(String line, String filename ){
        System.out.println(line);
        if (filename != null){
            try{
                writeToFile(line, filename);
            } catch (IOException e) {
                System.out.println("ERROR: Can not write in "+ filename);
                e.printStackTrace();
            }

        }
    }

    public static void writeToFile(String line, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
        writer.append(line);
        writer.append("\n");
        writer.close();
    }
}
