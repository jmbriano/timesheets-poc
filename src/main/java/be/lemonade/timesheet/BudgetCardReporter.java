package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ActivityKey;
import be.lemonade.timesheet.model.SwordEmployee;
import be.lemonade.timesheet.model.SwordTimeEntry;
import be.lemonade.timesheet.util.ConfigurationReader;
import be.lemonade.timesheet.util.DateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class BudgetCardReporter {


    public static Map<String,Double> qtmTimePerPerson(String qtm, List<SwordTimeEntry> timeEntries) {

        Map<String,Double> timePerPerson = new HashMap<String,Double>();
        for (SwordTimeEntry te: timeEntries){
            if (te.getActivity().getQtm_rfa().equals(qtm)){
                if (timePerPerson.containsKey(te.getEmployee())){
                    timePerPerson.put(te.getEmployee(), timePerPerson.get(te.getEmployee())+te.getTime_hs());
                } else {
                    timePerPerson.put(te.getEmployee(),te.getTime_hs());
                }
            }
        }

        return timePerPerson;
    }

}
