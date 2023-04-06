package de.fabulousfox.engine;

import de.fabulousfox.engine.utils.Key;
import de.fabulousfox.engine.wrapper.Shader;
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
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class Renderer {
    private final int ZFAR = 2000;//2147483647;
    private final float ZNEAR = 0.1f;

    private final long window;
    private int windowWidth, windowHeight;

    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    private float deltaTime;
    private float lastFrame;

    private double lastMouseXP, lastMouseYP, mouseOffX, mouseOffY;

    private final Shader SHADER_GRID, SHADER_POST, SHADER_GRID_DEBUG_CUBE;

    private ArrayList<Model> models;

    private int VAO_POST;
    private int VBO_POST;

    private int gBuffer, gBufferRboDepth, gBufferALBEDO, gBufferNORMAL, gBufferLIGHTING;


    private float[] defaultCubeMesh = {
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1f,
            1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1f,
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1f,

            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0f,
            1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0f,
            1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0f,
            1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0f,
            0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0f,

            0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 3f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 3f,
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 3f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 3f,
            0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 3f,
            0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 3f,

            1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 2f,
            1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 2f,
            1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 2f,
            1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 2f,
            1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 2f,
            1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 2f,

            0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 5f,
            1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 5f,
            1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 5f,
            1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 5f,
            0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 5f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 5f,

            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 4f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 4f,
            1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 4f,
            1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 4f,
            0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 4f,
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 4f
    };

    private int defaultCubeMeshVBO, defaultCubeMeshVAO;

    public Renderer(int windowWidth, int windowHeight, String windowTitle) {
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
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
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
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) glfwSetWindowShouldClose(window, true);
        });;

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initialize GLFW framebuffer...");
        try (MemoryStack stack = stackPush()) {
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

        glfwSetErrorCallback((window, error) -> {
            System.err.println("GLFW Error: " + error);
        });

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing GLFW OpenGL Context...");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);

        GL.createCapabilities();

        glEnable(GL_DEBUG_OUTPUT);
        glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
            String pType = switch (type) {
                case GL_DEBUG_TYPE_ERROR -> "ERROR";
                case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "DEPRECATED_BEHAVIOR";
                case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "UNDEFINED_BEHAVIOR";
                case GL_DEBUG_TYPE_PORTABILITY -> "PORTABILITY";
                case GL_DEBUG_TYPE_PERFORMANCE -> "PERFORMANCE";
                case GL_DEBUG_TYPE_MARKER -> "MARKER";
                case GL_DEBUG_TYPE_PUSH_GROUP -> "PUSH_GROUP";
                case GL_DEBUG_TYPE_POP_GROUP -> "POP_GROUP";
                case GL_DEBUG_TYPE_OTHER -> "OTHER";
                default -> type + "";
            };

            String pSeverity = switch (severity) {
                case GL_DEBUG_SEVERITY_HIGH -> "HIGH";
                case GL_DEBUG_SEVERITY_MEDIUM -> "MEDIUM";
                case GL_DEBUG_SEVERITY_LOW -> "LOW";
                case GL_DEBUG_SEVERITY_NOTIFICATION -> "NOTIFICATION";
                default -> severity + "";
            };

            String pID = switch (id) {
                case GL_INVALID_VALUE -> "INVALID_VALUE";
                case GL_INVALID_FRAMEBUFFER_OPERATION -> "INVALID_FRAMEBUFFER_OPERATION";
                case GL_INVALID_OPERATION -> "INVALID_OPERATION";
                case GL_STACK_OVERFLOW -> "STACK_OVERFLOW";
                case GL_STACK_UNDERFLOW -> "STACK_UNDERFLOW";
                case GL_OUT_OF_MEMORY -> "OUT_OF_MEMORY";
                case GL_INVALID_ENUM -> "INVALID_ENUM";
                case GL_CONTEXT_LOST -> "CONTEXT_LOST";
                case GL_DEBUG_SEVERITY_NOTIFICATION -> "NOTIFICATION";
                default -> id + "";
            };

            String pSource = switch (source) {
                case GL_DEBUG_SOURCE_API -> "API";
                case GL_DEBUG_SOURCE_WINDOW_SYSTEM -> "WINDOW_SYSTEM";
                case GL_DEBUG_SOURCE_SHADER_COMPILER -> "SHADER_COMPILER";
                case GL_DEBUG_SOURCE_THIRD_PARTY -> "THIRD_PARTY";
                case GL_DEBUG_SOURCE_APPLICATION -> "APPLICATION";
                case GL_DEBUG_SOURCE_OTHER -> "OTHER";
                default -> source + "";
            };

            System.out.println("OpenGL ["+pSource+"]["+pType+"]["+pSeverity+"]["+pID+"]: " + memUTF8(message));
        }, NULL);


        glEnable(GL_TEXTURE_2D);

        //glEnable(GL_CULL_FACE);
        //glCullFace(GL_FRONT);
        glFrontFace(GL_CW);

        float[] quadVertices = {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f
        };
        VAO_POST = glGenVertexArrays();
        VBO_POST = glGenBuffers();
        glBindVertexArray(VAO_POST);
        glBindBuffer(GL_ARRAY_BUFFER, VBO_POST);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing Shaders...");

        System.out.println("SHADER_GRID_DEBUG");
        SHADER_GRID = new Shader(
                "shader/main/VERT.vert",
                "shader/main/FRAGMENT.frag"
        );
        System.out.println("SHADER_POST_DEBUG");
        SHADER_POST = new Shader(
                "shader/post/POST.vert",
                "shader/post/POST.frag"
        );
        System.out.println("SHADER_GRID_DEBUG_CUBE");
        SHADER_GRID_DEBUG_CUBE = new Shader(
                "shader/main/VERT.vert",
                "shader/debug/CUBE.frag"
        );

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing Matrix...");

        projectionMatrix = getProjectionMatrix();
        viewMatrix = getViewMatrix(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1));

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing gBuffer...");

        gBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer);

        gBufferALBEDO = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, gBufferALBEDO);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, windowWidth, windowHeight, 0, GL_RGBA, GL_FLOAT, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gBufferALBEDO, 0);

        gBufferNORMAL = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, gBufferNORMAL);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, gBufferNORMAL, 0);

        gBufferLIGHTING = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, gBufferLIGHTING);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGBA, GL_FLOAT, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, gBufferLIGHTING, 0);

        int[] attachments = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};
        glDrawBuffers(attachments);

        gBufferRboDepth = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, gBufferRboDepth);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, windowWidth, windowHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, gBufferRboDepth);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Framebuffer not complete!");
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //////////////////////////////////////////////////////////////////////////////////////

        defaultCubeMeshVAO = glGenVertexArrays();
        glBindVertexArray(defaultCubeMeshVAO);

        defaultCubeMeshVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, defaultCubeMeshVBO);
        glBufferData(GL_ARRAY_BUFFER, defaultCubeMesh, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, (3 + 3) * 4, 0);
        glEnableVertexAttribArray(0); // Position
        glVertexAttribPointer(1, 3, GL_FLOAT, false, (3 + 3) * 4, 4 * (3));
        glEnableVertexAttribArray(1); // UV

        glBindVertexArray(0);

        //////////////////////////////////////////////////////////////////////////////////////

        System.out.println("Initializing World...");

        models = new ArrayList<>();
        //models.addAll(VoxelLoader.load("/models/vehicle/boat/mediumboat.vox"));
        //models.addAll(VoxelLoader.load("/models/menger.vox"));
        //models.addAll(VoxelLoader.load("/models/castle.vox"));
        models.addAll(VoxelLoader.load("/models/castle_full.vox"));
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    public void render(Vector3f position, Vector3f direction) {
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glFrontFace(GL_CW);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // gBuffer Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        viewMatrix = getViewMatrix(position, direction);
        Shader s = glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS ? SHADER_GRID_DEBUG_CUBE : SHADER_GRID;
        s.use();
        s.setMatrix4f("projection", projectionMatrix);
        s.setMatrix4f("view", viewMatrix);
        s.setVector3f("position", position);
        s.setVector3f("rotation", direction.normalize());

        s.setFloat("zNear", ZNEAR);

        s.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));

        Model.sortModelList(position, models);

        for (Model model : models) {
            glActiveTexture(GL_TEXTURE5);
            glBindTexture(GL_TEXTURE_3D, model.getTextureId());
            s.setInt("voxelTexture", 5);

            model.prepareShader(s);
            glBindVertexArray(defaultCubeMeshVAO);
            glDrawArrays(GL_TRIANGLES, 0, 36);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Final Pass
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBlitFramebuffer(0, 0, windowWidth, windowHeight, 0, 0, windowWidth, windowHeight, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        SHADER_POST.use();
        int debugDisplayMode = 0;
        if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) debugDisplayMode = 1;
        if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) debugDisplayMode = 2;
        if (glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS) debugDisplayMode = 3;
        if (glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS) debugDisplayMode = 4;
        if (glfwGetKey(window, GLFW_KEY_5) == GLFW_PRESS) debugDisplayMode = 5;
        if (glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS) debugDisplayMode = 6;
        if (glfwGetKey(window, GLFW_KEY_7) == GLFW_PRESS) debugDisplayMode = 7;
        if (glfwGetKey(window, GLFW_KEY_8) == GLFW_PRESS) debugDisplayMode = 8;
        if (glfwGetKey(window, GLFW_KEY_9) == GLFW_PRESS) debugDisplayMode = 9;
        SHADER_POST.setInt("debugDisplayMode", debugDisplayMode);

        glActiveTexture(GL_TEXTURE10);
        glBindTexture(GL_TEXTURE_2D, gBufferALBEDO);
        SHADER_POST.setInt("gBufferALBEDO", 10);

        glActiveTexture(GL_TEXTURE11);
        glBindTexture(GL_TEXTURE_2D, gBufferNORMAL);
        SHADER_POST.setInt("gBufferNORMAL", 11);

        glActiveTexture(GL_TEXTURE12);
        glBindTexture(GL_TEXTURE_2D, gBufferLIGHTING);
        SHADER_POST.setInt("gBufferLIGHTING", 12);

        SHADER_POST.setVector2f("iResolution", new Vector2f(windowWidth, windowHeight));

        glBindVertexArray(VAO_POST);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

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

    public Matrix4f getViewMatrix(Vector3f position, Vector3f direction) {
        Vector3f front = direction.normalize();
        Vector3f right = new Vector3f(front).cross(new Vector3f(0, 1, 0)).normalize();
        Vector3f up = new Vector3f(right).cross(front).normalize();
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public Matrix4f getProjectionMatrix() {
        return new Matrix4f().perspective((float) Math.toRadians(70), (float) windowWidth / (float) windowHeight, ZNEAR, ZFAR);
    }

    public double getMouseMoveX() {
        return mouseOffX;
    }

    public double getMouseMoveY() {
        return mouseOffY;
    }

    public float getFrameTime() {
        return deltaTime;
    }

    public double getTime() {
        return glfwGetTime();
    }

    public boolean getIfKeyIsPressed(Key key) {
        if (key == Key.WALK_FORWARD) return glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
        if (key == Key.WALK_BACKWARD) return glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
        if (key == Key.WALK_LEFT) return glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
        if (key == Key.WALK_RIGHT) return glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
        if (key == Key.CROUCH) return glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
        if (key == Key.JUMP) return glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
        return false;
    }

    public void destroy() {
        for (Model model : models) {
            model.remove();
        }
        SHADER_GRID.delete();
        SHADER_POST.delete();
        SHADER_GRID_DEBUG_CUBE.delete();

        glDeleteFramebuffers(gBuffer);
        glDeleteTextures(gBufferALBEDO);
        glDeleteTextures(gBufferNORMAL);

        glDeleteBuffers(defaultCubeMeshVAO);
        glDeleteBuffers(defaultCubeMeshVBO);

        glDeleteBuffers(VBO_POST);
        glDeleteVertexArrays(VAO_POST);
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
