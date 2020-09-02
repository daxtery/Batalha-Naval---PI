package JavaFX;

import Common.Direction;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import util.Point;


abstract class SpriteTileFX {

    private Image imageToDraw;

    int x;
    int y;

    int l;
    int c;

    double width;
    double height;

    Direction dir;

    /**
     * @param _i1
     * @param _i2
     * @param boardCoord if you've given the parameters as boardCoordenates or actual x and y values
     */
    public SpriteTileFX(int _i1, int _i2, boolean boardCoord, Direction _dir){
        dir = _dir;
        if(boardCoord)
            setPositionBoard(_i1, _i2);
        else
            setPosition(_i1, _i2);
    }

    void setImageToDraw(Image i){
        imageToDraw = i;
        width = i.getWidth();
        height = i.getHeight();
    }

    Image getImageToDraw(){
        return imageToDraw;
    }

    void draw(GraphicsContext gc){
        gc.drawImage(imageToDraw, x, y);
    }

    void setPosition(int _x, int _y){
        x = _x;
        y = _y;
    }

    void setPositionBoard(Point p){
        setPositionBoard(p.x, p.y);
    }

    void setPositionBoard(int _l, int _c){
        l = _l;
        c = _c;
        y = _l * TileFX.TILE_SIZE;
        x = _c * TileFX.TILE_SIZE;
    }

    Image giveImageBasedOnDirection(Image i){
        return giveImageBasedOnDirection(i, dir);
    }

    static Image giveImageBasedOnDirection(Image i, Direction dir){
        return switch (dir) {
            case Right -> rotateImage(0, i);
            case Down -> rotateImage(90, i);
            case Left -> rotateImage(180, i);
            case Up -> rotateImage(270, i);
        };
    }

    static Image rotateImage(int angle, Image image){

        ImageView iv = new ImageView(image);
        iv.setRotate(angle);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.rgb(0,0,0,0));

        Canvas canvas = new Canvas(TileFX.TILE_SIZE, TileFX.TILE_SIZE);

        return iv.snapshot(params, null);
    }

    boolean contains(double x, double y){
        //System.out.println("X TO LOOK : " + x);
        //System.out.println("Y TO LOOK : " + y);

        //System.out.println("MIN X: " + this.x);
        //System.out.println("MAX X: " + (this.x + width));

        //System.out.println("MIN Y: " + this.y);
        //System.out.println("MAX Y: " + (this.y + height));
        return (y > this.y && y < this.y + height) && (x > this.x && x < this.x + this.width);
    }

}
