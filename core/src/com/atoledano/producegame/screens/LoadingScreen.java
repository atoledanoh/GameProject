package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadingScreen extends AbstractScreen {

    public LoadingScreen(final ProduceGame context) {
        super(context);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 1, 0, 1);

        if (Gdx.input.isKeyPressed(Input.Keys.G)){
            context.setScreen(ScreenType.GAME);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
