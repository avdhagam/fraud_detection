package com.cars24.fraud_detection.utils;

@Component
public class PythonExecutor {

    public String runScript(String scriptName, String inputData) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "src/main/resources/python_workflows/" + scriptName, inputData);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error running Python script: " + scriptName, e);
        }
    }
}

