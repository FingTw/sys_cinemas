package com.example.cinema.admin.application.ports.in;

import com.example.cinema.admin.application.dto.CinemaComplexDTO;
import com.example.cinema.admin.application.dto.CinemaDTO;
import java.util.List;

public interface AdminCinemaUseCase {
    CinemaComplexDTO createComplex(CinemaComplexDTO dto);
    List<CinemaComplexDTO> getAllComplexes();
    CinemaComplexDTO getComplexById(String id);
    CinemaComplexDTO updateComplex(String id, CinemaComplexDTO dto);
    void deleteComplex(String id);

    CinemaDTO createCinema(CinemaDTO dto);
    List<CinemaDTO> getAllCinemas();
    List<CinemaDTO> getCinemasByComplex(String complexId);
    CinemaDTO getCinemaById(String id);
    CinemaDTO updateCinema(String id, CinemaDTO dto);
    void deleteCinema(String id);
}
