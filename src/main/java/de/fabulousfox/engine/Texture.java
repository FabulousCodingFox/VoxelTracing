package de.fabulousfox.engine;

import org.lwjgl.BufferUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private final int texture;

    public Texture(String path, int texChannel) {
        glActiveTexture(texChannel);
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        // set the texture wrapping parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // load image, create texture and generate mipmaps
        String absolutePath;
        if(path.startsWith("C:/")){
            absolutePath = path;
        } else {
            absolutePath = Objects.requireNonNull(FileUtils.class.getClassLoader().getResource(path)).getPath().substring(1);
        }
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

        /*int channelamount = channels.get();

        int format = GL_RED;
        if (channelamount == 3) {
            format = GL_RGB;
        } else if (channelamount == 4) {
            format = GL_RGBA;
        }*/

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, x.get(), y.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
        stbi_image_free(image);
    }

    public int get() {
        return texture;
    }
}
