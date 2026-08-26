package com.example.cinema.booking.adapter.web;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/camunda")
@RequiredArgsConstructor
@Slf4j
public class CamundaTaskController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final ManagementService managementService;

    // ═══════════════════════════════════════════════════════════
    // PROCESS MONITOR ENDPOINTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Danh sách Process Definitions đã deploy
     */
    @GetMapping("/process-definitions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessDefinition pd : definitions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", pd.getId());
            item.put("key", pd.getKey());
            item.put("name", pd.getName());
            item.put("version", pd.getVersion());
            item.put("deploymentId", pd.getDeploymentId());
            item.put("suspended", pd.isSuspended());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Diagram hình ảnh (SVG/PNG) của Process Definition
     */
    @GetMapping("/process-definitions/{id}/diagram")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<byte[]> getProcessDiagram(@PathVariable String id) {
        try {
            InputStream stream = repositoryService.getProcessDiagram(id);
            if (stream == null) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = stream.readAllBytes();
            String diagramResource = repositoryService.getProcessDefinition(id).getDiagramResourceName();
            MediaType mediaType = diagramResource != null && diagramResource.endsWith(".svg")
                    ? MediaType.valueOf("image/svg+xml")
                    : MediaType.IMAGE_PNG;
            return ResponseEntity.ok().contentType(mediaType).body(bytes);
        } catch (Exception e) {
            log.error("Error getting process diagram: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * BPMN XML của Process Definition
     */
    @GetMapping("/process-definitions/{id}/xml")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<Map<String, String>> getProcessXml(@PathVariable String id) {
        try {
            InputStream stream = repositoryService.getProcessModel(id);
            if (stream == null) return ResponseEntity.notFound().build();
            String xml = new String(stream.readAllBytes());
            return ResponseEntity.ok(Map.of("bpmn20Xml", xml));
        } catch (Exception e) {
            log.error("Error getting process XML: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Thống kê tổng hợp Camunda
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<Map<String, Object>> getStats() {
        long definitionCount = repositoryService.createProcessDefinitionQuery().latestVersion().count();
        long activeInstances = runtimeService.createProcessInstanceQuery().active().count();
        long activeTasks = taskService.createTaskQuery().active().count();
        long incidentCount = runtimeService.createIncidentQuery().count();
        long completedInstances = historyService.createHistoricProcessInstanceQuery().finished().count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("definitionCount", definitionCount);
        stats.put("activeInstances", activeInstances);
        stats.put("activeTasks", activeTasks);
        stats.put("incidentCount", incidentCount);
        stats.put("completedInstances", completedInstances);
        return ResponseEntity.ok(stats);
    }

    /**
     * Danh sách process instances đang chạy
     */
    @GetMapping("/process-instances")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getProcessInstances() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .active()
                .orderByProcessInstanceId().desc()
                .listPage(0, 50);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessInstance pi : instances) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", pi.getId());
            item.put("processDefinitionId", pi.getProcessDefinitionId());
            item.put("businessKey", pi.getBusinessKey());
            item.put("suspended", pi.isSuspended());
            
            // Add active activity IDs (nodes where the token is currently waiting)
            List<String> activeActivities = runtimeService.getActiveActivityIds(pi.getId());
            item.put("activeActivityIds", activeActivities);

            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Lịch sử process instances (đã hoàn thành)
     */
    @GetMapping("/process-instances/history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getHistoricInstances() {
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc()
                .listPage(0, 50);

        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricProcessInstance hpi : instances) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", hpi.getId());
            item.put("processDefinitionId", hpi.getProcessDefinitionId());
            item.put("processDefinitionKey", hpi.getProcessDefinitionKey());
            item.put("processDefinitionName", hpi.getProcessDefinitionName());
            item.put("businessKey", hpi.getBusinessKey());
            item.put("startTime", hpi.getStartTime());
            item.put("endTime", hpi.getEndTime());
            item.put("state", hpi.getState());
            item.put("durationInMillis", hpi.getDurationInMillis());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    // ═══════════════════════════════════════════════════════════
    // HISTORIC VISUALIZATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Các bước (activities) mà một Historic Process Instance đã đi qua
     */
    @GetMapping("/process-instances/history/{id}/activities")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getHistoricActivities(@PathVariable String id) {
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(id)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricActivityInstance hai : activities) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", hai.getId());
            item.put("activityId", hai.getActivityId());
            item.put("activityName", hai.getActivityName());
            item.put("activityType", hai.getActivityType());
            item.put("startTime", hai.getStartTime());
            item.put("endTime", hai.getEndTime());
            item.put("durationInMillis", hai.getDurationInMillis());
            item.put("canceled", hai.isCanceled());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Biến số (variables) cuối cùng của một Historic Process Instance
     */
    @GetMapping("/process-instances/history/{id}/variables")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<Map<String, Object>> getHistoricVariables(@PathVariable String id) {
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(id)
                .list();

        Map<String, Object> result = new HashMap<>();
        for (HistoricVariableInstance hvi : variables) {
            result.put(hvi.getName(), hvi.getValue());
        }
        return ResponseEntity.ok(result);
    }

    // ═══════════════════════════════════════════════════════════
    // COCKPIT - INSTANCE MANAGEMENT & INTERVENTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Xem variables của một Process Instance đang chạy
     */
    @GetMapping("/process-instances/{id}/variables")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<Map<String, Object>> getInstanceVariables(@PathVariable String id) {
        try {
            Map<String, Object> variables = runtimeService.getVariables(id);
            return ResponseEntity.ok(variables);
        } catch (Exception e) {
            log.error("Error getting variables for instance {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Hủy/Terminate một Process Instance
     */
    @DeleteMapping("/process-instances/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> terminateInstance(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Terminated by Admin") String reason) {
        log.info("Terminating process instance {} with reason: {}", id, reason);
        runtimeService.deleteProcessInstance(id, reason);
        return ResponseEntity.ok(Map.of("message", "Instance terminated successfully"));
    }

    /**
     * Tạm dừng hoặc Kích hoạt lại một Process Instance
     */
    @PutMapping("/process-instances/{id}/suspended")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> toggleInstanceSuspension(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> payload) {
        boolean suspend = payload.getOrDefault("suspended", true);
        log.info("Toggling suspension for instance {} to: {}", id, suspend);
        
        if (suspend) {
            runtimeService.suspendProcessInstanceById(id);
        } else {
            runtimeService.activateProcessInstanceById(id);
        }
        return ResponseEntity.ok(Map.of("message", "Instance suspension toggled successfully"));
    }

    /**
     * Danh sách Incidents (Lỗi) đang xảy ra
     */
    @GetMapping("/incidents")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getIncidents() {
        List<Incident> incidents = runtimeService.createIncidentQuery().list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Incident incident : incidents) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", incident.getId());
            item.put("incidentType", incident.getIncidentType());
            item.put("incidentMessage", incident.getIncidentMessage());
            item.put("processInstanceId", incident.getProcessInstanceId());
            item.put("processDefinitionId", incident.getProcessDefinitionId());
            item.put("activityId", incident.getActivityId());
            item.put("failedActivityId", incident.getFailedActivityId());
            item.put("jobDefinitionId", incident.getJobDefinitionId());
            item.put("configuration", incident.getConfiguration());
            item.put("incidentTimestamp", incident.getIncidentTimestamp());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Retry một failed Job (Reset retries)
     */
    @PostMapping("/jobs/{jobId}/retries")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> retryJob(@PathVariable String jobId) {
        log.info("Retrying job: {}", jobId);
        // Set retries back to 3 to force Camunda to re-execute the job
        managementService.setJobRetries(jobId, 3);
        return ResponseEntity.ok(Map.of("message", "Job retries reset successfully"));
    }

    // ═══════════════════════════════════════════════════════════
    // EXISTING TASK ENDPOINTS
    // ═══════════════════════════════════════════════════════════

    /**
     * API Khởi chạy thực thể tiến trình BPMN (Start Process Instance)
     */
    @PostMapping("/process/start/{processKey}")
    public ResponseEntity<Map<String, String>> startProcess(
            @PathVariable String processKey,
            @RequestBody Map<String, Object> variables) {
        log.info("Starting Camunda process: [{}] with variables: {}", processKey, variables);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, variables);

        Map<String, String> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getId());
        response.put("businessKey", processInstance.getBusinessKey());
        response.put("status", "STARTED");

        return ResponseEntity.ok(response);
    }

    /**
     * API Lấy danh sách các User Task đang chờ xử lý (Active User Tasks)
     * Hỗ trợ lọc theo Assignee (Người được giao) hoặc Candidate Group (Nhóm được phân công)
     */
    @GetMapping("/tasks/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<List<CamundaTaskDto>> getActiveTasks(
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String candidateGroup) {
        
        log.info("Querying active tasks. Assignee: [{}], CandidateGroup: [{}]", assignee, candidateGroup);

        var query = taskService.createTaskQuery().active();

        if (assignee != null && !assignee.trim().isEmpty()) {
            query.taskAssignee(assignee);
        }
        if (candidateGroup != null && !candidateGroup.trim().isEmpty()) {
            query.taskCandidateGroup(candidateGroup);
        }

        List<Task> tasks = query.list();
        List<CamundaTaskDto> dtoList = new ArrayList<>();

        for (Task t : tasks) {
            CamundaTaskDto dto = new CamundaTaskDto();
            dto.setTaskId(t.getId());
            dto.setName(t.getName());
            dto.setAssignee(t.getAssignee());
            dto.setCreateTime(t.getCreateTime().toString());
            dto.setProcessInstanceId(t.getProcessInstanceId());
            dto.setProcessDefinitionId(t.getProcessDefinitionId());
            dto.setTaskDefinitionKey(t.getTaskDefinitionKey());
            
            // Lấy các biến liên quan đến task này để trả về cho FE hiển thị form
            Map<String, Object> variables = runtimeService.getVariables(t.getExecutionId());
            dto.setVariables(variables);

            dtoList.add(dto);
        }

        return ResponseEntity.ok(dtoList);
    }

    /**
     * API Nhận việc (Claim Task) - Gán một User Task cho một người dùng cụ thể
     */
    @PostMapping("/tasks/{taskId}/claim")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<Map<String, String>> claimTask(
            @PathVariable String taskId,
            Authentication authentication) {
        
        String currentUserId = authentication.getName();
        log.info("User [{}] claiming task [{}]", currentUserId, taskId);

        taskService.claim(taskId, currentUserId);

        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("assignee", currentUserId);
        response.put("status", "CLAIMED");

        return ResponseEntity.ok(response);
    }

    /**
     * API Hoàn thành Task (Complete Task) - Nộp biểu mẫu duyệt để đi tiếp quy trình
     */
    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<Map<String, String>> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables) {
        
        log.info("Completing task [{}] with variables: {}", taskId, variables);

        taskService.complete(taskId, variables);

        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", "COMPLETED");

        return ResponseEntity.ok(response);
    }

    @Data
    public static class CamundaTaskDto {
        private String taskId;
        private String name;
        private String assignee;
        private String createTime;
        private String processInstanceId;
        private String processDefinitionId;
        private String taskDefinitionKey;
        private Map<String, Object> variables;
    }
}

