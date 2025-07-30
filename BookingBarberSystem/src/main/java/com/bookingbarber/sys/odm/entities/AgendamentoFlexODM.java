package com.bookingbarber.sys.odm.entities;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "solicitacoes_agendamento_odm")
public class AgendamentoFlexODM {

    @Id
    private String id; // ID do MongoDB (String)

    // --- Dados Denormalizados Recebidos da UI ---
    private Long clienteId; // ID relacional do cliente logado
    private String profissionalEmail;
    private List<String> servicosNomes;
    private OffsetDateTime dataHora;
    private Map<String, String> atributosDinamicos;

    // --- Campos de Controle de Sincronização ---
    private String status; // Ex: "PENDENTE_NORMALIZACAO", "SINCRONIZADO_COM_ERRO", "SINCRONIZADO_OK"

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getProfissionalEmail() {
        return profissionalEmail;
    }

    public void setProfissionalEmail(String profissionalEmail) {
        this.profissionalEmail = profissionalEmail;
    }

    public List<String> getServicosNomes() {
        return servicosNomes;
    }

    public void setServicosNomes(List<String> servicosNomes) {
        this.servicosNomes = servicosNomes;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(OffsetDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Map<String, String> getAtributosDinamicos() {
        return atributosDinamicos;
    }

    public void setAtributosDinamicos(Map<String, String> atributosDinamicos) {
        this.atributosDinamicos = atributosDinamicos;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
