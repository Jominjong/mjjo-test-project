package com.cookhub.mjjo.service.recipe;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cookhub.mjjo.dto.recipe.RecipeUpdateRequest;

import static com.cookhub.mjjo.jooq.generated.tables.ChBoard.CH_BOARD;
import static com.cookhub.mjjo.jooq.generated.tables.ChIngredients.CH_INGREDIENTS;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecipeEditService {

    private final DSLContext dsl;

    @Transactional
    public void update(Integer boardNo, RecipeUpdateRequest req) {
        // BOARD UPDATE
        dsl.update(CH_BOARD)
           .set(CH_BOARD.BOARD_TITLE, req.title())
           .set(CH_BOARD.BOARD_CON, req.content())
           .set(CH_BOARD.CG_NO, req.categoryNo())
           .set(CH_BOARD.UPDATED_AT, LocalDateTime.now())
           .where(CH_BOARD.BOARD_NO.eq(boardNo))
           .execute();

        // INGREDIENTS REPLACE(간단전략: 전량 삭제 후 재삽입)
        dsl.deleteFrom(CH_INGREDIENTS)
           .where(CH_INGREDIENTS.BOARD_NO.eq(boardNo))
           .execute();

        if (req.ingredients() != null && !req.ingredients().isEmpty()) {
            var step = dsl.insertInto(CH_INGREDIENTS,
                    CH_INGREDIENTS.BOARD_NO, CH_INGREDIENTS.ING_NAME, CH_INGREDIENTS.ING_AMOUNT);
            for (var ing : req.ingredients()) {
                step.values(boardNo, ing.name(), ing.amount());
            }
            step.execute();
        }
    }
}
