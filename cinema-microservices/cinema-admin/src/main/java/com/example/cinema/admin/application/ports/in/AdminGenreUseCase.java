package com.example.cinema.admin.application.ports.in;

import com.example.cinema.admin.application.dto.GenreDTO;
import java.util.List;

public interface AdminGenreUseCase {
    GenreDTO createGenre(GenreDTO dto);
    List<GenreDTO> getAllGenres();
    GenreDTO getGenreById(String id);
    GenreDTO updateGenre(String id, GenreDTO dto);
    void deleteGenre(String id);
}
