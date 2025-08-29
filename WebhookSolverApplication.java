package Jar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;

@SpringBootApplication
public class WebhookSolverApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(WebhookSolverApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Send POST request to generateWebhook
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        JSONObject body = new JSONObject();
        body.put("name", "ramtrinadh");
        body.put("regNo", "22bce20368");
        body.put("email", "ramkapalavyi@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Webhook Response: " + response.getBody());

        JSONObject respJson = new JSONObject(response.getBody());
        String webhookUrl = respJson.getString("webhook");
        String accessToken = respJson.getString("accessToken");

        // 2. Pick SQL question based on regNo last digit
        String regNo = "22bce20368";
        String sqlQuery;

        int lastDigit = Character.getNumericValue(regNo.charAt(regNo.length() - 1));
        if (lastDigit % 2 == 1) {
            // Odd → Question 1
            sqlQuery = "SELECT DEPARTMENT, COUNT(*) AS EMPLOYEE_COUNT " +
                       "FROM EMPLOYEE " +
                       "GROUP BY DEPARTMENT " +
                       "ORDER BY EMPLOYEE_COUNT DESC " +
                       "LIMIT 1;";
        } else {
            // Even → Question 2
            sqlQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                       "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                       "FROM EMPLOYEE e1 " +
                       "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                       "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
                       "AND e2.DOB > e1.DOB " +
                       "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                       "ORDER BY e1.EMP_ID DESC;";
        }

        // 3. Submit final query
        JSONObject finalBody = new JSONObject();
        finalBody.put("finalQuery", sqlQuery);

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(accessToken);

        HttpEntity<String> finalEntity = new HttpEntity<>(finalBody.toString(), authHeaders);
        ResponseEntity<String> finalResp = restTemplate.postForEntity(webhookUrl, finalEntity, String.class);

        System.out.println("Final submission response: " + finalResp.getBody());
    }
}
