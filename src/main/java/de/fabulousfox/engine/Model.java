package de.fabulousfox.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL33.*;

public class Model {
    public static final float VOXEL_SIZE = 0.05f;

    private static int idCounter = 0;
    private final int id;

    private final Vector3f position;

    private final Texture3D data;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private final int textureSizeX;
    private final int textureSizeY;

    private int vao;

    public Model(Texture3D data, Vector3f position, int sizeX, int sizeY, int sizeZ, int textureSizeX, int textureSizeY) {
        this.position = new Vector3f(position).mul(VOXEL_SIZE);

        this.id = idCounter;
        idCounter++;

        this.data = data;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        final float dimX = sizeX * VOXEL_SIZE;
        final float dimY = sizeY * VOXEL_SIZE;
        final float dimZ = sizeZ * VOXEL_SIZE;

        this.textureSizeX = textureSizeX;
        this.textureSizeY = textureSizeY;

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

                0.0f, 0.0f, dimZ,  1.0f, 1.0f, 3f,
                0.0f, dimY, 0.0f,  0.0f, 0.0f, 3f,
                0.0f, dimY, dimZ,  1.0f, 0.0f, 3f,
                0.0f, dimY, 0.0f,  0.0f, 0.0f, 3f,
                0.0f, 0.0f, dimZ,  1.0f, 1.0f, 3f,
                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 3f,

                dimX, dimY, dimZ,  0.0f, 0.0f, 2f,
                dimX, dimY, 0.0f,  1.0f, 0.0f, 2f,
                dimX, 0.0f, 0.0f,  1.0f, 1.0f, 2f,
                dimX, 0.0f, 0.0f,  1.0f, 1.0f, 2f,
                dimX, 0.0f, dimZ,  0.0f, 1.0f, 2f,
                dimX, dimY, dimZ,  0.0f, 0.0f, 2f,

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

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, (3+3)*4, 0);
        glEnableVertexAttribArray(0); // Position
        glVertexAttribPointer(1, 3, GL_FLOAT, false, (3+3)*4, 4*(3));
        glEnableVertexAttribArray(1); // UV

        glBindVertexArray(0);
    }

    public void prepareShader(Shader shader) {
        shader.setMatrix4f("model", new Matrix4f().translate(this.position));
        shader.setInt("dataContainer", this.data.getChannelNum());
        shader.setInt("sizeX", this.sizeX);
        shader.setInt("sizeY", this.sizeY);
        shader.setInt("sizeZ", this.sizeZ);
        shader.setFloat("voxelSize", VOXEL_SIZE);
        shader.setInt("textureSizeX", this.textureSizeX);
        shader.setInt("textureSizeY", this.textureSizeY);
    }

    public int getId() {
        return id;
    }

    public int getVAO() {
        return vao;
    }

}
