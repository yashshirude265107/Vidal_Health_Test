package com.example.webhooksql.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.webhooksql.model.FinalQueryRequest;
import com.example.webhooksql.model.WebhookRequest;
import com.example.webhooksql.model.WebhookResponse;





@Service
public class WebhookService {

    private static final String GENERATE_WEBHOOK_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    private static final String SUBMIT_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    public void startProcess() {

        RestTemplate restTemplate = new RestTemplate();

     
        WebhookRequest request = new WebhookRequest(
                "John Doe",
                "REG12347",
                "john@example.com"
        );

        ResponseEntity<WebhookResponse> response =
                restTemplate.postForEntity(
                        GENERATE_WEBHOOK_URL,
                        request,
                        WebhookResponse.class
                );

        WebhookResponse webhookResponse = response.getBody();

        if (webhookResponse == null) {
            System.out.println("Webhook generation failed");
            return;
        }

        String webhookUrl = webhookResponse.getWebhook();
        String token = webhookResponse.getAccessToken();

        System.out.println("Webhook URL received");
        System.out.println("Access token received");


        String finalSqlQuery = solveSql();

  
        submitFinalQuery(webhookUrl, token, finalSqlQuery);
    }


    private String solveSql() {

        return "SELECT d.DEPARTMENT_NAME AS DEPARTMENT_NAME, " +
               "SUM(p.AMOUNT) AS SALARY, " +
               "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, " +
               "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE " +
               "FROM DEPARTMENT d " +
               "JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT " +
               "JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
               "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
               "GROUP BY d.DEPARTMENT_NAME, e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB " +
               "HAVING SUM(p.AMOUNT) = ( " +
               "SELECT MAX(total_salary) FROM ( " +
               "SELECT e2.EMP_ID, SUM(p2.AMOUNT) AS total_salary " +
               "FROM EMPLOYEE e2 " +
               "JOIN PAYMENTS p2 ON e2.EMP_ID = p2.EMP_ID " +
               "WHERE e2.DEPARTMENT = d.DEPARTMENT_ID " +
               "AND DAY(p2.PAYMENT_TIME) <> 1 " +
               "GROUP BY e2.EMP_ID ) temp )";
    }


   
    private void submitFinalQuery(String webhookUrl, String token, String finalQuery) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        FinalQueryRequest body = new FinalQueryRequest(finalQuery);

        HttpEntity<FinalQueryRequest> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        webhookUrl,
                        entity,
                        String.class
                );

        System.out.println("Submission response status: " + response.getStatusCode());
    }
}
