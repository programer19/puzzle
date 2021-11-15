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
import javax.swing.JTextField;
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
        frame.getContentPane().setLayout(new GridLayout(4, 2, 10, 10));
        frame.setSize(300, 200);
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
        
        JButton fileManagerButton = new JButton("Выберите изображение");
        frame.getContentPane().add(fileManagerButton);
        
        final JButton goButton = new JButton("Начать");
        goButton.setVisible(false);
        frame.getContentPane().add(goButton);
        
        fileManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                final BufferedImage source = readImg();
                goButton.setVisible(true);
                
                goButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        MainWindow window = new MainWindow(
                                source, 
                                Integer.valueOf(puzzleImageWidthField.getText()),
                                Integer.valueOf(puzzleColumnsField.getText()),
                                Integer.valueOf(puzzleRowsField.getText())
                        );
                        frame.setVisible(false);
                    }
                });
            }
        });
        
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
}
