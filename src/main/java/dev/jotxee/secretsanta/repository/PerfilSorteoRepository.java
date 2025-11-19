package dev.jotxee.secretsanta.repository;

import dev.jotxee.secretsanta.entity.PerfilSorteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilSorteoRepository extends JpaRepository<PerfilSorteo, Long> {
    
    Optional<PerfilSorteo> findByToken(String token);
    
    List<PerfilSorteo> findBySorteoId(Long sorteoId);
    
    List<PerfilSorteo> findByUsuarioId(Long usuarioId);
    
    @Query("SELECT ps FROM PerfilSorteo ps JOIN FETCH ps.usuario JOIN FETCH ps.sorteo WHERE ps.usuario.id = :usuarioId")
    List<PerfilSorteo> findByUsuarioIdWithDetails(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT ps FROM PerfilSorteo ps JOIN FETCH ps.usuario WHERE ps.sorteo.id = :sorteoId")
    List<PerfilSorteo> findBySorteoIdWithUsuario(@Param("sorteoId") Long sorteoId);
    
    Optional<PerfilSorteo> findByUsuarioIdAndSorteoId(Long usuarioId, Long sorteoId);
    
    @Query("SELECT ps FROM PerfilSorteo ps WHERE ps.sorteo.id = :sorteoId AND ps.asignadoA = :email")
    List<PerfilSorteo> findBySorteoIdAndAsignadoA(@Param("sorteoId") Long sorteoId, @Param("email") String email);
    
    @Query("SELECT ps FROM PerfilSorteo ps JOIN FETCH ps.usuario WHERE ps.id = :id")
    Optional<PerfilSorteo> findByIdWithUsuario(@Param("id") Long id);
}
