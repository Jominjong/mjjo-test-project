package com.cookhub.mjjo.service.recipe;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.cookhub.mjjo.jooq.generated.tables.ChBoard.CH_BOARD;
import static com.cookhub.mjjo.jooq.generated.tables.ChIngredients.CH_INGREDIENTS;

@Service
@RequiredArgsConstructor
public class RecipeDeleteService {

    private final DSLContext dsl;

    @Transactional
    public void delete(Integer boardNo) {
        dsl.deleteFrom(CH_INGREDIENTS)
           .where(CH_INGREDIENTS.BOARD_NO.eq(boardNo))
           .execute();

        dsl.deleteFrom(CH_BOARD)
           .where(CH_BOARD.BOARD_NO.eq(boardNo))
           .execute();
    }
}
