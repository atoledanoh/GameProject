package com.atoledano.producegame.map;

public enum MapType {
    MAP_1("map/map.tmx"),
    MAP_2("map/newmap.tmx");

    private final String filePath;

    MapType(final String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
