package com.wasiq.inmobiliaria.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CloudinaryServiceTest {

    private Uploader uploader;
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        Cloudinary cloudinary = mock(Cloudinary.class);
        uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryService = new CloudinaryService(cloudinary);
    }

    @Test
    void uploadReturnsHttpsUrl() throws Exception {
        when(uploader.upload(any(), anyMap())).thenReturn(Map.of(
                "url", "http://res.cloudinary.com/demo/image/upload/v1/abc123.png",
                "secure_url", "https://res.cloudinary.com/demo/image/upload/v1/abc123.png"));

        String url = cloudinaryService.uploadFile(new MockMultipartFile("files", new byte[]{1}));

        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/abc123.png", url);
    }

    @Test
    void deleteUsesPublicIdFromStoredUrl() throws Exception {
        cloudinaryService.deleteFile("https://res.cloudinary.com/demo/image/upload/v1712345678/abc123.jpg");

        verify(uploader).destroy(eq("abc123"), anyMap());
    }

    @Test
    void deleteHandlesLegacyHttpUrlsWithoutVersion() throws Exception {
        cloudinaryService.deleteFile("http://res.cloudinary.com/demo/image/upload/folder/abc123.png");

        verify(uploader).destroy(eq("folder/abc123"), anyMap());
    }

    @Test
    void deleteIgnoresUrlsOutsideCloudinary() throws Exception {
        cloudinaryService.deleteFile("https://example.com/image/upload/v1/abc123.jpg");

        verify(uploader, never()).destroy(anyString(), anyMap());
    }

    @Test
    void deleteFailureDoesNotPropagate() throws Exception {
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new RuntimeException("cloudinary down"));

        assertDoesNotThrow(() ->
                cloudinaryService.deleteFile("https://res.cloudinary.com/demo/image/upload/v1/abc123.jpg"));
    }
}
