package com.adeptview.expenseportal.imports;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.auth.User;
import com.adeptview.expenseportal.auth.UserRepository;
import com.adeptview.expenseportal.claims.ClaimService;
import com.adeptview.expenseportal.common.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Imports claims exported from the previous expense system:
 *
 * <pre>
 * &lt;claims&gt;
 *   &lt;claim owner="alice" date="2026-10-05" amount="38.20" costCentre="ENG"&gt;Airport parking&lt;/claim&gt;
 * &lt;/claims&gt;
 * </pre>
 *
 * The whole file is imported or none of it.
 */
@Service
public class ClaimImportService {

    private static final Logger log = LoggerFactory.getLogger(ClaimImportService.class);

    private final UserRepository users;
    private final ClaimService claims;

    public ClaimImportService(UserRepository users, ClaimService claims) {
        this.users = users;
        this.claims = claims;
    }

    @Transactional
    public ImportResult importClaims(String xml, CurrentUser importer)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        NodeList entries = document.getElementsByTagName("claim");
        List<Long> created = new ArrayList<>();
        for (int i = 0; i < entries.getLength(); i++) {
            created.add(importOne((Element) entries.item(i), i + 1, importer));
        }

        log.info("Imported {} claim(s) for {}", created.size(), importer.username());
        return new ImportResult(created.size(), created);
    }

    private Long importOne(Element entry, int position, CurrentUser importer) {
        String owner = entry.getAttribute("owner");
        User employee = users.findByUsername(owner)
                .orElseThrow(() -> new BadRequestException("Claim " + position + ": unknown employee '" + owner + "'"));

        String title = entry.getTextContent().trim();
        if (title.isEmpty()) {
            throw new BadRequestException("Claim " + position + ": the title is missing");
        }

        return claims.recordImported(employee, title, amountOf(entry, position), dateOf(entry, position),
                entry.getAttribute("costCentre"), importer.username());
    }

    private static BigDecimal amountOf(Element entry, int position) {
        try {
            BigDecimal amount = new BigDecimal(entry.getAttribute("amount"));
            if (amount.signum() <= 0) {
                throw new BadRequestException("Claim " + position + ": the amount must be positive");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Claim " + position + ": the amount is not a number");
        }
    }

    private static LocalDate dateOf(Element entry, int position) {
        try {
            return LocalDate.parse(entry.getAttribute("date"));
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Claim " + position + ": the date must look like 2026-10-05");
        }
    }
}
