package com.bookingbarber.sys.orm.repositories;

import com.bookingbarber.sys.orm.entities.ServicoAgendado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoAgendadoRepository extends JpaRepository<ServicoAgendado, Long> {
    boolean existsByServicoId(Long id);
}
