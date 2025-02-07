package mini_project_a;

import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class HealthsysManager {

	// DB의 연결정보를 담고 있는 객체
	public static Connection adminConn = null;
	public static Connection appConn = null;

	// ORACLE DB 연결 정보 - 관리자
	private final String ADMIN_URL = "jdbc:oracle:thin:@localhost:1521:free";
	private final String ADMIN_USER = "C##HEALTHSYS_ADMIN";
	private final String ADMIN_PASSWORD = "ADMINHAKY";

	// ORACLE DB 연결 정보 - 사용자
	private final String APP_URL = "jdbc:oracle:thin:@localhost:1521:free";
	private final String APP_USER = "C##HEALTHSYS_APP";
	private final String APP_PASSWORD = "0000";

	// static 영역에 싱글톤 객체 선언 및 생성
	private static final HealthsysManager HEALTH_MANAGERR = new HealthsysManager();

	// 접근지정자를 private 로 설정하여 해당 클래스 이외에 접근을 막음.
	private HealthsysManager() {
	}

	// 호출될 생성자, 해당 생성자가 호출되면 싱글톤 객체를 반환
	public static HealthsysManager getInstance() {
		return HEALTH_MANAGERR;
	}

	public Connection getAdminConnection() throws SQLException {

		if (adminConn == null || adminConn.isClosed()) {
			try {
				// 데이터베이스 연결
				adminConn = DriverManager.getConnection(ADMIN_URL, ADMIN_USER, ADMIN_PASSWORD);
//				System.out.println("관리자 DB 연결 되었습니다.");

			} catch (SQLException e) {
				// 관리자 계정 로그인 오류 처리
				System.err.printf("관리자 계정 접속 문제 발생 : " + e.getMessage(), e.toString());
				JOptionPane.showMessageDialog(new JFrame(), this, "관리자 계정 접속 중 오류가 발생했습니다.", 0);
			}
		}
		// 연결 성공
		return adminConn;
	}

	public Connection getAppConnection() throws HeadlessException, SQLException {

		if (appConn == null || appConn.isClosed()) {
			try {
				// 데이터베이스 연결
				appConn = DriverManager.getConnection(APP_URL, APP_USER, APP_PASSWORD);
//				System.out.println("사용자 DB 연결 되었습니다.");
			} catch (SQLException e) {
				// 사용자 계정 로그인 오류 처리
				System.err.printf("사용자 계정 접속 문제 발생 : " + e.getMessage(), e.toString());
				JOptionPane.showMessageDialog(new JFrame(), this, "사용자 계정 접속 중 오류가 발생했습니다.", 0);
			}
		}
		// 연결 성공
		return appConn;
	}

	public void closeConnection() {
		try {
			if (adminConn != null || !adminConn.isClosed())
				adminConn.close();
			if (appConn != null && !appConn.isClosed())
				appConn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}