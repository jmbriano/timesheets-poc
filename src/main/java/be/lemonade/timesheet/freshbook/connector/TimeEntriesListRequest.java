package be.lemonade.timesheet.freshbook.connector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeEntriesListRequest {

    public static String buildRequest(Date from, Date to, Integer page) {

        StringBuilder requestXML = new StringBuilder();
        requestXML.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        requestXML.append("<request method=\"time_entry.list\">");
        requestXML.append("<page>"+page+"</page>");
        requestXML.append("<per_page>50</per_page>");
        if (from != null){
            requestXML.append("<date_from>"+formatDate(from)+"</date_from>");
        }
        if (to != null){
            requestXML.append("<date_to>"+formatDate(to)+"</date_to>");
        }
        requestXML.append("</request>");

        return requestXML.toString();
    }

    private static String formatDate(Date from) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(from);
    }



}
