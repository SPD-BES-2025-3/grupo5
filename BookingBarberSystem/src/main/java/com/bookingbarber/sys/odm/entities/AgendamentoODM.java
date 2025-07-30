package com.bookingbarber.sys.odm.entities;

import com.bookingbarber.sys.odm.dto.ClienteOdmDTO;
import com.bookingbarber.sys.odm.dto.ProfissionalOdmDTO;
import com.bookingbarber.sys.odm.dto.ServicoOdmDTO;
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

    private ClienteOdmDTO clienteODM;
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

    public ClienteOdmDTO getClienteODM() {
        return clienteODM;
    }

    public void setClienteODM(ClienteOdmDTO clienteODM) {
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
}
