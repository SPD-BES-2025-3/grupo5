package com.bookingbarber.sys.odm.repositories;

import com.bookingbarber.sys.odm.entities.AgendamentoODM;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AgendamentoODMRespository extends MongoRepository<AgendamentoODM, String> {

    List<AgendamentoODM> findByProfissionalODM_ProfissionalIdAndHorarioInicioBetween(Long profissionalId, OffsetDateTime inicio, OffsetDateTime fim);
}
