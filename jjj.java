import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;

public class OAuth2TokenManager {

    private static final String TOKEN_URL = "https://oauth2-server.com/token";
    private static final String CLIENT_ID = "your-client-id";
    private static final String CLIENT_SECRET = "your-client-secret";

    private String accessToken;
    private String refreshToken;
    private long tokenExpiryTime;

    private OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

    /**
     * Request an access token using client credentials and store the refresh token.
     */
    public void requestAccessToken() throws Exception {
        OAuthClientRequest request = OAuthClientRequest
            .tokenLocation(TOKEN_URL)
            .setGrantType(GrantType.CLIENT_CREDENTIALS)
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .buildBodyMessage();

        OAuthAccessTokenResponse response = oAuthClient.accessToken(request);

        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();  // Store refresh token
        this.tokenExpiryTime = System.currentTimeMillis() + (response.getExpiresIn() * 1000);
    }

    /**
     * Refresh the access token using the stored refresh token.
     */
    public void refreshAccessToken() throws Exception {
        if (this.refreshToken == null) {
            throw new IllegalStateException("No refresh token available.");
        }

        OAuthClientRequest refreshTokenRequest = OAuthClientRequest
            .tokenLocation(TOKEN_URL)
            .setGrantType(GrantType.REFRESH_TOKEN)
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .setRefreshToken(this.refreshToken)
            .buildBodyMessage();

        OAuthAccessTokenResponse response = oAuthClient.accessToken(refreshTokenRequest);

        this.accessToken = response.getAccessToken();
        this.tokenExpiryTime = System.currentTimeMillis() + (response.getExpiresIn() * 1000);
    }

    /**
     * Get the current access token, refreshing it if necessary.
     */
    public String getAccessToken() throws Exception {
        if (System.currentTimeMillis() > this.tokenExpiryTime - 60000) {
            // Token is about to expire, refresh it
            refreshAccessToken();
        }
        return this.accessToken;
    }

    /**
     * Example method to demonstrate usage.
     */
    public String getProtectedResource() throws Exception {
        String token = getAccessToken();

        // Now you can use the token to access a protected resource (e.g., via HttpURLConnection)
        // Use the token in the Authorization header of your request.
        return token;  // This is just a placeholder
    }

    public static void main(String[] args) throws Exception {
        OAuth2TokenManager manager = new OAuth2TokenManager();
        manager.requestAccessToken();

        // Use the access token in subsequent requests
        String protectedResource = manager.getProtectedResource();
        System.out.println(protectedResource);
    }
}
