package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 */

@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource; // 의존관계 주입

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally{
            close(con, pstmt, null); // 쿼리 실행 후 리소스(Connection,PreparedStatement)를 정리
        }
    }

    // 데이터 조회
    public Member findById(String memberId) throws SQLException {
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
            log.error("db error", e);
            throw e;
        }
        finally{
            close(con, pstmt, rs); // 헤제는 역순
        }
    }

    // 데이터 변경
    public void update(String memberId, int money) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally{
            close(con, pstmt, null); // 쿼리 실행 후 리소스(Connection,PreparedStatement)를 정리
        }
    }

    // 회원 삭제
    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id =?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
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

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
