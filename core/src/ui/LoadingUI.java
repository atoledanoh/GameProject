package ui;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.StringBuilder;

public class LoadingUI extends Table {
    private final String loadingString;
    private final ProgressBar progressBar;
    private final TextButton anyKeyButton;
    private final TextButton textButton;

    public LoadingUI(final ProduceGame context) {
        super(context.getSkin());

        setFillParent(true);

        final I18NBundle i18NBundle = context.getI18NBundle();

        progressBar = new ProgressBar(0, 1, 0.01f, false, getSkin(), "default");
        progressBar.setAnimateDuration(2);

        loadingString = i18NBundle.format("loading");
        textButton = new TextButton(loadingString, getSkin(), "huge");
        textButton.getLabel().setWrap(true);

        anyKeyButton = new TextButton(i18NBundle.format("pressAnyKey"), getSkin(), "normal");
        anyKeyButton.getLabel().setWrap(true);
        anyKeyButton.setVisible(false);

        add(anyKeyButton).expand().fill().center().row();
        add(textButton).expandX().fillX().bottom().row();
        add(progressBar).expandX().fillX().bottom().pad(50, 50, 50, 50);
        bottom();

//        setDebug(true);
    }

    public void setProgress(final float progress) {
        progressBar.setValue(progress);

        final StringBuilder stringBuilder = textButton.getLabel().getText();
        stringBuilder.setLength(0);
        stringBuilder.append(loadingString);
        stringBuilder.append(" (");
        stringBuilder.append(progress * 100);
        stringBuilder.append("%)");
        textButton.getLabel().invalidateHierarchy();

        if (progress >= 1 && !anyKeyButton.isVisible()) {
            anyKeyButton.setVisible(true);
            anyKeyButton.setColor(1, 1, 1, 0);
            anyKeyButton.addAction(Actions.forever(Actions.sequence(Actions.alpha(1, 1), Actions.alpha(0, 1))));
        }
    }
}
