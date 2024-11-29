package security;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Getter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ZAPService {
    private static final String BASE_URL = "http://127.0.0.1:8088";
    private static final String API_KEY = System.getenv("zap_api_key");

    @Getter
    private static List<ZapAlert> alerts;

    public static void scanCurrentPage(String pageUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("apikey", API_KEY);
        params.put("url", pageUrl);
        params.put("recurse", "false");

        Response response = RestAssured.given().params(params).get(BASE_URL + "/JSON/ascan/action/scan/");

        if (response.statusCode() == 200) {
            System.out.println("Active scan started successfully for the page: " + pageUrl);
        } else {
            System.err.println("Failed to start active scan: " + response.body().asString());
        }

        getAlerts(pageUrl);
    }

    public static void getAlerts(String targetUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("apikey", API_KEY);
        params.put("url", targetUrl);

        Response response = RestAssured.given().params(params).get(BASE_URL + "/JSON/core/view/alerts/");
        if (response.statusCode() == 200) {
            Gson gson = new Gson();
            ZapAlertsResponse zapAlertsResponse = gson.fromJson(response.body().asString(), ZapAlertsResponse.class);
            alerts = zapAlertsResponse.getAlerts();
        } else {
            System.err.println("Failed to retrieve alerts: " + response.body().asString());
        }
    }

    public static void generateHtmlReport(String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>ZAP Scan Report</title></head><body>");
            html.append("<h1>ZAP Scan Report</h1>");
            html.append("<h2>Summary</h2>");

            Map<String, Long> summary = alerts.stream()
                    .collect(Collectors.groupingBy(ZapAlert::getRisk, Collectors.counting()));

            html.append("<table border='1'><tr><th>Risk Level</th><th>Count</th></tr>");
            for (Map.Entry<String, Long> entry : summary.entrySet()) {
                html.append(String.format("<tr><td>%s</td><td>%d</td></tr>", entry.getKey(), entry.getValue()));
            }
            html.append("</table>");

            html.append("<h2>Detailed Findings</h2><table border='1'>");
            html.append("<tr><th>Alert</th><th>Risk</th><th>URL</th><th>Description</th><th>Solution</th></tr>");
            for (ZapAlert alert : alerts) {
                html.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                        alert.getAlert(), alert.getRisk(), alert.getUrl(), alert.getDescription(), alert.getSolution()));
            }
            html.append("</table></body></html>");
            writer.write(html.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Assertions
    public static void assertAlertsArePresent() {
        if (alerts == null || alerts.isEmpty()) {
            throw new AssertionError("No alerts found.");
        }
    }

    public static void assertNoHighRiskAlerts() {
        List<ZapAlert> highRiskAlerts = alerts.stream()
                .filter(a -> "High".equalsIgnoreCase(a.getRisk()))
                .collect(Collectors.toList());
        if (!highRiskAlerts.isEmpty()) {
            throw new AssertionError("High-risk alerts found: " + highRiskAlerts);
        }
    }

    public static void assertAlertsBelowRiskLevel(String maxRiskLevel) {
        List<String> riskLevels = Arrays.asList("Informational", "Low", "Medium", "High");
        int maxRiskIndex = riskLevels.indexOf(maxRiskLevel);

        if (maxRiskIndex == -1) {
            throw new IllegalArgumentException("Invalid risk level: " + maxRiskLevel);
        }

        List<ZapAlert> invalidAlerts = alerts.stream()
                .filter(a -> riskLevels.indexOf(a.getRisk()) > maxRiskIndex)
                .collect(Collectors.toList());

        if (!invalidAlerts.isEmpty()) {
            String alertNames = invalidAlerts.stream().map(ZapAlert::getAlert).collect(Collectors.joining(", "));
            throw new AssertionError("Alerts with risk level above " + maxRiskLevel + " found: " + alertNames);
        }
    }

    public static void assertAllAlertsHaveSolutions() {
        List<ZapAlert> alertsWithoutSolutions = alerts.stream()
                .filter(a -> a.getSolution() == null || a.getSolution().isEmpty())
                .collect(Collectors.toList());
        if (!alertsWithoutSolutions.isEmpty()) {
            throw new AssertionError("Alerts without solutions: " + alertsWithoutSolutions.size());
        }
    }
}
