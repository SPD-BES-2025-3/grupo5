package com.middleware.sync_integrator.orm.entities;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_cliente")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Cliente extends Usuario{

    private OffsetDateTime dataCriacao;

    private OffsetDateTime ultimaVisita;

    private Integer pontosFidelidade;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private Set<Agendamento> agendamentos;

    public Cliente(){}

    public Cliente(Long id, String nome, String sobrenome, String cpf, String email, String password, String urlFotoPerfil, OffsetDateTime dataCriacao, OffsetDateTime ultimaVisita, Integer pontosFidelidade) {
        super(id, nome, sobrenome, cpf, email, password, urlFotoPerfil);
        this.dataCriacao = dataCriacao;
        this.ultimaVisita = ultimaVisita;
        this.pontosFidelidade = pontosFidelidade;
    }


    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(OffsetDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public OffsetDateTime getUltimaVisita() {
        return ultimaVisita;
    }

    public void setUltimaVisita(OffsetDateTime ultimaVisita) {
        this.ultimaVisita = ultimaVisita;
    }

    public Integer getPontosFidelidade() {
        return pontosFidelidade;
    }

    public void setPontosFidelidade(Integer pontosFidelidade) {
        this.pontosFidelidade = pontosFidelidade;
    }
}
