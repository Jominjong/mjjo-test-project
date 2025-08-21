package com.cookhub.mjjo.service.recipe;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cookhub.mjjo.dto.recipe.RecipeUpdateRequest;

import static com.cookhub.mjjo.jooq.generated.tables.ChBoard.CH_BOARD;
import static com.cookhub.mjjo.jooq.generated.tables.ChIngredients.CH_INGREDIENTS;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeEditService {

    private final DSLContext dsl;

    @Transactional
    public void update(Integer boardNo, RecipeUpdateRequest req) {
        // 1) 게시글 업데이트 (DB 시간 사용)
        int updated = dsl.update(CH_BOARD)
                .set(CH_BOARD.BOARD_TITLE, req.title())       // 컬럼명 실제 스키마와 일치 확인!
                .set(CH_BOARD.BOARD_CON, req.content())     // 컬럼명 확인!
                .set(CH_BOARD.CG_NO, req.categoryNo())
                .set(CH_BOARD.UPDATED_AT, LocalDateTime.now())
                .where(CH_BOARD.BOARD_NO.eq(boardNo))
                .execute();

        if (updated == 0) {
            // 존재하지 않는 게시글
            throw new IllegalArgumentException("존재하지 않는 게시글입니다. boardNo=" + boardNo);
        }

        // 2) 재료 전체 삭제
        dsl.deleteFrom(CH_INGREDIENTS)
           .where(CH_INGREDIENTS.BOARD_NO.eq(boardNo))
           .execute();

        // 3) 재삽입 (batchInsert 추천)
        List<RecipeUpdateRequest.Ingredient> ings = req.ingredients();
        if (ings != null && !ings.isEmpty()) {
            var step = dsl.insertInto(
                CH_INGREDIENTS,
                CH_INGREDIENTS.BOARD_NO,
                CH_INGREDIENTS.ING_NAME,
                CH_INGREDIENTS.ING_AMOUNT
            );
            for (var ing : ings) {
                step.values(boardNo, ing.name(), ing.amount()); // 클래스 DTO면 getter
            }
            step.execute();
        }
    }
}
