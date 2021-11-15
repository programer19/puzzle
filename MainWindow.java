package games.javapuzzle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;

/**
 *
 * @author programer
 */
public class MainWindow {
    
    private final int PUZZLE_IMAGE_WIDTH = 1000;
    private int PUZZLE_IMAGE_HEIGHT;
    private final int PUZZLE_ROWS = 10;
    private final int PUZZLE_COLUMNS = 15;
    
    private JLabel[][] piecesLabels;
    private MyLabelUI lu;
    
    public MainWindow() {
        JFrame frame = new JFrame();
        makeLabelUI(frame);
        frame.setTitle("Puzzle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        
        BufferedImage img = this.readImg();
        
        if (img != null) {
            PUZZLE_IMAGE_HEIGHT = PUZZLE_IMAGE_WIDTH*img.getHeight()/img.getWidth();

            ImagePuzzle puzzle = new ImagePuzzle(img, PUZZLE_IMAGE_WIDTH, PUZZLE_COLUMNS, PUZZLE_ROWS);

            piecesLabels = new JLabel[puzzle.getRows()][];
            for (int a = 0; a < puzzle.getRows(); a++) {
                piecesLabels[a] = new JLabel[puzzle.getColumns()];
                for (int b = 0; b < puzzle.getColumns(); b++) {
                    piecesLabels[a][b] = this.showImg(frame, puzzle.getPiece(b, a), 10+PUZZLE_IMAGE_WIDTH/PUZZLE_COLUMNS*b, 10+PUZZLE_IMAGE_HEIGHT/PUZZLE_ROWS*a);
                    addConnectCorrection(frame, piecesLabels[a][b], b, a);
                }
            }

            frame.setBounds(100, 100, (int)(PUZZLE_IMAGE_WIDTH*1.5), (int)(PUZZLE_IMAGE_HEIGHT*1.5));
            frame.setVisible(true);
        } else {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    private void makeLabelUI(JFrame frame) {
        lu = new MyLabelUI();
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
    
    private JLabel showImg(JFrame frame, BufferedImage img, int x, int y) {
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        label.setUI(lu);
        label.setLocation(x, y);
        label.setSize(img.getWidth(), img.getHeight());
        frame.add(label);
        Point2D start = new Point2D.Double();
        Point2D startLocation = new Point2D.Double();
        label.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1)) {
                    frame.getContentPane().setComponentZOrder(label, 0);
                    start.setLocation(e.getLocationOnScreen());
                    startLocation.setLocation(label.getLocation());

                    MouseInputAdapter dragAdapter = new MouseInputAdapter() {
                        public void mouseDragged(MouseEvent e) {
                            Point2D current = e.getLocationOnScreen();
                            label.setLocation(
                                    (int)(current.getX() - start.getX() + startLocation.getX()), 
                                    (int)(current.getY() - start.getY() + startLocation.getY())
                            );
                        }
                    };
                    label.addMouseMotionListener(dragAdapter);

                    MouseInputAdapter releaseAdapter = new MouseInputAdapter() {
                        public void mouseReleased(MouseEvent e) {
                            label.removeMouseMotionListener(dragAdapter);
                            label.removeMouseListener(this);
                        }
                    };
                    label.addMouseListener(releaseAdapter);
                }
            }
        });

        return label;
    }
    
    private void addConnectCorrection(JFrame frame, JLabel piece, int column, int row) {
        int pieceHeight = PUZZLE_IMAGE_HEIGHT / PUZZLE_ROWS;
        int pieceWidth = PUZZLE_IMAGE_WIDTH / PUZZLE_COLUMNS;
        piece.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1)) {
                    Point2D position = piece.getLocation();
                    JLabel topPiece = row > 0 ? piecesLabels[row - 1][column] : null;
                    JLabel bottomPiece = row < PUZZLE_ROWS - 1 ? piecesLabels[row + 1][column] : null;
                    JLabel leftPiece = column > 0 ? piecesLabels[row][column - 1] : null;
                    JLabel rightPiece = column < PUZZLE_COLUMNS - 1 ? piecesLabels[row][column + 1] : null;
                    if ((topPiece != null) && (new Point2D.Double(topPiece.getX(), topPiece.getY() + pieceHeight).distance(position) < 10)) {
                        piece.setLocation(topPiece.getX(), topPiece.getY() + pieceHeight);
                    } else if ((bottomPiece != null) && (new Point2D.Double(bottomPiece.getX(), bottomPiece.getY() - pieceHeight).distance(position) < 10)) {
                        piece.setLocation(bottomPiece.getX(), bottomPiece.getY() - pieceHeight);
                    } else if ((leftPiece != null) && (new Point2D.Double(leftPiece.getX() + pieceWidth, leftPiece.getY()).distance(position) < 10)) {
                        piece.setLocation(leftPiece.getX() + pieceWidth, leftPiece.getY());
                    } else if ((rightPiece != null) && (new Point2D.Double(rightPiece.getX() - pieceWidth, rightPiece.getY()).distance(position) < 10)) {
                        piece.setLocation(rightPiece.getX() - pieceWidth, rightPiece.getY());
                    }                    
                }
            }
        });
    }

    private static class MyLabelUI extends LabelUI {
        
        public static ComponentUI createUI(JComponent c) {
            return new MyLabelUI();
        }

        @Override
        public boolean contains(JComponent l, int x, int y) {
            Rectangle r = new Rectangle(0, 0, l.getWidth(), l.getHeight());
            if (r.contains(new Point2D.Double(x, y))) {
                ImageIcon i = (ImageIcon)((JLabel)l).getIcon();
                return (new Color(((BufferedImage)i.getImage()).getRGB(x, y), true)).getAlpha() != 0;
            } else {
                return false;
            }
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            ImageIcon i = (ImageIcon)((JLabel)c).getIcon();
            g.drawImage(i.getImage(), 0, 0, null);
        }

        @Override
        public void update(Graphics g, JComponent c) {
            super.update(g, c);
        }
    }
}
