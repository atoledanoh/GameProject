package ui;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class GameUI extends Table {
    public GameUI(final ProduceGame context) {
        super(context.getSkin());

        setFillParent(true);

        add(new TextButton("Test text for the game screen", getSkin(), "huge"));
    }
}
