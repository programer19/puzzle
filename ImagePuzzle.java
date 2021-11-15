package games.javapuzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 *
 * @author programer
 */
public class ImagePuzzle {
    
    private BufferedImage source;
    private int puzzleWidth;
    private int puzzleHeight;
    private int puzzleColumns;
    private int puzzleRows;
    private BufferedImage[][] puzzles;
    
    private int nextPiece = 0;

    public ImagePuzzle(BufferedImage image, int puzzleWidth, int puzzleColumns, int puzzleRows) {
        this.puzzleWidth = puzzleWidth;
        this.puzzleHeight = puzzleWidth*image.getHeight()/image.getWidth();
        this.puzzleColumns = puzzleColumns;
        this.puzzleRows = puzzleRows;
        this.source = scaleImage(image);
        splitImageToPuzzles();
    }
    
    public BufferedImage getPiece(int column, int row) {
        return puzzles[row][column];
    }
    
    public int getColumns() {
        return puzzleColumns;
    }
    
    public int getRows() {
        return puzzleRows;
    }
    
    private BufferedImage scaleImage(BufferedImage image) {
        int x = puzzleWidth/puzzleColumns/3;
        int y = puzzleHeight/puzzleRows/3;
        BufferedImage scaledImg = new BufferedImage(2*x+puzzleWidth+1, 2*y+puzzleHeight+1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D scaledImgG = scaledImg.createGraphics();
        scaledImgG.drawImage(image.getScaledInstance(puzzleWidth, puzzleHeight, Image.SCALE_SMOOTH), x, y, null);
        scaledImgG.dispose();
        return scaledImg;
    }
    
    private void splitImageToPuzzles() {
        this.puzzles = new BufferedImage[puzzleRows][];
        for (int row = 0; row < puzzleRows; row++) {
            this.puzzles[row] = new BufferedImage[puzzleColumns];
            for (int column = 0; column < puzzleColumns; column++) {
                Path2D shape = getCutPath(row == puzzleRows - 1, column == puzzleColumns - 1);
                this.puzzles[row][column] = makeNextPiece(shape);
                deleteNextPieceFromSource(shape);
                nextPiece++;
            }
        }
    }
    
    private BufferedImage makeNextPiece(Path2D cutShape) {
        int pieceWidth = puzzleWidth/puzzleColumns;
        int pieceHeight = puzzleHeight/puzzleRows;
        int x = (nextPiece % puzzleColumns) * pieceWidth;
        int y = nextPiece / puzzleColumns * pieceHeight;
        
        BufferedImage piece = new BufferedImage(pieceWidth*5/3, pieceHeight*5/3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphic = piece.createGraphics();
        graphic.drawImage(source.getSubimage(x, y, pieceWidth*5/3, pieceHeight*5/3), 0, 0, null);
        Path2D pieceShape = new Path2D.Double();
        pieceShape.append(cutShape, true);
        pieceShape.lineTo(pieceWidth*4/3, 0);
        pieceShape.lineTo(pieceWidth*5/3, 0);
        pieceShape.lineTo(pieceWidth*5/3, pieceHeight*5/3);
        pieceShape.lineTo(0, pieceHeight*5/3);
        pieceShape.lineTo(0, pieceHeight*4/3);
        pieceShape.closePath();
        graphic.clip(pieceShape);
        graphic.setBackground(new Color(0, 0, 0, 0));
        graphic.clearRect(0, 0, piece.getWidth(), piece.getHeight());
        graphic.dispose();
        
        return piece;
    }
    
    private void deleteNextPieceFromSource(Path2D cutShape) {
        int pieceWidth = puzzleWidth/puzzleColumns;
        int pieceHeight = puzzleHeight/puzzleRows;
        int x = (nextPiece % puzzleColumns) * pieceWidth;
        int y = nextPiece / puzzleColumns * pieceHeight;
        
        Path2D fullCutShape = new Path2D.Double();
        fullCutShape.append(cutShape, true);
        fullCutShape.lineTo(pieceWidth*4/3, 0);
        fullCutShape.lineTo(0, 0);
        fullCutShape.lineTo(0, pieceHeight*4/3);
        fullCutShape.transform(AffineTransform.getTranslateInstance(x, y));
        fullCutShape.closePath();
        
        Graphics2D imgG = source.createGraphics();
        imgG.setBackground(new Color(0, 0, 0, 0));
        imgG.setClip(fullCutShape);
        imgG.clearRect(0, 0, x+pieceWidth*5/3, y+pieceHeight*5/3);
        imgG.dispose();
    }
    
    private Path2D getCutPath(boolean uncutB, boolean uncutR) {
        int pieceWidth = puzzleWidth/puzzleColumns;
        int pieceHeight = puzzleHeight/puzzleRows;
        
        Path2D shape;
        if (uncutB) {
            shape = new Path2D.Double();
            shape.moveTo(0, 0);
            shape.lineTo(pieceWidth, 0);
        } else {
            shape = randomizeConnection(pieceWidth, pieceHeight/3);
        }
        shape.transform(AffineTransform.getTranslateInstance(pieceWidth/3, pieceHeight*4/3));
        
        Path2D shape2;
        if (uncutR) {
            shape2 = new Path2D.Double();
            shape2.moveTo(0, 0);
            shape2.lineTo(pieceHeight, 0);
            shape2.transform(AffineTransform.getRotateInstance(Math.PI*3/2));
            shape2.transform(AffineTransform.getTranslateInstance(0, pieceHeight));
        } else {
            shape2 = randomizeConnection(pieceHeight, pieceWidth/3, true);
        }
        shape2.transform(AffineTransform.getTranslateInstance(pieceWidth*4/3, pieceHeight/3));
        
        shape.append(shape2, true);
        return shape;
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
}
