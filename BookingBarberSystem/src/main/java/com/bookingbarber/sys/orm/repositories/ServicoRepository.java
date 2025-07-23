package com.bookingbarber.sys.orm.repositories;

import com.bookingbarber.sys.orm.entities.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Optional<Object> findByNomeIgnoreCase(String nome);

    Optional<Object> findByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
