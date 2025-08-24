package com.cookhub.mjjo.service.recipe;

import com.cookhub.mjjo.dto.recipe.RecipeResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.cookhub.mjjo.jooq.generated.tables.ChBoard.CH_BOARD;
import static com.cookhub.mjjo.jooq.generated.tables.ChIngredients.CH_INGREDIENTS;

@Service
@RequiredArgsConstructor
public class RecipeViewService {

    private final DSLContext dsl;

    @Transactional(readOnly = true)
    public RecipeResponse getByBoardNo(Integer userNo, Integer boardNo) {
        var b = dsl.select(
                    CH_BOARD.BOARD_NO,
                    CH_BOARD.USER_NO,
                    CH_BOARD.BOARD_TITLE,
                    CH_BOARD.BOARD_CON,
                    CH_BOARD.CG_NO,
                    CH_BOARD.CREATED_AT,
                    CH_BOARD.UPDATED_AT
                )
                .from(CH_BOARD)
                .where(CH_BOARD.BOARD_NO.eq(boardNo))
//                .and(CH_BOARD.USER_NO.eq(userNo))
                .fetchOne();

        if (b == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: ");
        Integer owner = b.get(CH_BOARD.USER_NO, Integer.class);
        if (owner == null || !owner.equals(userNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 글만 조회할 수 있습니다.");
        }
        
        var ingredients = dsl.select(
                CH_INGREDIENTS.ING_NO,
                CH_INGREDIENTS.ING_NAME,
                CH_INGREDIENTS.ING_AMOUNT
            )
            .from(CH_INGREDIENTS)
            .where(CH_INGREDIENTS.BOARD_NO.eq(boardNo))
            .orderBy(CH_INGREDIENTS.ING_NO.asc())
            .fetch(r -> new RecipeResponse.Ingredient(
                r.get(CH_INGREDIENTS.ING_NO, Integer.class),
                r.get(CH_INGREDIENTS.ING_NAME, String.class),
                r.get(CH_INGREDIENTS.ING_AMOUNT, String.class)
            ));

        return new RecipeResponse(
                b.get(CH_BOARD.BOARD_NO, Integer.class),
                b.get(CH_BOARD.USER_NO, Integer.class),
                b.get(CH_BOARD.BOARD_TITLE, String.class),
                b.get(CH_BOARD.BOARD_CON, String.class),
                b.get(CH_BOARD.CG_NO, Integer.class),
                b.get(CH_BOARD.CREATED_AT),
                b.get(CH_BOARD.UPDATED_AT),
                ingredients
        );
    }
}
