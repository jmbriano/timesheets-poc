package be.lemonade.timesheet.freshbook.connector;

import org.w3c.dom.Document;
import sun.misc.BASE64Encoder;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class FreshbookConnector {

    public static String request(URL url, String action, String token) throws IOException {

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        // Add headers
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "TimesheetTool");
        connection.setRequestProperty("Content-Type", "text/plain; charset=\"utf8\"");

        BASE64Encoder encoder = new BASE64Encoder();
        String encoded = encoder.encode((token).getBytes("UTF-8"));
        connection.setRequestProperty("Authorization", "Basic " + encoded);

        // Send data
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(action.getBytes());
        outputStream.flush();
        outputStream.close();

        // Read response
        int returnCode = connection.getResponseCode();
        InputStream connectionIn = null;

        if (returnCode==200)
            connectionIn = connection.getInputStream();
        else
            connectionIn = connection.getErrorStream();

        BufferedReader buffer = new BufferedReader(new InputStreamReader(connectionIn));
        StringBuilder response = new StringBuilder();

        String inputLine;
        while ((inputLine = buffer.readLine()) != null)
            response.append(inputLine+"\n");
        buffer.close();

        connection.getInputStream().close();

        return response.toString();
    }

}
