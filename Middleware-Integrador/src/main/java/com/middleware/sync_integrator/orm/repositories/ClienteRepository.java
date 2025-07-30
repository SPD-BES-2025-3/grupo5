package com.middleware.sync_integrator.orm.repositories;

import com.middleware.sync_integrator.orm.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
