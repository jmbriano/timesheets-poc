package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ActivityKey;
import be.lemonade.timesheet.model.ClientTimeEntry;
import be.lemonade.timesheet.model.SwordEmployee;
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

    public static void main(String[] args) throws IOException, InvalidFormatException {
        SwordEmployee employee = new SwordEmployee("Test user");
        employee.addSwordEntry(new ClientTimeEntry("Test user", "project1", "sc1", "qtm1", "ci1", "wp1", "task1", DateUtil.createDate(1,4,2017),5));
        employee.addSwordEntry(new ClientTimeEntry("Test user", "project1", "sc1", "qtm1", "ci1", "wp1", "task1", DateUtil.createDate(2,4,2017),6));
        employee.addSwordEntry(new ClientTimeEntry("Test user", "project1", "sc1", "qtm1", "ci1", "wp1", "task1", DateUtil.createDate(3,4,2017),7));
        employee.addSwordEntry(new ClientTimeEntry("Test user", "project1", "sc1", "qtm1", "ci1", "wp2", "task1", DateUtil.createDate(4,4,2017),8));
        employee.addSwordEntry(new ClientTimeEntry("Test user", "project1", "sc1", "qtm1", "ci1", "wp2", "task1", DateUtil.createDate(5,4,2017),9));
        employee.addSwordEntry(new ClientTimeEntry("Test user", "project1", "sc1", "qtm1", "ci1", "wp2", "task1", DateUtil.createDate(1,4,2017),2));
        employee.addSwordEntry(new ClientTimeEntry("Test user", "project1", "sc1", "qtm2", "ci1", "wp1", "task1", DateUtil.createDate(1,4,2017),1));

        write(employee);

        SwordEmployee employee2 = new SwordEmployee("Test user2");
        employee2.addSwordEntry(new ClientTimeEntry("Test user2", "project1", "sc1", "qtm1", "ci1", "wp1", "task1", DateUtil.createDate(1,4,2017),5));
        employee2.addSwordEntry(new ClientTimeEntry("Test user2", "project1", "sc1", "qtm1", "ci1", "wp1", "task1", DateUtil.createDate(2,4,2017),6));
        employee2.addSwordEntry(new ClientTimeEntry("Test user2", "project1", "sc1", "qtm1", "ci1", "wp1", "task1", DateUtil.createDate(3,4,2017),7));
        employee2.addSwordEntry(new ClientTimeEntry("Test user2", "project1", "sc1", "qtm1", "ci1", "wp2", "task1", DateUtil.createDate(4,4,2017),8));
        employee2.addSwordEntry(new ClientTimeEntry("Test user2", "project1", "sc1", "qtm1", "ci1", "wp2", "task1", DateUtil.createDate(5,4,2017),9));
        employee2.addSwordEntry(new ClientTimeEntry("Test user2", "project1", "sc1", "qtm1", "ci1", "wp2", "task1", DateUtil.createDate(1,4,2017),2));
        employee2.addSwordEntry(new ClientTimeEntry("Test user2", "project1", "sc1", "qtm2", "ci1", "wp1", "task1", DateUtil.createDate(1,4,2017),1));

        write(employee2);

    }

    public static void write(SwordEmployee employee) throws IOException, InvalidFormatException {

        ConfigurationReader conf = new ConfigurationReader();
        int month = Integer.parseInt(conf.getValue(ConfigurationReader.MONTH));
        int year = Integer.parseInt(conf.getValue(ConfigurationReader.YEAR));
        int firstRowWithEntries = Integer.parseInt(conf.getValue(ConfigurationReader.FIRST_PROJECT_ROW))-1;
        String format = conf.getValue(ConfigurationReader.HOURS_FORMAT);
        String templateFileName = conf.getValue(ConfigurationReader.TEMPLATE_FILENAME);
        String outputNameTemplate = conf.getValue(ConfigurationReader.OUTPUT_FILENAME);

        String employeeName = employee.getName();

        // Open template XLS
        InputStream inp = new FileInputStream(templateFileName);
        Workbook wb = WorkbookFactory.create(inp);

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
            writeTimes(sheet.getRow(rowIndex),key,month,year,employee, format);

            rowIndex++;
        }

        // Recalculate formulas
        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        // Write output file
        String filename = createOutputFilename(year, month, employeeName, outputNameTemplate);
        System.out.println("  Created file: "+ filename);
        FileOutputStream fileOut = new FileOutputStream(filename);
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

    private static void writeTimes(Row row, ActivityKey key, int month, int year, SwordEmployee employee, String format) {
        int firstDayColumn = CellReference.convertColStringToIndex("G");

        // Iterate each day and ask number of hours
        for (int day = 1; day <= lastDayOfMonth(month, year); day++) {
            Date date = DateUtil.createDate(day, month, year);

            double hours = employee.getTotalTimeForActivityOnDate(key, date);

            // Write value in cell (only if bigger than 0)
            if (hours > 0) {

                Cell cell = row.getCell(firstDayColumn + day - 1);
                if (cell == null)
                    cell = row.createCell(firstDayColumn + day - 1);

                CellStyle style = cell.getCellStyle();
                style.setDataFormat(row.getSheet().getWorkbook().createDataFormat().getFormat(format));

                cell.setCellType(CellType.NUMERIC);
                cell.setCellStyle(style);
                cell.setCellValue(hours);

            }
        }
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
}
