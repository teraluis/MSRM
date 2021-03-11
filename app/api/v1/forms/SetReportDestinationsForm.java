package api.v1.forms;

import java.util.List;

public class SetReportDestinationsForm {

    public SetReportDestinationsForm() {
    }

    public List<ReportDestinationForm> getReportDestinations() {
        return reportDestinations;
    }

    public void setReportDestinations(List<ReportDestinationForm> reportDestinations) {
        this.reportDestinations = reportDestinations;
    }

    protected List<ReportDestinationForm> reportDestinations;
}
