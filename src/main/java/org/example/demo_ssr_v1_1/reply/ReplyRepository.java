package org.example.demo_ssr_v1_1.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 Repository 인터페이스
 * 
 * 핵심 개념:
 * 1. JpaRepository<Reply, Long>: Spring Data JPA가 제공하는 인터페이스
 * 2. 쿼리 메서드: 메서드 이름만으로 쿼리 자동 생성
 * 3. @Query: 복잡한 쿼리는 직접 작성
 */
@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    /**
     * 게시글 ID로 댓글 목록 조회 (작성자 정보 포함, JOIN FETCH 사용)
     * 
     * JOIN FETCH를 사용하여 Reply, Board, User를 한 번의 쿼리로 함께 조회합니다.
     * OSIV False 환경에서도 안전하게 사용 가능합니다.
     * 
     * 생성되는 SQL:
     * SELECT r.*, b.*, u.* 
     * FROM reply_tb r 
     * INNER JOIN board_tb b ON r.board_id = b.id 
     * INNER JOIN user_tb u ON r.user_id = u.id 
     * WHERE r.board_id = ?
     * ORDER BY r.created_at ASC
     * 
     * @param boardId 게시글 ID
     * @return 댓글 목록 (생성일 기준 오름차순)
     */
    @Query("SELECT r FROM Reply r JOIN FETCH r.user JOIN FETCH r.board WHERE r.board.id = :boardId ORDER BY r.createdAt ASC")
    List<Reply> findByBoardIdWithUser(@Param("boardId") Long boardId);
    
    /**
     * 댓글 ID로 조회 (작성자 정보 포함, JOIN FETCH 사용)
     * 
     * @param id 댓글 ID
     * @return 댓글 (Optional)
     */
    @Query("SELECT r FROM Reply r JOIN FETCH r.user JOIN FETCH r.board WHERE r.id = :id")
    Optional<Reply> findByIdWithUser(@Param("id") Long id);



    /**
     * 게시글 ID로 댓글 삭제 (JPQL 직접 작성)
     * * [기능 설명]
     * 게시글 삭제 시, 해당 게시글에 달린 댓글들이 남아있으면 외래키(FK) 제약조건 위반 에러가 발생합니다.
     * 이를 방지하기 위해 게시글을 삭제하기 직전에 이 메서드를 호출하여 속한 댓글들을 먼저 일괄 삭제합니다.
     * * =========================================================================
     * [학생들 주의사항: 예전엔 안 썼던 @Modifying을 지금은 왜 쓸까요?]
     * * 1. 이전 수업에서는 (기본 제공 메서드 & 더티 체킹)
     * - boardRepository.deleteById(id) 같은 기본 메서드나, 
     * - 객체 상태만 바꾸는 '더티 체킹(Dirty Checking)'을 사용했습니다.
     * - 이때는 스프링 JPA가 알아서 DELETE/UPDATE 처리를 해주기 때문에 어노테이션이 필요 없었습니다.
     * * 2. 지금은 (커스텀 @Query 사용)
     * - 우리가 @Query를 열고 직접 SQL(JPQL)을 작성하면, 
     * 스프링은 무조건 "아, 데이터를 조회(SELECT)하려는 거구나!"라고 오해합니다.
     * - 따라서 @Modifying을 반드시 붙여서 "이건 조회가 아니라 데이터 변경(DELETE) 작업이야!"
     * 라고 스프링에게 명확히 알려주어야 합니다. 누락 시 100% 에러가 발생합니다.
     * =========================================================================
     * * @param boardId 삭제할 기준이 되는 게시글 ID
     */
    @Modifying
    @Query("DELETE FROM Reply r WHERE r.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
    
    /**
     * 게시글 ID로 댓글 삭제
     * 
     * 게시글 삭제 시 외래키 제약조건 위반을 방지하기 위해
     * 게시글에 속한 모든 댓글을 먼저 삭제하는 데 사용
     * 
     * @param boardId 게시글 ID
     */
    // void deleteByBoardId(Long boardId);

    
}

