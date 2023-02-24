package de.fabulousfox.engine;

import java.awt.image.BufferedImage;

public class VoxelTexture {
    public static int MAX_TEXTURE_SIZE = 4096;

    private int sizeX;
    private int sizeY;
    private int sizeZ;

    private BufferedImage texture;
    private int imageWidth;
    private int imageHeight;
    private int overflowAmount;

    public VoxelTexture(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        this.overflowAmount = (int)Math.floor((float)(sizeX * sizeY) / (float)MAX_TEXTURE_SIZE);
        this.imageHeight = Math.min(sizeX * sizeY, MAX_TEXTURE_SIZE);
        this.imageWidth = sizeZ + sizeZ * overflowAmount;

        this.texture = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
    }

    public void setVoxel(int x, int y, int z, int color) {
        int overflows = 0;
        int xp = z;
        int yp = x + y * sizeX;

        while (yp >= MAX_TEXTURE_SIZE) {
            yp -= MAX_TEXTURE_SIZE;
            xp += sizeZ;
            overflows++;
        }

        if(xp < 0 || xp >= imageWidth || yp < 0 || yp >= imageHeight) return;

        this.texture.setRGB(xp, yp, color);
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }
}
