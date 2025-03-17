package com.cars24.fraud_detection.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {

    private static final String JPG_EXTENSION = ".jpg";
    private static final String PNG_EXTENSION = ".png";
    /**
     * Checks if the given file name has a valid image extension (JPG or PNG).
     *
     * @param fileName The name of the file
     * @return true if the file is a JPG or PNG, false otherwise
     */
    public static boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(JPG_EXTENSION) || lowerCaseFileName.endsWith(PNG_EXTENSION);
    }
}
