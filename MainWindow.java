package games.javapuzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
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
        PUZZLE_IMAGE_HEIGHT = PUZZLE_IMAGE_WIDTH*img.getHeight()/img.getWidth();
        BufferedImage scaledImg = this.scaleImg(
                img, 
                PUZZLE_IMAGE_WIDTH/PUZZLE_COLUMNS/3, 
                PUZZLE_IMAGE_HEIGHT/PUZZLE_ROWS/3,
                PUZZLE_IMAGE_WIDTH, 
                PUZZLE_IMAGE_HEIGHT
        );
        for (int a=0; a<PUZZLE_ROWS; a++) {
            for (int b=0; b<PUZZLE_COLUMNS; b++) {
                BufferedImage imgPiece = this.cutPiece(
                        scaledImg, 
                        PUZZLE_IMAGE_WIDTH/PUZZLE_COLUMNS*b, 
                        PUZZLE_IMAGE_HEIGHT/PUZZLE_ROWS*a, 
                        PUZZLE_IMAGE_WIDTH/PUZZLE_COLUMNS, 
                        PUZZLE_IMAGE_HEIGHT/PUZZLE_ROWS, 
                        a == PUZZLE_ROWS - 1, 
                        b == PUZZLE_COLUMNS - 1
                );
                this.showImg(frame, imgPiece, 10+PUZZLE_IMAGE_WIDTH/PUZZLE_COLUMNS*4/3*b, 10+PUZZLE_IMAGE_HEIGHT/PUZZLE_ROWS*4/3*a);
            }
        }
        
        frame.setBounds(100, 100, (int)(PUZZLE_IMAGE_WIDTH*1.5), (int)(PUZZLE_IMAGE_HEIGHT*1.5));
        frame.setVisible(true);
    }
    
    private BufferedImage readImg() {
        JFileChooser file = new JFileChooser();
        file.removeChoosableFileFilter(file.getFileFilter());
        file.addChoosableFileFilter(new FileNameExtensionFilter("images", new String[] {"png", "jpg"}));
        file.showOpenDialog(null);
        
        BufferedImage img = null;
        try {
            img = ImageIO.read(file.getSelectedFile());
        } catch (IOException e) {}
        return img;
    }
    
    private BufferedImage scaleImg(BufferedImage img, int x, int y, int width, int height) {
        BufferedImage scaledImg = new BufferedImage(2*x+width, 2*y+height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D scaledImgG = scaledImg.createGraphics();
        scaledImgG.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), x, y, null);
        scaledImgG.dispose();
        return scaledImg;
    }
    
    private void showImg(JFrame frame, BufferedImage img, int x, int y) {
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        label.setLocation(x, y);
        label.setSize(img.getWidth(), img.getHeight());
        frame.add(label);
    }

    private BufferedImage cutPiece(BufferedImage img, int x, int y, int width, int height, boolean uncutB, boolean uncutR) {
        Path2D shape = this.getCutPath(width, height, uncutB, uncutR);
        BufferedImage piece = makePiece(img, x, y, width, height, shape);
        deletePieceFromImage(img, x, y, width, height, shape);
        
        return piece;
    }
    
    private BufferedImage cutPiece(BufferedImage img, int x, int y, int width, int height) {
        return cutPiece(img, x, y, width, height, false, false);
    }
    
    private BufferedImage makePiece(BufferedImage img, int x, int y, int width, int height, Path2D cutShape) {
        BufferedImage piece = new BufferedImage(width*5/3, height*5/3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphic = piece.createGraphics();
        graphic.drawImage(img.getSubimage(x, y, width*5/3, height*5/3), 0, 0, null);
        Path2D pieceShape = new Path2D.Double();
        pieceShape.append(cutShape, true);
        pieceShape.lineTo(width*4/3, 0);
        pieceShape.lineTo(width*5/3, 0);
        pieceShape.lineTo(width*5/3, height*5/3);
        pieceShape.lineTo(0, height*5/3);
        pieceShape.lineTo(0, height*4/3);
        pieceShape.closePath();
        graphic.clip(pieceShape);
        graphic.setBackground(new Color(0, 0, 0, 0));
        graphic.clearRect(0, 0, piece.getWidth(), piece.getHeight());
        graphic.dispose();
        
        return piece;
    }
    
    private void deletePieceFromImage(BufferedImage img, int x, int y, int width, int height, Path2D cutShape) {
        Path2D fullCutShape = new Path2D.Double();
        fullCutShape.append(cutShape, true);
        fullCutShape.lineTo(width*4/3, 0);
        fullCutShape.lineTo(0, 0);
        fullCutShape.lineTo(0, height*4/3);
        fullCutShape.transform(AffineTransform.getTranslateInstance(x, y));
        fullCutShape.closePath();
        
        Graphics2D imgG = img.createGraphics();
        imgG.setBackground(new Color(0, 0, 0, 0));
        imgG.setClip(fullCutShape);
        imgG.clearRect(0, 0, x+width*5/3, y+height*5/3);
        imgG.dispose();
    }
    
    private Path2D randomizeConnection(int length, int size, boolean vertical) {
        Random randomizer = new Random();
        int direction = randomizer.nextDouble() < 0.5 ? -1 : 1;
        double[] params = new double[]{
            -randomizer.nextDouble()/12 - 1.0/12 + 3.0/7,
            -randomizer.nextDouble()/12 - 1.0/12 + 1.0/2,
            randomizer.nextDouble()/12 + 1.0/12 + 4.0/7,
            randomizer.nextDouble()/12 + 1.0/12 + 1.0/2,
            randomizer.nextDouble()/6 - 1.0/12 + 1.0/2,
        };
        Path2D shape = new Path2D.Double();
        shape.moveTo(0, 0);
        shape.lineTo(length*3/7, 0);
        shape.curveTo(length*params[0], size*2/3*direction, length*params[1], (size-1)*direction, length*params[4], (size-1)*direction);
        shape.curveTo(length*params[3], (size-1)*direction, length*params[2], size*2/3*direction, length*4/7, 0);
        shape.lineTo(length, 0);
        if (vertical) {
            shape.transform(AffineTransform.getRotateInstance(Math.PI*3/2));
            shape.transform(AffineTransform.getTranslateInstance(0, length));
        }
        return shape;
    }
    
    private Path2D randomizeConnection(int length, int size) {
        return this.randomizeConnection(length, size, false);
    }
    
    private Path2D getCutPath(int width, int height, boolean uncutB, boolean uncutR) {
        Path2D shape;
        if (uncutB) {
            shape = new Path2D.Double();
            shape.moveTo(0, 0);
            shape.lineTo(width, 0);
        } else {
            shape = randomizeConnection(width, height/3);
        }
        shape.transform(AffineTransform.getTranslateInstance(width/3, height*4/3));
        
        Path2D shape2;
        if (uncutR) {
            shape2 = new Path2D.Double();
            shape2.moveTo(0, 0);
            shape2.lineTo(height, 0);
            shape2.transform(AffineTransform.getRotateInstance(Math.PI*3/2));
            shape2.transform(AffineTransform.getTranslateInstance(0, height));
        } else {
            shape2 = randomizeConnection(height, width/3, true);
        }
        shape2.transform(AffineTransform.getTranslateInstance(width*4/3, height/3));
        
        shape.append(shape2, true);
        return shape;
    }
}
