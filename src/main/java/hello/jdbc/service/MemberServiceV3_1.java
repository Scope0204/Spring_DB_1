package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜젝션 - 트랜잭션 매니저
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository; // 커넥션을 넘김

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 트랜 잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{

            // 비즈니스 로직 수행 : 계좌 이체
            bizLogic(fromId, toId, money);
            transactionManager.commit(status); // 성공시 커밋

        }catch (Exception e){
            transactionManager.rollback(status); // 실패시 롤백
            throw new IllegalStateException(e);
        }

    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember); // 검증. 성공 시 다음 동작(update) 실행
        memberRepository.update(toId, toMember.getMoney() + money);
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
