package com.cookhub.mjjo.service.register;

import com.cookhub.mjjo.dto.register.RegisterRequest;
import com.cookhub.mjjo.dto.register.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.cookhub.mjjo.jooq.generated.tables.ChUsers.CH_USERS;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final DSLContext dsl;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public RegisterResponse register(RegisterRequest req) {
        boolean exists = dsl.fetchExists(
                dsl.selectFrom(CH_USERS).where(CH_USERS.USER_EMAIL.eq(req.getEmail()))
        );
        if (exists) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        var rec = dsl.insertInto(CH_USERS)
                .set(CH_USERS.USER_EMAIL, req.getEmail())
                .set(CH_USERS.USER_PW, encoder.encode(req.getPassword()))
                .set(CH_USERS.USER_NAME, req.getName())
                .returning(CH_USERS.USER_NO, CH_USERS.USER_EMAIL, CH_USERS.USER_NAME)
                .fetchOne();

        return new RegisterResponse(
                rec.get(CH_USERS.USER_NO),
                rec.get(CH_USERS.USER_EMAIL),
                rec.get(CH_USERS.USER_NAME)
        );
    }

    public RegisterResponse getUserById(Integer userNo) {
        var rec = dsl.selectFrom(CH_USERS)
                .where(CH_USERS.USER_NO.eq(userNo))
                .fetchOne();

        if (rec == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return new RegisterResponse(
                rec.get(CH_USERS.USER_NO),
                rec.get(CH_USERS.USER_EMAIL),
                rec.get(CH_USERS.USER_NAME)
        );
    }
}
