package be.lemonade.timesheet.freshbook.connector;

public class TaskDetailsRequest extends FreshbookEntityRequest {

    public static String buildRequest(String id) {

        return buildRequest("task.get","task_id",id);
    }

}
