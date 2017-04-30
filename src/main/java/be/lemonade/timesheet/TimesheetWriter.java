package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ActivityKey;
import be.lemonade.timesheet.model.SwordEmployee;
import com.sun.tools.javac.util.Assert;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.*;
import java.util.*;

public class TimesheetWriter {

    public static void write(SwordEmployee employee) throws IOException, InvalidFormatException {

        String employeeName = employee.getName();

        // Open template XLS
        InputStream inp = new FileInputStream("ts-in.xlsx");
        Workbook wb = WorkbookFactory.create(inp);

        // Write employee name
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(2);
        Cell cell = row.getCell(CellReference.convertColStringToIndex("P"));
        if (cell == null)
            cell = row.createCell(CellReference.convertColStringToIndex("P"));
        cell.setCellType(CellType.STRING);
        cell.setCellValue(employeeName);

        // Get unique Activity list
        List<ActivityKey> allKeys = employee.getAllTimeEntryKeys();

        // Iterate the list of Activity
        for (ActivityKey key : allKeys) {

            // Iterate each day and ask number of hours
            for (int i = 1; i <= lastDayOfMonth(4, 2017); i++) {
                Date date = null;
                double hours = employee.getTotalTimeForActivityOnDate(key, date);

                // Write value in cell (only if bigger than 0)
                if (hours < 0) {
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(hours);
                } else {
                    cell.setCellType(CellType.BLANK);
                }
            }
        }

        // Recalculate formulas
        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        // Write output file
        FileOutputStream fileOut = new FileOutputStream("Timesheet " + employeeName);
        wb.write(fileOut);
        fileOut.close();
    }

    private static int lastDayOfMonth(int month, int year) {
        int days = 0;

        if (month == 0) {
            System.out.println("Please enter a valid month number");
        } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            days = 31;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            days = 30;
        } else {
            if ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0))) {
                days = 29;
            } else {
                days = 28;
            }
            // I thought of maybe creating an array of int
            // and then checking if month was contained within the array, it's shorter than all those ORs
        }
        return days;
    }
}
