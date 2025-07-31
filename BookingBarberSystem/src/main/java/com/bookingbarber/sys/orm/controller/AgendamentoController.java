package com.bookingbarber.sys.orm.controller;

import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoRequestDTO;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoResponseDTO;
import com.bookingbarber.sys.orm.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controlador responsável por gerenciar as operações relacionadas a agendamentos.
 * Oferece endpoints para criação, listagem, consulta por cliente e exclusão de agendamentos.
 *
 * @author Guilherme
 * @since 1.0
 */
@Controller
@RequestMapping("/api-orm/agendamento")
public class AgendamentoController {

    /**
     * Serviço responsável pela lógica de negócio relacionada aos agendamentos.
     */
    @Autowired
    private AgendamentoService agendamentoService;

    /**
     * Realiza o agendamento com base nas informações fornecidas.
     *
     * @param dto Objeto contendo os dados necessários para criar um agendamento
     * @return ResponseEntity contendo os dados do agendamento criado
     */
    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> agendar(@RequestBody AgendamentoRequestDTO dto){
        var  agendamentoCriado = agendamentoService.insertAgendamento(dto);
        return new ResponseEntity<>(agendamentoCriado, HttpStatus.OK);
    }

    /**
     * Retorna a lista de todos os agendamentos cadastrados no sistema.
     *
     * @return ResponseEntity com a lista de agendamentos
     */
    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> findAllAgendamentos(){
        var  agendamentoCriado = agendamentoService.findAllAgendamentos();
        return new ResponseEntity<>(agendamentoCriado, HttpStatus.CREATED);
    }

    /**
     * Retorna o histórico de agendamentos de um cliente específico.
     *
     * @param clienteId ID do cliente a ser consultado
     * @return ResponseEntity com a lista de agendamentos do cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarHistoricoPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(agendamentoService.listarPorCliente(clienteId));
    }

    /**
     * Remove um agendamento com base no seu ID.
     *
     * @param id Identificador do agendamento a ser deletado
     * @return ResponseEntity com status HTTP 204 (No Content) em caso de sucesso
     */
    @DeleteMapping(value = "/deletar-registro/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        agendamentoService.deletarRegistro(id);
        return ResponseEntity.noContent().build();
    }


}
