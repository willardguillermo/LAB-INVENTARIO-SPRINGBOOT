package com.willard.inventario.repository;

import com.willard.inventario.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long>,
        JpaSpecificationExecutor<Auditoria> {
}
