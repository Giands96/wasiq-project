package com.wasiq.inmobiliaria.media;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

//* Validates uploads by their content, never by the client-declared Content-Type */
public final class ImageFiles {

    public static final int MAX_IMAGES_PER_PROPERTY = 4;

    private static final int HEADER_LENGTH = 12;

    private ImageFiles() {
    }

    public static void requireWithinLimit(int imageCount) {
        if (imageCount > MAX_IMAGES_PER_PROPERTY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se permiten un máximo de " + MAX_IMAGES_PER_PROPERTY + " imágenes por propiedad");
        }
    }

    public static void requireSupportedImages(List<MultipartFile> files) {
        files.forEach(ImageFiles::detectContentType);
    }

    //* Returns the real image type (JPEG, PNG or WebP) or rejects the file */
    public static String detectContentType(MultipartFile file) {
        byte[] header = readHeader(file);
        if (startsWith(header, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (startsWith(header, 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n')) {
            return "image/png";
        }
        if (startsWith(header, 'R', 'I', 'F', 'F') && header.length >= HEADER_LENGTH
                && Arrays.equals(Arrays.copyOfRange(header, 8, 12), new byte[]{'W', 'E', 'B', 'P'})) {
            return "image/webp";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imágenes JPEG, PNG o WebP");
    }

    private static byte[] readHeader(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            return input.readNBytes(HEADER_LENGTH);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen");
        }
    }

    private static boolean startsWith(byte[] header, int... signature) {
        if (header.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((header[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
