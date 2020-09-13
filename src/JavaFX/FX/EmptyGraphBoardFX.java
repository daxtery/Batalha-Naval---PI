package JavaFX.FX;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class EmptyGraphBoardFX extends Canvas {

    final static Image BACKGROUND_WATER = new Image("images/water_bg.jpg");
    GraphicsContext gc;

    AnimationTimer anim;

    EmptyGraphBoardFX(int lines, int columns, int _w, int _h) {
        super(_w, _h);
        gc = getGraphicsContext2D();

        anim = new AnimationTimer() {
            final double perSec = 1;
            final int x_max = columns * TileFX.TILE_SIZE;
            final int y_max = lines * TileFX.TILE_SIZE;
            long lastNano = System.nanoTime();

            public void handle(long currentNanoTime) {
                if ((currentNanoTime - lastNano) / 1000000000.0 > perSec) {
                    gc.drawImage(BACKGROUND_WATER, 0, 0);
                    for (int l = 0; l < lines; l++)
                        gc.strokeLine(0, l * TileFX.TILE_SIZE, x_max, l * TileFX.TILE_SIZE);
                    for (int c = 0; c < columns; c++)
                        gc.strokeLine(c * TileFX.TILE_SIZE, 0, c * TileFX.TILE_SIZE, y_max);
                    lastNano = currentNanoTime;
                }
            }
        };

    }

    public void startAnimating() {
        anim.start();
    }

    public void stopAnimating() {
        anim.stop();
    }
}
