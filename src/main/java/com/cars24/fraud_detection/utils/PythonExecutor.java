package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.exception.PythonExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PythonExecutor {

    private static final String PYTHON_SCRIPT_FOLDER = System.getProperty("user.dir") + "\\python_workflows\\";

    public String runPythonScript(String scriptName, String... args) {
        try {
            // Construct full script path
            String scriptPath = Paths.get(PYTHON_SCRIPT_FOLDER, scriptName).toString();

            // Validate script existence
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists()) {
                throw new PythonExecutionException("Python script not found: " + scriptPath);
            }

            // Prepare command: python3 scriptPath arg1 arg2 ...
            List<String> command = new ArrayList<>();
            command.add("python3"); // Ensure correct Python version
            command.add(scriptPath);

            for (String arg : args) {
                command.add(arg);
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
                // Capture error stream
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorOutput = errorReader.lines().collect(Collectors.joining("\n"));
                log.error("Python script failed with exit code {}: {}", exitCode, errorOutput);
                throw new PythonExecutionException("Python script execution failed with exit code " + exitCode + ": " + errorOutput);
            }

            log.info("Python script executed successfully: {}", scriptOutput);
            return scriptOutput;

        } catch (Exception e) {
            log.error("Error executing Python script: {}", e.getMessage(), e);
            throw new PythonExecutionException("Error executing Python script", e);
        }
    }
}