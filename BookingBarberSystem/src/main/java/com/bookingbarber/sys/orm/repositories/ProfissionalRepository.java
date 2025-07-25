package com.bookingbarber.sys.orm.repositories;

import com.bookingbarber.sys.orm.entities.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    boolean existsByEspecialidadesId(Long especialidadeId);

    Optional<Profissional> findByNumeroRegistroAndIdNot(String numeroRegistro, Long id);

}
