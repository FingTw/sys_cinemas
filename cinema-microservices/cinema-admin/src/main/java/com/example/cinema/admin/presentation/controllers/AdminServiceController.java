package com.example.cinema.admin.presentation.controllers;

import com.example.cinema.admin.application.dto.AdminServiceDTO;
import com.example.cinema.admin.infrastructure.database.entities.ServiceJpaEntity;
import com.example.cinema.admin.infrastructure.database.repositories.SpringDataServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
@Slf4j
public class AdminServiceController {

    private final SpringDataServiceRepository serviceRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasAuthority('USER_MANAGE')")
    public ResponseEntity<List<AdminServiceDTO>> getAllServices() {
        log.info("[Admin] Lấy danh sách services");
        List<ServiceJpaEntity> entities = serviceRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));
        
        List<AdminServiceDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ') or hasAuthority('USER_MANAGE')")
    public ResponseEntity<AdminServiceDTO> getServiceById(@PathVariable String id) {
        return serviceRepository.findById(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<AdminServiceDTO> createService(@RequestBody AdminServiceDTO dto) {
        log.info("[Admin] Tạo service mới: {}", dto.getTitle());
        ServiceJpaEntity entity = ServiceJpaEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .linkUrl(dto.getLinkUrl())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
                
        ServiceJpaEntity saved = serviceRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<AdminServiceDTO> updateService(@PathVariable String id, @RequestBody AdminServiceDTO dto) {
        log.info("[Admin] Cập nhật service: {}", id);
        return serviceRepository.findById(id)
                .map(entity -> {
                    entity.setTitle(dto.getTitle());
                    entity.setDescription(dto.getDescription());
                    entity.setImageUrl(dto.getImageUrl());
                    entity.setLinkUrl(dto.getLinkUrl());
                    entity.setDisplayOrder(dto.getDisplayOrder());
                    entity.setActive(dto.getActive());
                    return serviceRepository.save(entity);
                })
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        log.info("[Admin] Xóa service: {}", id);
        if (serviceRepository.existsById(id)) {
            serviceRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private AdminServiceDTO mapToDTO(ServiceJpaEntity entity) {
        return AdminServiceDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .linkUrl(entity.getLinkUrl())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
