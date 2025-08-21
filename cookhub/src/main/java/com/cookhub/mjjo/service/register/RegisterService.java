package com.cookhub.mjjo.service.register;

import com.cookhub.mjjo.dto.register.RegisterRequest;
import com.cookhub.mjjo.dto.register.RegisterResponse;
import com.cookhub.jooq.generated.tables.ChUsers;
import com.cookhub.jooq.generated.tables.records.ChUsersRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // optional, 암호화용

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final DSLContext dsl;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RegisterResponse register(RegisterRequest request) {
        ChUsersRecord record = dsl.newRecord(ChUsers.CH_USERS);
        record.setUserEmail(request.getEmail());
        record.setUserPw(passwordEncoder.encode(request.getPassword())); // optional
        record.setUserName(request.getName());
        record.store();

        return new RegisterResponse(record.getUserNo(), record.getUserEmail(), record.getUserName());
    }

    public RegisterResponse getUserById(Integer userNo) {
        ChUsersRecord record = dsl.selectFrom(ChUsers.CH_USERS)
                                   .where(ChUsers.CH_USERS.USER_NO.eq(userNo))
                                   .fetchOne();

        if (record == null) return null;

        return new RegisterResponse(record.getUserNo(), record.getUserEmail(), record.getUserName());
    }
}
