package com.example.cinema.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogServiceInfoDto {
    private String serviceName;
    private String logFilePath;
    private long fileSize;
    private String lastModified;
}
