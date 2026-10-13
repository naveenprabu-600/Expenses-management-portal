package com.adeptview.expenseportal.imports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.auth.Role;
import com.adeptview.expenseportal.auth.User;
import com.adeptview.expenseportal.auth.UserRepository;
import com.adeptview.expenseportal.claims.ClaimService;
import com.adeptview.expenseportal.common.BadRequestException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.SAXException;

@ExtendWith(MockitoExtension.class)
class ClaimImportServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private ClaimService claims;

    @InjectMocks
    private ClaimImportService imports;

    private final CurrentUser fiona = new CurrentUser(5L, "fiona", Role.FINANCE);

    @Test
    void importsEveryClaimInTheFile() throws Exception {
        User alice = employee("alice", "ENG");
        User priya = employee("priya.raman", "FIN");
        when(users.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(users.findByUsername("priya.raman")).thenReturn(Optional.of(priya));
        when(claims.recordImported(any(User.class), anyString(), any(BigDecimal.class), any(LocalDate.class),
                anyString(), eq("fiona"))).thenReturn(2001L, 2002L);

        String xml = """
                <claims>
                  <claim owner="alice" date="2026-10-05" amount="38.20" costCentre="ENG">Airport parking</claim>
                  <claim owner="priya.raman" date="2026-10-06" amount="120.00" costCentre="FIN">Audit workshop</claim>
                </claims>
                """;

        ImportResult result = imports.importClaims(xml, fiona);

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.claimIds()).containsExactly(2001L, 2002L);
        verify(claims).recordImported(alice, "Airport parking", new BigDecimal("38.20"), LocalDate.of(2026, 10, 5),
                "ENG", "fiona");
    }

    @Test
    void refusesAnUnknownEmployee() {
        when(users.findByUsername("zoe")).thenReturn(Optional.empty());
        String xml = "<claims><claim owner=\"zoe\" date=\"2026-10-05\" amount=\"10.00\" costCentre=\"ENG\">Lunch</claim></claims>";

        assertThatThrownBy(() -> imports.importClaims(xml, fiona))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("zoe");
    }

    @Test
    void refusesAFileThatIsNotXml() {
        assertThatThrownBy(() -> imports.importClaims("owner,date,amount", fiona)).isInstanceOf(SAXException.class);
    }

    private static User employee(String username, String department) {
        return new User(username, username, "unused", Role.EMPLOYEE, department);
    }
}
