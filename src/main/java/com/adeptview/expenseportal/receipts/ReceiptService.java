package com.adeptview.expenseportal.receipts;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

import com.adeptview.expenseportal.auth.CurrentUser;
import com.adeptview.expenseportal.claims.Claim;
import com.adeptview.expenseportal.claims.ClaimRepository;
import com.adeptview.expenseportal.claims.ClaimService;
import com.adeptview.expenseportal.claims.ClaimView;
import com.adeptview.expenseportal.common.BadRequestException;
import com.adeptview.expenseportal.common.ConflictException;
import com.adeptview.expenseportal.common.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Receipts are fingerprinted when attached, so the same receipt cannot be
 * claimed twice.
 */
@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private static final int MAX_NAME_LENGTH = 100;

    private final ClaimRepository claims;
    private final ClaimService claimService;

    public ReceiptService(ClaimRepository claims, ClaimService claimService) {
        this.claims = claims;
        this.claimService = claimService;
    }

    @Transactional
    public ClaimView attach(long claimId, MultipartFile file, CurrentUser user)
            throws IOException, NoSuchAlgorithmException {
        Claim claim = find(claimId);
        if (!Objects.equals(claim.getOwnerId(), user.id())) {
            throw new AccessDeniedException("Only the claimant can attach a receipt");
        }
        if (file.isEmpty()) {
            throw new BadRequestException("The receipt file is empty");
        }

        byte[] content = file.getBytes();
        String fingerprint = checksum(content);
        Optional<Claim> earlier = claims.findFirstByReceiptChecksumAndIdNot(fingerprint, claimId);
        if (earlier.isPresent()) {
            throw new ConflictException("This receipt is already attached to claim " + earlier.get().getId());
        }

        claim.attachReceipt(safeName(file.getOriginalFilename()), fingerprint, content);
        log.info("Receipt attached to claim {} ({} bytes)", claimId, content.length);
        return claimService.view(claim);
    }

    String checksum(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hashed = digest.digest(content);
        return String.format("%032x", new BigInteger(1, hashed));
    }

    @Transactional(readOnly = true)
    public Receipt download(long claimId, CurrentUser user) {
        Claim claim = find(claimId);
        claimService.ensureCanView(claim, user);
        if (claim.getReceiptData() == null) {
            throw new NotFoundException("Claim " + claimId + " has no receipt");
        }
        return new Receipt(claim.getReceiptName(), claim.getReceiptData());
    }

    private Claim find(long claimId) {
        return claims.findById(claimId).orElseThrow(() -> new NotFoundException("Claim " + claimId + " not found"));
    }

    private static String safeName(String original) {
        String name = original == null ? "" : original.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.isBlank()) {
            return "receipt";
        }
        return name.length() > MAX_NAME_LENGTH ? name.substring(name.length() - MAX_NAME_LENGTH) : name;
    }

    public record Receipt(String name, byte[] content) {
    }
}
