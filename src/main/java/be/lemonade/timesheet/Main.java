package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ClientTimeEntry;
import be.lemonade.timesheet.model.Employee;
import be.lemonade.timesheet.model.FreshbookTimeEntry;
import be.lemonade.timesheet.model.exceptions.MissingMapperEntryException;
import be.lemonade.timesheet.util.ConfigurationReader;
import be.lemonade.timesheet.util.DateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static String version = "3.3";

    public static void main(String[] args) throws IOException {

        ConfigurationReader config = new ConfigurationReader();
        List<String> warningMoreHours = new ArrayList<String>();
        List<String> warningLessHours = new ArrayList<String>();

        String mapperErrorLog = buildMissingMapperOutputFilename(config);
        String standardLog = buildBudgetCardOutputFilename(config);

        println("=================================================================", standardLog);
        println("Running Timesheets tool v"+version+" on "+ new Date().toString(), standardLog);
        println("=================================================================", standardLog);

        try {

            List<FreshbookTimeEntry> freshbookTimeEntries;

            freshbookTimeEntries = loadFreshbookTimeEntries(config, standardLog);

            println("",standardLog);

            // filter relevant entries
            List<FreshbookTimeEntry> filteredFreshbookTimeEntries = filterClients(freshbookTimeEntries, config.getValue(ConfigurationReader.RELEVANT_PROJECTS));

            if (filteredFreshbookTimeEntries.isEmpty()){
                println("No relevant entries for:'"+config.getValue(ConfigurationReader.RELEVANT_PROJECTS)+"'. Ending tool.",standardLog);
                return;
            }

            println("Filtered relevant entries for:'"+config.getValue(ConfigurationReader.RELEVANT_PROJECTS)+"': "+filteredFreshbookTimeEntries.size(),standardLog);

            println("",standardLog);

            List<ClientTimeEntry> swordTimeEntries;

            filteredFreshbookTimeEntries = TimeEntryTransformer.transformNames(filteredFreshbookTimeEntries);

            swordTimeEntries = TimeEntryTransformer.transform(filteredFreshbookTimeEntries);

            createTimesheets(config, swordTimeEntries, warningLessHours, warningMoreHours, standardLog);

            printWarnings(config, warningLessHours, warningMoreHours,standardLog);

            generateBudgetCardsSummary(swordTimeEntries, standardLog);

        } catch (MissingMapperEntryException e){

            String outputFileName = buildMissingMapperOutputFilename(config);

            println("There are lines missing in the mapper. Check '"+outputFileName+"'for details", standardLog);

            println(e.getMessage(), outputFileName,false);

            writeToFile(e.getMessage(),standardLog,true);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e){
            println(e.getMessage(),standardLog);
        }

        println("=================================================================", standardLog);
        println("Exiting Timesheets tool v"+version+" on "+ new Date().toString(), standardLog);
        println("=================================================================", standardLog);


    }

    private static List<FreshbookTimeEntry> loadFreshbookTimeEntries(ConfigurationReader config, String logName) {
        List<FreshbookTimeEntry> freshbookTimeEntries;
        String source = config.getValue(ConfigurationReader.DATA_SOURCE);

        if ("ONLINE".equalsIgnoreCase(source)){

            println("Reading Time Entries from Freshbook API: " + config.getValue(ConfigurationReader.FRESHBOOK_API_URL), logName);

            FreshbookOnlineReader onlineLoader = new FreshbookOnlineReader(
                    config.getValue(ConfigurationReader.FRESHBOOK_API_URL),
                    config.getValue(ConfigurationReader.FRESHBOOK_API_TOKEN),
                    true,
                    config.getValue(ConfigurationReader.OUTPUT_DIR)+config.getValue(ConfigurationReader.FRESHBOOK_OUTPUT_CSV_NAME));


            Integer month = Integer.parseInt(config.getValue(ConfigurationReader.MONTH));
            Integer year = Integer.parseInt(config.getValue(ConfigurationReader.YEAR));
            Date from = DateUtil.createDate(1,month, year);
            Date to = DateUtil.getLastDateOfMonth(from);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            println("   Period extracted: " + df.format(from) +" to "+ df.format(to) + " (both included)", logName);

            try {

                freshbookTimeEntries = onlineLoader.parseRecords(from,to);

            } catch (Exception e) {
                println("ERROR: An error occurred while obtaining the Time Entries from Freshbook. If the problem persist, export the CSV from Freshbooks and set configuration to use DATA_SOURCE=CSV", logName);
                println("ERROR: Exception: "+e.getMessage(), logName);
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            println("   The Time Entries obtained were saved in: " + config.getValue(ConfigurationReader.OUTPUT_DIR)+config.getValue(ConfigurationReader.FRESHBOOK_OUTPUT_CSV_NAME), logName);

        } else if ("CSV".equalsIgnoreCase(source)){
            println("Reading freshbook records from CSV file: "+ config.getValue(ConfigurationReader.FRESHBOOK_EXPORT_FILENAME),logName);
            try {
                freshbookTimeEntries = FreshbookCSVReader.parseRecords(config.getValue(ConfigurationReader.FRESHBOOK_EXPORT_FILENAME));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else {
            println("ERROR: DATA_SOURCE should be either ONLINE or CSV", logName);
            throw new RuntimeException("ERROR: DATA_SOURCE should be either ONLINE or CSV");
        }
        println("   Loading finished. Total entries found: "+freshbookTimeEntries.size(),logName);
        return freshbookTimeEntries;
    }

    private static void printWarnings(ConfigurationReader conf, List<String> warningLessHours, List<String> warningMoreHours, String logName) {

        println("",logName);

        if (warningLessHours.size()>0 || warningMoreHours.size()>0){
            int warningLimit = Integer.parseInt(conf.getValue(ConfigurationReader.WARNING_LIMIT_HR));
            println("\nHours volume analysis. Limit used is "+warningLimit+" hours:\n",logName);
        }

        println("   Space left:",logName);
        for (String warning: warningLessHours){
            println("       WARNING: " + warning,logName);
        }

        println("",logName);

        println("   Overwork:",logName);
        for (String warning: warningMoreHours){
            println("       WARNING: " + warning,logName);
        }
        println("",logName);
    }

    private static void generateBudgetCardsSummary(List<ClientTimeEntry> swordTimeEntries, String outputFileName) {

        println("Summary for Budget Cards:\n", outputFileName);
        List<String> qtms = getQTMs(swordTimeEntries);
        Collections.sort(qtms);
        qtms.remove(""); // remove horizontal activities

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

    private static void createTimesheets(ConfigurationReader conf, List<ClientTimeEntry> swordTimeEntries, List<String> warningsLess, List<String> warningsMore, String logName) throws IOException {

        int month = Integer.parseInt(conf.getValue(ConfigurationReader.MONTH));
        int year = Integer.parseInt(conf.getValue(ConfigurationReader.YEAR));
        int warningLimit = Integer.parseInt(conf.getValue(ConfigurationReader.WARNING_LIMIT_HR));

        int weekDays = DateUtil.getWeekDaysInMonth(month,year);
        int targetHours = weekDays * 8;

        // Get distinct employees
        println("Writing Timesheets",logName);
        Map<String, List<ClientTimeEntry>> employees = getEmployeesMap(swordTimeEntries);
        int i = 1;
        for (String name : employees.keySet()){
            println("   "+i+"/"+employees.keySet().size()+": "+ name.toUpperCase(),logName);
            Employee employee = new Employee(name);
            for (ClientTimeEntry timeEntry : employees.get(name)){
                employee.addSwordEntry(timeEntry);
            }

            try {
                // Write timesheets
                TimesheetWriter.write(employee);

                checkTimeVsTrgetTime(warningsLess, warningsMore, warningLimit, targetHours, employee);

            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    private static void checkTimeVsTrgetTime(List<String> warningsLess, List<String> warningsMore, int warningLimit, int targetHours, Employee employee) {
        int total = (int)employee.getTotalTime();
        if ((total-targetHours)>warningLimit){
            warningsMore.add(employee.getName() + " worked " + total +" hr, which is "+(total-targetHours)+" MORE than the month target ("+targetHours+")");
        }
        if ((targetHours-total)>warningLimit){
            warningsLess.add(employee.getName() + " worked " + total +" hr, which is "+(targetHours-total)+" LESS than the month target ("+targetHours+")");
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

    private static void println(String text, String filename){
        println(text,filename,true);
    }

    private static void println(String text, String filename, boolean append){

        // Print the text in the standard output
        System.out.println(text);

        if (filename != null){
            try{
                writeToFile(text, filename, append);
            } catch (IOException e) {
                System.out.println("ERROR: Can not write in "+ filename+". Error details:");
                e.printStackTrace();
            }

        }
    }

    public static void writeToFile(String line, String filename, boolean append) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, append));
        writer.append(line);
        writer.append("\n");
        writer.close();
    }
}
