package com.middleware.sync_integrator.odm.service;

import com.middleware.sync_integrator.odm.entities.AgendamentoODM;
import com.middleware.sync_integrator.odm.repositories.AgendamentoODMRespository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AgendamentoODMService {
    private AgendamentoODMRespository agendamentoODMRespository;

    public void salvarAgendamento(AgendamentoODM documento){
        agendamentoODMRespository.save(documento);
    }

    public AgendamentoODM buscarAgendamentoPorId(String id){
        return agendamentoODMRespository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Agendamento não encontrado."));
    }

    public List<AgendamentoODM> listarAgendamentoPorProfissionalEData(Long profissionalId, LocalDate date){
        OffsetDateTime inicioDoDia = date.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toOffsetDateTime();
        OffsetDateTime fimDoDia = date.atTime(LocalTime.MAX).atZone(ZoneId.of("America/Sao_Paulo")).toOffsetDateTime();
        return  agendamentoODMRespository.findByProfissionalODM_ProfissionalIdAndHorarioInicioBetween(profissionalId, inicioDoDia, fimDoDia);
    }

    public void deletarAgendamento(String id){
        agendamentoODMRespository.deleteById(id);
    }

}
