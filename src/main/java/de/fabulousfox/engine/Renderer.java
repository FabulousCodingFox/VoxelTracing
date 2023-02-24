package de.fabulousfox.engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Renderer {
    private final long window;
    private int windowWidth, windowHeight;

    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    private float deltaTime;
    private float lastFrame;

    private double lastMouseXP, lastMouseYP, mouseOffX, mouseOffY;

    private int FRAMEBUFFER;
    private int FRAMEBUFFER_COLORBUFFER;
    private int FRAMEBUFFER_RENDERBUFFER1;

    private final Shader SHADER_GRID, SHADER_POST;

    private int VAO_WORLD;
    private int vaosize;
    private ArrayList<Model> models;

    private int VAO_POST;
    private int VBO_POST;

    public Renderer(int windowWidth, int windowHeight, String windowTitle){
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.deltaTime = 0.0f;

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("LWJGL Version: " + Version.getVersion());
        System.out.println("GLFW Version: " + org.lwjgl.glfw.GLFW.glfwGetVersionString());

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing GLFW...");
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
        glfwMakeContextCurrent(window);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        double[] a = new double[1];
        double[] b = new double[1];
        glfwGetCursorPos(window, a, b);
        lastMouseXP = a[0];
        lastMouseYP = b[0];
        mouseOffX = 0d;
        mouseOffY = 0d;

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Setting GLFW window callbacks...");
        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            this.windowWidth = width;
            this.windowHeight = height;
            glViewport(0, 0, width, height);
            projectionMatrix = getProjectionMatrix();

            // Resize Framebuffer
            glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, windowWidth, windowHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER, 0);
            glBindRenderbuffer(GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, windowWidth, windowHeight);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glBindRenderbuffer(GL_RENDERBUFFER, 0);
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(window, true);
        });

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initialize GLFW framebuffer...");
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            assert vidmode != null;
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing GLFW OpenGL Context...");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        glEnable(GL_TEXTURE_2D);

        float[] quadVertices = {
                -1.0f,  1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f,  1.0f,
                1.0f, -1.0f,
                1.0f,  1.0f
        };
        VAO_POST = glGenVertexArrays();
        VBO_POST = glGenBuffers();
        glBindVertexArray(VAO_POST);
        glBindBuffer(GL_ARRAY_BUFFER, VBO_POST);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);

        System.out.println("Ititializing Framebuffer");

        FRAMEBUFFER = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);

        FRAMEBUFFER_COLORBUFFER = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, windowWidth, windowHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER, 0);

        FRAMEBUFFER_RENDERBUFFER1 = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, windowWidth, windowHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, FRAMEBUFFER_RENDERBUFFER1);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
            System.out.println("Framebuffer not initialized");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing Shaders...");
        System.out.println("SHADER_GRID_DEBUG");
        SHADER_GRID = new Shader(
                "shader/debug/SHADER_GRID_DEBUG.vert",
                "shader/debug/SHADER_GRID_DEBUG.frag"
        );
        System.out.println("SHADER_POST_DEBUG");
        SHADER_POST = new Shader(
                "shader/debug/SHADER_POST_DEBUG.vert",
                "shader/debug/SHADER_POST_DEBUG.frag"
        );

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing Matrix...");

        projectionMatrix = getProjectionMatrix();
        viewMatrix = getViewMatrix(new Vector3f(0,0,0), new Vector3f(0,0,1));

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing World...");

        VAO_WORLD = glGenVertexArrays();
        glBindVertexArray(VAO_WORLD);

        models = new ArrayList<>();

        //models.add(new Model(new Texture("models/debug/car.png", GL_TEXTURE1), new Vector3f(0, 0, 0), 23, 12, 52));

        models.addAll(VoxelLoader.load("/models/castle.vox"));

        vaosize = 36 * models.size();
    }

    public boolean shouldClose(){
        return glfwWindowShouldClose(window);
    }

    public void render(Vector3f position, Vector3f direction){
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        final boolean FRAMEBUFFFER = false;

        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        // First Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if(FRAMEBUFFFER) glBindFramebuffer(GL_FRAMEBUFFER, FRAMEBUFFER);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        viewMatrix = getViewMatrix(position, direction);

        SHADER_GRID.use();
        SHADER_GRID.setMatrix4f("projection", projectionMatrix);
        SHADER_GRID.setMatrix4f("view", viewMatrix);
        SHADER_GRID.setVector3f("position", position);
        SHADER_GRID.setVector3f("rotation", direction.normalize());
        SHADER_GRID.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));
        SHADER_GRID.setFloat("iTime", (float) getTime());

        glBindBuffer(GL_ARRAY_BUFFER, VAO_WORLD);

        for(Model model : models){
            model.prepareShader(SHADER_GRID);
            glDrawArrays(GL_TRIANGLES, 36 * model.getId(), 36 * (model.getId() + 1));
        }

        // Second Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if(FRAMEBUFFFER) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0); // back to default
            glDisable(GL_DEPTH_TEST);
            glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            //glPolygonMode(GL_FRONT_FACE, GL_LINE);

            SHADER_POST.use();
            SHADER_POST.setInt("screenTexture", 0);
            SHADER_POST.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));
            glBindVertexArray(VAO_POST);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, FRAMEBUFFER_COLORBUFFER);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        double[] a = new double[1];
        double[] b = new double[1];
        glfwGetCursorPos(window, a, b);
        mouseOffX = a[0] - lastMouseXP;
        mouseOffY = b[0] - lastMouseYP;
        lastMouseXP = a[0];
        lastMouseYP = b[0];

        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public Matrix4f getViewMatrix(Vector3f position, Vector3f direction){
        Vector3f front = direction.normalize();
        Vector3f right = new Vector3f(front).cross(new Vector3f(0,1,0)).normalize();
        Vector3f up = new Vector3f(right).cross(front).normalize();
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public Matrix4f getProjectionMatrix() {
        return new Matrix4f().perspective((float) Math.toRadians(70), (float)windowWidth/(float)windowHeight, 0.1f, Integer.MAX_VALUE);
    }

    public double getMouseMoveX(){
        return mouseOffX;
    }

    public double getMouseMoveY(){
        return mouseOffY;
    }

    public float getFrameTime(){
        return deltaTime;
    }

    public double getTime(){
        return glfwGetTime();
    }

    public boolean getIfKeyIsPressed(Key key){
        if(key == Key.WALK_FORWARD) return glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
        if(key == Key.WALK_BACKWARD) return glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
        if(key == Key.WALK_LEFT) return glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
        if(key == Key.WALK_RIGHT) return glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
        if(key == Key.CROUCH) return glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
        if(key == Key.JUMP) return glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
        return false;
    }
}
