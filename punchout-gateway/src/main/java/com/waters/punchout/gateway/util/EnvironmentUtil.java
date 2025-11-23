package com.waters.punchout.gateway.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class EnvironmentUtil {

    private static final Set<String> ALLOWED_ENVIRONMENTS = new HashSet<>(
            Arrays.asList("dev", "stage", "prod", "s4-dev")
    );

    private static final String DEFAULT_ENVIRONMENT = "dev";

    private EnvironmentUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String normalize(String environment) {
        if (environment == null || environment.trim().isEmpty()) {
            log.warn("Environment is null or empty, defaulting to: {}", DEFAULT_ENVIRONMENT);
            return DEFAULT_ENVIRONMENT;
        }

        String normalized = environment.trim().toLowerCase();

        if (!ALLOWED_ENVIRONMENTS.contains(normalized)) {
            log.warn("Invalid environment value '{}', defaulting to: {}. Allowed values: {}", 
                    environment, DEFAULT_ENVIRONMENT, ALLOWED_ENVIRONMENTS);
            return DEFAULT_ENVIRONMENT;
        }

        return normalized;
    }

    public static boolean isValid(String environment) {
        if (environment == null || environment.trim().isEmpty()) {
            return false;
        }
        return ALLOWED_ENVIRONMENTS.contains(environment.trim().toLowerCase());
    }

    public static Set<String> getAllowedEnvironments() {
        return new HashSet<>(ALLOWED_ENVIRONMENTS);
    }
}
