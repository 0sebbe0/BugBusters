package com.example.decathlon.gui;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import java.awt.*;

import com.example.decathlon.deca.*;
import com.example.decathlon.heptathlon.*;


public class MainGUI {

    private JTextField nameField;
    private JTextField resultField;
    private JComboBox<String> disciplineBox;
    private JTextArea outputArea;

    public static void main(String[] args) {
        new MainGUI().createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Track and Field Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JPanel panel = new JPanel(new GridLayout(6, 1));

        // Input for competitor's name
        nameField = new JTextField(20);
        panel.add(new JLabel("Enter Competitor's Name:"));
        panel.add(nameField);

        // Dropdown for selecting discipline
        String[] disciplines = {
                "100m", "400m", "1500m", "110m Hurdles",
                "Long Jump", "High Jump", "Pole Vault",
                "Discus Throw", "Javelin Throw", "Shot Put",
                "Hep 100m Hurdles", "Hep High Jump", "Hep Shot Put",
                "Hep 200m", "Hep Long Jump", "Hep Javelin Throw", "Hep 800m"
        };
        disciplineBox = new JComboBox<>(disciplines);
        panel.add(new JLabel("Select Discipline:"));
        panel.add(disciplineBox);

        // Input for result
        resultField = new JTextField(10);
        panel.add(new JLabel("Enter Result:"));
        panel.add(resultField);

        // Button to calculate and display result
        JButton calculateButton = new JButton("Calculate Score");
        calculateButton.addActionListener(new CalculateButtonListener());
        panel.add(calculateButton);

        // Output area
        outputArea = new JTextArea(5, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane);

        frame.add(panel);
        frame.setVisible(true);
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String discipline = (String) disciplineBox.getSelectedItem();
            String resultText = resultField.getText();

            try {
                double result = Double.parseDouble(resultText);

                int score = 0;
                try {
                    switch (discipline) {
                    case "100m":            score = new Deca100M().calculateResult(result); break;
                    case "400m":            score = new Deca400M().calculateResult(result); break;
                    case "1500m":           score = new Deca1500M().calculateResult(result); break;
                    case "110m Hurdles":    score = new Deca110MHurdles().calculateResult(result); break;
                    case "Long Jump":       score = new DecaLongJump().calculateResult(result); break;
                    case "High Jump":       score = new DecaHighJump().calculateResult(result); break;
                    case "Pole Vault":      score = new DecaPoleVault().calculateResult(result); break;
                    case "Discus Throw":    score = new DecaDiscusThrow().calculateResult(result); break;
                    case "Javelin Throw":   score = new DecaJavelinThrow().calculateResult(result); break;
                    case "Shot Put":        score = new DecaShotPut().calculateResult(result); break;
                    case "Hep 100m Hurdles":    score = new Hep100MHurdles().calculateResult(result); break;
                    case "Hep High Jump":       score = new HeptHightJump().calculateResult(result); break;
                    case "Hep Shot Put":        score = new HeptShotPut().calculateResult(result); break;
                    case "Hep 200m":            score = new Hep200M().calculateResult(result); break;
                    case "Hep Long Jump":       score = new HeptLongJump().calculateResult(result); break;
                    case "Hep Javelin Throw":   score = new HeptJavelinThrow().calculateResult(result); break;
                    case "Hep 800m":            score = new Hep800M().calculateResult(result); break;
                    }
                }catch (InvalidResultException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Invalid Result", JOptionPane.ERROR_MESSAGE);
                return;
            }

                outputArea.append("Competitor: " + name + "\n");
                outputArea.append("Discipline: " + discipline + "\n");
                outputArea.append("Result: " + result + "\n");
                outputArea.append("Score: " + score + "\n\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number for the result.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
