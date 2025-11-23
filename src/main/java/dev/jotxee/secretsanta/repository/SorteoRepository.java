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
    
    /**
     * Consulta optimizada para obtener sorteos compartidos entre dos usuarios.
     * Usa una única consulta SQL en lugar de múltiples consultas y filtrado en memoria.
     */
    @Query("SELECT DISTINCT s FROM Sorteo s " +
           "WHERE EXISTS (SELECT 1 FROM PerfilSorteo p1 WHERE p1.sorteo = s AND p1.usuario.id = :userId1) " +
           "AND EXISTS (SELECT 1 FROM PerfilSorteo p2 WHERE p2.sorteo = s AND p2.usuario.id = :userId2)")
    List<Sorteo> findSharedSorteos(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
