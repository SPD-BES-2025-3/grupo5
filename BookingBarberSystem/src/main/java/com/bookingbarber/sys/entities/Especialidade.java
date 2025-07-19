package com.bookingbarber.sys.entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "tb_especialidade")
public class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    public Especialidade(){}

    @ManyToMany(mappedBy = "especialidades")
    private Set<Profissional> profissionais;

    public Especialidade(Long id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
