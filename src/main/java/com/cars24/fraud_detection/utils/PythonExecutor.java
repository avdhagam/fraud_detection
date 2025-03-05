package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.exception.PythonExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PythonExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser

    public Map<String, Object> runPythonScript(String scriptName, Object... args) {
        try {
            // Prepare command: python3 scriptName arg1 arg2 ...
            List<String> command = new ArrayList<>();
            command.add("python3");
            command.add(scriptName);

            for (Object arg : args) {
                if (arg instanceof Map) {
                    command.add(objectMapper.writeValueAsString(arg)); // Convert Map to JSON
                } else {
                    command.add(arg.toString());
                }
            }

            log.info("Executing Python script: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Merge stderr with stdout

            Process process = processBuilder.start();

            // Capture output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String scriptOutput = reader.lines().collect(Collectors.joining("\n"));

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("Python script failed with exit code {}: {}", exitCode, scriptOutput);
                throw new PythonExecutionException("Python script execution failed with exit code " + exitCode);
            }

            // Parse the output (Assuming JSON-like structure)
            Map<String, Object> result = new HashMap<>();
            result.put("output", scriptOutput);

            log.info("Python script executed successfully: {}", scriptOutput);
            return result;
        } catch (Exception e) {
            log.error("Error executing Python script: {}", e.getMessage(), e);
            throw new PythonExecutionException("Error executing Python script", e);
        }
    }
}
