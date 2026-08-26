package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.LogServiceInfoDto;
import com.example.cinema.admin.dto.LogTraceDto;
import com.example.cinema.admin.services.AdminLogUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AdminLogUseCaseImpl implements AdminLogUseCase {

    @Value("${logging.tracing.base-dir:..}")
    private String baseDir;

    // Pattern matching standard Log4j2 header: yyyy-MM-dd HH:mm:ss.SSS LEVEL [SERVICE] ...
    private static final Pattern LOG_HEADER_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(INFO|WARN|ERROR|DEBUG|TRACE|FATAL)\\s+\\[([^\\]]*)\\]\\s+(.*)$"
    );

    // Regex to extract Request ID (8-char hex or 36-char UUID)
    private static final Pattern REQUEST_ID_EXTRACTOR = Pattern.compile("\\[([a-fA-F0-9\\-]{8,36})\\]");

    @Override
    public List<LogTraceDto> traceLogs(String requestId) {
        log.info("Aggregating trace logs for Request ID: [{}]", requestId);
        List<LogTraceDto> matchedLogs = new ArrayList<>();
        
        if (requestId == null || requestId.trim().isEmpty()) {
            return matchedLogs;
        }

        List<File> logFiles = getAllLogFiles();
        log.info("Found {} log files to trace.", logFiles.size());

        for (File logFile : logFiles) {
            try {
                parseLogFile(logFile, requestId, matchedLogs);
            } catch (IOException e) {
                log.error("Failed to parse log file: {}", logFile.getAbsolutePath(), e);
            }
        }

        // Sort chronologically by timestamp string (yyyy-MM-dd HH:mm:ss.SSS)
        matchedLogs.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        
        log.info("Tracing completed. Matched {} entries.", matchedLogs.size());
        return matchedLogs;
    }

    @Override
    public List<LogTraceDto> getRecentLogs(int limit) {
        log.info("Aggregating recent logs. Limit: {}", limit);
        List<LogTraceDto> allLogs = new ArrayList<>();
        List<File> logFiles = getAllLogFiles();

        for (File logFile : logFiles) {
            try {
                parseAllLogs(logFile, allLogs);
            } catch (IOException e) {
                log.error("Failed to parse log file: {}", logFile.getAbsolutePath(), e);
            }
        }

        // Sort descending by timestamp (newest logs first)
        allLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // Limit results
        if (allLogs.size() > limit) {
            return allLogs.subList(0, limit);
        }
        return allLogs;
    }

    @Override
    public List<LogServiceInfoDto> getLogServicesInfo() {
        List<LogServiceInfoDto> servicesInfo = new ArrayList<>();
        List<File> logFiles = getAllLogFiles();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (File file : logFiles) {
            String serviceName = detectServiceName(file);
            servicesInfo.add(LogServiceInfoDto.builder()
                    .serviceName(serviceName)
                    .logFilePath(file.getAbsolutePath())
                    .fileSize(file.length())
                    .lastModified(sdf.format(new Date(file.lastModified())))
                    .build());
        }
        return servicesInfo;
    }

    private List<File> getAllLogFiles() {
        List<File> logFiles = new ArrayList<>();
        try {
            File root = new File(baseDir).getCanonicalFile();
            log.info("Log tracing scanning base directory: {}", root.getAbsolutePath());
            findLogFiles(root, logFiles, 1, 3); // Depth of 3 is enough to reach cinema-microservices/*/logs/*.log
        } catch (IOException e) {
            log.error("Error resolving canonical path for log base directory: {}", baseDir, e);
        }
        return logFiles;
    }

    private void findLogFiles(File dir, List<File> logFiles, int currentDepth, int maxDepth) {
        if (dir == null || !dir.exists() || currentDepth > maxDepth) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                String name = file.getName();
                if (name.equals(".git") || name.equals("target") || name.equals("node_modules") || name.equals(".idea") || name.equals("src") || name.equals("bin")) {
                    continue;
                }
                findLogFiles(file, logFiles, currentDepth + 1, maxDepth);
            } else if (file.isFile() && file.getName().endsWith(".log")) {
                logFiles.add(file);
            }
        }
    }

    private void parseLogFile(File file, String requestId, List<LogTraceDto> matchedLogs) throws IOException {
        String serviceNameFromFileName = detectServiceName(file);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            LogTraceDto.LogTraceDtoBuilder currentBuilder = null;
            StringBuilder messageAccumulator = new StringBuilder();
            boolean currentEntryMatched = false;
            String currentTimestamp = null;
            String currentLevel = null;
            String currentService = serviceNameFromFileName;
            String currentRequestId = null;

            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_HEADER_PATTERN.matcher(line);
                if (matcher.matches()) {
                    // Save previous entry if matched
                    if (currentBuilder != null && currentEntryMatched) {
                        currentBuilder.message(messageAccumulator.toString().trim());
                        currentBuilder.requestId(currentRequestId);
                        matchedLogs.add(currentBuilder.build());
                    }

                    // Reset for new entry
                    currentTimestamp = matcher.group(1);
                    currentLevel = matcher.group(2);
                    String parsedService = matcher.group(3).trim();
                    currentService = parsedService.isEmpty() ? serviceNameFromFileName : parsedService;
                    String rest = matcher.group(4);

                    currentBuilder = LogTraceDto.builder()
                            .timestamp(currentTimestamp)
                            .level(currentLevel)
                            .service(currentService);

                    messageAccumulator = new StringBuilder();
                    messageAccumulator.append(rest);

                    currentRequestId = extractRequestId(line);
                    currentEntryMatched = line.contains(requestId);
                } else {
                    // Continuation line
                    if (currentBuilder != null) {
                        messageAccumulator.append("\n").append(line);
                        if (currentRequestId == null) {
                            currentRequestId = extractRequestId(line);
                        }
                        if (!currentEntryMatched && line.contains(requestId)) {
                            currentEntryMatched = true;
                        }
                    }
                }
            }

            // Save last entry if matched
            if (currentBuilder != null && currentEntryMatched) {
                currentBuilder.message(messageAccumulator.toString().trim());
                currentBuilder.requestId(currentRequestId);
                matchedLogs.add(currentBuilder.build());
            }
        }
    }

    private void parseAllLogs(File file, List<LogTraceDto> allLogs) throws IOException {
        String serviceNameFromFileName = detectServiceName(file);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            LogTraceDto.LogTraceDtoBuilder currentBuilder = null;
            StringBuilder messageAccumulator = new StringBuilder();
            String currentTimestamp = null;
            String currentLevel = null;
            String currentService = serviceNameFromFileName;
            String currentRequestId = null;

            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_HEADER_PATTERN.matcher(line);
                if (matcher.matches()) {
                    // Save previous entry
                    if (currentBuilder != null) {
                        currentBuilder.message(messageAccumulator.toString().trim());
                        currentBuilder.requestId(currentRequestId);
                        allLogs.add(currentBuilder.build());
                    }

                    // Reset for new entry
                    currentTimestamp = matcher.group(1);
                    currentLevel = matcher.group(2);
                    String parsedService = matcher.group(3).trim();
                    currentService = parsedService.isEmpty() ? serviceNameFromFileName : parsedService;
                    String rest = matcher.group(4);

                    currentBuilder = LogTraceDto.builder()
                            .timestamp(currentTimestamp)
                            .level(currentLevel)
                            .service(currentService);

                    messageAccumulator = new StringBuilder();
                    messageAccumulator.append(rest);

                    currentRequestId = extractRequestId(line);
                } else {
                    // Continuation line
                    if (currentBuilder != null) {
                        messageAccumulator.append("\n").append(line);
                        if (currentRequestId == null) {
                            currentRequestId = extractRequestId(line);
                        }
                    }
                }
            }

            // Save last entry
            if (currentBuilder != null) {
                currentBuilder.message(messageAccumulator.toString().trim());
                currentBuilder.requestId(currentRequestId);
                allLogs.add(currentBuilder.build());
            }
        }
    }

    private String extractRequestId(String line) {
        if (line == null) return null;
        Matcher m = REQUEST_ID_EXTRACTOR.matcher(line);
        String fallback = null;
        while (m.find()) {
            String val = m.group(1);
            // Ignore placeholder dashes
            if (val.replace("-", "").isEmpty()) {
                continue;
            }
            if (val.length() == 36) {
                return val; // Prefer full UUID
            }
            if (val.length() == 8) {
                fallback = val;
            }
        }
        return fallback;
    }

    private String detectServiceName(File file) {
        String fileName = file.getName().toUpperCase();
        if (fileName.contains("ADMIN")) return "CINEMA-ADMIN";
        if (fileName.contains("IAM")) return "CINEMA-IAM";
        if (fileName.contains("GATEWAY")) return "CINEMA-GATEWAY";
        if (fileName.contains("CATALOG")) return "CINEMA-CATALOG";
        if (fileName.contains("BOOKING")) return "CINEMA-BOOKING";
        if (fileName.contains("FACILITY")) return "CINEMA-FACILITY";
        if (fileName.contains("SCHEDULING")) return "CINEMA-SCHEDULING";
        if (fileName.contains("NOTIFICATION")) return "CINEMA-NOTIFICATION";
        
        String parentName = file.getParentFile().getParentFile().getName().toUpperCase();
        if (parentName.startsWith("CINEMA-")) {
            return parentName;
        }
        return file.getName().replace(".log", "").toUpperCase();
    }
}
