package be.lemonade.timesheet;

import be.lemonade.timesheet.freshbook.connector.*;
import be.lemonade.timesheet.model.FreshbookTimeEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FreshbookOnlineReader {

    private final String url;
    private final String token;
    private final boolean outputCSV;
    private final String outputFilename;

    Map<String,String> staffCache;
    Map<String,String> projectsCache;
    Map<String,String> projectsClientsCache;
    Map<String,String> tasksCache;
    Map<String,String> clientsCache;

    public FreshbookOnlineReader (String url, String token, boolean outputCSV, String outputFilename){
        this.url = url;
        this.token = token;
        this.outputCSV = outputCSV;
        this.outputFilename = outputFilename;

        this.staffCache = new HashMap<String,String>();
        this.projectsCache = new HashMap<String,String>();
        this.tasksCache = new HashMap<String,String>();
        this.clientsCache = new HashMap<String,String>();
        this.projectsClientsCache = new HashMap<String,String>();

    }


    public List<FreshbookTimeEntry> parseRecords(Date from, Date to) throws IOException, XPathExpressionException, ParseException {


        if (this.outputCSV) {
            // Init the CSV output file
            String line = "Team,Date,Client,Project,Task,Notes,Hours,Billed";
            writeToFile(line, this.outputFilename, false);
        }

        List<FreshbookTimeEntry> records = new ArrayList<FreshbookTimeEntry>();

        Integer pages = 0;

        String response = FreshbookConnector.request(new URL(url),TimeEntriesListRequest.buildRequest(from, to, 1), token);

        Document document = convertToXML(response);

        pages = getNumberOfPages(document);

        final String expression = "/response/time_entries/time_entry";
        XPath xPath =  XPathFactory.newInstance().newXPath();

        for (int currentPage = 1; currentPage <= pages; currentPage++){
            System.out.println("   Loading Freshbooks pages: "+ currentPage +"/"+pages);

            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) node;
                    String staff_id = eElement.getElementsByTagName("staff_id").item(0).getTextContent();
                    String project_id = eElement.getElementsByTagName("project_id").item(0).getTextContent();
                    String task_id = eElement.getElementsByTagName("task_id").item(0).getTextContent();
                    String dateStr = eElement.getElementsByTagName("date").item(0).getTextContent();
                    String hours = eElement.getElementsByTagName("hours").item(0).getTextContent();

                    String staffName = getNameForStaffId(staff_id);
                    String projectName = getNameForProjectId(project_id);
                    String taskName = getNameForTaskId(task_id);
                    String clientName = getNameForClientId(this.projectsClientsCache.get(project_id));

                    //Date
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);

                    SimpleDateFormat csvFormat = new SimpleDateFormat("MM/dd/yy");
                    String dateStrCSV = csvFormat.format(date);

                    if (this.outputCSV) {
                        String line = "\"" + staffName + "\"," + dateStrCSV + ",\"" + clientName + "\"," + "\"" + projectName + "\"," + "\"" + taskName + "\",," + hours + ",";
                        writeToFile(line, this.outputFilename, true);
                    }

                    FreshbookTimeEntry newEntry = new FreshbookTimeEntry(staffName, clientName, projectName, taskName, dateStrCSV, hours);
                    records.add(newEntry);
                }
            }

            if (currentPage < pages) {
                // Load next page
                response = FreshbookConnector.request(new URL(url), TimeEntriesListRequest.buildRequest(from, to, currentPage + 1), token);
                document = convertToXML(response);
            }
        }

        return records;
    }

    private String getNameForStaffId(String staff_id) throws IOException, XPathExpressionException {

        if (this.staffCache.keySet().contains(staff_id))
            return this.staffCache.get(staff_id);

        String response = FreshbookConnector.request(new URL(url), StaffDetailsRequest.buildRequest(staff_id), token);

        Document document = convertToXML(response);

        final String expression = "/response/staff";
        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        Node node = nodeList.item(0);
        Element eElement = (Element) node;

        String first_name = eElement.getElementsByTagName("first_name").item(0).getTextContent();
        String last_name = eElement.getElementsByTagName("last_name").item(0).getTextContent();

        this.staffCache.put(staff_id, last_name+", "+first_name);

        return this.staffCache.get(staff_id);

    }

    private String getNameForProjectId(String project_id) throws IOException, XPathExpressionException {

        if (this.projectsCache.keySet().contains(project_id))
            return this.projectsCache.get(project_id);

        String response = FreshbookConnector.request(new URL(url), ProjectDetailsRequest.buildRequest(project_id), token);

        Document document = convertToXML(response);

        final String expression = "/response/project";
        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        Node node = nodeList.item(0);
        Element eElement = (Element) node;

        String name = eElement.getElementsByTagName("name").item(0).getTextContent();
        String client_id = eElement.getElementsByTagName("client_id").item(0).getTextContent();

        this.projectsCache.put(project_id, name);
        this.projectsClientsCache.put(project_id,client_id);

        return this.projectsCache.get(project_id);

    }

    private String getNameForClientId(String client_id) throws IOException, XPathExpressionException {

        if ("".equalsIgnoreCase(client_id)){
            return "Internal";
        }
        if (this.clientsCache.keySet().contains(client_id))
            return this.clientsCache.get(client_id);

        String response = FreshbookConnector.request(new URL(url), ClientDetailsRequest.buildRequest(client_id), token);

        Document document = convertToXML(response);

        final String expression = "/response/client";
        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        Node node = nodeList.item(0);
        Element eElement = (Element) node;

        String organization = eElement.getElementsByTagName("organization").item(0).getTextContent();

        this.clientsCache.put(client_id, organization);

        return this.clientsCache.get(client_id);

    }

    private String getNameForTaskId(String task_id) throws IOException, XPathExpressionException {

        if (this.tasksCache.keySet().contains(task_id))
            return this.tasksCache.get(task_id);

        String response = FreshbookConnector.request(new URL(url), TaskDetailsRequest.buildRequest(task_id), token);

        Document document = convertToXML(response);

        final String expression = "/response/task";
        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        Node node = nodeList.item(0);
        Element eElement = (Element) node;

        String name = eElement.getElementsByTagName("name").item(0).getTextContent();

        this.tasksCache.put(task_id,name);

        return this.tasksCache.get(task_id);

    }

    private static Integer getNumberOfPages(Document document) throws XPathExpressionException {
        XPath xPath =  XPathFactory.newInstance().newXPath();
        Integer pages;
        String expressionForPage = "/response/time_entries";
        Node root = ((NodeList) xPath.compile(expressionForPage).evaluate(document, XPathConstants.NODESET)).item(0);
        pages = Integer.parseInt(((Element)root).getAttribute("pages"));
        return pages;
    }

    private static Document convertToXML(String response) {

        String xmlString = response;
        Document document = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    public static void writeToFile(String line, String filename, boolean append) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, append));
        writer.append(line);
        writer.append("\n");
        writer.close();
    }

}