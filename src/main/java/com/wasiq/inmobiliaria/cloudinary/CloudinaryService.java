package com.wasiq.inmobiliaria.cloudinary;

import com.cloudinary.Cloudinary;
import com.wasiq.inmobiliaria.cloudinary.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile (MultipartFile file) {
        Map result;
        try {
            result = cloudinary.uploader().upload(file.getBytes(),Map.of());
        } catch (Exception e){
            throw new ImageUploadException("Error uploading file to Cloudinary : ",e);
        }
        return (String) result.get("url");
    }

}
