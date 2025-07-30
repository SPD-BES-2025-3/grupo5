package com.middleware.sync_integrator.orm.entities;

import com.middleware.sync_integrator.orm.entities.enums.StatusAgendamento;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_servico_agendado")
public class ServicoAgendado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id")
    private Agendamento agendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento statusAgendamento;

    public ServicoAgendado(){}

    public ServicoAgendado(Long id, Agendamento agendamento, Servico servico, BigDecimal valor, StatusAgendamento statusAgendamento) {
        this.id = id;
        this.agendamento = agendamento;
        this.servico = servico;
        this.valor = valor;
        this.statusAgendamento = statusAgendamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusAgendamento getStatusAgendamento() {
        return statusAgendamento;
    }

    public void setStatusAgendamento(StatusAgendamento statusAgendamento) {
        this.statusAgendamento = statusAgendamento;
    }
}
