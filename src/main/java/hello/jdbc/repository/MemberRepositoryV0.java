package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용
 */

@Slf4j
public class MemberRepositoryV0 {

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

    // 사용한 자원(리소스)들을 모두 close() 해야한다 -> 정리하지 않으면 리소스 누수(계속 유지되는 것)가 일어나 장애가 발생할 수 있다
    // rs는 쿼리를 조회하는 리소스
    private void close(Connection con, Statement stmt, ResultSet rs){

        if(rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close(); //SQLException 이 발생하더라도 catch 에서 잡아주므로, 이후의 con.close()도 정상적으로 확인할 수 있다
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if(con != null){
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection(); // DriverManager 를 통해 커넥션 획득
    }
}
