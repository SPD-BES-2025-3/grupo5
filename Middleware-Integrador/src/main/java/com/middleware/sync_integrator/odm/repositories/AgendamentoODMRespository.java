package com.middleware.sync_integrator.odm.repositories;

import com.middleware.sync_integrator.odm.entities.AgendamentoODM;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoODMRespository extends MongoRepository<AgendamentoODM, String> {

    List<AgendamentoODM> findByProfissionalODM_ProfissionalIdAndHorarioInicioBetween(Long profissionalId, OffsetDateTime inicio, OffsetDateTime fim);

    Optional<AgendamentoODM> findByAgendamentoId(Long agendamentoOrmId);
}
