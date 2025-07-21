package com.bookingbarber.sys.repositorys;

import com.bookingbarber.sys.entities.Role;
import com.bookingbarber.sys.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Object findByAuthority(String roleCliente);
}
