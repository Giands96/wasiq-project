package com.wasiq.inmobiliaria.cloudinary;

import com.cloudinary.Cloudinary;
import com.wasiq.inmobiliaria.cloudinary.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    //* https://res.cloudinary.com/<cloud>/image/upload/[v<version>/]<public_id>.<format> */
    private static final Pattern DELIVERY_URL =
            Pattern.compile("^https?://res\\.cloudinary\\.com/[^/]+/image/upload/(?:v\\d+/)?(.+?)(?:\\.[A-Za-z0-9]+)?$");

    private final Cloudinary cloudinary;

    public String uploadFile (MultipartFile file) {
        Map result = null;
        try {
            result = cloudinary.uploader().upload(file.getBytes(),Map.of());
        } catch (Exception e){
            throw new ImageUploadException("Error uploading file to Cloudinary : ",e);
        }
        return (String) result.get("secure_url");
    }

    //* Best effort: a failed deletion only leaves an orphan asset, it must not break the request */
    public void deleteFile(String url) {
        Matcher matcher = url == null ? null : DELIVERY_URL.matcher(url);
        if (matcher == null || !matcher.matches()) {
            log.warn("Skipping Cloudinary deletion, unrecognized URL: {}", url);
            return;
        }
        try {
            cloudinary.uploader().destroy(matcher.group(1), Map.of());
        } catch (Exception e) {
            log.warn("Could not delete Cloudinary asset {}", matcher.group(1), e);
        }
    }

}
