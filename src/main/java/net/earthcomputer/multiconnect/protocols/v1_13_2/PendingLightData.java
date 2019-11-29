package net.earthcomputer.multiconnect.protocols.v1_13_2;

public class PendingLightData {

    private static ThreadLocal<PendingLightData> instance = new ThreadLocal<>();

    public static void setInstance(PendingLightData instance) {
        PendingLightData.instance.set(instance);
    }

    public static PendingLightData getInstance() {
        return instance.get();
    }

    private byte[][] blockLight = new byte[16][];
    private byte[][] skyLight = new byte[16][];

    public void setBlockLight(int sectionY, byte[] blockLight) {
        this.blockLight[sectionY] = blockLight;
    }

    public void setSkyLight(int sectionY, byte[] skyLight) {
        this.skyLight[sectionY] = skyLight;
    }

    public byte[] getBlockLight(int sectionY) {
        return blockLight[sectionY];
    }

    public byte[] getSkyLight(int sectionY) {
        return skyLight[sectionY];
    }

}
