package com.adeptview.expenseportal.claims;

import java.util.List;

import jakarta.validation.Valid;

import com.adeptview.expenseportal.auth.CurrentUser;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claims;

    public ClaimController(ClaimService claims) {
        this.claims = claims;
    }

    @GetMapping
    public List<ClaimSummary> mine(@AuthenticationPrincipal Jwt jwt) {
        return claims.mine(CurrentUser.from(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimView submit(@Valid @RequestBody NewClaimRequest request, @AuthenticationPrincipal Jwt jwt) {
        return claims.submit(request, CurrentUser.from(jwt));
    }

    @GetMapping("/{id}")
    public ClaimView get(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return claims.get(id, CurrentUser.from(jwt));
    }
}
