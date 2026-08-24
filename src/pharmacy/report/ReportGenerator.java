package pharmacy.report;

public abstract class ReportGenerator {

    private String reportId;
    private String generatedDate;
    private String title;

    public ReportGenerator(String reportId, String generatedDate, String title) {
        this.reportId = reportId;
        this.generatedDate = generatedDate;
        this.title = title;
    }

    public String getReportId() {
        return reportId;
    }

    public String getGeneratedDate() {
        return generatedDate;
    }

    public String getTitle() {
        return title;
    }

    public abstract String generateReport();
}