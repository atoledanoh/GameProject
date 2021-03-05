package com.atoledano.producegame.screens;

import com.atoledano.producegame.ProduceGame;
import com.atoledano.producegame.audio.AudioType;
import com.atoledano.producegame.input.GameKeys;
import com.atoledano.producegame.input.InputManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.ScreenUtils;
import ui.LoadingUI;

public class LoadingScreen extends AbstractScreen<LoadingUI> {
    private final AssetManager assetManager;
    private boolean isMusicLoaded;

    public LoadingScreen(final ProduceGame context) {
        super(context);
        this.assetManager = context.getAssetManager();
        //loading map async
        assetManager.load("map/map.tmx", TiledMap.class);

        //loading audio
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
        ScreenUtils.clear(0, 0, 0, 1);

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
        audioManager.playAudio(AudioType.SELECT);
        if (assetManager.getProgress() >= 1) {
            context.setScreen(ScreenType.GAME);
        }
    }

    @Override
    public void keyUp(InputManager inputManager, GameKeys key) {

    }
}
