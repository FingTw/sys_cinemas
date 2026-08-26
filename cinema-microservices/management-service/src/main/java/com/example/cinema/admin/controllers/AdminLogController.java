package com.example.cinema.admin.controllers;

import com.example.cinema.admin.dto.LogServiceInfoDto;
import com.example.cinema.admin.dto.LogTraceDto;
import com.example.cinema.admin.services.AdminLogUseCaseImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/logs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class AdminLogController {

    private final AdminLogUseCaseImpl adminLogUseCase;

    @GetMapping("/trace/{requestId}")
    public ResponseEntity<List<LogTraceDto>> traceLogs(@PathVariable String requestId) {
        log.info("Admin request to trace logs for requestId: [{}]", requestId);
        List<LogTraceDto> logs = adminLogUseCase.traceLogs(requestId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/services")
    public ResponseEntity<List<LogServiceInfoDto>> getLogServicesInfo() {
        log.info("Admin request to fetch log services configuration info");
        List<LogServiceInfoDto> services = adminLogUseCase.getLogServicesInfo();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<LogTraceDto>> getRecentLogs(@RequestParam(defaultValue = "500") int limit) {
        log.info("Admin request to fetch recent logs. Limit: {}", limit);
        List<LogTraceDto> logs = adminLogUseCase.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }
}
