package dev.jotxee.secretsanta.repository;

import dev.jotxee.secretsanta.entity.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {
    
    Optional<Participante> findByToken(String token);
    
    List<Participante> findBySorteoId(Long sorteoId);

    Optional<Participante> findByEmail(String email);

    List<Participante> findBySorteoIdAndAsignadoA(Long sorteoId, String asignadoA);

    @Query("SELECT p FROM Participante p LEFT JOIN FETCH p.sorteo WHERE p.id = :id")
    Optional<Participante> findByIdWithSorteo(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Participante p LEFT JOIN FETCH p.sorteo WHERE p.email = :email")
    List<Participante> findByEmailWithSorteo(@Param("email") String email);
}
