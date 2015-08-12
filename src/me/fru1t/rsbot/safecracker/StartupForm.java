package me.fru1t.rsbot.safecracker;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Window.Type;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.AbstractStartupForm;
import me.fru1t.rsbot.common.framework.components.SettingsCallback;
import me.fru1t.rsbot.common.items.Food;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.ActionEvent;


public class StartupForm extends AbstractStartupForm<Settings> {
	private JFrame settingsForm;
	private final HashMap<String, Food> foodMap;

	/**
	 * Create the application.
	 */
	@Inject
	public StartupForm(SettingsCallback<Settings> callback) {
		super(callback);
		
		this.foodMap = new HashMap<>();
		this.initialize();
		this.settingsForm.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		settingsForm = new JFrame();
		settingsForm.getContentPane().setFont(new Font("Calibri", Font.PLAIN, 16));
		settingsForm.setResizable(false);
		settingsForm.getContentPane().setForeground(Color.CYAN);
		settingsForm.getContentPane().setBackground(Color.DARK_GRAY);
		settingsForm.getContentPane().setLayout(null);
		
		JLabel lblFood = new JLabel("Food");
		lblFood.setFont(new Font("Calibri", Font.BOLD, 16));
		lblFood.setForeground(Color.CYAN);
		lblFood.setBounds(20, 20, 46, 14);
		settingsForm.getContentPane().add(lblFood);
		
		JLabel lblNewLabel = new JLabel("Have food in your inventory to automatically set this");
		lblNewLabel.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblNewLabel.setForeground(Color.GRAY);
		lblNewLabel.setBounds(20, 35, 364, 14);
		settingsForm.getContentPane().add(lblNewLabel);
		
		// Java 7 shenanigans doesn't have 'effectively final'.
		final JComboBox<Enum<Food>> foodSelectorComboBox = new JComboBox<Enum<Food>>();
		foodSelectorComboBox.setForeground(Color.BLACK);
		foodSelectorComboBox.setToolTipText("Select food and click add to add it to the list of food you want consumed during safecracking");
		foodSelectorComboBox.setModel(new DefaultComboBoxModel<Enum<Food>>(Food.values()));
		foodSelectorComboBox.setFont(new Font("Calibri", Font.PLAIN, 14));
		foodSelectorComboBox.setBackground(Color.WHITE);
		foodSelectorComboBox.setBounds(20, 52, 244, 20);
		settingsForm.getContentPane().add(foodSelectorComboBox);
		
		JButton addBtn = new JButton("+");
		addBtn.setForeground(Color.BLACK);
		addBtn.setToolTipText("Add the selected food to the list");
		addBtn.setBackground(Color.WHITE);
		addBtn.setFont(new Font("Calibri", Font.PLAIN, 16));
		addBtn.setBounds(274, 52, 45, 20);
		settingsForm.getContentPane().add(addBtn);
		
		JButton rmBtn = new JButton("-");
		rmBtn.setForeground(Color.BLACK);
		rmBtn.setToolTipText("Remove the selected food from the list");
		rmBtn.setFont(new Font("Calibri", Font.PLAIN, 16));
		rmBtn.setBackground(Color.WHITE);
		rmBtn.setBounds(329, 52, 45, 20);
		settingsForm.getContentPane().add(rmBtn);
		
		final java.awt.List foodListJList = new java.awt.List();
		foodListJList.setMultipleMode(false);
		foodListJList.setForeground(Color.CYAN);
		foodListJList.setFont(new Font("Calibri", Font.PLAIN, 14));
		foodListJList.setBackground(Color.BLACK);
		foodListJList.setBounds(20, 78, 354, 107);
		
		settingsForm.getContentPane().add(foodListJList);
		
		JLabel lblPreferredSafe = new JLabel("Preferred Safe");
		lblPreferredSafe.setFont(new Font("Calibri", Font.BOLD, 16));
		lblPreferredSafe.setForeground(Color.CYAN);
		lblPreferredSafe.setBounds(20, 266, 141, 14);
		settingsForm.getContentPane().add(lblPreferredSafe);
		
		JLabel lblUnlessYouHave = new JLabel("Unless you have an extreme prejudice against a certain safe,");
		lblUnlessYouHave.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblUnlessYouHave.setForeground(Color.GRAY);
		lblUnlessYouHave.setBounds(20, 282, 364, 14);
		settingsForm.getContentPane().add(lblUnlessYouHave);
		
		JLabel lblThisSettingOn = new JLabel("it's best to leave this on automatic.");
		lblThisSettingOn.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblThisSettingOn.setForeground(Color.GRAY);
		lblThisSettingOn.setBounds(20, 294, 364, 14);
		settingsForm.getContentPane().add(lblThisSettingOn);
		
		final JComboBox<Enum<RoguesDenSafeCracker.Safe>> preferredSafeComboBox = new JComboBox<Enum<RoguesDenSafeCracker.Safe>>();
		preferredSafeComboBox.setFont(new Font("Calibri", Font.PLAIN, 14));
		preferredSafeComboBox.setModel(new DefaultComboBoxModel<Enum<RoguesDenSafeCracker.Safe>>(RoguesDenSafeCracker.Safe.values()));
		preferredSafeComboBox.setForeground(Color.BLACK);
		preferredSafeComboBox.setBackground(Color.WHITE);
		preferredSafeComboBox.setBounds(20, 312, 354, 20);
		settingsForm.getContentPane().add(preferredSafeComboBox);
		
		JLabel lblFoodAmountPer = new JLabel("Food Banking Style");
		lblFoodAmountPer.setForeground(Color.CYAN);
		lblFoodAmountPer.setFont(new Font("Calibri", Font.BOLD, 16));
		lblFoodAmountPer.setBounds(20, 201, 204, 14);
		settingsForm.getContentPane().add(lblFoodAmountPer);
		
		final JRadioButton constant = new JRadioButton("Same amount every time");
		constant.setFont(new Font("Calibri", Font.PLAIN, 14));
		constant.setForeground(Color.CYAN);
		constant.setBackground(Color.DARK_GRAY);
		constant.setBounds(20, 236, 266, 23);
		settingsForm.getContentPane().add(constant);
		
		JRadioButton automatic = new JRadioButton("Automatic amount");
		automatic.setSelected(true);
		automatic.setForeground(Color.CYAN);
		automatic.setFont(new Font("Calibri", Font.PLAIN, 14));
		automatic.setBackground(Color.DARK_GRAY);
		automatic.setBounds(20, 216, 266, 23);
		settingsForm.getContentPane().add(automatic);
		settingsForm.setType(Type.UTILITY);
		settingsForm.setTitle("Fru1tstand's Safe Cracker");
		settingsForm.setBackground(Color.BLACK);
		settingsForm.setAlwaysOnTop(true);
		settingsForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsForm.setBounds(100, 100, 400, 465);
		
		ButtonGroup foodSelectMethod = new ButtonGroup();
		foodSelectMethod.add(constant);
		foodSelectMethod.add(automatic);
		
		JSpinner foodAmountSpinner = new JSpinner();
		foodAmountSpinner.setModel(new SpinnerNumberModel(4, 0, 27, 1));
		foodAmountSpinner.setFont(new Font("Calibri", Font.PLAIN, 22));
		foodAmountSpinner.setForeground(Color.CYAN);
		foodAmountSpinner.setBackground(Color.DARK_GRAY);
		foodAmountSpinner.setBounds(292, 216, 82, 37);
		settingsForm.getContentPane().add(foodAmountSpinner);
		
		JButton startBtn = new JButton("Start!");
		startBtn.setForeground(Color.BLACK);
		startBtn.setBackground(Color.WHITE);
		startBtn.setFont(new Font("Calibri", Font.PLAIN, 16));
		startBtn.setBounds(20, 392, 110, 23);
		settingsForm.getContentPane().add(startBtn);
		
		JLabel lblQuestionsCommentsWant = new JLabel("Questions? Comments? Feature requests?");
		lblQuestionsCommentsWant.setHorizontalAlignment(SwingConstants.TRAILING);
		lblQuestionsCommentsWant.setForeground(Color.GRAY);
		lblQuestionsCommentsWant.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblQuestionsCommentsWant.setBounds(20, 370, 354, 14);
		settingsForm.getContentPane().add(lblQuestionsCommentsWant);
		
		JLabel lblPostInThe = new JLabel("Post in the forums and I'll be sure to respond.");
		lblPostInThe.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPostInThe.setForeground(Color.GRAY);
		lblPostInThe.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblPostInThe.setBounds(20, 380, 354, 14);
		settingsForm.getContentPane().add(lblPostInThe);
		
		JLabel lblCreatedWithLove = new JLabel("Created, with love, by fru1tstand.");
		lblCreatedWithLove.setHorizontalAlignment(SwingConstants.TRAILING);
		lblCreatedWithLove.setForeground(Color.CYAN);
		lblCreatedWithLove.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblCreatedWithLove.setBounds(20, 401, 354, 14);
		settingsForm.getContentPane().add(lblCreatedWithLove);
		
		JLabel lblThisScriptIs = new JLabel("This script is open source. Check it out on Github.");
		lblThisScriptIs.setHorizontalAlignment(SwingConstants.TRAILING);
		lblThisScriptIs.setForeground(Color.GRAY);
		lblThisScriptIs.setFont(new Font("Calibri", Font.PLAIN, 11));
		lblThisScriptIs.setBounds(20, 390, 354, 14);
		settingsForm.getContentPane().add(lblThisScriptIs);
		
		{
			addBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Food selectedFood = (Food) foodSelectorComboBox.getSelectedItem();
					if (!foodMap.containsKey(selectedFood.name())) {
						foodListJList.add(selectedFood.name());
						foodMap.put(selectedFood.name(), selectedFood);
					}
				}
			});
			rmBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String selectedFood = foodListJList.getSelectedItem();
					if (foodMap.containsKey(selectedFood)) {
						foodListJList.remove(selectedFood);
						foodMap.remove(selectedFood);
					}
				}
			});
			startBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					settingsForm.setVisible(false);
					Settings settings = new Settings();
					settings.setFoods(new ArrayList<Food>(foodMap.values()));
					settings.setBankStyleConstant(constant.isSelected());
					settings.setPreferredSafe((RoguesDenSafeCracker.Safe) preferredSafeComboBox.getSelectedItem());
					settingsForm.dispose();
					callback.call(settings);
				}
			});
		}
	}
}
