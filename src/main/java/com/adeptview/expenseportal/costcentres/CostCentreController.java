package com.adeptview.expenseportal.costcentres;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cost-centres")
public class CostCentreController {

    private final CostCentreRepository costCentres;

    public CostCentreController(CostCentreRepository costCentres) {
        this.costCentres = costCentres;
    }

    @GetMapping
    public List<CostCentreView> list() {
        return costCentres.findAllByOrderByCodeAsc().stream()
                .map(centre -> new CostCentreView(centre.getCode(), centre.getName()))
                .toList();
    }

    public record CostCentreView(String code, String name) {
    }
}
