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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 *
 * @author programer
 */
public class MainWindow {
    
    private final int puzzleImageWidth;
    private int puzzleImageHeight;
    private final int puzzleRows;
    private final int puzzleColumns;
    
    private JLabel[][] piecesLabels;
    private List<HashMap<Point,JLabel>> solvedParts;
    private MyLabelUI lu;
    
    public MainWindow(BufferedImage img, int puzzleImageWidth, int puzzleColumns, int puzzleRows) {
        this.puzzleImageWidth = puzzleImageWidth;
        this.puzzleColumns = puzzleColumns;
        this.puzzleRows = puzzleRows;
        
        solvedParts = new ArrayList<>();
        
        final JFrame frame = new JFrame();
        makeLabelUI(frame);
        frame.setTitle("Puzzle");
        frame.getContentPane().setBackground(new Color(80, 80, 80));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        
        if (img != null) {
            puzzleImageHeight = puzzleImageWidth*img.getHeight()/img.getWidth();

            ImagePuzzle puzzle = new ImagePuzzle(img, puzzleImageWidth, puzzleColumns, puzzleRows);

            piecesLabels = new JLabel[puzzle.getRows()][];
            for (int a = 0; a < puzzle.getRows(); a++) {
                piecesLabels[a] = new JLabel[puzzle.getColumns()];
                for (int b = 0; b < puzzle.getColumns(); b++) {
                    piecesLabels[a][b] = this.showImg(frame, puzzle.getPiece(b, a), 10+puzzleImageWidth/puzzleColumns*b, 10+puzzleImageHeight/puzzleRows*a, b, a);
                    addConnectCorrection(frame, piecesLabels[a][b], b, a);
                    HashMap<Point,JLabel> m = new HashMap<>();
                    m.put(new Point(b, a), piecesLabels[a][b]);
                    solvedParts.add(m);
                }
            }

            frame.setBounds(100, 100, (int)(puzzleImageWidth + puzzleImageWidth/puzzleColumns + 20), (int)(puzzleImageHeight + puzzleImageHeight/puzzleRows + 80));
            frame.setVisible(true);
            
            Timer timer = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    Random randomizer = new Random();
                    for (JLabel[] piecesRow: piecesLabels) {
                        for (JLabel piece: piecesRow) {
                            piece.setLocation(
                                    randomizer.nextInt(frame.getContentPane().getWidth() - piece.getWidth()), 
                                    randomizer.nextInt(frame.getContentPane().getHeight() - piece.getHeight())
                            );
                        }
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    private void makeLabelUI(JFrame frame) {
        lu = new MyLabelUI();
    }
    
    private JLabel showImg(final JFrame frame, BufferedImage img, final int x, final int y, final int column, final int row) {
        final ImageIcon icon = new ImageIcon(img);
        final JLabel label = new JLabel(icon);
        label.setUI(lu);
        label.setLocation(x, y);
        label.setSize(img.getWidth(), img.getHeight());
        frame.add(label);
        final Point2D start = new Point2D.Double();
        label.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(final MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1)) {
                    start.setLocation(e.getLocationOnScreen());
                    
                    final HashMap<Point,JLabel> pieceCollection = new HashMap<>();
                    final HashMap<Point,Point2D> locationsCollection = new HashMap<>();
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

                    final MouseInputAdapter dragAdapter = new MouseInputAdapter() {
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
    
    private void addConnectCorrection(JFrame frame, final JLabel piece, final int column, final int row) {
        final int pieceHeight = puzzleImageHeight / puzzleRows;
        final int pieceWidth = puzzleImageWidth / puzzleColumns;
        piece.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1)) {
                    HashMap<Point,JLabel> pieceCollection = getSolvedPart(column, row);
                    
                    Point2D position = piece.getLocation();
                    JLabel topPiece = row > 0 ? piecesLabels[row - 1][column] : null;
                    JLabel bottomPiece = row < puzzleRows - 1 ? piecesLabels[row + 1][column] : null;
                    JLabel leftPiece = column > 0 ? piecesLabels[row][column - 1] : null;
                    JLabel rightPiece = column < puzzleColumns - 1 ? piecesLabels[row][column + 1] : null;
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