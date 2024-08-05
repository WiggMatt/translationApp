import org.junit.jupiter.api.Test;
import ru.matthew.auth.TokenResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenResponseTest {

    @Test
    public void testGettersAndSetters() {
        TokenResponse tokenResponse = new TokenResponse();
        String iamToken = "testIamToken";
        String expiresAt = "2024-08-04T23:21:19.721+03:00";

        tokenResponse.setIamToken(iamToken);
        tokenResponse.setExpiresAt(expiresAt);

        assertEquals(iamToken, tokenResponse.getIamToken());
        assertEquals(expiresAt, tokenResponse.getExpiresAt());
    }
}
