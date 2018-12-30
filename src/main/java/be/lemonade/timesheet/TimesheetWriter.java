package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ActivityKey;
import be.lemonade.timesheet.model.Employee;
import be.lemonade.timesheet.util.ConfigurationReader;
import be.lemonade.timesheet.util.DateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimesheetWriter {

    private static int SHEET_ID = 0;

    public static void write(Employee employee) throws IOException, InvalidFormatException {

        ConfigurationReader conf = new ConfigurationReader();
        int month = Integer.parseInt(conf.getValue(ConfigurationReader.MONTH));
        int year = Integer.parseInt(conf.getValue(ConfigurationReader.YEAR));
        int firstRowWithEntries = Integer.parseInt(conf.getValue(ConfigurationReader.FIRST_PROJECT_ROW))-1;
        String format = conf.getValue(ConfigurationReader.HOURS_FORMAT);
        String templateFileName = conf.getValue(ConfigurationReader.TEMPLATE_FILENAME);
        String outputNameTemplate = conf.getValue(ConfigurationReader.OUTPUT_FILENAME);
        String outputDirectory = conf.getValue(ConfigurationReader.OUTPUT_DIR);
        String splitMode = conf.getValue(ConfigurationReader.SPLIT_MODE);

        String employeeName = employee.getName();

        // Open template XLS
        InputStream templateFile = new FileInputStream(templateFileName);
        Workbook wb = WorkbookFactory.create(templateFile);

        Sheet sheet = wb.getSheetAt(SHEET_ID);

        // Write employee name
        writeEmployeeName(sheet, employeeName);
        writeTimesheetDate(sheet, DateUtil.createDate(1,month,year));

        // Get unique Activity list
        List<ActivityKey> allKeys = employee.getAllTimeEntryKeys();

        int rowIndex = firstRowWithEntries;

        // Iterate the list of Activity
        for (ActivityKey key : allKeys) {

            writeRowHeader(sheet, rowIndex,key);
            if ("reality".equalsIgnoreCase(splitMode)) {
                writeTimesPerDay(sheet.getRow(rowIndex), key, month, year, employee, format);
            } else if ("evenly".equalsIgnoreCase(splitMode)){
                    writeTimesEvenly(sheet.getRow(rowIndex), key, month, year, employee, format);
            } else if ("provisional".equalsIgnoreCase(splitMode)){
                boolean expand = "YES".equalsIgnoreCase(conf.getValue(ConfigurationReader.EXPAND));
                writeProvisionalTimes(sheet.getRow(rowIndex), key, month, year, employee, format, expand);
            }else {
                throw new RuntimeException("Invalid SPLIT_MODE found in configuration file: ("+splitMode+"). Options are: [reality|evenly]");
            }
            rowIndex++;
        }

        // Recalculate formulas
        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        // Write output file
        String filename = createOutputFilename(year, month, employeeName, outputNameTemplate);
        System.out.println("  Created file: "+ filename);
        FileOutputStream fileOut = new FileOutputStream(outputDirectory+filename);
        wb.write(fileOut);
        fileOut.close();
    }

    private static String createOutputFilename(int year, int month, String employeeName, String outputNameTemplate) {
        String name = outputNameTemplate;
        name = name.replace("YYYY", String.format("%04d", year));
        name = name.replace("MM", String.format("%02d", month));
        name = name.replace("NNNNN", formatEmployeeNameForFilename(employeeName));
        return name;


    }

    private static String formatEmployeeNameForFilename(String employeeName) {
        String[] namePart = employeeName.split(",");
        String lastname = namePart[0].trim().toUpperCase();
        String name = namePart[1].trim();
        return lastname+"_"+name;

    }

    private static void writeTimesPerDay(Row row, ActivityKey key, int month, int year, Employee employee, String format) {
        int firstDayColumn = CellReference.convertColStringToIndex("G");

        // Iterate each day and ask number of hours
        for (int day = 1; day <= lastDayOfMonth(month, year); day++) {
            Date date = DateUtil.createDate(day, month, year);

            double hours = employee.getTotalTimeForActivityOnDate(key, date);

            // Write value in cell (only if bigger than 0)
            if (hours > 0) {

                int cellNum = firstDayColumn + day - 1;
                writeValueInCell(row, cellNum, hours, format);

            }
        }
    }

    private static void writeTimesEvenly(Row row, ActivityKey key, int month, int year, Employee employee, String format) {
        int firstDayColumn = CellReference.convertColStringToIndex("G");

        double totalTimeForActivity = employee.getTotalTimeForActivity(key);
        int workingDaysInMonth = getWeekDaysInMonth(month,year);

        double hoursPerDay = totalTimeForActivity/workingDaysInMonth;
        Calendar cal = Calendar.getInstance();

        // Iterate each day and ask number of hours
        for (int day = 1; day <= lastDayOfMonth(month, year); day++) {
            Date date = DateUtil.createDate(day, month, year);

            // Write value in cell (only if bigger than 0)
            cal.setTime(date);
            if (hoursPerDay > 0 && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {

                int cellNum = firstDayColumn + day - 1;
                writeValueInCell(row, cellNum, hoursPerDay, format);

            }
        }
    }

    private static void writeProvisionalTimes(Row row, ActivityKey key, int month, int year, Employee employee, String format, boolean expand) {
        int firstDayColumn = CellReference.convertColStringToIndex("G");

        double totalHoursInActivity = employee.getTotalTimeForActivity(key);

        if (totalHoursInActivity<=0)
            return;

        Calendar cal = Calendar.getInstance();

        if (expand){

            double totalHoursForEmployee = employee.getTotalTime();
            int workingDaysInMonth = getWeekDaysInMonth(month,year);
            double targetWorkingHoursInMonth = workingDaysInMonth * 8;

            totalHoursInActivity = totalHoursInActivity * (targetWorkingHoursInMonth/totalHoursForEmployee);

        }

        int day = 1;
        Date date = DateUtil.createDate(day, month, year);
        cal.setTime(date);

        while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            day++;
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        int cellNum = firstDayColumn + day - 1;

        writeValueInCell(row, cellNum, totalHoursInActivity, format);

    }

    private static void writeValueInCell(Row row, int cellNum, double totalHoursInActivity, String format) {
        Cell cell = row.getCell(cellNum);
        if (cell == null)
            cell = row.createCell(cellNum);

        CellStyle style = cell.getCellStyle();
        style.setDataFormat(row.getSheet().getWorkbook().createDataFormat().getFormat(format));

        cell.setCellType(CellType.NUMERIC);
        cell.setCellStyle(style);
        cell.setCellValue(totalHoursInActivity);
    }

    private static void writeRowHeader(Sheet sheet, int rowIndex, ActivityKey key) {
        writeStringInCell(sheet, rowIndex,CellReference.convertColStringToIndex("A"),key.getProject());
        writeStringInCell(sheet, rowIndex,CellReference.convertColStringToIndex("B"),key.getSpecificContract());
        writeStringInCell(sheet, rowIndex,CellReference.convertColStringToIndex("C"),key.getQtm_rfa());
        writeStringInCell(sheet, rowIndex,CellReference.convertColStringToIndex("D"),key.getCI());
        writeStringInCell(sheet, rowIndex,CellReference.convertColStringToIndex("E"),key.getWp());
    }

    private static void writeStringInCell(Sheet sheet, int rowIndex, int colIndex, String text) {
        Row row = sheet.getRow(rowIndex);
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            cell = row.createCell(colIndex);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(text);
    }

    private static void writeTimesheetDate(Sheet sheet, Date date) {

        Row row = sheet.getRow(1);
        Cell cell = row.getCell(CellReference.convertColStringToIndex("E"));
        if (cell == null)
            cell = row.createCell(CellReference.convertColStringToIndex("E"));
        CellStyle cellStyle = cell.getCellStyle();
        CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("mmm-yyyy"));
        cell.setCellStyle(cellStyle);
        cell.setCellValue(date);

    }

    private static void writeEmployeeName(Sheet sheet, String name) {
        Row row = sheet.getRow(1);
        Cell cell = row.getCell(CellReference.convertColStringToIndex("P"));
        if (cell == null)
            cell = row.createCell(CellReference.convertColStringToIndex("P"));
        cell.setCellType(CellType.STRING);
        cell.setCellValue(name);
    }

    private static int lastDayOfMonth(int month, int year) {
        Date firstDay = DateUtil.createDate(1,month,year);
        Date date = DateUtil.getLastDateOfMonth(firstDay);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.DAY_OF_MONTH);

    }

    public static int getWeekDaysInMonth(int month, int year) {

        if (month<1 || month > 12){
            throw new RuntimeException("Invalid month: "+month);
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(DateUtil.createDate(1,month,year));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(DateUtil.createDate(lastDayOfMonth(month,year),month,year));

        int workDays = 0;

        do {
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                workDays++;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (startCal.getTimeInMillis() <= endCal.getTimeInMillis());

        return workDays;
    }
}
