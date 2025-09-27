package com.vijay.mockserver.mock;

import com.vijay.mockserver.user.User;
import com.vijay.mockserver.user.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/mocks")
public class ImportExportController {

    private final ImportExportService importExportService;
    private final UserRepository userRepository;

    public ImportExportController(ImportExportService importExportService, UserRepository userRepository) {
        this.importExportService = importExportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportMocks() {
        User user = getCurrentUser();
        String jsonData = importExportService.exportMocks(user);
        
        String filename = "mocks_export_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(jsonData);
    }

    @PostMapping("/export/selected")
    public ResponseEntity<String> exportSelectedMocks(@RequestBody List<Long> mockIds) {
        User user = getCurrentUser();
        String jsonData = importExportService.exportMocksByIds(user, mockIds);
        
        String filename = "selected_mocks_export_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(jsonData);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportExportService.ImportResult> importMocks(@RequestBody String jsonData) {
        User user = getCurrentUser();
        ImportExportService.ImportResult result = importExportService.importMocks(user, jsonData);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/file")
    public ResponseEntity<ImportExportService.ImportResult> importMocksFromFile(@RequestParam("file") MultipartFile file) {
        try {
            User user = getCurrentUser();
            String jsonData = new String(file.getBytes(), StandardCharsets.UTF_8);
            ImportExportService.ImportResult result = importExportService.importMocks(user, jsonData);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/export/template")
    public ResponseEntity<String> getImportTemplate() {
        String template = """
            {
              "version": "1.0",
              "timestamp": 1234567890,
              "mocks": [
                {
                  "endpoint": "/api/users",
                  "method": "GET",
                  "responseJson": "{\\"users\\": [{\\"id\\": 1, \\"name\\": \\"John Doe\\"}]}",
                  "statusCode": 200,
                  "delayMs": 0,
                  "requestHeaders": "{\\"Authorization\\": \\"Bearer.*\\"}",
                  "queryParameters": "{\\"page\\": \\"\\\\d+\\"}",
                  "requestBodyPattern": null,
                  "priority": 0,
                  "isActive": true
                }
              ]
            }
            """;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "import_template.json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(template);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }
}
