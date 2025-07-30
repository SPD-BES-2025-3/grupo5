package com.middleware.sync_integrator.odm.entities;

import com.middleware.sync_integrator.odm.dto.ClienteOmdDTO;
import com.middleware.sync_integrator.odm.dto.ProfissionalOdmDTO;
import com.middleware.sync_integrator.odm.dto.ServicoOdmDTO;
import com.middleware.sync_integrator.orm.entities.enums.StatusAgendamento;
import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Document(collection = "agendamentos_denormalizados")
public class AgendamentoODM {
    @Id
    private String id;
    private Long agendamentoId;
    private OffsetDateTime horarioInicio;
    private OffsetDateTime horarioFim;
    private BigDecimal valorTotal;
    private StatusAgendamento statusAgendamento;

    private ClienteOmdDTO clienteODM;
    private ProfissionalOdmDTO profissionalODM;
    private List<ServicoOdmDTO> servicosODM;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(Long agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public OffsetDateTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(OffsetDateTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public OffsetDateTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(OffsetDateTime horarioFim) {
        this.horarioFim = horarioFim;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public ClienteOmdDTO getClienteODM() {
        return clienteODM;
    }

    public void setClienteODM(ClienteOmdDTO clienteODM) {
        this.clienteODM = clienteODM;
    }

    public ProfissionalOdmDTO getProfissionalODM() {
        return profissionalODM;
    }

    public void setProfissionalODM(ProfissionalOdmDTO profissionalODM) {
        this.profissionalODM = profissionalODM;
    }

    public List<ServicoOdmDTO> getServicosODM() {
        return servicosODM;
    }

    public void setServicosODM(List<ServicoOdmDTO> servicosODM) {
        this.servicosODM = servicosODM;
    }

    public StatusAgendamento getStatusAgendamento() {
        return statusAgendamento;
    }

    public void setStatusAgendamento(StatusAgendamento statusAgendamento) {
        this.statusAgendamento = statusAgendamento;
    }

}
