package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfilRecrutadorRepository extends JpaRepository<PerfilRecrutador, Long> {

    List<PerfilRecrutador> findByEmpresaVinculadaIdAndStatusVinculoEmpresa(
            Long empresaId,
            StatusVinculoEmpresa statusVinculoEmpresa
    );
}
