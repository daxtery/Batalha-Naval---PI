package JavaFX.Scenes;

import JavaFX.App;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;

public abstract class BaseGameScene extends Scene {

    protected final App app;

    public BaseGameScene(App app, Parent parent) {
        super(parent, Screen.getPrimary().getVisualBounds().getWidth(),
                Screen.getPrimary().getVisualBounds().getHeight());
        this.app = app;
    }

    public abstract void OnSceneSet();

    public abstract void OnSceneUnset();
}
