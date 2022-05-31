package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 예외 누수 문제 해결
 * 체크 예외를 런타임 예외로 변경
 * MemberRepository 인터페이스 사용
 * throws SQLException 제거
 */

@Slf4j
public class MemberRepositoryV4_1 implements MemberRepository {

    private final DataSource dataSource; // 의존관계 주입

    public MemberRepositoryV4_1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Member save(Member member){
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null; // 데이터 베이스의 쿼리를 날림. Statement 의 자식타입 -> ?를 통한 파라미터 바인딩이 가능하게 됨.

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,member.getMemberId()); // sql 에 대한 파라미터 바인딩
            pstmt.setInt(2,member.getMoney());
            pstmt.executeUpdate(); // statement 를 통해 준비된 SQL 을 커넥션을 통해 실제 데이터베이스에 전달. 건 수(int)를 반환함
            return member;
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally{
            close(con, pstmt, null); // 쿼리 실행 후 리소스(Connection,PreparedStatement)를 정리
        }
    }


    // 데이터 조회
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e) {
            throw new MyDbException(e);
        }
        finally{
            close(con, pstmt, rs); // 헤제는 역순
        }
    }

    // 데이터 변경
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?"; // 2개의 쿼리 파라미터를 받음

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1,money) ;
            pstmt.setString(2,memberId);
            int resultSize = pstmt.executeUpdate(); // 쿼리를 실행하고 영향받은 row수
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally{
            close(con, pstmt, null); // 쿼리 실행 후 리소스(Connection,PreparedStatement)를 정리
        }
    }

    // 회원 삭제
    @Override
    public void delete(String memberId){
        String sql = "delete from member where member_id =?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally{
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs){
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야한다!
        DataSourceUtils.releaseConnection(con, dataSource); // 커넥션 제거
    }

    private Connection getConnection() {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
