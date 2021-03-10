package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.audio.AudioType;
import com.atoledano.producegame.input.GameKeys;
import com.atoledano.producegame.input.InputManager;
import com.atoledano.producegame.map.MapType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.atoledano.producegame.view.LoadingUI;

public class LoadingScreen extends AbstractScreen<LoadingUI> {
    private final AssetManager assetManager;
    private boolean isMusicLoaded;

    public LoadingScreen(final ProduceGame context) {
        super(context);
        this.assetManager = context.getAssetManager();

        //load characters and effects
        assetManager.load("characters/player.atlas", TextureAtlas.class);

        //load maps
        for (final MapType mapType:MapType.values()){
            assetManager.load(mapType.getFilePath(),TiledMap.class);
        }

        //load audio
        isMusicLoaded = false;
        for (final AudioType audioType : AudioType.values()) {
            assetManager.load(audioType.getFilePath(), audioType.isMusic() ? Music.class : Sound.class);
        }
    }

    @Override
    protected LoadingUI getScreenUI(final ProduceGame context) {
        return new LoadingUI(context);
    }

    @Override
    public void render(float delta) {
//        ScreenUtils.clear(0, 0, 0, 1);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //check if the loading of assets was correct, then loads game screen
        assetManager.update();
        if (!isMusicLoaded && assetManager.isLoaded(AudioType.INTRO.getFilePath())) {
            isMusicLoaded = true;
            audioManager.playAudio(AudioType.INTRO);
        }
        screenUI.setProgress(assetManager.getProgress());
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
        audioManager.stopAudio();
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

    @Override
    public void keyPressed(InputManager inputManager, GameKeys key) {
        if (assetManager.getProgress() >= 1) {
            audioManager.playAudio(AudioType.SELECT);
            context.setScreen(ScreenType.GAME);
        }
    }

    @Override
    public void keyUp(InputManager inputManager, GameKeys key) {

    }
}
