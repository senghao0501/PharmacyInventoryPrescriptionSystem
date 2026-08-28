package pharmacy.report;

import java.util.Date;

public abstract class ReportGenerator {
    private String reportId;
    private Date generatedDate;
    private String title;

    public ReportGenerator(String reportId, String title) {
        this.reportId = reportId;
        this.title = title;
        this.generatedDate = new Date();
    }

    public abstract String generate();

    public String getReportId() { return reportId; }
    public Date getGeneratedDate() { return generatedDate; }
    public String getTitle() { return title; }
}