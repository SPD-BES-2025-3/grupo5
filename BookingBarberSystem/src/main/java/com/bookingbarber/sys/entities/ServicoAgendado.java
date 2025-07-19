package com.bookingbarber.sys.entities;

import com.bookingbarber.sys.entities.enums.StatusAgendamento;
import jakarta.persistence.*;

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

    private Double valor;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento statusAgendamento;

    public ServicoAgendado(){}

    public ServicoAgendado(Long id, Agendamento agendamento, Servico servico, Double valor, StatusAgendamento statusAgendamento) {
        this.id = id;
        this.agendamento = agendamento;
        this.servico = servico;
        this.valor = valor;
        this.statusAgendamento = statusAgendamento;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public StatusAgendamento getStatusAgendamento() {
        return statusAgendamento;
    }

    public void setStatusAgendamento(StatusAgendamento statusAgendamento) {
        this.statusAgendamento = statusAgendamento;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public Servico getServico() {
        return servico;
    }
}
