package games.javapuzzle;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author programer
 */
public class MainWindow {
    
    private final int PUZZLE_IMAGE_WIDTH = 1000;
    private int PUZZLE_IMAGE_HEIGHT;
    private final int PUZZLE_ROWS = 10;
    private final int PUZZLE_COLUMNS = 15;
    
    public MainWindow() {
        JFrame frame = new JFrame();
        frame.setTitle("Puzzle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        
        BufferedImage img = this.readImg();
        
        if (img != null) {
            PUZZLE_IMAGE_HEIGHT = PUZZLE_IMAGE_WIDTH*img.getHeight()/img.getWidth();

            ImagePuzzle puzzle = new ImagePuzzle(img, PUZZLE_IMAGE_WIDTH, PUZZLE_COLUMNS, PUZZLE_ROWS);

            for (int a = 0; a < puzzle.getRows(); a++) {
                for (int b = 0; b < puzzle.getColumns(); b++) {
                    this.showImg(frame, puzzle.getPiece(b, a), 10+PUZZLE_IMAGE_WIDTH/PUZZLE_COLUMNS*4/3*b, 10+PUZZLE_IMAGE_HEIGHT/PUZZLE_ROWS*4/3*a);
                }
            }

            frame.setBounds(100, 100, (int)(PUZZLE_IMAGE_WIDTH*1.5), (int)(PUZZLE_IMAGE_HEIGHT*1.5));
            frame.setVisible(true);
        } else {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
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
    
    private void showImg(JFrame frame, BufferedImage img, int x, int y) {
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        label.setLocation(x, y);
        label.setSize(img.getWidth(), img.getHeight());
        frame.add(label);
        Point2D start = new Point2D.Double();
        Point2D startLocation = new Point2D.Double();
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        start.setLocation(e.getLocationOnScreen());
                        startLocation.setLocation(label.getLocation());
                    }
                }
            }
        });
        label.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent moveE) {
                Point2D current = moveE.getLocationOnScreen();
                label.setLocation(
                        (int)(current.getX() - start.getX() + startLocation.getX()), 
                        (int)(current.getY() - start.getY() + startLocation.getY())
                );
            }
        });
    }
}
