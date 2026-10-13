package com.adeptview.expenseportal.costcentres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Where a cost is charged. Every cost centre belongs to a department, and the
 * department's manager approves the claims charged to it.
 */
@Entity
@Table(name = "cost_centres")
public class CostCentre {

    @Id
    @Column(length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    protected CostCentre() {
    }

    public CostCentre(String code, String name, String departmentCode) {
        this.code = code;
        this.name = name;
        this.departmentCode = departmentCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }
}
