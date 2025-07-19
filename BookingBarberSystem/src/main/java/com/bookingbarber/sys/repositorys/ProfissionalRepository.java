package com.bookingbarber.sys.repositorys;

import com.bookingbarber.sys.entities.Cliente;
import com.bookingbarber.sys.entities.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
}
