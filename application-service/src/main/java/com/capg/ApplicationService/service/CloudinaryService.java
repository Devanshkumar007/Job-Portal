package com.capg.ApplicationService.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, String> uploadResume(MultipartFile file) {
        try {
            if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException("Only PDF files are allowed");
            }

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "resumes",
                            "format", "pdf"
                    )
            );

            Map<String, String> response = new HashMap<>();
            response.put("url", result.get("secure_url").toString());
            response.put("publicId", result.get("public_id").toString());
            return response;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload resume", e);
        }
    }

    public void deleteResume(String publicId) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "raw")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete resume from Cloudinary", e);
        }
    }
}