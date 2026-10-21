package com.samuellima.playlab.support;

public record Viewport(String profile, int width, int height) {

    public boolean isMobile() {
        return "mobile".equalsIgnoreCase(profile);
    }

    @Override
    public String toString() {
        return profile + " (" + width + "x" + height + ")";
    }
}
