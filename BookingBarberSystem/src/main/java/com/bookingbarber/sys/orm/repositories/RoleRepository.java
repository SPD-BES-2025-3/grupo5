package com.bookingbarber.sys.orm.repositories;

import com.bookingbarber.sys.orm.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Object findByAuthority(String roleCliente);
}
