package com.bookingbarber.sys.repositorys;

import com.bookingbarber.sys.entities.ServicoAgendado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoAgendadoRepository extends JpaRepository<ServicoAgendado, Long> {
}
