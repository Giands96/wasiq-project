package com.wasiq.inmobiliaria.property.service;

import com.wasiq.inmobiliaria.cloudinary.CloudinaryService;
import com.wasiq.inmobiliaria.property.repository.PropertyRepository;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PropertyServiceTest {

    private PropertyRepository propertyRepository;
    private PropertyService propertyService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        propertyRepository = mock(PropertyRepository.class);
        when(propertyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        when(propertyRepository.findByTitleContainingAndActiveTrue(anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());
        propertyService = new PropertyService(propertyRepository, mock(UserRepository.class),
                mock(CloudinaryService.class));
    }

    @SuppressWarnings("unchecked")
    private Pageable requestedPage() {
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(propertyRepository).findAll(any(Specification.class), pageable.capture());
        return pageable.getValue();
    }

    @Test
    void publicSearchCapsPageSize() {
        propertyService.getFilteredProperties(null, null, null, null, null, null, null, 0, 1_000_000);

        assertEquals(PropertyService.MAX_PAGE_SIZE, requestedPage().getPageSize());
    }

    @Test
    void publicSearchKeepsRequestedSizeWithinLimit() {
        propertyService.getFilteredProperties(null, null, null, null, null, null, null, 2, 10);

        Pageable pageable = requestedPage();
        assertEquals(10, pageable.getPageSize());
        assertEquals(2, pageable.getPageNumber());
    }

    @Test
    void invalidPageAndSizeFallBackToSafeValues() {
        propertyService.getFilteredProperties(null, null, null, null, null, null, null, -3, 0);

        Pageable pageable = requestedPage();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
    }

    @Test
    void adminSearchCapsPageSize() {
        propertyService.getAdminProperties(null, null, null, null, 0, 1_000_000);

        assertEquals(PropertyService.MAX_PAGE_SIZE, requestedPage().getPageSize());
    }

    @Test
    void titleSearchCapsPageSize() {
        propertyService.findByTitleContaining("casa", 0, 1_000_000);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(propertyRepository).findByTitleContainingAndActiveTrue(any(), pageable.capture());
        assertEquals(PropertyService.MAX_PAGE_SIZE, pageable.getValue().getPageSize());
    }
}
