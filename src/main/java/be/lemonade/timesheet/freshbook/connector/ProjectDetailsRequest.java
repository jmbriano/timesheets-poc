package be.lemonade.timesheet.freshbook.connector;

public class ProjectDetailsRequest extends FreshbookEntityRequest {

    public static String buildRequest(String id) {
        return buildRequest("project.get","project_id",id);
    }

}
