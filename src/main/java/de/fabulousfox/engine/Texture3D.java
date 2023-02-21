package de.fabulousfox.engine;

import org.lwjgl.BufferUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture3D {
    private int texID;

    public Texture3D(String folderpath, int texChannel) {
        glActiveTexture(texChannel);
        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, texID);

        // set the texture wrapping parameters
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int WIDTH = 8;
        int HEIGHT = 8;
        int DEPTH = 8;

        glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB8, WIDTH, HEIGHT, DEPTH, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        for(int i = 0; i < DEPTH; i++){
            System.out.println("Loading texture " + (i+1) + " of " + DEPTH);
            String absolutePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                    folderpath + "/" + (i+1) + ".png"
            )).getPath().substring(1);
            if(!System.getProperty("os.name").contains("Windows")){
                absolutePath = File.separator + absolutePath;
            }
            stbi_set_flip_vertically_on_load(true);
            IntBuffer x = BufferUtils.createIntBuffer(1);
            IntBuffer y = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            ByteBuffer image = stbi_load(absolutePath, x, y, channels, STBI_rgb_alpha);
            if (image == null) {
                throw new IllegalStateException("Could not decode image file ["+ absolutePath +"]: ["+ stbi_failure_reason() +"]");
            }

            glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, i, WIDTH, HEIGHT, 1, GL_RGBA, GL_UNSIGNED_BYTE, image);

            stbi_image_free(image);
        }
    }

    public int get() {
        return texID;
    }
}
