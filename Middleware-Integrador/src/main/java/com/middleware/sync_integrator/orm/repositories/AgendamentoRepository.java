package com.middleware.sync_integrator.orm.repositories;

import com.middleware.sync_integrator.orm.entities.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("SELECT a FROM Agendamento a WHERE a.profissional.id = :profissionalId " +
            "AND a.status <> 'CANCELADO' " +
            "AND a.horarioInicio < :fimDoIntervalo " +
            "AND a.horarioFim > :inicioDoIntervalo")
    List<Agendamento> findOverlappingAppointments(
            Long profissionalId,
            OffsetDateTime inicioDoIntervalo,
            OffsetDateTime fimDoIntervalo
    );

    /**
     * CORRIGIDO: O nome do método agora reflete o campo `horarioInicio` da entidade.
     * Busca todos os agendamentos de um profissional cujo horário de início está dentro de um período.
     */
    List<Agendamento> findAllByProfissionalIdAndHorarioInicioBetween(
            Long profissionalId,
            OffsetDateTime inicioDoDia,
            OffsetDateTime fimDoDia
    );
}
