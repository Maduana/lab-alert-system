package za.co.lab.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EmailLinkUtil {

    private static final String SECRET = "CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_KEY_2026";
    private static final long DEFAULT_EXPIRY_MS = 24L * 60L * 60L * 1000L; // 24 hours

    public static long generateExpiry() {
        return System.currentTimeMillis() + DEFAULT_EXPIRY_MS;
    }

    public static String sign(int alertId, String action, long expires) {
        try {
            String payload = alertId + "|" + action + "|" + expires;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(keySpec);

            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        } catch (Exception e) {
            throw new RuntimeException("Unable to sign email link", e);
        }
    }

    public static boolean isValid(int alertId, String action, long expires, String signature) {
        if (System.currentTimeMillis() > expires) {
            return false;
        }

        String expected = sign(alertId, action, expires);

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }
}