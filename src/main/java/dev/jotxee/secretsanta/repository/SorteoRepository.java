package dev.jotxee.secretsanta.repository;

import dev.jotxee.secretsanta.entity.Sorteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SorteoRepository extends JpaRepository<Sorteo, Long> {
    
    @Query("SELECT DISTINCT s FROM Sorteo s LEFT JOIN FETCH s.perfiles p LEFT JOIN FETCH p.usuario WHERE s.activo = true")
    List<Sorteo> findByActivoTrue();

    @Query("SELECT DISTINCT s FROM Sorteo s LEFT JOIN FETCH s.perfiles p LEFT JOIN FETCH p.usuario WHERE s.id IN :ids")
    List<Sorteo> findByIdInWithPerfiles(@Param("ids") List<Long> ids);
}
