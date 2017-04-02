import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nemo on 3/31/17.
 */
public class TimesheetWriter {

    public static void main(String[] args) throws IOException, InvalidFormatException {
        TimesheetWriter t = new TimesheetWriter();
        t.writeSomething();
    }

    public void writeSomething() throws IOException, InvalidFormatException {

        InputStream inp = new FileInputStream("ts-in.xlsx");

        Workbook wb = WorkbookFactory.create(inp);
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(5);
        Cell cell = row.getCell(CellReference.convertColStringToIndex("B"));
        if (cell == null)
            cell = row.createCell(CellReference.convertColStringToIndex("B"));
        cell.setCellType(CellType.STRING);
        cell.setCellValue("a test");

        Row row2 = sheet.getRow(5);
        Cell cell2 = row2.getCell(CellReference.convertColStringToIndex("G"));
        if (cell2 == null)
            cell2 = row.createCell(CellReference.convertColStringToIndex("G"));

        cell2.setCellValue(2.33);

        Row row3 = sheet.getRow(16);

        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        Cell cell3 = row3.getCell(CellReference.convertColStringToIndex("G"));
        System.out.println("cell3 = " + cell3.getNumericCellValue());




        XSSFCellStyle style=(XSSFCellStyle) wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("0.0"));

        cell2.setCellType(CellType.NUMERIC);
        cell2.setCellStyle(style);



        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("ts-out.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }



}
