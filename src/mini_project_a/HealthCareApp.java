package mini_project_a;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

public class HealthCareApp extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField loginUsernameField; // 로그인 시 아이디 입력 필드
	private JPasswordField loginPasswordField; // 로그인 시 비밀번호 입력 필드
	private boolean isAdmin = false; // 로그인된 사용자의 관리자 여부를 나타내는 변수

	public HealthCareApp() {
		// 프레임 설정
		setTitle("회원 관리 시스템");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);

		// 여러 기능을 탭으로 구분할 수 있는 JTabbedPane 생성
		JTabbedPane tabbedPane = new JTabbedPane();

		// 로그인 패널 추가
		JPanel loginPanel = createLoginPanel();
		tabbedPane.addTab("로그인", loginPanel);

		// 탭 패널을 프레임에 추가하고 프레임을 표시
		add(tabbedPane);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// 로그인 패널을 생성하는 메서드
	private JPanel createLoginPanel() {

		JPanel loginPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10); // 컴포넌트 간의 여백 설정
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// 아이디 입력 필드 및 레이블 추가
		loginPanel.add(new JLabel("아이디:"), gbc);
		loginUsernameField = new JTextField(20);
		gbc.gridx = 1;
		loginPanel.add(loginUsernameField, gbc);

		// 비밀번호 입력 필드 및 레이블 추가
		gbc.gridy = 1;
		gbc.gridx = 0;
		loginPanel.add(new JLabel("비밀번호:"), gbc);
		loginPasswordField = new JPasswordField(20);
		gbc.gridx = 1;
		loginPanel.add(loginPasswordField, gbc);

		// 로그인 버튼 추가
		JButton loginButton = new JButton("로그인");
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2; // 버튼이 두 개의 셀을 차지하도록 설정

		loginButton.addActionListener(e -> loginUser());
		loginPanel.add(loginButton, gbc);

		// 회원가입 버튼 추가
		JButton registerButton = new JButton("회원가입");
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 2;

		// 회원가입 다이얼로그를 표시
		registerButton.addActionListener(e -> new MemberRegistrationDialog(this).setVisible(true)); // 회원가입 다이얼로그 열기
		loginPanel.add(registerButton, gbc);

		return loginPanel;
	}

	// 로그인 처리 메서드
	private void loginUser() {
		String username = loginUsernameField.getText(); // 입력된 아이디 가져오기
		String password = new String(loginPasswordField.getPassword()); // 입력된 비밀번호 가져오기

		// 비밀번호를 해시하여 비교
		String hashedPassword = hashPassword(password);

		// 데이터베이스 연동하여 사용자 인증
		boolean auth = authenticateUser(username, hashedPassword);

		if (auth) {

			if (isAdmin) {
				// 관리자 계정일 경우 관리자 패널을 표시
//				System.err.println("loginUser : "+username+", "+password);
				JOptionPane.showMessageDialog(this, "관리자로 로그인되었습니다.");
				showAdminPanel();
			} else {
				// 일반 사용자로 로그인된 경우
				JOptionPane.showMessageDialog(this, "사용자로 로그인되었습니다.");
				setVisible(false);
//				System.err.println("loginUser_사용자 : "+username+", "+password);
				// 일반 사용자 패널 표시
				new HealthManagementProgram(username);
			}
			updateLastLogin(username); // 마지막 로그인 시간 업데이트
		} else {
			JOptionPane.showMessageDialog(this, "로그인에 실패했습니다. 아이디와 비밀번호를 확인하세요.");
		}
	}

	// 사용자 인증 메서드 (데이터베이스 연동)
	private boolean authenticateUser(String username, String hashedPassword) {

		try {
			// --------관리자--------
			HealthsysManager.getInstance().getAdminConnection();
			PreparedStatement stmt = HealthsysManager.adminConn
					.prepareStatement("SELECT * FROM admins WHERE username = ? AND password = ? AND is_admin = ?");

			stmt.setString(1, username);
			stmt.setString(2, hashedPassword); // 암호화된 비밀번호를 비교합니다.
			stmt.setString(3, "1");
			stmt.executeUpdate();
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				isAdmin = rs.getString("is_admin").equals("1") ? true : false;
				return isAdmin;
			}
			// --------사용자--------
			HealthsysManager.getInstance().getAppConnection();
			PreparedStatement stmt2 = HealthsysManager.appConn
					.prepareStatement("SELECT * FROM members WHERE username = ? AND password = ?");

			stmt2.setString(1, username);
			stmt2.setString(2, hashedPassword); // 암호화된 비밀번호를 비교합니다.
			stmt.executeUpdate();
			isAdmin = false;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}

	// 마지막 로그인 시간을 업데이트하는 메서드
	private void updateLastLogin(String username) {

		try {
			HealthsysManager.getInstance().getAppConnection();
			PreparedStatement stmt = HealthsysManager.appConn
					.prepareStatement("UPDATE members SET ACCESS_DATE = SYSDATE WHERE username = ?");

			stmt.setString(1, username);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// SHA-256 암호화를 사용하여 비밀번호를 해시하는 메서드
	private String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(encodedhash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	// 바이트 배열을 16진수 문자열로 변환하는 메서드
	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	// 관리자 패널을 표시하는 메서드
	private void showAdminPanel() {
		JTabbedPane adminTabbedPane = new JTabbedPane(); // 관리자 기능을 위한 탭 생성

		// 회원 관리 패널 추가
		JPanel memberManagementPanel = new JPanel(new BorderLayout());
		JTabbedPane memberSubTabbedPane = new JTabbedPane();

		memberSubTabbedPane.addTab("회원 조회", createMemberListPanel());

		memberManagementPanel.add(memberSubTabbedPane, BorderLayout.CENTER);

		adminTabbedPane.addTab("회원 관리", memberManagementPanel);

		// 트레이너 스케줄 관리 패널 추가
		JPanel trainerSchedulePanel = new JPanel();
		DefaultTableModel tableModel = new DefaultTableModel(new Object[] { "ID", "시간", "회원 이름", "수업 종류", "상태" }, 0) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable trainerscheduleTable = new JTable(tableModel);
		trainerSchedulePanel.add(trainerscheduleTable);
		adminTabbedPane.addTab("트레이너 스케줄 관리", trainerscheduleTable);

		adminTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int selectedIndex = adminTabbedPane.getSelectedIndex();
				if (selectedIndex == 1) {
					tableModel.setRowCount(0);
					loadTrainerSchedule(tableModel, new Frame(), isAdmin);
				}

			}
		});

		// 문의 사항 관리 버튼을 우측 끝으로 배치
		JButton inquiryManagementButton = new JButton("문의 사항 관리");
		inquiryManagementButton.addActionListener(e -> new InquiryManagementDialog(this).setVisible(true));

		// 우측에 버튼을 배치할 패널 생성
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(inquiryManagementButton, BorderLayout.EAST);

		// 메인 패널 생성 및 구성
		JPanel adminPanel = new JPanel(new BorderLayout());
		adminPanel.add(buttonPanel, BorderLayout.NORTH); // 상단에 문의사항 관리 버튼 배치
		adminPanel.add(adminTabbedPane, BorderLayout.CENTER); // 나머지 기능들 배치

		// 로그아웃 버튼 추가
		JButton logoutButton = new JButton("로그아웃");
		logoutButton.addActionListener(e -> logout());
		adminPanel.add(logoutButton, BorderLayout.SOUTH);

		// 기존 로그인 화면 제거 후 관리자 패널 추가
		getContentPane().removeAll();
		getContentPane().add(adminPanel);
		revalidate();
		repaint();
	}

	// 로그아웃 메서드 - 로그인 화면으로 돌아가는 메서드
	private void logout() {
		getContentPane().removeAll();
		JPanel loginPanel = createLoginPanel(); // 로그인 패널 생성
		add(loginPanel); // 로그인 패널을 다시 프레임에 추가
		revalidate();
		repaint();

	}

	// 회원 조회 패널 생성 메서드
	private JPanel createMemberListPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		DefaultTableModel model = new DefaultTableModel(new Object[] { "아이디", "이름", "마지막 접속" }, 0);
		JTable table = new JTable(model);

		// 데이터베이스에서 회원 목록 로드
		loadMemberData(model);

		// 탈퇴 버튼 추가
		JButton deleteButton = new JButton("회원 탈퇴");
		deleteButton.addActionListener(e -> deleteMember(table, model));

		// 패널에 테이블 및 버튼 추가
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(deleteButton, BorderLayout.SOUTH);

		return panel;
	}

	private void loadMemberData(DefaultTableModel model) {

		try {
			HealthsysManager.getInstance().getAppConnection();
			Statement stmt = HealthsysManager.appConn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT username, name, ACCESS_DATE FROM members");

			while (rs.next()) {
				String username = rs.getString("username");
				String name = rs.getString("name");
				String AccessDate = rs.getString("ACCESS_DATE");
				String lastLoginTime = (AccessDate != null) ? AccessDate.toString() : "기록 없음";
				model.addRow(new Object[] { username, name, lastLoginTime });
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void deleteMember(JTable table, DefaultTableModel model) {
		int selectedRow = table.getSelectedRow();
		if (selectedRow != -1) {
			String username = (String) model.getValueAt(selectedRow, 0);
			try {
				HealthsysManager.getInstance().getAppConnection();
				// inquiries 테이블에서 해당 회원의 문의 사항 삭제
				PreparedStatement deleteInquiriesStmt = HealthsysManager.appConn.prepareStatement(
						"DELETE FROM inquiries WHERE USERNAME = (SELECT USERNAME FROM members WHERE username = ?)");

				deleteInquiriesStmt.setString(1, username);
				deleteInquiriesStmt.executeUpdate();

				// members 테이블에서 회원 삭제
				PreparedStatement deleteMemberStmt = HealthsysManager.appConn
						.prepareStatement("DELETE FROM members WHERE username = ?");
				deleteMemberStmt.setString(1, username);

				int confirm = JOptionPane.showConfirmDialog(this, "정말로 회원을 탈퇴시키겠습니까?", "회원 탈퇴",
						JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					deleteMemberStmt.executeUpdate();
					model.removeRow(selectedRow);
					JOptionPane.showMessageDialog(this, "회원이 성공적으로 탈퇴되었습니다.");
				}

			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "회원 탈퇴 중 오류가 발생했습니다.");
			}
		} else {
			JOptionPane.showMessageDialog(this, "탈퇴할 회원을 선택하세요.");
		}
	}

	private void loadTrainerSchedule(DefaultTableModel model, Frame parent, boolean isAdmin) {
		try {
			HealthsysManager.getInstance().getAdminConnection();
			PreparedStatement stmt = HealthsysManager.appConn.prepareStatement("SELECT * FROM SCHEDULES");

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("SCHEDULE_ID");
				int time = rs.getInt("TRAINING_TIME");
				String classType = rs.getString("CLASS_TYPE");
				String status = rs.getString("STATUS");
				String memberName = rs.getString("USERNAME");
				String trainerName = rs.getString("TRAINER_ID");
				model.addRow(new Object[] { id, time, classType, status, memberName, trainerName });
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 문의사항 관리 다이얼로그 클래스
	class InquiryManagementDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InquiryManagementDialog(Frame parent) {
			super(parent, "문의 사항 관리", true);
			setSize(800, 600);

			// 전체 패널 레이아웃 설정
			JPanel mainPanel = new JPanel(new GridLayout(1, 1));

			// 문의 내역, 회원 번호, 처리 상태 패널 추가
			mainPanel.add(createInquiryListPanel());

			add(mainPanel);
			setLocationRelativeTo(parent);
		}

		private JPanel createInquiryListPanel() {
			JPanel panel = new JPanel(new BorderLayout());
			panel.setBorder(BorderFactory.createTitledBorder("문의 내역"));

			// 테이블 모델 생성 및 설정
			DefaultTableModel tableModel = new DefaultTableModel(new Object[] { "번호", "제목", "내용", "등록날짜", "상태", "답변", "아이디" }, 0);
			JTable inquiryTable = new JTable(tableModel);
			panel.add(new JScrollPane(inquiryTable), BorderLayout.CENTER);

			// 답변 입력 및 저장 버튼
			JPanel answerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JTextField answerField = new JTextField(20);
			JButton saveButton = new JButton("저장");
			saveButton.addActionListener(e ->  {
			    saveAnswer(inquiryTable, tableModel, answerField.getText());
			    answerField.setText(""); // 답변 저장 후 입력 필드 초기화
			});
			answerPanel.add(new JLabel("답변:"));
			answerPanel.add(answerField);
			answerPanel.add(saveButton);

			panel.add(answerPanel, BorderLayout.SOUTH);
			loadInquiryData(tableModel); // 데이터 로드

			return panel;
		}

		private void loadInquiryData(DefaultTableModel tableModel) {
			try {
				HealthsysManager.getInstance().getAppConnection();
				PreparedStatement stmt = HealthsysManager.appConn.prepareStatement(
						"SELECT * FROM inquiries");
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					int inquiryId = rs.getInt("inquiry_id");
					String subject = rs.getString("inquiry_subject");
					String details = rs.getString("inquiry_details");
					String date = rs.getString("inquiry_date");
					String status = rs.getString("status");
					String answer = rs.getString("answer");
					String username = rs.getString("username");
					tableModel.addRow(new Object[] { inquiryId, subject, details, date, status, answer, username });
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private void saveAnswer(JTable table, DefaultTableModel tableModel, String answer) {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				int inquiryId = (int) tableModel.getValueAt(selectedRow, 0);

				try {
					HealthsysManager.getInstance().getAppConnection();
					PreparedStatement stmt = HealthsysManager.appConn
							.prepareStatement("UPDATE inquiries SET answer = ?, status = ? WHERE inquiry_id = ?");

					stmt.setString(1, answer);
					stmt.setString(2, "답변완료");
					stmt.setInt(3, inquiryId);
					stmt.executeUpdate();

					tableModel.setValueAt(answer, selectedRow, 5);
					JOptionPane.showMessageDialog(this, "답변이 저장되었습니다.");

				} catch (SQLException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "답변 저장 중 오류가 발생했습니다.");
				}
			} else {
				JOptionPane.showMessageDialog(this, "저장할 답변을 선택하세요.");
			}
		}
	}

	// 회원가입 다이얼로그 클래스
	class MemberRegistrationDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTextField usernameField;
		private JPasswordField passwordField;
		private JTextField nameField;
		private JCheckBox isAdminCheckBox;

		public MemberRegistrationDialog(Frame parent) {
			super(parent, "회원 가입", true);
			setSize(600, 400); // 다이얼로그 크기 설정
			setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(10, 10, 10, 10); // 여백을 좀 더 넉넉하게 설정
			gbc.fill = GridBagConstraints.HORIZONTAL;

			// 사용자명 필드
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(new JLabel("아이디:"), gbc);
			usernameField = new JTextField(20); // 입력칸 크기를 약간 줄임
			gbc.gridx = 1;
			add(usernameField, gbc);

			// 비밀번호 필드
			gbc.gridx = 0;
			gbc.gridy = 1;
			add(new JLabel("비밀번호:"), gbc);
			passwordField = new JPasswordField(20); // 입력칸 크기를 약간 줄임
			gbc.gridx = 1;
			add(passwordField, gbc);

			// 이름 필드
			gbc.gridx = 0;
			gbc.gridy = 2;
			add(new JLabel("이름:"), gbc);
			nameField = new JTextField(20); // 입력칸 크기를 약간 줄임
			gbc.gridx = 1;
			add(nameField, gbc);

			// 관리자 체크박스
			gbc.gridx = 0;
			gbc.gridy = 3;
			add(new JLabel("관리자:"), gbc);
			isAdminCheckBox = new JCheckBox();
			gbc.gridx = 1;
			add(isAdminCheckBox, gbc);

			// 아이디 중복 확인 버튼
			JButton checkDuplicateButton = new JButton("아이디 중복 확인");
			gbc.gridy = 4;
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			checkDuplicateButton.addActionListener(e -> checkUsernameDuplicate());
			add(checkDuplicateButton, gbc);

			// 회원가입 버튼
			JButton registerButton = new JButton("회원가입");
			gbc.gridy = 5;
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			registerButton.addActionListener(e -> registerUser());
			add(registerButton, gbc);

			// 다이얼로그를 중앙에 위치시키기
			setLocationRelativeTo(parent);
		}

		// 아이디 중복 확인 메서드
		private void checkUsernameDuplicate() {
			String username = usernameField.getText();
			if (isUsernameDuplicate(username)) {
				JOptionPane.showMessageDialog(this, "이미 사용 중인 아이디입니다.");
			} else {
				JOptionPane.showMessageDialog(this, "사용 가능한 아이디입니다.");
			}
		}

		// 아이디 중복 확인을 위한 데이터베이스 쿼리
		private boolean isUsernameDuplicate(String username) {
			try {
				// 관리자
				HealthsysManager.getInstance().getAdminConnection();
				PreparedStatement stmt = HealthsysManager.adminConn
						.prepareStatement("SELECT COUNT(*) FROM admins WHERE username = ?");

				stmt.setString(1, username);
				stmt.executeUpdate();

				ResultSet rs = stmt.executeQuery();

				if (rs.next() && rs.getInt(1) > 0) {
					return true;
				}

				// 사용자
				HealthsysManager.getInstance().getAppConnection();
				PreparedStatement stmt2 = HealthsysManager.appConn
						.prepareStatement("SELECT COUNT(*) FROM members WHERE username = ?");

				stmt2.setString(1, username);
				stmt2.executeUpdate();

				ResultSet rs2 = stmt2.executeQuery();

				if (rs2.next() && rs2.getInt(1) > 0) {
					return true;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return false;
		}

		// 회원 등록 처리 메서드
		private void registerUser() {
			String username = usernameField.getText();
			String password = new String(passwordField.getPassword());
			String name = nameField.getText();
			boolean isAdmin = isAdminCheckBox.isSelected();

			// 비밀번호를 해시 처리
			String hashedPassword = hashPassword(password);

			// 데이터베이스에 사용자 정보 저장
			if (saveUserToDatabase(username, hashedPassword, name, isAdmin)) {
				JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다.");
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "회원가입 중 오류가 발생했습니다.");
			}
		}

		// 사용자 정보를 데이터베이스에 저장하는 메서드
		private boolean saveUserToDatabase(String username, String password, String name, boolean isAdmin) {
			try {
				// 관리자 정보 저장
				if (isAdmin) {
					HealthsysManager.getInstance().getAdminConnection();
					PreparedStatement stmt = HealthsysManager.adminConn
							.prepareStatement("INSERT INTO admins (username, password, is_admin) VALUES (?, ?, ?)");

					stmt.setString(1, username);
					stmt.setString(2, password); // 암호화된 비밀번호 저장
					stmt.setInt(3, 1);
					stmt.executeUpdate();

					return true;
				} else {
					HealthsysManager.getInstance().getAppConnection();

					PreparedStatement stmt = HealthsysManager.appConn
							.prepareStatement("INSERT INTO members (username, password, name) VALUES (?, ?, ?)");

					stmt.setString(1, username);
					stmt.setString(2, password); // 암호화된 비밀번호 저장
					stmt.setString(3, name);
					stmt.executeUpdate();

					return true;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return false;
		}

		// 비밀번호를 SHA-256으로 해싱하는 메서드
		private String hashPassword(String password) {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
				return bytesToHex(encodedhash);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		// 바이트 배열을 16진수 문자열로 변환하는 메서드
		private String bytesToHex(byte[] hash) {
			StringBuilder hexString = new StringBuilder(2 * hash.length);
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		}
	}

	class TrainerScheduleDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTable scheduleTable;
		private DefaultTableModel tableModel;

		public TrainerScheduleDialog(Frame parent, boolean isAdmin) {
			super(parent, "트레이너 스케줄 관리", true);
			setSize(600, 400);
			setLayout(new BorderLayout());

			tableModel = new DefaultTableModel(new Object[] { "ID", "시간", "회원 이름", "수업 종류", "상태" }, 0);
			scheduleTable = new JTable(tableModel);
			add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

			loadTrainerSchedule(); // 트레이너 스케줄 로드

			JPanel controlPanel = new JPanel();

			JButton addButton = new JButton("일정 추가");
			addButton.addActionListener(e -> addSchedule());

			JButton updateButton = new JButton("일정 수정");
			updateButton.addActionListener(e -> updateSchedule());

			JButton deleteButton = new JButton("일정 삭제");
			deleteButton.addActionListener(e -> deleteSchedule());

			if (isAdmin) {
				controlPanel.add(addButton);
			}
			controlPanel.add(updateButton);
			controlPanel.add(deleteButton);

			add(controlPanel, BorderLayout.SOUTH);
		}

		private void loadTrainerSchedule() {
			try {
				HealthsysManager.getInstance().getAdminConnection();
				PreparedStatement stmt = HealthsysManager.appConn.prepareStatement("SELECT * FROM trainer_schedule");

				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					int id = rs.getInt("id");
					String time = rs.getString("time");
					String memberName = rs.getString("member_name");
					String classType = rs.getString("class_type");
					String status = rs.getString("status");
					tableModel.addRow(new Object[] { id, time, memberName, classType, status });
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private void addSchedule() {
			String time = JOptionPane.showInputDialog(this, "시간을 입력하세요:");
			String memberName = JOptionPane.showInputDialog(this, "회원 이름을 입력하세요:");
			String classType = JOptionPane.showInputDialog(this, "수업 종류를 입력하세요:");
			String status = JOptionPane.showInputDialog(this, "상태를 입력하세요:");

			if (time != null && memberName != null && classType != null && status != null) {
				try {
					HealthsysManager.getInstance().getAdminConnection();
					PreparedStatement stmt = HealthsysManager.appConn.prepareStatement(
							"INSERT INTO trainer_schedule (time, member_name, class_type, status) VALUES (?, ?, ?, ?)");

					stmt.setString(1, time);
					stmt.setString(2, memberName);
					stmt.setString(3, classType);
					stmt.setString(4, status);
					stmt.executeUpdate();

					ResultSet generatedKeys = stmt.getGeneratedKeys();
					if (generatedKeys.next()) {
						int id = generatedKeys.getInt(1);
						tableModel.addRow(new Object[] { id, time, memberName, classType, status });
					}

					JOptionPane.showMessageDialog(this, "일정이 추가되었습니다.");

				} catch (SQLException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "일정 추가 중 오류가 발생했습니다.");
				}
			} else {
				JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.");
			}
		}

		private void updateSchedule() {
			int selectedRow = scheduleTable.getSelectedRow();
			if (selectedRow != -1) {
				String time = JOptionPane.showInputDialog(this, "새 시간 입력:", tableModel.getValueAt(selectedRow, 1));
				String memberName = JOptionPane.showInputDialog(this, "새 회원 이름 입력:",
						tableModel.getValueAt(selectedRow, 2));
				String classType = JOptionPane.showInputDialog(this, "새 수업 종류 입력:",
						tableModel.getValueAt(selectedRow, 3));
				String status = JOptionPane.showInputDialog(this, "새 상태 입력:", tableModel.getValueAt(selectedRow, 4));

				if (time != null && memberName != null && classType != null && status != null) {
					int id = (int) tableModel.getValueAt(selectedRow, 0);

					try {
						HealthsysManager.getInstance().getAdminConnection();

						PreparedStatement stmt = HealthsysManager.appConn.prepareStatement(
								"UPDATE trainer_schedule SET time = ?, member_name = ?, class_type = ?, status = ? WHERE id = ?");

						stmt.setString(1, time);
						stmt.setString(2, memberName);
						stmt.setString(3, classType);
						stmt.setString(4, status);
						stmt.setInt(5, id);
						stmt.executeUpdate();

						tableModel.setValueAt(time, selectedRow, 1);
						tableModel.setValueAt(memberName, selectedRow, 2);
						tableModel.setValueAt(classType, selectedRow, 3);
						tableModel.setValueAt(status, selectedRow, 4);

						JOptionPane.showMessageDialog(this, "일정이 수정되었습니다.");

					} catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(this, "일정 수정 중 오류가 발생했습니다.");
					}
				} else {
					JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.");
				}
			} else {
				JOptionPane.showMessageDialog(this, "수정할 일정을 선택하세요.");
			}
		}

		private void deleteSchedule() {
			int selectedRow = scheduleTable.getSelectedRow();
			if (selectedRow != -1) {
				int confirmation = JOptionPane.showConfirmDialog(this, "정말로 삭제하시겠습니까?", "삭제 확인",
						JOptionPane.YES_NO_OPTION);
				if (confirmation == JOptionPane.YES_OPTION) {
					int id = (int) tableModel.getValueAt(selectedRow, 0);
					try {
						HealthsysManager.getInstance().getAdminConnection();

						PreparedStatement stmt = HealthsysManager.appConn
								.prepareStatement("DELETE FROM trainer_schedule WHERE id = ?");

						stmt.setInt(1, id);
						stmt.executeUpdate();

						tableModel.removeRow(selectedRow);
						JOptionPane.showMessageDialog(this, "일정이 삭제되었습니다.");

					} catch (SQLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(this, "일정 삭제 중 오류가 발생했습니다.");
					}
				}
			} else {
				JOptionPane.showMessageDialog(this, "삭제할 일정을 선택하세요.");
			}
		}
	}

	public static void main(String[] args) {
		new HealthCareApp(); // 프로그램 실행
	}
}