package de.fabulousfox.engine;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;

public class ShaderWriteableTextureBuffer {
    private int id, texID, type;

    public ShaderWriteableTextureBuffer(int size, int usage, int type) {
        this.type = type;

        id = glGenBuffers();

        glBindBuffer(GL_TEXTURE_BUFFER, id);
        glBufferData(GL_TEXTURE_BUFFER, size, usage);
        texID = glGenTextures();
        glBindBuffer(GL_TEXTURE_BUFFER, 0);
    }

    public void bind(Shader shader, String uniform, int channel) {
        glActiveTexture(GL_TEXTURE0 + channel);
        glBindTexture(GL_TEXTURE_BUFFER, texID);
        glTexBuffer(GL_TEXTURE_BUFFER, type, id);
        glBindImageTexture(channel, texID, 0, false, 0, GL_READ_WRITE , type);
        shader.setInt(uniform, texID);
    }

    public void clear(){
        glBindTexture(GL_TEXTURE_BUFFER, texID);
        glClearTexImage(texID, 0, type, GL_FLOAT, (ByteBuffer) null);
    }

    public void destroy() {
        glDeleteBuffers(id);
    }
}
