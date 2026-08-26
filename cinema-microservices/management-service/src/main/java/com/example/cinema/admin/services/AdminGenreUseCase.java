package com.example.cinema.admin.services;

import com.example.cinema.admin.dto.GenreDTO;
import java.util.List;

public interface AdminGenreUseCase {
    GenreDTO createGenre(GenreDTO dto);
    List<GenreDTO> getAllGenres();
    GenreDTO getGenreById(String id);
    GenreDTO updateGenre(String id, GenreDTO dto);
    void deleteGenre(String id);
}
