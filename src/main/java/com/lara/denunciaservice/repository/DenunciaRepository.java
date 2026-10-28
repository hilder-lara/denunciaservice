package com.lara.denunciaservice.repository;

import com.lara.denunciaservice.entity.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DenunciaRepository extends JpaRepository<Denuncia, Integer> {
    // OFFSET es la cantidad exacta de filas omitidas; no es el número de página.
    @Query(value = "SELECT * FROM denuncias ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Denuncia> findPaginadas(@Param("offset") int offset, @Param("limit") int limit);

    List<Denuncia> findByDniOrderByIdAsc(String dni);
}
