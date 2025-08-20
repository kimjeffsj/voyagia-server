package com.voyagia.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.file.upload")
public class FileUploadProperties {

    private String maxFileSize;
    private String maxRequestSize;
    private List<String> allowedExtensions;
    private Map<String, String> imageSizes;

    public FileUploadProperties() {
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(String maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public Map<String, String> getImageSizes() {
        return imageSizes;
    }

    public void setImageSizes(Map<String, String> imageSizes) {
        this.imageSizes = imageSizes;
    }

    public boolean isExtensionAllowed(String extension) {
        if (allowedExtensions == null || extension == null) {
            return false;
        }
        return allowedExtensions.contains(extension.toLowerCase());
    }

    public int[] getImageDimensions(String sizeKey) {
        if (imageSizes == null || !imageSizes.containsKey(sizeKey)) {
            return new int[]{0, 0};
        }
        
        String size = imageSizes.get(sizeKey);
        String[] dimensions = size.split("x");
        
        if (dimensions.length != 2) {
            return new int[]{0, 0};
        }
        
        try {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            return new int[]{width, height};
        } catch (NumberFormatException e) {
            return new int[]{0, 0};
        }
    }
}