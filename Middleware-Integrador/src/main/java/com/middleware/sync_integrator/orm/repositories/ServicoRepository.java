package com.middleware.sync_integrator.orm.repositories;


import com.middleware.sync_integrator.orm.entities.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Optional<Object> findByNomeIgnoreCase(String nome);

    Optional<Object> findByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Servico> findByNomeIn(List<String> servicosNomes);
}
