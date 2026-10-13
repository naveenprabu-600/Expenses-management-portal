package com.adeptview.expenseportal.costcentres;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CostCentreRepository extends JpaRepository<CostCentre, String> {

    List<CostCentre> findAllByOrderByCodeAsc();
}
