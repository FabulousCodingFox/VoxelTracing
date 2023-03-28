package de.fabulousfox.engine.wrapper;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class Texture2D {
    private int texture;

    private ByteBuffer image;

    private int sizeX, sizeY;

    public Texture2D(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.image = BufferUtils.createByteBuffer(sizeX * sizeY * 32);
        BufferUtils.zeroBuffer(image);
    }

    public void setPixel(int x, int y, int color) {
        int address = (x + y * sizeX) * 4;

        image.put(address, (byte) ((color >> 16) & 0xFF)); // Red component
        image.put(address + 1, (byte) ((color >> 8) & 0xFF));  // Green component
        image.put(address + 2, (byte) (color & 0xFF));         // Blue component
        image.put(address + 3, (byte) ((color >> 24) & 0xFF)); // Alpha component
    }

    public void create() {
        glActiveTexture(GL_TEXTURE5);
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32I, sizeX, sizeY, 0, GL_R, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public int get() {
        return texture;
    }

    public void remove() {
        glDeleteTextures(texture);
    }
}
