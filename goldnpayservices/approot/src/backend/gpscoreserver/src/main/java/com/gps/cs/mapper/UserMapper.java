package com.gps.cs.mapper;

import com.gps.cs.dto.ClientDtos.*;
import com.gps.cs.dto.UserDtos.UserResponse;
import com.gps.cs.entity.BankAccount;
import com.gps.cs.entity.KycDocument;
import com.gps.cs.entity.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getAddress(), u.getRole(),
                u.getStatus(), u.getKycStatus(), u.getCreatedAt());
    }

    public static KycDocumentResponse toResponse(KycDocument d) {
        return new KycDocumentResponse(d.getId(), d.getDocType(), d.getContentType(), d.getSizeBytes(), d.getStatus(),
                d.getRejectReason(), d.getCreatedAt());
    }

    public static BankAccountResponse toResponse(BankAccount b) {
        return new BankAccountResponse(b.getId(), b.getAccountHolder(), "XXXXXX" + b.getAccountLast4(), b.getIfsc(),
                b.getBankName(), b.isPrimaryAccount());
    }
}
