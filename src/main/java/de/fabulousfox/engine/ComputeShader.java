package de.fabulousfox.engine;

import static org.lwjgl.opengl.GL33.*;

public class ComputeShader {
    private int compute;
    private int ID;

    public ComputeShader(String computeShaderPath){
        compute = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(compute, "");
        glCompileShader(compute);
        if(glGetShaderi(compute, GL_COMPILE_STATUS) == GL_FALSE){
            System.out.println(
                    glGetShaderInfoLog(compute, glGetShaderi(compute, GL_INFO_LOG_LENGTH))
            );
            throw new RuntimeException("Compute shader failed to compile: "+computeShaderPath);
        }

        ID = glCreateProgram();
        glAttachShader(ID, compute);
        glLinkProgram(ID);
        if(glGetProgrami(ID, GL_LINK_STATUS) == GL_FALSE){
            System.out.println(
                    glGetProgramInfoLog(ID, glGetProgrami(ID, GL_INFO_LOG_LENGTH))
            );
            throw new RuntimeException("Shader program failed to link: "+computeShaderPath);
        }


    }
}
