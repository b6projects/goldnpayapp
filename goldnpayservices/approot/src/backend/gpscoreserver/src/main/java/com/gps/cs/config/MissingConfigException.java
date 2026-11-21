package com.gps.cs.config;

import java.util.List;

public class MissingConfigException extends RuntimeException {
    private final List<String> problems;

    public MissingConfigException(List<String> problems) {
        super("Missing required configuration: " + String.join(", ", problems));
        this.problems = problems;
    }

    public List<String> getProblems() {
        return problems;
    }
}
