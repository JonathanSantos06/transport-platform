package com.transport.order.service;

import com.transport.order.entity.Order;
import com.transport.order.entity.OrderFile;
import com.transport.order.entity.OrderStatus;
import com.transport.order.exception.FileUploadException;
import com.transport.order.exception.OrderNotFoundException;
import com.transport.order.repository.OrderFileRepository;
import com.transport.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFileServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderFileRepository orderFileRepository;

    private OrderFileService orderFileService;

    @BeforeEach
    void setUp() {
        orderFileService = new OrderFileService(
                orderRepository,
                orderFileRepository
        );
    }

    @Test
    void shouldUploadPdfFile() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = createOrder(orderId, driverId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "contenido pdf".getBytes()
        );

        OrderFile savedFile = new OrderFile(
                UUID.randomUUID(),
                orderId,
                "documento.pdf",
                "application/pdf",
                "uploads/documento.pdf",
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderFileRepository.save(any(OrderFile.class)))
                .thenReturn(savedFile);

        OrderFile response = orderFileService.upload(orderId, file);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals("documento.pdf", response.getFileName());
        assertEquals("application/pdf", response.getContentType());

        verify(orderRepository).findById(orderId);
        verify(orderFileRepository).save(any(OrderFile.class));
    }

    @Test
    void shouldUploadPngFile() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = createOrder(orderId, driverId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagen.png",
                "image/png",
                "contenido png".getBytes()
        );

        OrderFile savedFile = new OrderFile(
                UUID.randomUUID(),
                orderId,
                "imagen.png",
                "image/png",
                "uploads/imagen.png",
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderFileRepository.save(any(OrderFile.class)))
                .thenReturn(savedFile);

        OrderFile response = orderFileService.upload(orderId, file);

        assertNotNull(response);
        assertEquals("imagen.png", response.getFileName());
        assertEquals("image/png", response.getContentType());

        verify(orderFileRepository).save(any(OrderFile.class));
    }

    @Test
    void shouldUploadJpgFile() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = createOrder(orderId, driverId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagen.jpg",
                "image/jpeg",
                "contenido jpg".getBytes()
        );

        OrderFile savedFile = new OrderFile(
                UUID.randomUUID(),
                orderId,
                "imagen.jpg",
                "image/jpeg",
                "uploads/imagen.jpg",
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderFileRepository.save(any(OrderFile.class)))
                .thenReturn(savedFile);

        OrderFile response = orderFileService.upload(orderId, file);

        assertNotNull(response);
        assertEquals("imagen.jpg", response.getFileName());
        assertEquals("image/jpeg", response.getContentType());

        verify(orderFileRepository).save(any(OrderFile.class));
    }

    @Test
    void shouldRejectEmptyFile() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = createOrder(orderId, driverId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                new byte[0]
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        FileUploadException exception = assertThrows(
                FileUploadException.class,
                () -> orderFileService.upload(orderId, file)
        );

        assertEquals(
                "File is required",
                exception.getMessage()
        );

        verify(orderFileRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnsupportedFileType() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = createOrder(orderId, driverId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.txt",
                "text/plain",
                "contenido".getBytes()
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        FileUploadException exception = assertThrows(
                FileUploadException.class,
                () -> orderFileService.upload(orderId, file)
        );

        assertEquals(
                "Only PDF, PNG and JPG/JPEG files are allowed",
                exception.getMessage()
        );

        verify(orderFileRepository, never()).save(any());
    }

    @Test
    void shouldRejectUploadWhenOrderHasNoDriver() {

        UUID orderId = UUID.randomUUID();

        Order order = createOrder(orderId, null);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "contenido pdf".getBytes()
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        FileUploadException exception = assertThrows(
                FileUploadException.class,
                () -> orderFileService.upload(orderId, file)
        );

        assertEquals(
                "A driver must be assigned before uploading files",
                exception.getMessage()
        );

        verify(orderFileRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        UUID orderId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "contenido pdf".getBytes()
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderFileService.upload(orderId, file)
        );

        verify(orderFileRepository, never()).save(any());
    }

    private Order createOrder(UUID orderId, UUID driverId) {

        return new Order(
                orderId,
                OrderStatus.CREATED,
                "Ciudad de México",
                "Guadalajara",
                null,
                null,
                driverId
        );
    }

    @Test
    void shouldSanitizePathTraversalFileName() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = createOrder(orderId, driverId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../documento.pdf",
                "application/pdf",
                "contenido pdf".getBytes()
        );

        OrderFile savedFile = new OrderFile(
                null,
                orderId,
                "../../documento.pdf",
                "application/pdf",
                "uploads/" + orderId + "/documento.pdf",
                null
        );

        when(orderFileRepository.save(any(OrderFile.class)))
                .thenReturn(savedFile);

        OrderFile result = orderFileService.upload(orderId, file);

        ArgumentCaptor<OrderFile> captor =
                ArgumentCaptor.forClass(OrderFile.class);

        verify(orderFileRepository).save(captor.capture());

        OrderFile capturedFile = captor.getValue();

        assertEquals(
                "../../documento.pdf",
                capturedFile.getFileName()
        );

        assertFalse(
                capturedFile.getFilePath().contains("../")
        );

        assertTrue(
                Paths.get(capturedFile.getFilePath())
                        .getFileName()
                        .toString()
                        .endsWith("_documento.pdf")
        );

        assertEquals(
                savedFile.getFileName(),
                result.getFileName()
        );
    }
}