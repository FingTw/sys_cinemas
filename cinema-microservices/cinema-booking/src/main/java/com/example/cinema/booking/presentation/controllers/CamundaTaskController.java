package com.example.cinema.booking.presentation.controllers;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
