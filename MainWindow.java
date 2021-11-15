package games.javapuzzle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private List<HashMap<Point,JLabel>> solvedParts;
    private MyLabelUI lu;
    
    public MainWindow() {
        solvedParts = new ArrayList<>();
        
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
                    piecesLabels[a][b] = this.showImg(frame, puzzle.getPiece(b, a), 10+PUZZLE_IMAGE_WIDTH/PUZZLE_COLUMNS*b, 10+PUZZLE_IMAGE_HEIGHT/PUZZLE_ROWS*a, b, a);
                    addConnectCorrection(frame, piecesLabels[a][b], b, a);
                    HashMap<Point,JLabel> m = new HashMap<>();
                    m.put(new Point(b, a), piecesLabels[a][b]);
                    solvedParts.add(m);
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
    
    private JLabel showImg(JFrame frame, BufferedImage img, int x, int y, int column, int row) {
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        label.setUI(lu);
        label.setLocation(x, y);
        label.setSize(img.getWidth(), img.getHeight());
        frame.add(label);
        Point2D start = new Point2D.Double();
        label.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1)) {
                    start.setLocation(e.getLocationOnScreen());
                    
                    HashMap<Point,JLabel> pieceCollection = new HashMap<>();
                    HashMap<Point,Point2D> locationsCollection = new HashMap<>();
                    for (HashMap<Point,JLabel> solvedPart: solvedParts) {
                        if (solvedPart.containsKey(new Point(column, row))) {
                            pieceCollection.putAll(solvedPart);
                            for (Map.Entry<Point,JLabel> sp: solvedPart.entrySet()) {
                                locationsCollection.put(sp.getKey(), sp.getValue().getLocation());
                                frame.getContentPane().setComponentZOrder(sp.getValue(), 0);
                            }
                            break;
                        }
                    }

                    MouseInputAdapter dragAdapter = new MouseInputAdapter() {
                        public void mouseDragged(MouseEvent eMove) {
                            Point2D current = e.getLocationOnScreen();
                            
                            for (Map.Entry<Point,JLabel> l: pieceCollection.entrySet()) {
                                l.getValue().setLocation(
                                        (int)(eMove.getXOnScreen() - start.getX() + locationsCollection.get(l.getKey()).getX()), 
                                        (int)(eMove.getYOnScreen() - start.getY() + locationsCollection.get(l.getKey()).getY())
                                );
                            }
                        }
                    };
                    label.addMouseMotionListener(dragAdapter);

                    MouseInputAdapter releaseAdapter = new MouseInputAdapter() {
                        public void mouseReleased(MouseEvent eRel) {
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
                    HashMap<Point,JLabel> pieceCollection = getSolvedPart(column, row);
                    
                    Point2D position = piece.getLocation();
                    JLabel topPiece = row > 0 ? piecesLabels[row - 1][column] : null;
                    JLabel bottomPiece = row < PUZZLE_ROWS - 1 ? piecesLabels[row + 1][column] : null;
                    JLabel leftPiece = column > 0 ? piecesLabels[row][column - 1] : null;
                    JLabel rightPiece = column < PUZZLE_COLUMNS - 1 ? piecesLabels[row][column + 1] : null;
                    if ((topPiece != null) && (new Point2D.Double(topPiece.getX(), topPiece.getY() + pieceHeight).distance(position) < 10)) {
                        Point diff = new Point(topPiece.getX() - piece.getX(), topPiece.getY() + pieceHeight - piece.getY());
                        HashMap<Point,JLabel> topPieceCollection = getSolvedPart(column, row - 1);
                        if (!topPieceCollection.equals(pieceCollection)) {
                            for (JLabel p: pieceCollection.values()) {
                                p.setLocation((int)(p.getX() + diff.getX()), (int)(p.getY() + diff.getY()));
                            }
                            pieceCollection.putAll(topPieceCollection);
                            solvedParts.remove(topPieceCollection);
                        }
                    }
                    if ((bottomPiece != null) && (new Point2D.Double(bottomPiece.getX(), bottomPiece.getY() - pieceHeight).distance(position) < 10)) {
                        Point diff = new Point(bottomPiece.getX() - piece.getX(), bottomPiece.getY() - pieceHeight - piece.getY());
                        HashMap<Point,JLabel> bottomPieceCollection = getSolvedPart(column, row + 1);
                        if (!bottomPieceCollection.equals(pieceCollection)) {
                            for (JLabel p: pieceCollection.values()) {
                                p.setLocation((int)(p.getX() + diff.getX()), (int)(p.getY() + diff.getY()));
                            }
                            pieceCollection.putAll(bottomPieceCollection);
                            solvedParts.remove(bottomPieceCollection);
                        }
                    }
                    if ((leftPiece != null) && (new Point2D.Double(leftPiece.getX() + pieceWidth, leftPiece.getY()).distance(position) < 10)) {
                        Point diff = new Point(leftPiece.getX() + pieceWidth - piece.getX(), leftPiece.getY() - piece.getY());
                        HashMap<Point,JLabel> leftPieceCollection = getSolvedPart(column - 1, row);
                        if (!leftPieceCollection.equals(pieceCollection)) {
                            for (JLabel p: pieceCollection.values()) {
                                p.setLocation((int)(p.getX() + diff.getX()), (int)(p.getY() + diff.getY()));
                            }
                            pieceCollection.putAll(leftPieceCollection);
                            solvedParts.remove(leftPieceCollection);
                        }
                    }
                    if ((rightPiece != null) && (new Point2D.Double(rightPiece.getX() - pieceWidth, rightPiece.getY()).distance(position) < 10)) {
                        Point diff = new Point(rightPiece.getX() - pieceWidth - piece.getX(), rightPiece.getY() - piece.getY());
                        HashMap<Point,JLabel> rightPieceCollection = getSolvedPart(column + 1, row);
                        if (!rightPieceCollection.equals(pieceCollection)) {
                            for (JLabel p: pieceCollection.values()) {
                                p.setLocation((int)(p.getX() + diff.getX()), (int)(p.getY() + diff.getY()));
                            }
                            pieceCollection.putAll(rightPieceCollection);
                            solvedParts.remove(rightPieceCollection);
                        }
                    }
                }
            }
        });
    }
    
    private HashMap<Point,JLabel> getSolvedPart(int column, int row) {
        for (HashMap<Point,JLabel> solvedPart: solvedParts) {
            if (solvedPart.containsKey(new Point(column, row))) {
                return solvedPart;
            }
        }
        return null;
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
