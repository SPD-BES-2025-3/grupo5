package com.middleware.sync_integrator.odm.repositories;

import com.middleware.sync_integrator.odm.entities.AgendamentoFlexODM;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgendamentoFlexODMRepository extends MongoRepository<AgendamentoFlexODM, String> {

}
