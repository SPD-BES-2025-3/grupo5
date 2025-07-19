package com.bookingbarber.sys.repositorys;

import com.bookingbarber.sys.entities.Agendamento;
import com.bookingbarber.sys.entities.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {
}
