package games.javapuzzle;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author programer
 */
public class ConfigWindow {
    private final JFrame frame;
    
    public ConfigWindow() {
        frame = new JFrame();
        //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Puzzle config");
        frame.setLayout(null);
        frame.getContentPane().setLayout(new GridLayout(5, 2, 10, 10));
        frame.setSize(300, 250);
        createControls();
        frame.setVisible(true);

    }
    
    private void createControls() {
        frame.getContentPane().add(new JLabel("Пазлов в ширину"));
        final JTextField puzzleColumnsField = new JTextField("15");
        frame.getContentPane().add(puzzleColumnsField);
        
        frame.getContentPane().add(new JLabel("Пазлов в высоту"));
        final JTextField puzzleRowsField = new JTextField("10");
        frame.getContentPane().add(puzzleRowsField);
        
        frame.getContentPane().add(new JLabel("Ширина картинки"));
        final JTextField puzzleImageWidthField = new JTextField("1000");
        frame.getContentPane().add(puzzleImageWidthField);
        
        JButton fileManagerButton = new JButton("<html>Выберите<br>изображение</html>");
        frame.getContentPane().add(fileManagerButton);
        
        final JButton goButton = new JButton("Начать");
        goButton.setVisible(true);
        frame.getContentPane().add(goButton);
        
        final JLabel validationLabel = new JLabel("<html>Поля заполнены<br>неверно</html>");
        validationLabel.setVisible(false);
        frame.getContentPane().add(validationLabel);
        
        final GoButtonAction goClick = new GoButtonAction(frame, validationLabel, puzzleColumnsField, puzzleRowsField, puzzleImageWidthField);
        
        goButton.addActionListener(goClick);
        
        fileManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                goClick.setSource(readImg());
                validationLabel.setVisible(false);
            }
        });
        
        ValidationClearingListener validationClearingListener = new ValidationClearingListener(validationLabel);
        
        puzzleColumnsField.getDocument().addDocumentListener(validationClearingListener);
        puzzleRowsField.getDocument().addDocumentListener(validationClearingListener);
        puzzleImageWidthField.getDocument().addDocumentListener(validationClearingListener);
    }
    
    private BufferedImage readImg() {
        JFileChooser file = new JFileChooser();
        file.removeChoosableFileFilter(file.getFileFilter());
        file.addChoosableFileFilter(new FileNameExtensionFilter("images", new String[] {"png", "jpg"}));
        
        if (file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            BufferedImage img = null;
            try {
                img = ImageIO.read(file.getSelectedFile());
            } catch (IOException e) {
                return null;
            }
            return img;
        } else {
            return null;
        }
    }
    
    private static class ValidationClearingListener implements DocumentListener {
        
        private JLabel label;
        
        ValidationClearingListener(JLabel label) {
            this.label = label;
        }

        @Override
        public void insertUpdate(DocumentEvent de) {
            label.setVisible(false);
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            label.setVisible(false);
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            label.setVisible(false);
        }
        
    }
    
    private static class GoButtonAction implements ActionListener {
        private BufferedImage src;
        private JFrame frame;
        private JLabel validationLabel;
        private JTextField puzzleColumnsField;
        private JTextField puzzleRowsField;
        private JTextField puzzleImageWidthField;
        
        public GoButtonAction(JFrame frame, JLabel validationLabel, JTextField puzzleColumnsField, JTextField puzzleRowsField, JTextField puzzleImageWidthField) {
            this.frame = frame;
            this.validationLabel = validationLabel;
            this.puzzleColumnsField = puzzleColumnsField;
            this.puzzleRowsField = puzzleRowsField;
            this.puzzleImageWidthField = puzzleImageWidthField;
        }

        public void setSource(BufferedImage src) {
            this.src = src;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (!validateFields()) {
                validationLabel.setVisible(true);
            } else {
                MainWindow window = new MainWindow(
                        src, 
                        Integer.valueOf(puzzleImageWidthField.getText()),
                        Integer.valueOf(puzzleColumnsField.getText()),
                        Integer.valueOf(puzzleRowsField.getText())
                );
                frame.setVisible(false);
            }
        }
        
        private boolean validateFields() {
            boolean notValid = true;
            try {
                notValid = ((src == null)
                 || (Integer.valueOf(puzzleImageWidthField.getText()) <= 0)
                 || (Integer.valueOf(puzzleColumnsField.getText()) <= 0)
                 || (Integer.valueOf(puzzleRowsField.getText()) <= 0)
                );
            } catch (NumberFormatException e) {
            }
            return !notValid;
        }
    }
}
