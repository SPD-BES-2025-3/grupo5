package com.bookingbarber.sys.orm.repositories;

import com.bookingbarber.sys.orm.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<Object> findByEmailAndIdNot(String email, Long id);

    Optional<Object> findByCpfAndIdNot(String cpf, Long id);
}
