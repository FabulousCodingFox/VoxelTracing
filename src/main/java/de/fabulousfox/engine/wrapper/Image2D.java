package de.fabulousfox.engine.wrapper;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class Image2D {
    private int texture;

    private ByteBuffer image;

    private int sizeX, sizeY;

    public Image2D(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.image = BufferUtils.createByteBuffer(sizeX * sizeY * 32);
        BufferUtils.zeroBuffer(image);
    }

    public void setPixel(int x, int y, int color) {
        int address = (x + y * sizeX) * 32;
        image.put(address, (byte) color);
    }

    public void create() {
        glActiveTexture(GL_TEXTURE5);
        texture = glGenTextures();
        glBindTexture(GL_IMAGE_2D, texture);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_IMAGE_2D, 0, GL_R32UI, sizeX, sizeY, 0, GL_R, GL_UNSIGNED_BYTE, image);
    }

    public int get() {
        return texture;
    }

    public void remove() {
        glDeleteTextures(texture);
    }
}
