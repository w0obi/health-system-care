package mini_project_a;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class HealthManagementProgram extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// 운동 기록 필드
	private JLabel photo1Label;
	private JLabel photo2Label;
	private JTextField dateField;
	private JTextField countField;
	private JTextField timeField;
	private JTextField intensityField;
	private String selectedExercise;
	private DefaultListModel<String> exerciseListModel;

	// 운동 기록 조회 필드
	private JTable exerciseTable;

	// 개인정보 필드
	private JTextField nameField;
	private JTextField ageField;
	private JTextField heightField;
	private JTextField weightField;
	private JTextField bmiField;

	// 문의사항 필드
	private JTable inquiryTable;

	public HealthManagementProgram(String username) {

		setTitle("건강 관리 프로그램");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 로그아웃
		JButton logoutButton = new JButton("로그아웃");
		logoutButton.addActionListener(e -> {
			setVisible(false);
			new HealthCareApp();
		});

		// JTabbedPane 생성
		JTabbedPane tabbedPane = new JTabbedPane();

		// 운동 탭
		JPanel exercisePanel = createExercisePanel(username);
		tabbedPane.addTab("운동", exercisePanel);

		// 운동 정보 탭
		JPanel exerciseInfoPanel = createExerciseInfoPanel(username);
		tabbedPane.addTab("운동 정보", exerciseInfoPanel);

		// 개인정보 탭
		JPanel personalInfoPanel = createPersonalInfoPanel(username);
		tabbedPane.addTab("개인 정보", personalInfoPanel);

		// 문의사항 버튼 추가
		JButton inquiryButton = createInquiryButton(username);

		// 패널에 탭 패널과 문의사항 버튼 추가
		add(tabbedPane, BorderLayout.CENTER);

		// 문의사항 버튼을 우측 상단에 추가하기 위해 별도의 패널을 생성
		JPanel topPanel = new JPanel();
		topPanel.add(inquiryButton);

		topPanel.add(logoutButton);// 로그아웃 버튼 추가
		add(topPanel, BorderLayout.EAST);

		// 개인정보 탭 선택 시 데이터를 불러오도록 ChangeListener 추가
		tabbedPane.addChangeListener(e -> {
			JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
			int selectedIndex = sourceTabbedPane.getSelectedIndex();// 0부터 시작

			if (selectedIndex == 2) { // "개인 정보" 탭을 선택
				fetchPersonalInfo(username);
			}
		});

		add(tabbedPane);
		setVisible(true);
		setLocationRelativeTo(null);
	}

	// 운동 탭 패널 생성 메소드
	public JPanel createExercisePanel(String username) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// 왼쪽 리스트
		exerciseListModel = new DefaultListModel<>();
		exerciseListModel.addElement("팔굽혀펴기");
		exerciseListModel.addElement("윗몸일으키기");
		exerciseListModel.addElement("스쿼트");

		JList<String> exerciseList = new JList<>(exerciseListModel);
		exerciseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exerciseList.addListSelectionListener(e -> selectedExercise = exerciseList.getSelectedValue());
		exerciseList.addListSelectionListener(e -> updatePhotos(exerciseList.getSelectedValue()));
		panel.add(new JScrollPane(exerciseList), BorderLayout.WEST);

		// 중앙 패널: 사진 및 날짜/횟수/시간 입력
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(5, 2));

		photo1Label = new JLabel();
		photo2Label = new JLabel();
		photo1Label.setHorizontalAlignment(JLabel.CENTER);
		photo2Label.setHorizontalAlignment(JLabel.CENTER);
		photo1Label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		photo2Label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		centerPanel.add(photo1Label);
		centerPanel.add(photo2Label);

		centerPanel.add(new JLabel("오늘 날짜를 입력하세요 ex)YYYY-MM-DD:"));
		LocalDate today = LocalDate.now();
		dateField = new JTextField(String.valueOf(today)); // 현재 날짜로 기본값 설정
		centerPanel.add(dateField);

		centerPanel.add(new JLabel("오늘 운동한 횟수를 입력하세요:"));
		countField = new JTextField();
		centerPanel.add(countField);

		centerPanel.add(new JLabel("운동 시간을 입력하세요 (분 단위):"));
		timeField = new JTextField();
		centerPanel.add(timeField);

		centerPanel.add(new JLabel("운동 강도를 입력하세요:"));
		intensityField = new JTextField();
		centerPanel.add(intensityField);

		panel.add(centerPanel, BorderLayout.CENTER);

		// 하단 버튼
		JButton button = new JButton("저장");
		button.addActionListener(e -> {
//			System.err.println(selectedExercise + "" + username);
			modifyExercise(selectedExercise, username);
			saveExerciseData(username);
		});
		panel.add(button, BorderLayout.SOUTH);

		return panel;
	}

	// 운동 사진 업데이트 메소드
	public void updatePhotos(String exercise) {
		if (exercise != null) {
			switch (exercise) {
			case "팔굽혀펴기":
				ImageIcon icon = new ImageIcon(getClass().getResource("/mini_project_a/img/push1.png"));
				Image img = icon.getImage();
				img = img.getScaledInstance(photo1Label.getWidth(), photo1Label.getHeight(), Image.SCALE_SMOOTH);
				icon = new ImageIcon(img);

				photo1Label.setIcon(icon);

				icon = new ImageIcon(getClass().getResource("/mini_project_a/img/push2.png"));
				img = icon.getImage();
				img = img.getScaledInstance(photo1Label.getWidth(), photo1Label.getHeight(), Image.SCALE_SMOOTH);
				icon = new ImageIcon(img);

				photo2Label.setIcon(icon);
				break;
			case "윗몸일으키기":
				icon = new ImageIcon(getClass().getResource("/mini_project_a/img/situp1.png"));
				img = icon.getImage();
				img = img.getScaledInstance(photo1Label.getWidth(), photo1Label.getHeight(), Image.SCALE_SMOOTH);
				icon = new ImageIcon(img);

				photo1Label.setIcon(icon);

				icon = new ImageIcon(getClass().getResource("/mini_project_a/img/situp2.png"));
				img = icon.getImage();
				img = img.getScaledInstance(photo1Label.getWidth(), photo1Label.getHeight(), Image.SCALE_SMOOTH);
				icon = new ImageIcon(img);

				photo2Label.setIcon(icon);

				break;
			case "스쿼트":
				icon = new ImageIcon(getClass().getResource("/mini_project_a/img/squat1.png"));
				img = icon.getImage();
				img = img.getScaledInstance(photo1Label.getWidth(), photo1Label.getHeight(), Image.SCALE_SMOOTH);
				icon = new ImageIcon(img);

				photo1Label.setIcon(icon);

				icon = new ImageIcon(getClass().getResource("/mini_project_a/img/squat2.png"));
				img = icon.getImage();
				img = img.getScaledInstance(photo1Label.getWidth(), photo1Label.getHeight(), Image.SCALE_SMOOTH);
				icon = new ImageIcon(img);

				photo2Label.setIcon(icon);
				break;
			default:
				photo1Label.setIcon(null);
				photo2Label.setIcon(null);
			}
		} else {
			photo1Label.setIcon(null);
			photo2Label.setIcon(null);
		}
	}

	// 운동 종목 수정 메소드
	public void modifyExercise(String selectedExercise, String username) {
		try {
			HealthsysManager.getInstance().getAppConnection();
			PreparedStatement stmt = HealthsysManager.appConn
					.prepareStatement("INSERT INTO exercises (exercise_name, username) VALUES (?, ?)");

			stmt.setString(1, selectedExercise);
			stmt.setString(2, username);
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 운동 기록 저장 메소드
	public void saveExerciseData(String username) {

		String date = dateField.getText();
		String count = countField.getText();
		String time = timeField.getText();
		String intensity = intensityField.getText();

		if (selectedExercise == null || date.isEmpty() || count.isEmpty() || time.isEmpty() || intensity.isEmpty()) {
			JOptionPane.showMessageDialog(this, "모든 필드를 입력하세요.");
			return;
		} else {
			try {
				HealthsysManager.getInstance().getAppConnection();
				PreparedStatement stmt_old = HealthsysManager.appConn
						.prepareStatement("SELECT exercise_id FROM exercises WHERE username = ?");

				stmt_old.setString(1, username);
				stmt_old.executeUpdate();

				ResultSet rs = stmt_old.executeQuery();

				int exercise_id = 0;
				if (rs.next()) {
					exercise_id = rs.getInt("exercise_id");
				}

				PreparedStatement stmt = HealthsysManager.appConn.prepareStatement(
						"INSERT INTO records (exercise_id, exercise_date, exercise_count, exercise_time, exercise_intensity, username) VALUES (?, ?, ?, ?, ?, ?)");

				stmt.setInt(1, exercise_id);
				stmt.setDate(2, java.sql.Date.valueOf(String.valueOf(date)));
				stmt.setInt(3, Integer.parseInt(count));
				stmt.setInt(4, Integer.parseInt(time));
				stmt.setString(5, intensity);
				stmt.setString(6, username);
				stmt.executeUpdate();
				
//				System.err.println(exercise_id);
//				System.err.println(java.sql.Date.valueOf(String.valueOf(date)));
//				System.err.println(Integer.parseInt(count));
//				System.err.println(Integer.parseInt(time));
//				System.err.println(intensity);
//				System.err.println(username);

				// 입력 필드 초기화
	            countField.setText("");
	            timeField.setText("");
	            intensityField.setText("");
				JOptionPane.showMessageDialog(this, "운동 데이터가 저장되었습니다.");

			} catch (SQLException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "데이터 저장 중 오류가 발생했습니다.");
			}
		}
	}

	// 운동 기록 조회 탭 패널 생성 메소드
	public JPanel createExerciseInfoPanel(String username) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		String[] columnNames = { "운동종목", "날짜", "횟수", "운동 시간(분)", "운동 강도" };
		Object[][] data = {};

		exerciseTable = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(exerciseTable);

		JButton fetchButton = new JButton("불러오기");
		fetchButton.addActionListener(e -> fetchExerciseData(username));

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(fetchButton, BorderLayout.SOUTH);

		return panel;
	}

	// 운동 기록 조회 불러오기 메소드
	public void fetchExerciseData(String username) {
	    try {
	        HealthsysManager.getInstance().getAppConnection();

	        String query = "SELECT * FROM records WHERE username = ? ORDER BY exercise_date DESC";
	        try (PreparedStatement stmt = HealthsysManager.appConn.prepareStatement(query)) {
	            stmt.setString(1, username);
	            try (ResultSet rs = stmt.executeQuery()) {

	                List<Object[]> dataList = new ArrayList<>(); // 동적 리스트 사용
	                while (rs.next()) {
	                    String exercise_id = rs.getString("exercise_id");
	                    
	                    // exercise 테이블 조회
	                    String exerciseQuery = "SELECT * FROM exercises WHERE exercise_id = ?";
	                    try (PreparedStatement exerciseStmt = HealthsysManager.appConn.prepareStatement(exerciseQuery)) {
	                        exerciseStmt.setString(1, exercise_id);
	                        try (ResultSet exerciseRs = exerciseStmt.executeQuery()) {
	                            if (exerciseRs.next()) { // 결과가 있을 경우에만 가져오기
	                                Object[] row = new Object[]{
	                                    exerciseRs.getString("exercise_name"),
	                                    rs.getDate("exercise_date"),
	                                    rs.getInt("exercise_count"),
	                                    rs.getInt("exercise_time"),
	                                    rs.getString("exercise_intensity")
	                                };
	                                dataList.add(row);
	                            }
	                        }
	                    }
	                }

	                // 리스트를 배열로 변환
	                Object[][] data = dataList.toArray(new Object[0][]);
	                String[] columnNames = { "운동종목", "날짜", "횟수", "운동 시간(분)", "운동 강도" };
	                exerciseTable.setModel(new DefaultTableModel(data, columnNames));
	            }
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(this, "데이터 불러오기 중 오류가 발생했습니다.");
	    }
	}


	// 개인정보 탭 패널 생성 메소드
	public JPanel createPersonalInfoPanel(String username) {
		JPanel panel = new JPanel(new BorderLayout());

		// 좌측 패널
		JPanel leftPanel = new JPanel(new GridLayout(5, 1));
		JLabel nameLabel = new JLabel("이름:");
		JLabel ageLabel = new JLabel("나이:");
		JLabel heightLabel = new JLabel("키:");
		JLabel weightLabel = new JLabel("몸무게:");
		JLabel bmiLabel = new JLabel("BMI:");

		leftPanel.add(nameLabel);
		leftPanel.add(ageLabel);
		leftPanel.add(heightLabel);
		leftPanel.add(weightLabel);
		leftPanel.add(bmiLabel);

		// 우측 패널
		JPanel rightPanel = new JPanel(new GridLayout(5, 1));
		nameField = new JTextField(20);
		ageField = new JTextField(20);
		heightField = new JTextField(20);
		weightField = new JTextField(20);
		bmiField = new JTextField(20);

		rightPanel.add(nameField);
		rightPanel.add(ageField);
		rightPanel.add(heightField);
		rightPanel.add(weightField);
		rightPanel.add(bmiField);

		// 필드 비활성화 (수정 불가 상태)
		nameField.setEditable(false);
		ageField.setEditable(false);
		heightField.setEditable(false);
		weightField.setEditable(false);
		bmiField.setEditable(false);

		// 패널 구성
		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.CENTER);

		JButton editButton = new JButton("수정");
		JButton saveButton = new JButton("저장");
		JButton cancelButton = new JButton("취소");

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(editButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		// 처음에는 "취소", "저장" 버튼 숨기기
		cancelButton.setVisible(false);
		saveButton.setVisible(false);

		// -------------------------------------
		// 수정 버튼 클릭 시 입력 필드 활성화
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nameField.setEditable(true);
				ageField.setEditable(true);
				heightField.setEditable(true);
				weightField.setEditable(true);
				
				// "수정" 버튼 숨기고 "취소", "저장" 버튼 표시
		        editButton.setVisible(false);
		        cancelButton.setVisible(true);
		        saveButton.setVisible(true);
			}
		});
		
		// 취소 버튼 클릭 시
		cancelButton.addActionListener(e -> {
		    fetchPersonalInfo(username); // 기존 데이터 다시 불러오기

		    // 필드 비활성화
		    nameField.setEditable(false);
		    ageField.setEditable(false);
		    heightField.setEditable(false);
		    weightField.setEditable(false);

		    // 버튼 상태 변경
		    editButton.setVisible(true);
		    saveButton.setVisible(false);
		    cancelButton.setVisible(false);
		});

		// 저장 버튼 클릭 시 이벤트 처리
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 입력된 값을 가져오기
				String name = nameField.getText();
				int age = Integer.parseInt(ageField.getText());
				double height = Double.parseDouble(heightField.getText());
				double weight = Double.parseDouble(weightField.getText());
				double bmi = weight / Math.pow(height / 100, 2); // BMI 계산
				double roundedBmi = Math.round(bmi * 10) / 10.0;// 소수점 1자리까지 반올림

				// DB에 업데이트
				try {
					HealthsysManager.getInstance().getAppConnection();
					PreparedStatement stmt = HealthsysManager.appConn.prepareStatement(
							"UPDATE members SET name = ?, age = ?, height = ?, weight = ?, bmi = ? WHERE username = ?");

					// 입력된 값을 SQL 쿼리에 바인딩
					stmt.setString(1, name);
					stmt.setInt(2, age);
					stmt.setDouble(3, height);
					stmt.setDouble(4, weight);
					stmt.setDouble(5, roundedBmi);
					stmt.setString(6, username);

//					System.err.println(name);
//					System.err.println(age);
//					System.err.println(height);
//					System.err.println(weight);
//					System.err.println(roundedBmi);

					// 업데이트 실행
					int rowsUpdated = stmt.executeUpdate(); // 변경된 행 수 반환
					if (rowsUpdated > 0) {
						JOptionPane.showMessageDialog(panel, "회원 정보가 성공적으로 업데이트되었습니다.");
						// 필드 비활성화 (다시 수정 불가 상태로)
						nameField.setEditable(false);
						ageField.setEditable(false);
						heightField.setEditable(false);
						weightField.setEditable(false);
						
						// **변경된 데이터 새로고침**
		                fetchPersonalInfo(username);
		                
		                // 버튼 상태 복구
		                editButton.setVisible(true);
		                cancelButton.setVisible(false);
		                saveButton.setVisible(false);
					} else {
						JOptionPane.showMessageDialog(panel, "회원 정보를 업데이트할 수 없습니다.");
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		return panel;
	}

	// DB에서 개인정보 불러오는 메소드
	public void fetchPersonalInfo(String username) {
		try {
			HealthsysManager.getInstance().getAppConnection();
			PreparedStatement stmt = HealthsysManager.appConn
					.prepareStatement("SELECT * FROM members WHERE username = ?");

			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				nameField.setText(rs.getString("name"));
				ageField.setText(rs.getString("age"));
				heightField.setText(rs.getString("height"));
				weightField.setText(rs.getString("weight"));
				bmiField.setText(rs.getString("bmi"));
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// 문의사항 버튼 생성 메소드
	public JButton createInquiryButton(String username) {
	    JButton inquiryButton = new JButton("문의사항");

	    inquiryButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent ae) {
	            // 문의사항 다이얼로그 생성
	            JDialog inquiryDialog = new JDialog(HealthManagementProgram.this, "문의사항", true);
	            inquiryDialog.setLayout(new BorderLayout());

	            // 문의사항 테이블
	            String[] columnNames = { "문의제목", "문의내용", "문의날짜", "처리상태", "관리자답변", "아이디" };
	            DefaultTableModel model = new DefaultTableModel(columnNames, 0);

	            if (inquiryTable == null) {
	                inquiryTable = new JTable(model);  // 최초에만 테이블을 생성
	            } else {
	                inquiryTable.setModel(model);  // 기존 테이블 모델을 갱신
	            }

	            inquiryTable.setEnabled(false);  // 셀 수정 비활성화
	            
	            // 기존 데이터 로드
	            loadInquiryData(model, username);

	            JScrollPane tableScrollPane = new JScrollPane(inquiryTable);
	            inquiryDialog.add(tableScrollPane, BorderLayout.EAST);

	            // 제목 및 내용 입력을 위한 패널
	            JPanel inputPanel = new JPanel(new GridBagLayout());
	            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	            GridBagConstraints gbc = new GridBagConstraints();
	            gbc.fill = GridBagConstraints.BOTH;
	            gbc.insets = new Insets(5, 5, 5, 5);
	            gbc.gridx = 0;
	            gbc.weightx = 1.0; // 너비 확장

	            // 제목 입력란 (높이 비율 2)
	            gbc.gridy = 0;
	            gbc.weighty = 0.2; // 20%
	            JTextField titleField = new JTextField();
	            inputPanel.add(titleField, gbc);

	            // 내용 입력란 (높이 비율 8)
	            gbc.gridy = 1;
	            gbc.weighty = 0.8; // 80%
	            JTextArea detailArea = new JTextArea();
	            JScrollPane detailScrollPane = new JScrollPane(detailArea);
	            inputPanel.add(detailScrollPane, gbc);

	            // 저장 버튼 (아래쪽)
	            JButton saveButton = new JButton("저장");
	            
	            saveButton.addActionListener(ev -> {
	                String title = titleField.getText();
	                String detail = detailArea.getText();

	                // 유효성 검사
	                if (title.isEmpty()) {
	                    JOptionPane.showMessageDialog(inquiryDialog, "제목을 입력해 주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }

	                if (detail.isEmpty()) {
	                    JOptionPane.showMessageDialog(inquiryDialog, "내용을 입력해 주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }

	                try {
	                    HealthsysManager.getInstance().getAppConnection();
	                    PreparedStatement stmt = HealthsysManager.appConn.prepareStatement(
	                        "INSERT INTO inquiries (inquiry_subject, inquiry_details, username) VALUES (?, ?, ?)"
	                    );
	                    stmt.setString(1, title);
	                    stmt.setCharacterStream(2, new StringReader(detail), detail.length());
	                    stmt.setString(3, username);
	                    stmt.executeUpdate();

	                    // 데이터 저장 후 JTable 갱신 (중복 방지)
	                    loadInquiryData(model, username);
	                    
	                    // 입력 필드 초기화
	                    titleField.setText("");
	                    detailArea.setText("");

	                    JOptionPane.showMessageDialog(inquiryDialog, "문의사항 데이터가 저장되었습니다.");
	                } catch (SQLException ex) {
	                    ex.printStackTrace();
	                    JOptionPane.showMessageDialog(inquiryDialog, "데이터 저장 중 오류가 발생했습니다.");
	                }
	            });

	            // 다이얼로그에 추가
	            inquiryDialog.add(inputPanel, BorderLayout.CENTER);
	            inquiryDialog.add(saveButton, BorderLayout.SOUTH);

	            // 다이얼로그 크기 및 위치 설정
	            inquiryDialog.setSize(800, 600);
	            inquiryDialog.setLocationRelativeTo(null);
	            inquiryDialog.setVisible(true);
	        }
	    });

	    return inquiryButton;
	}

	// JTable 갱신 메서드
	public void loadInquiryData(DefaultTableModel model, String loginUsername) {
	    try {
	        HealthsysManager.getInstance().getAppConnection();
	        PreparedStatement stmt = HealthsysManager.appConn.prepareStatement("SELECT * FROM inquiries WHERE username = ?");
	        stmt.setString(1, loginUsername);
	        
	        ResultSet rs = stmt.executeQuery();

	        // 기존 데이터 삭제 (중복 방지)
	        model.setRowCount(0);

	        // 결과를 DefaultTableModel에 추가
	        while (rs.next()) {
	            String title = rs.getString("inquiry_subject");
	            String detail = rs.getString("inquiry_details");
	            java.sql.Date inquiryDate = rs.getDate("inquiry_date");
	            String status = rs.getString("status");
	            String answer = rs.getString("answer");
	            String username = rs.getString("username");

	            // 날짜 포맷팅
	            String formattedDate = (inquiryDate != null) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(inquiryDate) : "";

	            model.addRow(new Object[]{
	                title,
	                detail,
	                formattedDate,
	                status,
	                answer != null ? answer : "",
	                username
	            });
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(null, "데이터 불러오기 중 오류가 발생했습니다.");
	    }
	}

}
