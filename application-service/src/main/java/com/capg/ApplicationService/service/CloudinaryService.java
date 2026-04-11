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
        return uploadPdf(file, "resumes", "Failed to upload resume");
    }

    public Map<String, String> uploadOfferLetter(MultipartFile file) {
        return uploadPdf(file, "offer-letters", "Failed to upload offer letter");
    }

    private Map<String, String> uploadPdf(MultipartFile file,
                                          String folder,
                                          String errorMessage) {
        try {
            if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException("Only PDF files are allowed");
            }

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", folder,
                            "format", "pdf"
                    )
            );

            Map<String, String> response = new HashMap<>();
            response.put("url", result.get("secure_url").toString());
            response.put("publicId", result.get("public_id").toString());
            return response;

        } catch (IOException e) {
            throw new RuntimeException(errorMessage, e);
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

    public void deleteOfferLetter(String publicId) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "raw")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete offer letter from Cloudinary", e);
        }
    }
}
