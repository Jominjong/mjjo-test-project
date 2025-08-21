package com.cookhub.mjjo.service.recipe;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cookhub.mjjo.dto.recipe.RecipeSaveRequest;

import static com.cookhub.mjjo.jooq.generated.tables.ChBoard.CH_BOARD;
import static com.cookhub.mjjo.jooq.generated.tables.ChIngredients.CH_INGREDIENTS;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecipeAddService {

    private final DSLContext dsl;

    @Transactional
    public Integer create(RecipeSaveRequest req) {
        var now = LocalDateTime.now();
        // BOARD INSERT
        Integer boardNo = dsl.insertInto(CH_BOARD)
                .set(CH_BOARD.USER_NO, req.userNo())
                .set(CH_BOARD.BOARD_TITLE, req.title())
                .set(CH_BOARD.BOARD_CON, req.content())
                .set(CH_BOARD.CG_NO, req.categoryNo())
                .set(CH_BOARD.CREATED_AT, now)
                .set(CH_BOARD.UPDATED_AT, now)
                .returning(CH_BOARD.BOARD_NO)
                .fetchOne()
                .get(CH_BOARD.BOARD_NO);

        // INGREDIENTS BULK INSERT
        if (req.ingredients() != null && !req.ingredients().isEmpty()) {
            var step = dsl.insertInto(CH_INGREDIENTS,
                    CH_INGREDIENTS.BOARD_NO, CH_INGREDIENTS.ING_NAME, CH_INGREDIENTS.ING_AMOUNT);
            for (var ing : req.ingredients()) {
                step.values(boardNo, ing.name(), ing.amount());
            }
            step.execute();
        }
        return boardNo;
    }
}
