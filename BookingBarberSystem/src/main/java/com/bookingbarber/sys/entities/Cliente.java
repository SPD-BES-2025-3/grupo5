package com.bookingbarber.sys.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "tb_cliente")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Cliente extends Usuario{

    private Date dataCriacao;

    private Date ultimaVisita;

    private Integer pontosFidelidade;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private Set<Agendamento> agendamentos;

    public Cliente(){}

    public Cliente(Long id, String nome, String sobrenome, String cpf, String email, String password, String urlFotoPerfil, Date dataCriacao, Date ultimaVisita, Integer pontosFidelidade) {
        super(id, nome, sobrenome, cpf, email, password, urlFotoPerfil);
        this.dataCriacao = dataCriacao;
        this.ultimaVisita = ultimaVisita;
        this.pontosFidelidade = pontosFidelidade;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getUltimaVisita() {
        return ultimaVisita;
    }

    public void setUltimaVisita(Date ultimaVisita) {
        this.ultimaVisita = ultimaVisita;
    }

    public Integer getPontosFidelidade() {
        return pontosFidelidade;
    }

    public void setPontosFidelidade(Integer pontosFidelidade) {
        this.pontosFidelidade = pontosFidelidade;
    }
}
