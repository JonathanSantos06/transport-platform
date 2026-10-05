package com.transport.order.service;

import com.transport.order.entity.Order;
import com.transport.order.entity.OrderFile;
import com.transport.order.exception.FileUploadException;
import com.transport.order.exception.OrderNotFoundException;
import com.transport.order.repository.OrderFileRepository;
import com.transport.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class OrderFileService {

    private static final String PDF = "application/pdf";
    private static final String PNG = "image/png";
    private static final String JPEG = "image/jpeg";

    private final OrderRepository orderRepository;
    private final OrderFileRepository orderFileRepository;

    private final Path uploadDirectory = Paths.get("uploads");

    public OrderFileService(
            OrderRepository orderRepository,
            OrderFileRepository orderFileRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderFileRepository = orderFileRepository;
    }

    public OrderFile upload(UUID orderId, MultipartFile file) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getDriverId() == null) {
            throw new FileUploadException(
                    "A driver must be assigned before uploading files"
            );
        }

        validateFile(file);

        String originalFileName = file.getOriginalFilename();

        /*
         * Sanitizamos el nombre para evitar path traversal.
         *
         * Ejemplos:
         * ../../documento.pdf  -> documento.pdf
         * ..\\..\\documento.pdf -> documento.pdf
         */
        String safeFileName = Paths
                .get(originalFileName.replace('\\', '/'))
                .getFileName()
                .toString();

        try {
            Path orderDirectory = uploadDirectory
                    .resolve(orderId.toString());

            Files.createDirectories(orderDirectory);

            String storedFileName = UUID.randomUUID()
                    + "_" + safeFileName;

            Path destination = orderDirectory
                    .resolve(storedFileName)
                    .normalize();

            /*
             * Validamos que el archivo final permanezca
             * dentro del directorio de la orden.
             */
            if (!destination.startsWith(orderDirectory.normalize())) {
                throw new FileUploadException(
                        "Invalid file path"
                );
            }

            Files.copy(
                    file.getInputStream(),
                    destination
            );

            OrderFile orderFile = new OrderFile(
                    null,
                    orderId,
                    originalFileName,
                    file.getContentType(),
                    destination.toString(),
                    null
            );

            return orderFileRepository.save(orderFile);

        } catch (IOException e) {
            throw new FileUploadException(
                    "Could not store the uploaded file"
            );
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is required");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new FileUploadException("File name is required");
        }

        String contentType = file.getContentType();

        if (!PDF.equals(contentType)
                && !PNG.equals(contentType)
                && !JPEG.equals(contentType)) {

            throw new FileUploadException(
                    "Only PDF, PNG and JPG/JPEG files are allowed"
            );
        }
    }
}