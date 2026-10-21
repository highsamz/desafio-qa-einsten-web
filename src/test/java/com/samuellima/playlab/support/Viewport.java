package com.samuellima.playlab.support;

public record Viewport(String profile, int width, int height) {

    @Override
    public String toString() {
        return profile + " (" + width + "x" + height + ")";
    }
}
