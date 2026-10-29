package com.wasiq.inmobiliaria.property.service;

import com.wasiq.inmobiliaria.cloudinary.CloudinaryService;
import com.wasiq.inmobiliaria.media.Media;
import com.wasiq.inmobiliaria.property.model.Property;
import com.wasiq.inmobiliaria.property.repository.PropertyRepository;
import com.wasiq.inmobiliaria.shared.dto.CreatePropertyRequest;
import com.wasiq.inmobiliaria.shared.dto.UpdatePropertyRequest;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PropertyServiceImagesTest {

    private static final String EMAIL = "owner@test.com";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0};

    private PropertyRepository propertyRepository;
    private CloudinaryService cloudinary;
    private PropertyService propertyService;
    private Property property;

    @BeforeEach
    void setUp() {
        propertyRepository = mock(PropertyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        cloudinary = mock(CloudinaryService.class);
        propertyService = new PropertyService(propertyRepository, userRepository, cloudinary);

        User owner = User.builder().id(1L).email(EMAIL).role(Role.USER).active(true).build();
        property = Property.builder().id(10L).slug("casa").owner(owner).images(new ArrayList<>()).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(propertyRepository.findBySlugAndActiveTrue("casa")).thenReturn(Optional.of(property));
        when(cloudinary.uploadFile(any())).thenReturn("https://res.cloudinary.com/demo/image/upload/v1/new.png");
    }

    private static MultipartFile file(byte[] content, String declaredType) {
        return new MockMultipartFile("files", "photo", declaredType, content);
    }

    private static Media image(long id) {
        return Media.builder().id(id).url("https://res.cloudinary.com/demo/image/upload/v1/img" + id + ".jpg")
                .type("image/jpeg").build();
    }

    private void assertBadRequest(Runnable action) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(400, exception.getStatusCode().value());
        verify(cloudinary, never()).uploadFile(any());
    }

    @Test
    void createRejectsFileThatIsNotAnImageEvenIfDeclaredAsOne() {
        MultipartFile html = file("<html><script>alert(1)</script>".getBytes(StandardCharsets.UTF_8), "image/png");

        assertBadRequest(() -> propertyService.savePropertyWithImage(new CreatePropertyRequest(), List.of(html), EMAIL));
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createRejectsSvgImages() {
        MultipartFile svg = file("<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8),
                "image/svg+xml");

        assertBadRequest(() -> propertyService.savePropertyWithImage(new CreatePropertyRequest(), List.of(svg), EMAIL));
    }

    @Test
    void createRejectsMoreThanFourImages() {
        List<MultipartFile> files = Collections.nCopies(5, file(PNG, "image/png"));

        assertBadRequest(() -> propertyService.savePropertyWithImage(new CreatePropertyRequest(), files, EMAIL));
    }

    @Test
    void createAcceptsJpegPngAndWebpAndStoresDetectedType() {
        propertyService.savePropertyWithImage(new CreatePropertyRequest(),
                List.of(file(JPEG, "application/octet-stream"), file(PNG, "image/png"), file(WEBP, "image/webp")),
                EMAIL);

        assertEquals(List.of("image/jpeg", "image/png", "image/webp"),
                property.getImages().stream().map(Media::getType).toList());
    }

    @Test
    void updateRejectsWhenKeptAndNewImagesExceedLimit() {
        property.getImages().addAll(List.of(image(1), image(2), image(3)));

        assertBadRequest(() -> propertyService.updateProperty("casa", new UpdatePropertyRequest(),
                List.of(file(PNG, "image/png"), file(PNG, "image/png")), List.of(1L, 2L, 3L), EMAIL));
        assertEquals(3, property.getImages().size());
    }

    @Test
    void updateRejectsInvalidFileBeforeTouchingExistingImages() {
        property.getImages().addAll(List.of(image(1), image(2)));

        assertBadRequest(() -> propertyService.updateProperty("casa", new UpdatePropertyRequest(),
                List.of(file(new byte[]{1, 2, 3}, "image/png")), List.of(1L), EMAIL));
        assertEquals(2, property.getImages().size());
        verify(cloudinary, never()).deleteFile(anyString());
    }

    @Test
    void updateOfPropertyWithoutImagesAcceptsNewImage() {
        property.setImages(null);

        propertyService.updateProperty("casa", new UpdatePropertyRequest(), List.of(file(PNG, "image/png")), null, EMAIL);

        assertEquals(1, property.getImages().size());
    }

    @Test
    void updateDeletesRemovedImagesFromCloudinary() {
        property.getImages().addAll(List.of(image(1), image(2)));

        propertyService.updateProperty("casa", new UpdatePropertyRequest(), null, List.of(1L), EMAIL);

        verify(cloudinary).deleteFile("https://res.cloudinary.com/demo/image/upload/v1/img2.jpg");
        verify(cloudinary, never()).deleteFile("https://res.cloudinary.com/demo/image/upload/v1/img1.jpg");
    }
}
