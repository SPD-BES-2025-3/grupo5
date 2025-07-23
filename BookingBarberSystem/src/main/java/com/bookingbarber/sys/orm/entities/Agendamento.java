package com.bookingbarber.sys.orm.entities;

import com.bookingbarber.sys.orm.entities.enums.StatusAgendamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_agendamento")
public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OffsetDateTime horarioInicio;

    private OffsetDateTime horarioFim;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    private BigDecimal valorTotal;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    private Profissional profissional;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ServicoAgendado> servicosAgendados;

    public Agendamento(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Set<ServicoAgendado> getServicosAgendados() {
        return servicosAgendados;
    }

    public void setServicosAgendados(Set<ServicoAgendado> servicosAgendados) {
        this.servicosAgendados = servicosAgendados;
    }
}
