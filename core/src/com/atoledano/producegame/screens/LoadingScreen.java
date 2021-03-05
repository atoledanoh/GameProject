package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.ScreenUtils;
import ui.LoadingUI;

public class LoadingScreen extends AbstractScreen<LoadingUI> {
    private final AssetManager assetManager;

    public LoadingScreen(final ProduceGame context) {
        super(context);
        this.assetManager = context.getAssetManager();
        //loading map async
        assetManager.load("map/map.tmx", TiledMap.class);
    }

    @Override
    protected LoadingUI getScreenUI(final ProduceGame context) {
        return new LoadingUI(context);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        //check if the loading of assets was correct, then loads game screen
        if (assetManager.update() && Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            context.setScreen(ScreenType.GAME);
        }
        screenUI.setProgress(assetManager.getProgress());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
