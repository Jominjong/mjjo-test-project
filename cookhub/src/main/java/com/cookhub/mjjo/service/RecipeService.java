package com.cookhub.mjjo.service;

import com.cookhub.mjjo.dto.recipe.*;

import java.util.List;
import org.jooq.Record;
import org.jooq.DSLContext;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import static com.cookhub.mjjo.jooq.generated.tables.ChBoard.CH_BOARD;
import static com.cookhub.mjjo.jooq.generated.tables.ChIngredients.CH_INGREDIENTS;


@Service
@RequiredArgsConstructor
public class RecipeService {

    private final DSLContext dsl;

/*--------------------------------------------------------------------------------------------*/
/*read*/
/*--------------------------------------------------------------------------------------------*/
    /* 내 레시피 목록 */
    @Transactional(readOnly = true)
    public MainResponse list(int page, int size, Integer categoryNo, String keyword, Integer userNo) {
        int pageSafe = Math.max(page, 1);
        int sizeSafe = Math.min(Math.max(size, 1), 100);
        int offset = (pageSafe - 1) * sizeSafe;

        var cond = CH_BOARD.USER_NO.eq(userNo); //소유자 제한
        if (categoryNo != null) {
            cond = cond.and(CH_BOARD.CG_NO.eq(categoryNo));
        }
        if (keyword != null && !keyword.isBlank()) {
            var like = "%" + keyword.trim() + "%";
            cond = cond.and(CH_BOARD.BOARD_TITLE.likeIgnoreCase(like));
        }
        // cond = cond.and(CH_BOARD.DELETED_AT.isNull()); // 소프트삭제 사용 시

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

    /* 내 레시피 단건 조회 */
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
                .and(CH_BOARD.USER_NO.eq(userNo))
                // .and(CH_BOARD.DELETED_AT.isNull())
                .fetchOne();

        if (b == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");

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

/*--------------------------------------------------------------------------------------------*/
/*create*/
/*--------------------------------------------------------------------------------------------*/
    @Transactional
    public Integer create(Integer userNo, RecipeSaveRequest req) {
        var now = LocalDateTime.now();
        
        /*보드 추가*/
        Integer boardNo = dsl.insertInto(CH_BOARD)
                .set(CH_BOARD.USER_NO, userNo)
                .set(CH_BOARD.BOARD_TITLE, req.title())
                .set(CH_BOARD.BOARD_CON, req.content())
                .set(CH_BOARD.CG_NO, req.categoryNo())
                .set(CH_BOARD.CREATED_AT, now)
                .set(CH_BOARD.UPDATED_AT, now)
                .returning(CH_BOARD.BOARD_NO)
                .fetchOne(CH_BOARD.BOARD_NO);
        
        /*재료 다중 추가*/
        if (req.ingredients() != null && !req.ingredients().isEmpty()) {
            var step = dsl.insertInto(
                    CH_INGREDIENTS,
                    CH_INGREDIENTS.BOARD_NO,
                    CH_INGREDIENTS.ING_NAME,
                    CH_INGREDIENTS.ING_AMOUNT
            );
            for (var ing : req.ingredients()) {
                step.values(boardNo, ing.name(), ing.amount());
            }
            step.execute();
        }
        return boardNo;
    }
    
/*--------------------------------------------------------------------------------------------*/
/*update*/
/*--------------------------------------------------------------------------------------------*/
    @Transactional      
    public void update(Integer userNo, Integer boardNo, RecipeUpdateRequest req) {
    	/*1) 게시글 업데이트*/
    	int updated = dsl.update(CH_BOARD)
                .set(CH_BOARD.BOARD_TITLE, req.title())
                .set(CH_BOARD.BOARD_CON, req.content())
                .set(CH_BOARD.CG_NO, req.categoryNo())
                .set(CH_BOARD.UPDATED_AT, LocalDateTime.now())
                .where(CH_BOARD.BOARD_NO.eq(boardNo))
                .and(CH_BOARD.USER_NO.eq(userNo))
                .execute();
        
        /*존재하지 않는 게시글*/
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없거나 존재하지 않습니다.");
        }
        /*2) 재료 전체 삭제*/
        dsl.deleteFrom(CH_INGREDIENTS)
        	.where(CH_INGREDIENTS.BOARD_NO.eq(boardNo))
        	.execute();

        /*3) 재삽입*/
        List<RecipeUpdateRequest.Ingredient> ings = req.ingredients();
        if (ings != null && !ings.isEmpty()) {
            var step = dsl.insertInto(
                CH_INGREDIENTS,
                CH_INGREDIENTS.BOARD_NO,
                CH_INGREDIENTS.ING_NAME,
                CH_INGREDIENTS.ING_AMOUNT
            );
            for (var ing : ings) {
                step.values(boardNo, ing.name(), ing.amount());
            }
            step.execute();
        }
    }
    
/*--------------------------------------------------------------------------------------------*/
/*delete*/
/*--------------------------------------------------------------------------------------------*/
    public void delete(Integer userNo, Integer boardNo) {
        // 1) 소유 확인
        boolean owns = dsl.fetchExists(
                dsl.selectOne()
                  .from(CH_BOARD)
                  .where(CH_BOARD.BOARD_NO.eq(boardNo))
                  .and(CH_BOARD.USER_NO.eq(userNo))
                  // .and(CH_BOARD.DELETED_AT.isNull())
        );
        if (!owns) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없거나 존재하지 않습니다.");

        // 2) 자식 먼저 삭제(캐스케이드 없다는 가정)
        dsl.deleteFrom(CH_INGREDIENTS)
           .where(CH_INGREDIENTS.BOARD_NO.eq(boardNo))
           .execute();

        // 3) 본문 삭제(또는 소프트삭제로 변경 가능)
        dsl.deleteFrom(CH_BOARD)
           .where(CH_BOARD.BOARD_NO.eq(boardNo))
           .and(CH_BOARD.USER_NO.eq(userNo))
           .execute();
        
        // 소프트 삭제 시:
        // dsl.update(CH_BOARD)
        //    .set(CH_BOARD.DELETED_AT, LocalDateTime.now())
        //    .where(CH_BOARD.BOARD_NO.eq(boardNo))
        //    .and(CH_BOARD.USER_NO.eq(userNo))
        //    .execute();
   }
}

