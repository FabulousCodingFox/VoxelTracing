package de.fabulousfox.engine;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class Texture3D {
    private final int texture;
    private final int channelNum;

    public Texture3D(String path, int texChannel, int texChannelNum, int sizeX, int sizeY, int sizeZ) {
        this.channelNum = texChannelNum;

        glActiveTexture(texChannel);
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, texture);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        ByteBuffer image = BufferUtils.createByteBuffer(sizeX * sizeY * sizeZ * 8);

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

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA8, sizeX, sizeY, sizeZ, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_3D);
    }

    public int get() {
        return texture;
    }

    public int getChannelNum() {
        return channelNum;
    }
}
