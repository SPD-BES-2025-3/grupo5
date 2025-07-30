package com.bookingbarber.sys.odm.repositories;

import com.bookingbarber.sys.odm.entities.AgendamentoFlexODM;
import com.bookingbarber.sys.odm.entities.AgendamentoODM;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgendamentoFlexODMRepository extends MongoRepository<AgendamentoFlexODM, String> {
}
