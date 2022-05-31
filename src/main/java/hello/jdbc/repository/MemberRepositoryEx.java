package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;

/**
 * 체크 예외 사용 : SQLException
 * 인터페이스의 메서드에도 체크 예외를 먼저 선언해야한다
 * JDBC 기술에 종속적인 인터페이스가 된다
 */
public interface MemberRepositoryEx {
    Member save(Member member) throws SQLException;
    Member findById(String memberId) throws SQLException;
    void update(String memberId, int money) throws SQLException;
    void delete(String memberId) throws SQLException;
}
