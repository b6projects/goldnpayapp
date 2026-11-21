package com.gps.cs.config;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/** Prints a short, readable startup failure instead of a stack trace. */
public class MissingConfigFailureAnalyzer extends AbstractFailureAnalyzer<MissingConfigException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MissingConfigException cause) {
        return new FailureAnalysis(
                "GoldNPay Core cannot start. Required configuration is missing:\n  - "
                        + String.join("\n  - ", cause.getProblems()),
                "Provide these as environment variables (secrets go in the files named by "
                        + "DB_SECRETS_PATH and APP_SECRETS_PATH, never in application.yml).",
                cause);
    }
}
