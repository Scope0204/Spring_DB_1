package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    //DriverManager 를 통해 커넥션 획득
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD); // 커넥션을 사용할 때 마다 파라미터를 받음
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    //DataSource 를 통해 커넥션 획득
    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource -> 항상 새로운 커넥션 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD); // 객체 생성시 파라미터를 넘김 -> 설정
        useDataSource(dataSource);
    }

    // HikariCP 커넥션 풀 사용
    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource(); // 스프링에서 JDBC 사용시, Hikari 오픈소스가 자동으로 import 되어있다
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);
        Thread.sleep(1000); // 커넥션 풀에서 커넥션 생성 시간 대기 (1초)
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection(); // 커넥션을 획득할때, 파라미터를 사용하지 않음 -> 사용
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }
}
