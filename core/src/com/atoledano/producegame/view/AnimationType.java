package com.atoledano.producegame.view;

public enum AnimationType {
    PLAYER_MOVE_UP("characters/player.atlas", "player", 0.1f, 0),
    PLAYER_MOVE_LEFT("characters/player.atlas", "player", 0.1f, 1),
    PLAYER_MOVE_DOWN("characters/player.atlas", "player", 0.1f, 2),
    PLAYER_MOVE_RIGHT("characters/player.atlas", "player", 0.1f, 3);

    private final String atlasPath;
    private final String atlasKey;
    private final float frameTime;
    private final int rowIndex;

    AnimationType(String atlasPath, String atlasKey, float frameTime, int rowIndex) {
        this.atlasPath = atlasPath;
        this.atlasKey = atlasKey;
        this.frameTime = frameTime;
        this.rowIndex = rowIndex;
    }

    public String getAtlasPath() {
        return atlasPath;
    }

    public String getAtlasKey() {
        return atlasKey;
    }

    public float getFrameTime() {
        return frameTime;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}
