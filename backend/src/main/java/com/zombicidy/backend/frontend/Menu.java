package com.zombicidy.backend.frontend;

import com.zombicidy.backend.EventListener;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Menu extends JFrame {
  private JFrame frame;
  private JPanel panel = new JPanel();
  private JButton easy = new JButton("Easy");
  private JButton medium = new JButton("Medium");
  private JButton hard = new JButton("Hard");
  private Font heading = new Font("Times Roman", Font.BOLD, 20);
  public Menu() {
    this.setTitle("Menu");
    this.setSize(600, 600);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame = this;

    panel.setLayout(new FlowLayout());

    easy.setPreferredSize(new Dimension(300, 200));
    easy.setFont(heading);
    easy.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        frame.dispose();
        new EventListener("easy");
      }
    });

    medium.setPreferredSize(new Dimension(300, 200));
    medium.setFont(heading);
    medium.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        frame.dispose();
        new EventListener("medium");
      }
    });

    hard.setPreferredSize(new Dimension(300, 200));
    hard.setFont(heading);
    hard.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        frame.dispose();
        new EventListener("hard");
      }
    });

    panel.add(easy);
    panel.add(medium);
    panel.add(hard);

    this.add(panel);

    this.setVisible(true);
  }
}
