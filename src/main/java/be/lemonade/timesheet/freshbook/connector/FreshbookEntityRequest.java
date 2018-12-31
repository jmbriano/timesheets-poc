package be.lemonade.timesheet.freshbook.connector;

public class FreshbookEntityRequest {

    public static String buildRequest(String action, String node, String id) {

        StringBuilder requestXML = new StringBuilder();
        requestXML.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        requestXML.append("<request method=\""+action+"\">");
        requestXML.append("<"+node+">"+id+"</"+node+">");
        requestXML.append("</request>");

        return requestXML.toString();
    }

}
