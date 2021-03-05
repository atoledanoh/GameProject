package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class AbstractScreen<T extends Table> implements Screen {
    protected final ProduceGame context;
    protected final FitViewport viewport;
    protected final World world;
    protected final Box2DDebugRenderer box2DDebugRenderer;
    protected final Stage stage;
    protected final T screenUI;

    public AbstractScreen(final ProduceGame context) {
        this.context = context;
        viewport = context.getScreenViewport();
        this.world = context.getWorld();
        this.box2DDebugRenderer = context.getBox2DDebugRenderer();

        stage = context.getStage();
        screenUI = getScreenUI(context);
    }

    protected abstract T getScreenUI(final ProduceGame context);

    @Override
    public void resize(final int width, final int height) {
        viewport.update(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        stage.addActor(screenUI);
    }

    @Override
    public void hide() {
        stage.getRoot().removeActor(screenUI);
    }
}
