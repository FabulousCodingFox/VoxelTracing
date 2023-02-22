package de.fabulousfox.engine;

import org.joml.Vector3f;

import static org.lwjgl.opengl.GL33.GL_FLOAT;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL33.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL33.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL33.glVertexAttribPointer;

public class Model {
    private final float voxelSize = 0.05f;

    private Texture data;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int vbo;

    public Model(Texture data, int sizeX, int sizeY, int sizeZ) {
        this.data = data;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        float dimX = sizeX * voxelSize;
        float dimY = sizeY * voxelSize;
        float dimZ = sizeZ * voxelSize;

        float[] vertices = {
                0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 1f,
                dimX, 0.0f, 0.0f,  1.0f, 0.0f, 1f,
                dimX, dimY, 0.0f,  1.0f, 1.0f, 1f,
                dimX, dimY, 0.0f,  1.0f, 1.0f, 1f,
                0.0f, dimY, 0.0f,  0.0f, 1.0f, 1f,
                0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 1f,

                0.0f, 0.0f, dimZ,  0.0f, 0.0f, 0f,
                dimX, 0.0f, dimZ,  1.0f, 0.0f, 0f,
                dimX, dimY, dimZ,  1.0f, 1.0f, 0f,
                dimX, dimY, dimZ,  1.0f, 1.0f, 0f,
                0.0f, dimY, dimZ,  0.0f, 1.0f, 0f,
                0.0f, 0.0f, dimZ,  0.0f, 0.0f, 0f,

                0.0f, dimY, dimZ,  1.0f, 0.0f, 3f,
                0.0f, dimY, 0.0f,  1.0f, 1.0f, 3f,
                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 3f,
                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 3f,
                0.0f, 0.0f, dimZ,  0.0f, 0.0f, 3f,
                0.0f, dimY, dimZ,  1.0f, 0.0f, 3f,

                dimX, dimY, dimZ,  1.0f, 0.0f, 2f,
                dimX, dimY, 0.0f,  1.0f, 1.0f, 2f,
                dimX, 0.0f, 0.0f,  0.0f, 1.0f, 2f,
                dimX, 0.0f, 0.0f,  0.0f, 1.0f, 2f,
                dimX, 0.0f, dimZ,  0.0f, 0.0f, 2f,
                dimX, dimY, dimZ,  1.0f, 0.0f, 2f,

                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 5f,
                dimX, 0.0f, 0.0f,  1.0f, 1.0f, 5f,
                dimX, 0.0f, dimZ,  1.0f, 0.0f, 5f,
                dimX, 0.0f, dimZ,  1.0f, 0.0f, 5f,
                0.0f, 0.0f, dimZ,  0.0f, 0.0f, 5f,
                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 5f,

                0.0f, dimY, 0.0f,  0.0f, 1.0f, 4f,
                dimX, dimY, 0.0f,  1.0f, 1.0f, 4f,
                dimX, dimY, dimZ,  1.0f, 0.0f, 4f,
                dimX, dimY, dimZ,  1.0f, 0.0f, 4f,
                0.0f, dimY, dimZ,  0.0f, 0.0f, 4f,
                0.0f, dimY, 0.0f,  0.0f, 1.0f, 4f
        };
        

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, (3+3)*4, 0);
        glEnableVertexAttribArray(0); // Position
        glVertexAttribPointer(1, 3, GL_FLOAT, false, (3+3)*4, 4*(3));
        glEnableVertexAttribArray(1); // UV
    }

    public void prepareShader(Shader shader, Vector3f position, Vector3f rotation) {
        shader.setInt("dataContainer", 1);
        shader.setInt("sizeX", sizeX);
        shader.setInt("sizeY", sizeY);
        shader.setInt("sizeZ", sizeZ);
        shader.setFloat("voxelSize", voxelSize);
        shader.setVector3f("position", position);
        shader.setVector3f("rotation", rotation);
    }

}
