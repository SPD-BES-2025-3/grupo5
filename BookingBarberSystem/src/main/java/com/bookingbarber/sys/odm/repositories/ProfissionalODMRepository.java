package com.bookingbarber.sys.odm.repositories;

import com.bookingbarber.sys.odm.entities.ProfissionalODM;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfissionalODMRepository extends MongoRepository<ProfissionalODM, String> {
    Optional<ProfissionalODM> findByProfissionalId(Long profissionalId);
}
