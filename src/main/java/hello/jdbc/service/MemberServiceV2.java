package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜젝션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository; // 커넥션을 넘김

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try{
            con.setAutoCommit(false); // 오토 커밋 -> 트랜잭션 시작

            // 비즈니스 로직 수행 : 계좌 이체
            bizLogic(con, fromId, toId, money);

            con.commit(); // 성공시 커밋 -> 트랜잭션 종료

        }catch (Exception e){
            con.rollback(); // 실패시 롤백 -> 트랜잭션 종료
            throw new IllegalStateException(e);
        }finally {
            release(con);
        }

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // 계좌이체
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember); // 검증. 성공 시 다음 동작(update) 실행
        memberRepository.update(toId, toMember.getMoney() + money);

    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember); // 검증. 성공 시 다음 동작(update) 실행
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }


    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void release(Connection con) {
        if (con != null){
            try{
                con.setAutoCommit(true); // 커넥션 풀 고려 (현재 false 이기 때문에)
                con.close();

            }
            catch(Exception e){
                log.info("error", e); // Exception 은 {}을 넣지 않아도된다
            }
        }
    }


}
