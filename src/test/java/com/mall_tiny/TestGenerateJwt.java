package com.mall_tiny;

import com.google.gson.Gson;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

public class TestGenerateJwt {

    private static final String SECRET = "mall_admin_secret"; // 你的密钥
    private static final String ALGORITHM = "HmacSHA512";

    public static void main(String[] args) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", "test");

        claims.put("created", 1732860097871L);
//        Long exp = new Date(System.currentTimeMillis() + 604800 * 1000).getTime()/1000;
        claims.put("exp", 1733464897L);

        String jwt = generateJwt(claims);
        System.out.println("Generated JWT: " + jwt);

        try {
            boolean isValid = validateJwt(jwt);
            System.out.println("Is JWT valid? " + isValid);
        } catch (SignatureException e) {
            System.out.println("JWT validation failed: " + e.getMessage());
        }
    }

    public static long getCurrentTime() {
        return new Date().getTime() / 1000; // 返回当前时间的秒数
    }

    public static String generateJwt(Map<String, Object> claims) {
        String header = createHeader();

        String payload = createPayload(claims);
        String signature = createSignature(header, payload);

        return header + "." + payload + "." + signature;
    }

    public static boolean validateJwt(String jwt) throws SignatureException {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String header = parts[0];
        String payload = parts[1];
        String signature = parts[2];

        String expectedSignature = createSignature(header, payload);

        return expectedSignature.equals(signature);
    }

    public static String createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS512");
//        header.put("typ", "JWT");

        System.out.println(new Gson().toJson(header));
        System.out.println(new String(new Gson().toJson(header).getBytes()));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(new Gson().toJson(header).getBytes()));
        // {"typ":"JWT","alg":"HS512"}
        // {"typ":"JWT","alg":"HS512"}
        // eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9

        // {"alg":"HS512"}
        // {"alg":"HS512"}
        // eyJhbGciOiJIUzUxMiJ9

        return Base64.getUrlEncoder().withoutPadding().encodeToString(new Gson().toJson(header).getBytes());
    }

    private static String createPayload(Map<String, Object> claims) {
        System.out.println(new Gson().toJson(claims));
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(new Gson().toJson(claims).getBytes());
        System.out.println("payload:" + payload);
        return payload;
    }

    private static String createSignature(String header, String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
//            String base64DecodedSecret = new String(Base64.getDecoder().decode(SECRET));
//            System.out.println(base64DecodedSecret);
//            SecretKeySpec secretKeySpec = new SecretKeySpec(base64DecodedSecret.getBytes(), ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET.getBytes(), ALGORITHM);
            mac.init(secretKeySpec);

            byte[] bytesToSign = (header + "." + payload).getBytes();
            byte[] signatureBytes = mac.doFinal(bytesToSign);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWT signature", e);
        }
    }
}