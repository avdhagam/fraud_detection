package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.exception.PythonExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PythonExecutor3 {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> runPythonScript(String scriptName, Object... args) {
        try {
            String pythonCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";
            List<String> command = new ArrayList<>();
            command.add(pythonCommand);
            command.add(scriptName);

            for (Object arg : args) {
                command.add(arg.toString());
            }

            log.info("Executing Python script: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String scriptOutput = reader.lines().collect(Collectors.joining("\n"));

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Python script failed with exit code {}: {}", exitCode, scriptOutput);
                throw new PythonExecutionException("Python script execution failed with exit code " + exitCode);
            }

            // Handle null or empty output gracefully
            if (scriptOutput == null || scriptOutput.trim().isEmpty()) {
                log.warn("Python script returned empty or null output.");
                Map<String, Object> result = new HashMap<>();
                result.put("output", ""); // Store an empty string as output
                return result;
            }

            // Attempt to parse the output as JSON
            try {
                Map<String, Object> result = objectMapper.readValue(scriptOutput, Map.class);
                log.info("Python script executed successfully with parsed JSON result: {}", result);
                return result;
            } catch (Exception jsonEx) {
                log.warn("Failed to parse Python output as JSON. Raw output:\n{}", scriptOutput);
                Map<String, Object> result = new HashMap<>();
                result.put("output", scriptOutput);
                result.put("parse_error", "Could not parse output as JSON");
                return result;
            }

        } catch (Exception e) {
            log.error("Error executing Python script: {}", e.getMessage(), e);
            throw new PythonExecutionException("Error executing Python script", e);
        }
    }
}