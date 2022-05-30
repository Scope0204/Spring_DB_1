package hello.jdbc.exception.basic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

public class UnCheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    /**
     * 런타임(언체크) 예외를 사용
     * 컨트롤러와 서비스의 의존관계가 사라지게 된다
     */
    static class Controller{
        Service service = new Service();

        public void request() {
            service.logic();

        }
    }
    static class Service{
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient{
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }

    }
    static class Repository{
        public void call(){
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e); // SQLException -> RuntimeSQLException
            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {

        // 이전 예외를 같이 넣을 수 있음
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }

}
