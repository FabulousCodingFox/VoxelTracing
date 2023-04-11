package de.fabulousfox.engine.wrapper;

import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class Texture3D {
    private int texture;

    private ByteBuffer image;

    private int sizeX, sizeY, sizeZ;

    public Texture3D(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.image = BufferUtils.createByteBuffer(sizeX * sizeY * sizeZ * 4);
        BufferUtils.zeroBuffer(image);
    }

    public void setPixel(int x, int y, int z, int color) {
        int address = (x + y * sizeX + z * sizeX * sizeY) * 4;

        image.put(address, (byte) ((color >> 16) & 0xFF)); // Red component
        image.put(address + 1, (byte) ((color >> 8) & 0xFF));  // Green component
        image.put(address + 2, (byte) (color & 0xFF));         // Blue component
        image.put(address + 3, (byte) ((color >> 24) & 0xFF)); // Alpha component
    }

    public void fill(){
        int defaultColor = new Color(0, 0, 0, 0).getRGB();
        int colorRed = new Color(255, 0, 0, 255).getRGB();
        int colorGreen = new Color(0, 255, 0, 255).getRGB();
        int colorBlue = new Color(0, 0, 255, 255).getRGB();
        int colorWhite = new Color(255, 255, 255, 255).getRGB();

        for(int x = 0; x < sizeX; x++){
            for(int y = 0; y < sizeY; y++){
                for(int z = 0; z < sizeZ; z++){
                    int color = -1;
                    if(x == 0 || x == sizeX - 1) color = color == -1 ? colorRed : colorWhite;
                    if(y == 0 || y == sizeY - 1) color = color == -1 ? colorGreen : colorWhite;
                    if(z == 0 || z == sizeZ - 1) color = color == -1 ? colorBlue : colorWhite;
                    if(color == -1) color = defaultColor;
                    setPixel(x, y, z, color);
                }
            }
        }
    }

    public void create() {
        glActiveTexture(GL_TEXTURE5);
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, texture);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA8, sizeX, sizeY, sizeZ, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_3D);
    }

    public int get() {
        return texture;
    }

    public void remove() {
        glDeleteTextures(texture);
    }

    /*
    for(int subimgid = 0; subimgid < sizeY; subimgid++){
        try {
            BufferedImage loadedSubImg = ImageIO.read(new File(path.replace("%s", String.valueOf(subimgid))));
            for (int pixelX = 0; pixelX < sizeX; pixelX++) {
                for (int pixelY = 0; pixelY < sizeZ; pixelY++) {
                    int voxelColor = loadedSubImg.getRGB(pixelX, pixelY);

                    int address = (pixelX + subimgid * sizeX + pixelY * sizeX * sizeY) * 4;

                    image.put(address, (byte) ((voxelColor >> 16) & 0xFF)); // Red component
                    image.put(address + 1, (byte) ((voxelColor >> 8) & 0xFF));  // Green component
                    image.put(address + 2, (byte) (voxelColor & 0xFF));         // Blue component
                    image.put(address + 3, (byte) ((voxelColor >> 24) & 0xFF)); // Alpha component. Only for RGBA
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     */
}
