package be.lemonade.timesheet.model.exceptions;

public class MissingMapperEntryException extends RuntimeException {

    public MissingMapperEntryException(String stringError) {
        super(stringError);
    }
}
