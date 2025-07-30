package com.middleware.sync_integrator.messaging.service;

import com.middleware.sync_integrator.odm.dto.ClienteOmdDTO;
import com.middleware.sync_integrator.odm.dto.ProfissionalOdmDTO;
import com.middleware.sync_integrator.odm.dto.ServicoOdmDTO;
import com.middleware.sync_integrator.odm.entities.AgendamentoODM;
import com.middleware.sync_integrator.odm.repositories.AgendamentoODMRespository;
import com.middleware.sync_integrator.orm.entities.Agendamento;
import com.middleware.sync_integrator.orm.entities.Servico;
import com.middleware.sync_integrator.orm.repositories.AgendamentoRepository;
import com.middleware.sync_integrator.orm.repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class OrmToOdmTransformerService {
    @Autowired
    private AgendamentoODMRespository agendamentoODMRespository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;


    @Transactional(readOnly = true)
    public void transformar(Long agendamentoOrmId){
        System.out.println("Iniciando transformação ORM para ODM.");

        Agendamento agendamentoORM = agendamentoRepository.findById(agendamentoOrmId)
                .orElseThrow(()->new EntityNotFoundException("Agendamento ORM não encontrado"));

        String nomeClienteCompleto = agendamentoORM.getCliente().getNome() + " " + agendamentoORM.getCliente().getSobrenome();
        var cliente = new ClienteOmdDTO(agendamentoORM.getCliente().getId(), nomeClienteCompleto);

        String nomeProfissionalCompleto = agendamentoORM.getProfissional().getNome() + " " + agendamentoORM.getProfissional().getSobrenome();
        var profissional = new ProfissionalOdmDTO(agendamentoORM.getProfissional().getId(), nomeProfissionalCompleto);

        List<ServicoOdmDTO> servicos = agendamentoORM.getServicosAgendados().stream()
                .map(sa -> new ServicoOdmDTO(sa.getServico().getId(), sa.getServico().getNome(), sa.getValor(),sa.getServico().getDuracao())).toList();

        AgendamentoODM agendamentoODM = agendamentoODMRespository.findByAgendamentoId(agendamentoOrmId)
                .orElse(new AgendamentoODM());

        agendamentoODM.setAgendamentoId(agendamentoORM.getId());
        agendamentoODM.setHorarioInicio(agendamentoORM.getHorarioInicio());
        agendamentoODM.setHorarioFim(agendamentoORM.getHorarioFim());
        agendamentoODM.setValorTotal(agendamentoORM.getValorTotal());
        agendamentoODM.setStatusAgendamento(agendamentoORM.getStatus());
        agendamentoODM.setClienteODM(cliente);
        agendamentoODM.setProfissionalODM(profissional);
        agendamentoODM.setServicosODM(servicos);

        agendamentoODMRespository.save(agendamentoODM);

        System.out.println("Agendamento " + agendamentoOrmId + " sincronizado com sucesso para o MongoDB.");
    }

    public void deletarDocumento(Long agendamentoOrmId) {
        agendamentoODMRespository.findByAgendamentoId(agendamentoOrmId).ifPresent(doc -> {
            agendamentoODMRespository.delete(doc);
            System.out.println("Documento do agendamento " + agendamentoOrmId + " removido do MongoDB.");
        });
    }
}
