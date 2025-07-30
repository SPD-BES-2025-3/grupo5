package com.middleware.sync_integrator.odm.repositories;

import com.middleware.sync_integrator.odm.entities.ProfissionalODM;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfissionalODMRepository extends MongoRepository<ProfissionalODM, String> {
    Optional<ProfissionalODM> findByProfissionalId(Long profissionalId);
}
