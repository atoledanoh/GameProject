package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadingScreen extends AbstractScreen {
    private final AssetManager assetManager;

    public LoadingScreen(final ProduceGame context) {
        super(context);
        this.assetManager = context.getAssetManager();
        //loading map async
        assetManager.load("map/map.tmx", TiledMap.class);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 1, 0, 1);

        //check if the loading of assets was correct, then loads game screen
        if (assetManager.update()){
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
