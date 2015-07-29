package me.fru1t.rsbot.safecracker;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Window.Type;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.List;

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
					window.frmFrutstandsSafeCracker.setVisible(true);
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
		lblFood.setFont(new Font("Calibri", Font.PLAIN, 16));
		lblFood.setForeground(Color.CYAN);
		lblFood.setBounds(20, 20, 46, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblFood);
		
		JLabel lblNewLabel = new JLabel("Start with all types of food you want to eat at script startup to");
		lblNewLabel.setForeground(Color.GRAY);
		lblNewLabel.setBounds(20, 35, 364, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblNewLabel);
		
		JLabel lblAutomaticallySetThis = new JLabel("automatically set this.");
		lblAutomaticallySetThis.setForeground(Color.GRAY);
		lblAutomaticallySetThis.setBounds(20, 50, 364, 14);
		frmFrutstandsSafeCracker.getContentPane().add(lblAutomaticallySetThis);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setForeground(Color.CYAN);
		comboBox.setToolTipText("Select food and click add to add it to the list of food you want consumed during safecracking");
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Anchovies", "Tacos"}));
		comboBox.setFont(new Font("Calibri", Font.PLAIN, 16));
		comboBox.setBackground(Color.DARK_GRAY);
		comboBox.setBounds(20, 72, 244, 20);
		frmFrutstandsSafeCracker.getContentPane().add(comboBox);
		
		JButton addBtn = new JButton("+");
		addBtn.setForeground(Color.CYAN);
		addBtn.setToolTipText("Add the selected food to the list");
		addBtn.setBackground(Color.DARK_GRAY);
		addBtn.setFont(new Font("Calibri", Font.PLAIN, 16));
		addBtn.setBounds(274, 71, 45, 22);
		frmFrutstandsSafeCracker.getContentPane().add(addBtn);
		
		JButton rmBtn = new JButton("-");
		rmBtn.setForeground(Color.CYAN);
		rmBtn.setToolTipText("Remove the selected food from the list");
		rmBtn.setFont(new Font("Calibri", Font.PLAIN, 16));
		rmBtn.setBackground(Color.DARK_GRAY);
		rmBtn.setBounds(329, 71, 45, 22);
		frmFrutstandsSafeCracker.getContentPane().add(rmBtn);
		
		List foodList = new List();
		foodList.setMultipleSelections(false);
		foodList.setForeground(Color.CYAN);
		foodList.setFont(new Font("Calibri", Font.PLAIN, 16));
		foodList.setBackground(Color.DARK_GRAY);
		foodList.setBounds(20, 98, 354, 107);
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
		frmFrutstandsSafeCracker.setType(Type.UTILITY);
		frmFrutstandsSafeCracker.setTitle("Fru1tstand's Safe Cracker");
		frmFrutstandsSafeCracker.setBackground(Color.BLACK);
		frmFrutstandsSafeCracker.setAlwaysOnTop(true);
		frmFrutstandsSafeCracker.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmFrutstandsSafeCracker.setBounds(100, 100, 400, 500);
	}
}
