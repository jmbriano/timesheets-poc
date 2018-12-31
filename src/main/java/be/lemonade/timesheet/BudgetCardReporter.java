package be.lemonade.timesheet;

import be.lemonade.timesheet.model.ClientTimeEntry;

import java.util.*;

public class BudgetCardReporter {


    public static Map<String,Double> qtmTimePerPerson(String qtm, List<ClientTimeEntry> timeEntries) {

        Map<String,Double> timePerPerson = new HashMap<String,Double>();
        for (ClientTimeEntry te: timeEntries){
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
