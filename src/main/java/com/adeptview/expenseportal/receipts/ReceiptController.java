package com.adeptview.expenseportal.receipts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.claims.ClaimView;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/claims/{id}/receipt")
public class ReceiptController {

    private final ReceiptService receipts;

    public ReceiptController(ReceiptService receipts) {
        this.receipts = receipts;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClaimView attach(@PathVariable long id, @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) throws IOException, NoSuchAlgorithmException {
        return receipts.attach(id, file, CurrentUser.from(jwt));
    }

    @GetMapping
    public ResponseEntity<byte[]> download(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        ReceiptService.Receipt receipt = receipts.download(id, CurrentUser.from(jwt));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(receipt.name(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(receipt.content());
    }
}
