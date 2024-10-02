import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class OAuth2Client {

    private String accessToken;
    private String refreshToken;
    private long tokenExpirationTime;

    private final String clientId = "your-client-id";
    private final String clientSecret = "your-client-secret";
    private final String tokenUri = "https://oauth2-server.com/token";
    private final String resourceUrl = "https://api.protected-resource.com/data";
    private final RestTemplate restTemplate = new RestTemplate();

    // Method to obtain a new access token using client_credentials grant type
    public void obtainAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            accessToken = (String) responseBody.get("access_token");
            refreshToken = (String) responseBody.get("refresh_token");
            int expiresIn = (Integer) responseBody.get("expires_in");
            tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L);
        }
    }

    // Method to refresh access token
    public void refreshAccessToken() {
        if (refreshToken == null) {
            throw new IllegalStateException("No refresh token available");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", refreshToken);
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            accessToken = (String) responseBody.get("access_token");
            int expiresIn = (Integer) responseBody.get("expires_in");
            tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L);
        }
    }

    // Method to make an authenticated request using the access token
    public String getProtectedResource() {
        if (System.currentTimeMillis() > tokenExpirationTime) {
            refreshAccessToken();  // Refresh token if it has expired
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                resourceUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        return response.getBody();
    }

    public static void main(String[] args) {
        OAuth2Client client = new OAuth2Client();
        client.obtainAccessToken();  // First obtain an access token
        String resource = client.getProtectedResource();  // Make the request with the token
        System.out.println(resource);
    }
}
