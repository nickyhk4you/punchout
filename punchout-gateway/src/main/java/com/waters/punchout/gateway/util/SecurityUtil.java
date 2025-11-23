package com.waters.punchout.gateway.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecurityUtil {
    
    private static final String MASKED_VALUE = "***REDACTED***";
    
    private static final Pattern[] SENSITIVE_JSON_PATTERNS = new Pattern[]{
        Pattern.compile("(\"password\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"token\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"access_token\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"refresh_token\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"api_key\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"apikey\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"secret\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"auth\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"authorization\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"credential\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\"email\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE)
    };
    
    private static final Pattern[] SENSITIVE_XML_PATTERNS = new Pattern[]{
        Pattern.compile("(<password>)([^<]*)(</password>)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<token>)([^<]*)(</token>)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<accessToken>)([^<]*)(</accessToken>)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<apiKey>)([^<]*)(</apiKey>)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<secret>)([^<]*)(</secret>)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<authorization>)([^<]*)(</authorization>)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<credential>)([^<]*)(</credential>)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<email>)([^<]*)(</email>)", Pattern.CASE_INSENSITIVE)
    };
    
    private static final Pattern COOKIE_PATTERN = Pattern.compile(
        "(wuser_key=)([^;\\s]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    public static String maskSecrets(String body) {
        if (body == null || body.trim().isEmpty()) {
            return body;
        }
        
        String masked = body;
        
        for (Pattern pattern : SENSITIVE_JSON_PATTERNS) {
            Matcher matcher = pattern.matcher(masked);
            masked = matcher.replaceAll("$1" + MASKED_VALUE + "$3");
        }
        
        for (Pattern pattern : SENSITIVE_XML_PATTERNS) {
            Matcher matcher = pattern.matcher(masked);
            masked = matcher.replaceAll("$1" + MASKED_VALUE + "$3");
        }
        
        Matcher cookieMatcher = COOKIE_PATTERN.matcher(masked);
        masked = cookieMatcher.replaceAll("$1" + MASKED_VALUE);
        
        return masked;
    }
    
    public static Map<String, String> maskHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return headers;
        }
        
        Map<String, String> maskedHeaders = new HashMap<>(headers);
        
        for (Map.Entry<String, String> entry : maskedHeaders.entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                String lowerKey = key.toLowerCase();
                if (lowerKey.equals("authorization") || 
                    lowerKey.equals("set-cookie") || 
                    lowerKey.equals("cookie") ||
                    lowerKey.equals("x-api-key") ||
                    lowerKey.equals("x-auth-token")) {
                    maskedHeaders.put(key, MASKED_VALUE);
                }
            }
        }
        
        return maskedHeaders;
    }
    
    public static String getMaskedValue() {
        return MASKED_VALUE;
    }
}
