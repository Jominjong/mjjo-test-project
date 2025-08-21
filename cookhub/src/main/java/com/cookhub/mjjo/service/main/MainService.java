package com.cookhub.mjjo.service.main;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import com.cookhub.mjjo.dto.main.MainResponse;

import static com.cookhub.mjjo.jooq.generated.tables.ChBoard.CH_BOARD;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {

    private final DSLContext dsl;

    public MainResponse list(int page, int size, Integer categoryNo, String keyword) {
        int pageSafe = Math.max(page, 1);
        int sizeSafe = Math.min(Math.max(size, 1), 100);
        int offset = (pageSafe - 1) * sizeSafe;

        var cond = CH_BOARD.BOARD_NO.isNotNull(); // always true, 점진적으로 조건 추가
        if (categoryNo != null) {
            cond = cond.and(CH_BOARD.CG_NO.eq(categoryNo));
        }
        if (keyword != null && !keyword.isBlank()) {
            var like = "%" + keyword.trim() + "%";
            cond = cond.and(CH_BOARD.BOARD_TITLE.likeIgnoreCase(like));
        }

        long total = dsl.selectCount()
                .from(CH_BOARD)
                .where(cond)
                .fetchOne(0, long.class);

        List<MainResponse.Item> items = dsl.select(
                    CH_BOARD.BOARD_NO,
                    CH_BOARD.BOARD_TITLE,
                    CH_BOARD.CG_NO,
                    CH_BOARD.USER_NO,
                    CH_BOARD.CREATED_AT,
                    CH_BOARD.UPDATED_AT
                )
                .from(CH_BOARD)
                .where(cond)
                .orderBy(CH_BOARD.CREATED_AT.desc())
                .limit(sizeSafe)
                .offset(offset)
                .fetch(this::toItem);

        int totalPages = (int) Math.ceil(total / (double) sizeSafe);
        boolean hasNext = pageSafe < totalPages;

        return new MainResponse(items, pageSafe, sizeSafe, total, totalPages, hasNext);
    }

    private MainResponse.Item toItem(Record r) {
        return new MainResponse.Item(
                r.get(CH_BOARD.BOARD_NO),
                r.get(CH_BOARD.BOARD_TITLE),
                r.get(CH_BOARD.CG_NO),
                r.get(CH_BOARD.USER_NO),
                r.get(CH_BOARD.CREATED_AT),
                r.get(CH_BOARD.UPDATED_AT)
        );
    }
}
