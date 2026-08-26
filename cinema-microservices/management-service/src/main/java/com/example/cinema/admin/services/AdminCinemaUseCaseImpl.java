package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.CinemaComplexDTO;
import com.example.cinema.admin.dto.CinemaDTO;
import com.example.cinema.admin.services.AdminCinemaUseCase;
import com.example.cinema.admin.entities.Cinema;
import com.example.cinema.admin.entities.CinemaComplex;
import com.example.cinema.admin.repositories.CinemaComplexRepository;
import com.example.cinema.admin.repositories.CinemaRepository;
import com.example.cinema.common.exception.ServerException;
import com.example.cinema.common.exception.ClientException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCinemaUseCaseImpl implements AdminCinemaUseCase {

    private final CinemaComplexRepository cinemaComplexRepository;
    private final CinemaRepository cinemaRepository;
    private final ModelMapper modelMapper;

    // --- COMPLEX CRUD ---

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cinemaComplexes", allEntries = true)
    })
    public CinemaComplexDTO createComplex(CinemaComplexDTO dto) {
        try {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new ClientException("Complex name cannot be empty");
            }
            CinemaComplex complex = CinemaComplex.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .build();
            CinemaComplex saved = cinemaComplexRepository.save(complex);
            log.info("Admin created Cinema Complex: [{}]", saved.getName());
            return modelMapper.map(saved, CinemaComplexDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Complex creation failed", e);
        }
    }

    @Override
    public List<CinemaComplexDTO> getAllComplexes() {
        try {
            return cinemaComplexRepository.findAll().stream()
                    .map(cc -> modelMapper.map(cc, CinemaComplexDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve complexes", e);
        }
    }

    @Override
    public CinemaComplexDTO getComplexById(String id) {
        try {
            CinemaComplex complex = cinemaComplexRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Complex not found with ID: " + id));
            return modelMapper.map(complex, CinemaComplexDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve complex", e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cinemaComplexes", allEntries = true)
    })
    public CinemaComplexDTO updateComplex(String id, CinemaComplexDTO dto) {
        try {
            CinemaComplex complex = cinemaComplexRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Complex not found with ID: " + id));
            complex.updateDetails(dto.getName(), dto.getDescription());
            CinemaComplex updated = cinemaComplexRepository.save(complex);
            log.info("Updated Complex ID: [{}]", updated.getId());
            return modelMapper.map(updated, CinemaComplexDTO.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Complex update failed", e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cinemaComplexes", allEntries = true)
    })
    public void deleteComplex(String id) {
        try {
            cinemaComplexRepository.deleteById(id);
            log.info("Deleted Complex ID: [{}]", id);
        } catch (Exception e) {
            throw new ServerException("Complex deletion failed", e);
        }
    }

    // --- CINEMA CRUD ---

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cinemas", allEntries = true),
            @CacheEvict(value = "cinemasByComplex", allEntries = true)
    })
    public CinemaDTO createCinema(CinemaDTO dto) {
        try {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new ClientException("Cinema name cannot be empty");
            }
            // Kiểm tra xem Complex có tồn tại không
            cinemaComplexRepository.findById(dto.getComplexId())
                    .orElseThrow(() -> new ClientException("Complex not found with ID: " + dto.getComplexId()));

            Cinema cinema = Cinema.builder()
                    .name(dto.getName())
                    .address(dto.getAddress())
                    .complexId(dto.getComplexId())
                    .build();
            Cinema saved = cinemaRepository.save(cinema);
            log.info("Admin created Cinema: [{}]", saved.getName());
            return convertToDTO(saved);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Cinema creation failed", e);
        }
    }

    @Override
    public List<CinemaDTO> getAllCinemas() {
        try {
            String staffCinemaId = com.example.cinema.admin.utils.SecurityUtils.getStaffCinemaId();
            return cinemaRepository.findAll().stream()
                    .filter(c -> staffCinemaId == null || staffCinemaId.equals(c.getId()))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve cinemas", e);
        }
    }

    @Override
    public List<CinemaDTO> getCinemasByComplex(String complexId) {
        try {
            return cinemaRepository.findByComplexId(complexId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve cinemas for complex " + complexId, e);
        }
    }

    @Override
    public CinemaDTO getCinemaById(String id) {
        try {
            Cinema cinema = cinemaRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Cinema not found with ID: " + id));
            return convertToDTO(cinema);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Failed to retrieve cinema", e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cinemas", allEntries = true),
            @CacheEvict(value = "cinemasByComplex", allEntries = true),
            @CacheEvict(value = "cinema", key = "#id")
    })
    public CinemaDTO updateCinema(String id, CinemaDTO dto) {
        try {
            Cinema cinema = cinemaRepository.findById(id)
                    .orElseThrow(() -> new ClientException("Cinema not found with ID: " + id));

            // Kiểm tra xem Complex có tồn tại không
            cinemaComplexRepository.findById(dto.getComplexId())
                    .orElseThrow(() -> new ClientException("Complex not found with ID: " + dto.getComplexId()));

            cinema.updateDetails(dto.getName(), dto.getAddress(), dto.getComplexId());
            Cinema updated = cinemaRepository.save(cinema);
            log.info("Updated Cinema ID: [{}]", updated.getId());
            return convertToDTO(updated);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("Cinema update failed", e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cinemas", allEntries = true),
            @CacheEvict(value = "cinemasByComplex", allEntries = true),
            @CacheEvict(value = "cinema", key = "#id")
    })
    public void deleteCinema(String id) {
        try {
            cinemaRepository.deleteById(id);
            log.info("Deleted Cinema ID: [{}]", id);
        } catch (Exception e) {
            throw new ServerException("Cinema deletion failed", e);
        }
    }

    private CinemaDTO convertToDTO(Cinema cinema) {
        CinemaDTO dto = modelMapper.map(cinema, CinemaDTO.class);
        cinemaComplexRepository.findById(cinema.getComplexId()).ifPresent(cc -> {
            dto.setComplexName(cc.getName());
        });
        return dto;
    }
}
