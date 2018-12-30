package be.lemonade.timesheet.freshbook.connector;

public class StaffDetailsRequest extends FreshbookEntityRequest {

    public static String buildRequest(String id) {

        return buildRequest("staff.get","staff_id",id);
    }

}
