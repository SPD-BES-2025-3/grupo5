package com.middleware.sync_integrator.odm.repositories;

import com.middleware.sync_integrator.odm.entities.ClienteODM;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteODMRepository extends MongoRepository<ClienteODM, String> {
    Optional<ClienteODM> findByClienteId(Long clientId);
}
