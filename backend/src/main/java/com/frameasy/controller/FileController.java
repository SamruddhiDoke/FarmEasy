package com.frameasy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves generated agreement PDFs and uploads.
 */
@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    @GetMapping("/{subDir}/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String subDir, @PathVariable String filename) {
        try {
            Path dir = Paths.get(uploadDir).resolve(subDir);
            Path file = dir.resolve(filename).normalize();
            if (!file.startsWith(dir)) {
                return ResponseEntity.badRequest().build();
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
