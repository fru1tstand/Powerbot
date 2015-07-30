package me.fru1t.rsbot.safecracker;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Window.Type;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.List;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class Startup {

	private JFrame frmFrutstandsSafeCracker;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Startup window = new Startup();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Startup() {
		initialize();
		frmFrutstandsSafeCracker.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFrutstandsSafeCracker = new JFrame();
		frmFrutstandsSafeCracker.getContentPane().setFont(new Font("Calibri", Font.PLAIN, 16));
		frmFrutstandsSafeCracker.setResizable(false);
		frmFrutstandsSafeCracker.getContentPane().setForeground(Color.CYAN);
		frmFrutstandsSafeCracker.getContentPane().setBackground(Color.DARK_GRAY);
		frmFrutstandsSafeCracker.getContentPane().setLayout(null);
		
		JLabel lblFood = new JLabel("Food");
		lblFood.setFont(new Font("Calibri", Font.BOLD, 16));
		lblFood.setForeground(Color.CYAN);
		lblFood.setBounds(20, 20, 46, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblFood);
		
		JLabel lblNewLabel = new JLabel("Have food in your inventory to automatically set this");
		lblNewLabel.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblNewLabel.setForeground(Color.GRAY);
		lblNewLabel.setBounds(20, 35, 364, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblNewLabel);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setForeground(Color.CYAN);
		comboBox.setToolTipText("Select food and click add to add it to the list of food you want consumed during safecracking");
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Anchovies", "Tacos"}));
		comboBox.setFont(new Font("Calibri", Font.PLAIN, 14));
		comboBox.setBackground(Color.DARK_GRAY);
		comboBox.setBounds(20, 52, 244, 20);
		frmFrutstandsSafeCracker.getContentPane().add(comboBox);
		
		JButton addBtn = new JButton("+");
		addBtn.setForeground(Color.CYAN);
		addBtn.setToolTipText("Add the selected food to the list");
		addBtn.setBackground(Color.DARK_GRAY);
		addBtn.setFont(new Font("Calibri", Font.PLAIN, 16));
		addBtn.setBounds(274, 52, 45, 20);
		frmFrutstandsSafeCracker.getContentPane().add(addBtn);
		
		JButton rmBtn = new JButton("-");
		rmBtn.setForeground(Color.CYAN);
		rmBtn.setToolTipText("Remove the selected food from the list");
		rmBtn.setFont(new Font("Calibri", Font.PLAIN, 16));
		rmBtn.setBackground(Color.DARK_GRAY);
		rmBtn.setBounds(329, 52, 45, 20);
		frmFrutstandsSafeCracker.getContentPane().add(rmBtn);
		
		List foodList = new List();
		foodList.setMultipleSelections(false);
		foodList.setForeground(Color.CYAN);
		foodList.setFont(new Font("Calibri", Font.PLAIN, 14));
		foodList.setBackground(Color.BLACK);
		foodList.setBounds(20, 78, 354, 107);
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		foodList.add("asdf");
		
		frmFrutstandsSafeCracker.getContentPane().add(foodList);
		
		JLabel lblPreferredSafe = new JLabel("Preferred Safe");
		lblPreferredSafe.setFont(new Font("Calibri", Font.BOLD, 16));
		lblPreferredSafe.setForeground(Color.CYAN);
		lblPreferredSafe.setBounds(20, 266, 141, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblPreferredSafe);
		
		JLabel lblUnlessYouHave = new JLabel("Unless you have an extreme prejudice against a certain safe,");
		lblUnlessYouHave.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblUnlessYouHave.setForeground(Color.GRAY);
		lblUnlessYouHave.setBounds(20, 282, 364, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblUnlessYouHave);
		
		JLabel lblThisSettingOn = new JLabel("it's best to leave this on automatic.");
		lblThisSettingOn.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblThisSettingOn.setForeground(Color.GRAY);
		lblThisSettingOn.setBounds(20, 294, 364, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblThisSettingOn);
		
		JComboBox preferredSafe = new JComboBox();
		preferredSafe.setFont(new Font("Calibri", Font.PLAIN, 14));
		preferredSafe.setModel(new DefaultComboBoxModel(new String[] {"Automatic", "South West", "North West", "South East", "North East"}));
		preferredSafe.setForeground(Color.CYAN);
		preferredSafe.setBackground(Color.DARK_GRAY);
		preferredSafe.setBounds(20, 312, 354, 20);
		frmFrutstandsSafeCracker.getContentPane().add(preferredSafe);
		
		JLabel lblFoodAmountPer = new JLabel("Food Banking Style");
		lblFoodAmountPer.setForeground(Color.CYAN);
		lblFoodAmountPer.setFont(new Font("Calibri", Font.BOLD, 16));
		lblFoodAmountPer.setBounds(20, 201, 204, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblFoodAmountPer);
		
		JRadioButton constant = new JRadioButton("Same amount every time");
		constant.setFont(new Font("Calibri", Font.PLAIN, 14));
		constant.setForeground(Color.CYAN);
		constant.setBackground(Color.DARK_GRAY);
		constant.setBounds(20, 236, 266, 23);
		frmFrutstandsSafeCracker.getContentPane().add(constant);
		
		JRadioButton automatic = new JRadioButton("Automatic amount");
		automatic.setSelected(true);
		automatic.setForeground(Color.CYAN);
		automatic.setFont(new Font("Calibri", Font.PLAIN, 14));
		automatic.setBackground(Color.DARK_GRAY);
		automatic.setBounds(20, 216, 266, 23);
		frmFrutstandsSafeCracker.getContentPane().add(automatic);
		frmFrutstandsSafeCracker.setType(Type.UTILITY);
		frmFrutstandsSafeCracker.setTitle("Fru1tstand's Safe Cracker");
		frmFrutstandsSafeCracker.setBackground(Color.BLACK);
		frmFrutstandsSafeCracker.setAlwaysOnTop(true);
		frmFrutstandsSafeCracker.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmFrutstandsSafeCracker.setBounds(100, 100, 400, 465);
		
		ButtonGroup foodSelectMethod = new ButtonGroup();
		foodSelectMethod.add(constant);
		foodSelectMethod.add(automatic);
		
		JSpinner foodAmount = new JSpinner();
		foodAmount.setModel(new SpinnerNumberModel(4, 0, 27, 1));
		foodAmount.setFont(new Font("Calibri", Font.PLAIN, 22));
		foodAmount.setForeground(Color.CYAN);
		foodAmount.setBackground(Color.DARK_GRAY);
		foodAmount.setBounds(292, 216, 82, 37);
		frmFrutstandsSafeCracker.getContentPane().add(foodAmount);
		
		JButton btnStart = new JButton("Start!");
		btnStart.setForeground(Color.CYAN);
		btnStart.setBackground(Color.DARK_GRAY);
		btnStart.setFont(new Font("Calibri", Font.PLAIN, 16));
		btnStart.setBounds(20, 392, 110, 23);
		frmFrutstandsSafeCracker.getContentPane().add(btnStart);
		
		JLabel lblQuestionsCommentsWant = new JLabel("Questions? Comments? Feature requests?");
		lblQuestionsCommentsWant.setHorizontalAlignment(SwingConstants.TRAILING);
		lblQuestionsCommentsWant.setForeground(Color.GRAY);
		lblQuestionsCommentsWant.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblQuestionsCommentsWant.setBounds(20, 370, 354, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblQuestionsCommentsWant);
		
		JLabel lblPostInThe = new JLabel("Post in the forums and I'll be sure to respond.");
		lblPostInThe.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPostInThe.setForeground(Color.GRAY);
		lblPostInThe.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblPostInThe.setBounds(20, 380, 354, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblPostInThe);
		
		JLabel lblCreatedWithLove = new JLabel("Created, with love, by fru1tstand.");
		lblCreatedWithLove.setHorizontalAlignment(SwingConstants.TRAILING);
		lblCreatedWithLove.setForeground(Color.CYAN);
		lblCreatedWithLove.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblCreatedWithLove.setBounds(20, 401, 354, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblCreatedWithLove);
		
		JLabel lblThisScriptIs = new JLabel("This script is open source. Check it out on Github.");
		lblThisScriptIs.setHorizontalAlignment(SwingConstants.TRAILING);
		lblThisScriptIs.setForeground(Color.GRAY);
		lblThisScriptIs.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblThisScriptIs.setBounds(20, 390, 354, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblThisScriptIs);
	}
}
