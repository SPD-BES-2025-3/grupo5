package com.bookingbarber.sys.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "tb_profissional")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Profissional extends Usuario {

    private String numeroRegistro;

    private Date dataContratacao;

    private Double percentualComissao;

    @OneToMany(mappedBy = "profissional")
    private Set<Agendamento> agendamentos;
    @ManyToMany
    @JoinTable(name = "profissiona_especialidade",
        joinColumns = @JoinColumn(name = "profissiona_id"),
        inverseJoinColumns = @JoinColumn(name = "especialidade_id"))
    private Set<Especialidade> especialidades;

    public Profissional(){}

    public Profissional(Long id, String nome, String sobrenome, String cpf, String email, String password, String urlFotoPerfil, String numeroRegistro, Date dataContratacao, Double percentualComissao) {
        super(id, nome, sobrenome, cpf, email, password, urlFotoPerfil);
        this.numeroRegistro = numeroRegistro;
        this.dataContratacao = dataContratacao;
        this.percentualComissao = percentualComissao;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public void setNumeroRegistro(String numeroRegistro) {
        this.numeroRegistro = numeroRegistro;
    }

    public Date getDataContratacao() {
        return dataContratacao;
    }

    public void setDataContratacao(Date dataContratacao) {
        this.dataContratacao = dataContratacao;
    }

    public Double getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(Double percentualComissao) {
        this.percentualComissao = percentualComissao;
    }
}
