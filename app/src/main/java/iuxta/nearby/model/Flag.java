package iuxta.nearby.model;

/**
 * Created by kelseykerr on 5/16/17.
 */
public class Flag {
    private String reporterNotes;

    public Flag() {

    }

    public Flag(String notes) {
        this.reporterNotes = notes;
    }

    public String getReporterNotes() {
        return reporterNotes;
    }

    public void setReporterNotes(String reporterNotes) {
        this.reporterNotes = reporterNotes;
    }
}
