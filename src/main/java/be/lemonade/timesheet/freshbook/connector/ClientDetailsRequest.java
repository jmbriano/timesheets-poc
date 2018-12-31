package be.lemonade.timesheet.freshbook.connector;

public class ClientDetailsRequest extends FreshbookEntityRequest {

    public static String buildRequest(String id) {
        return buildRequest("client.get","client_id",id);
    }

}
