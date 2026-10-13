package com.adeptview.expenseportal.imports;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import com.adeptview.expenseportal.auth.CurrentUser;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

@RestController
@RequestMapping("/api/imports")
public class ClaimImportController {

    private final ClaimImportService imports;

    public ClaimImportController(ClaimImportService imports) {
        this.imports = imports;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ImportResult importClaims(@RequestBody String xml, @AuthenticationPrincipal Jwt jwt)
            throws ParserConfigurationException, SAXException, IOException {
        return imports.importClaims(xml, CurrentUser.from(jwt));
    }
}
