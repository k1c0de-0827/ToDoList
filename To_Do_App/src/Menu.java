import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Menu{
	JFrame frame;
	JTextField task, date, time;
	JScrollPane todolist;
	JTable table;
	DefaultTableModel model;
	Connection con;
	Statement statm;
	
	public Menu() {
		frame = new JFrame("To-Do List");
		frame.setSize(550, 450);
		frame.setLayout(null);
		Frame();
		connect();
		ToDoList();
		load();
	}
	//frame
	public void Frame(){
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void ToDoList(){
		JButton Addbtn = new JButton("Add Task");
		JButton Deletebtn = new JButton("Complete");
		JButton Updatebtn = new JButton("Update");
		
		Addbtn.setBounds(400, 50, 100, 30);
		Deletebtn.setBounds(400, 100, 100, 30);
		Updatebtn.setBounds(400, 150, 100, 30);
		
		//labels
		JLabel label1 = new JLabel("Task: ");
		label1.setBounds(15, 45, 100, 30);
		frame.add(label1); 
		
		JLabel label2 = new JLabel("Date: ");
		label2.setBounds(15, 95, 100, 30);
		frame.add(label2); 
		
		JLabel label3 = new JLabel("Time: ");
		label3.setBounds(15, 150, 100, 30);
		frame.add(label3); 
		
		JLabel title = new JLabel("TO-DO-LIST");
		title.setBounds(10, 10, 100, 30);
		frame.add(title);
		
		//text fields
		task = new JTextField();
		task.setBounds(60, 45, 300, 30);
		task.setToolTipText("Enter your task");
		frame.add(task);
		
		date = new JTextField();
		date.setBounds(60, 95, 300, 30);
		date.setToolTipText("YYYY-MM-DD");
		frame.add(date);
		
		time = new JTextField();
		time.setBounds(60, 150, 300, 30);
		time.setToolTipText("HH:MM:SS");
		frame.add(time);
		
		// Scroll pane with table
		model = new DefaultTableModel(new String[] {"Task", "Date", "Time"}, 0);
		table = new JTable(model);
		todolist = new JScrollPane(table);
		todolist.setBounds(30, 200, 470, 200);
		frame.add(todolist);

		Addbtn.addActionListener(e -> {
			String taskText = task.getText().trim();
			String dateText = date.getText().trim();
			String timeText = time.getText().trim();

			if (taskText.isEmpty() || dateText.isEmpty() || timeText.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please fill out the form first.", "Missing data", JOptionPane.WARNING_MESSAGE);
				return;
			}

			try {
				PreparedStatement insert = con.prepareStatement("INSERT INTO `tasks`(`Task`,`TaskDate`,`TaskTime`) VALUES (?, ?, ?)");
				insert.setString(1, taskText);
				insert.setString(2, dateText);
				insert.setString(3, timeText);
				insert.executeUpdate();
				insert.close();
				load();
				clearFields();
				JOptionPane.showMessageDialog(frame, "Task added successfully.", "Added", JOptionPane.INFORMATION_MESSAGE);
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(frame, "Unable to add task. " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		});

		Deletebtn.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row == -1) {
				JOptionPane.showMessageDialog(frame, "Please select a row first.", "No selection", JOptionPane.WARNING_MESSAGE);
				return;
			}

			int option = JOptionPane.showConfirmDialog(frame, "Delete the selected task?", "Confirm delete", JOptionPane.YES_NO_OPTION);
			if (option != JOptionPane.YES_OPTION) {
				return;
			}

			String taskText = (String) model.getValueAt(row, 0);
			String dateText = (String) model.getValueAt(row, 1);
			String timeText = (String) model.getValueAt(row, 2);

			try {
				PreparedStatement delete = con.prepareStatement("DELETE FROM `tasks` WHERE `Task` = ? AND `TaskDate` = ? AND `TaskTime` = ? LIMIT 1");
				delete.setString(1, taskText);
				delete.setString(2, dateText);
				delete.setString(3, timeText);
				delete.executeUpdate();
				delete.close();
				load();
				clearFields();
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(frame, "Unable to delete task. " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		});

		Updatebtn.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row == -1) {
				JOptionPane.showMessageDialog(frame, "Please select a row first.", "No selection", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String taskText = task.getText().trim();
			String dateText = date.getText().trim();
			String timeText = time.getText().trim();

			if (taskText.isEmpty() || dateText.isEmpty() || timeText.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Please fill out the form first.", "Missing data", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String originalTask = (String) model.getValueAt(row, 0);
			String originalDate = (String) model.getValueAt(row, 1);
			String originalTime = (String) model.getValueAt(row, 2);

			try {
				PreparedStatement update = con.prepareStatement("UPDATE `tasks` SET `Task` = ?, `TaskDate` = ?, `TaskTime` = ? WHERE `Task` = ? AND `TaskDate` = ? AND `TaskTime` = ? LIMIT 1");
				update.setString(1, taskText);
				update.setString(2, dateText);
				update.setString(3, timeText);
				update.setString(4, originalTask);
				update.setString(5, originalDate);
				update.setString(6, originalTime);
				update.executeUpdate();
				update.close();
				load();
				clearFields();
				JOptionPane.showMessageDialog(frame, "Task updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(frame, "Unable to update task. " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		});

		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int row = table.getSelectedRow();
				if (row != -1) {
					task.setText((String) model.getValueAt(row, 0));
					date.setText((String) model.getValueAt(row, 1));
					time.setText((String) model.getValueAt(row, 2));
				}
			}
		});

		frame.add(Addbtn);
		frame.add(Deletebtn);
		frame.add(Updatebtn);
		frame.setVisible(true);
	}
	
	void connect() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/task_list", "root", "");
			statm = con.createStatement();
			System.out.println("Connected to the database.");
		} catch (Exception e) {
			System.out.println("Unable to connect program");
		}		
	}
	
	void load() {
		try {
			model.setRowCount(0);
			ResultSet resultSet = statm.executeQuery("SELECT * FROM `tasks`");
			while(resultSet.next()) {
				model.addRow(new Object[]{
					resultSet.getString("Task"),
					resultSet.getString("TaskDate"),
					resultSet.getString("TaskTime"),
				});
			} 
			
			LocalDate today = LocalDate.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			for(int i = 0; i < table.getRowCount(); i++) {
				String rowDate = (String) table.getValueAt(i, 1);
				LocalDate taskDate = LocalDate.parse(rowDate, formatter);
			if(taskDate.equals(today)) {
				JOptionPane.showMessageDialog(
						frame, 
						"You have a task due today.", 
						"Today's Deadline", 
						JOptionPane.INFORMATION_MESSAGE);
				break;
			} else if(taskDate.isBefore(today)){
				JOptionPane.showMessageDialog(
						frame, 
						"You have a task past its due.", 
						"Past Deadline", 
						JOptionPane.INFORMATION_MESSAGE);
				break;
			} else {
				System.out.println("No task due today/No overdue task");
			}
			}
			
		} catch (Exception e) {
			System.out.println("Unable to load program");
			e.printStackTrace();
		}
	}

	void clearFields() {
		task.setText("");
		date.setText("");
		time.setText("");
		table.clearSelection();
	}

	public static void main(String[] args) {
		new Menu();
	}
}