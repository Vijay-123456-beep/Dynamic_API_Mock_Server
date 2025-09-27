package com.vijay.mockserver.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.mockserver.mock.dto.MockEndpointRequest;
import com.vijay.mockserver.mock.dto.MockEndpointResponse;
import com.vijay.mockserver.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImportExportService {

    private final MockEndpointService mockEndpointService;
    private final ObjectMapper objectMapper;

    public ImportExportService(MockEndpointService mockEndpointService, ObjectMapper objectMapper) {
        this.mockEndpointService = mockEndpointService;
        this.objectMapper = objectMapper;
    }

    /**
     * Export all mocks for a user as JSON
     */
    public String exportMocks(User user) {
        try {
            List<MockEndpointResponse> mocks = mockEndpointService.listForCurrentUser();
            ExportData exportData = new ExportData();
            exportData.setVersion("1.0");
            exportData.setTimestamp(System.currentTimeMillis());
            exportData.setMocks(mocks);
            return objectMapper.writeValueAsString(exportData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export mocks", e);
        }
    }

    /**
     * Import mocks from JSON
     */
    public ImportResult importMocks(User user, String jsonData) {
        try {
            ExportData exportData = objectMapper.readValue(jsonData, ExportData.class);
            ImportResult result = new ImportResult();

            for (MockEndpointResponse mock : exportData.getMocks()) {
                try {
                    MockEndpointRequest request = convertToRequest(mock);
                    MockEndpointResponse created = mockEndpointService.create(request);
                    result.addSuccess(created);
                } catch (Exception e) {
                    result.addError(mock, e.getMessage());
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to import mocks", e);
        }
    }

    /**
     * Export specific mocks by IDs
     */
    public String exportMocksByIds(User user, List<Long> mockIds) {
        try {
            List<MockEndpointResponse> allMocks = mockEndpointService.listForCurrentUser();
            List<MockEndpointResponse> selectedMocks = allMocks.stream()
                    .filter(mock -> mockIds.contains(mock.getId()))
                    .collect(Collectors.toList());

            ExportData exportData = new ExportData();
            exportData.setVersion("1.0");
            exportData.setTimestamp(System.currentTimeMillis());
            exportData.setMocks(selectedMocks);
            return objectMapper.writeValueAsString(exportData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export selected mocks", e);
        }
    }

    private MockEndpointRequest convertToRequest(MockEndpointResponse response) {
        MockEndpointRequest request = new MockEndpointRequest();
        request.setEndpoint(response.getEndpoint());
        request.setMethod(response.getMethod());
        request.setResponseJson(response.getResponseJson());
        request.setDelayMs(response.getDelayMs());
        request.setStatusCode(response.getStatusCode());
        request.setRequestHeaders(response.getRequestHeaders());
        request.setQueryParameters(response.getQueryParameters());
        request.setRequestBodyPattern(response.getRequestBodyPattern());
        request.setPriority(response.getPriority());
        request.setIsActive(response.getIsActive());
        return request;
    }

    public static class ExportData {

        private String version;
        private Long timestamp;
        private List<MockEndpointResponse> mocks;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public List<MockEndpointResponse> getMocks() {
            return mocks;
        }

        public void setMocks(List<MockEndpointResponse> mocks) {
            this.mocks = mocks;
        }
    }

    public static class ImportResult {

        private final List<MockEndpointResponse> successful = new java.util.ArrayList<>();
        private final List<ImportError> errors = new java.util.ArrayList<>();

        public void addSuccess(MockEndpointResponse mock) {
            successful.add(mock);
        }

        public void addError(MockEndpointResponse mock, String error) {
            errors.add(new ImportError(mock, error));
        }

        public List<MockEndpointResponse> getSuccessful() {
            return successful;
        }

        public List<ImportError> getErrors() {
            return errors;
        }

        public int getSuccessCount() {
            return successful.size();
        }

        public int getErrorCount() {
            return errors.size();
        }
    }

    public static class ImportError {

        private final MockEndpointResponse mock;
        private final String error;

        public ImportError(MockEndpointResponse mock, String error) {
            this.mock = mock;
            this.error = error;
        }

        public MockEndpointResponse getMock() {
            return mock;
        }

        public String getError() {
            return error;
        }
    }
}
