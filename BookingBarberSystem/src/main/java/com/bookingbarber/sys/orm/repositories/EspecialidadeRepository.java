package com.bookingbarber.sys.orm.repositories;

import com.bookingbarber.sys.orm.entities.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {

    Optional<Especialidade> findByNomeIgnoreCase(String nome);

    Optional<Especialidade> findByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
