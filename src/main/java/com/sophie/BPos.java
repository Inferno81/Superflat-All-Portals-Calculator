package com.sophie;

public record BPos(int x, int z) {
    public BPos toNetherPos() {
        return new BPos(this.x / 8, this.z / 8);
    }

    public BPos toOverworldPos() {
        return new BPos(this.x * 8, this.z * 8);
    }

    public double distanceTo(BPos pos) {
        int dx = this.x - pos.x;
        int dz = this.z - pos.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
