package com.middleware.sync_integrator.orm.repositories;


import com.middleware.sync_integrator.orm.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Object findByAuthority(String roleCliente);
}
