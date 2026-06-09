package com.example.cinema.admin.application.ports.in;

import com.example.cinema.admin.application.dto.LogServiceInfoDto;
import com.example.cinema.admin.application.dto.LogTraceDto;
import java.util.List;

public interface AdminLogUseCase {
    List<LogTraceDto> traceLogs(String requestId);
    List<LogServiceInfoDto> getLogServicesInfo();
    List<LogTraceDto> getRecentLogs(int limit);
}
