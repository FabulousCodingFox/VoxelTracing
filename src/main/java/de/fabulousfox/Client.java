package de.fabulousfox;

import de.fabulousfox.engine.Key;
import de.fabulousfox.engine.Renderer;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.stream.Collectors;

public class Client {
    private final Renderer engine;

    private Vector3f playerPosition, playerLookAt, playerPrevPosition;
    private double yaw, pitch;

    private final float playerWalkSpeed, playerTurnSpeed;

    private Vector3f dir;

    public final Vector3f worldUp = new Vector3f(0,1,0);

    public Client(){
        engine = new Renderer(1280,960, "Voxel Renderer");

        playerPosition = new Vector3f(0.5f, 0, 0.5f);
        playerPrevPosition = new Vector3f(0.5f, 0, 0.5f);
        playerLookAt = new Vector3f(0, 0, 1);
        yaw = 0d;
        pitch = 0d;

        playerWalkSpeed = 2f;
        playerTurnSpeed = 25f;

        dir = new Vector3f(0, 0, 0);

        renderer();
    }

    public void renderer(){
        boolean running = true;

        double timestamp = engine.getTime();
        int fps = 0;

        while (running) {
            if(engine.getTime()-timestamp > 1d){
                timestamp = engine.getTime();
                System.out.println("[FPS]: "+fps);
                fps = 0;
            }
            fps++;

            // Event Queue
            float deltaTime = engine.getFrameTime();

            double mouseX = engine.getMouseMoveX();
            double mouseY = engine.getMouseMoveY();
            yaw = yaw + mouseX * playerTurnSpeed * deltaTime;
            pitch = pitch - mouseY * playerTurnSpeed * deltaTime;

            if(pitch < -90) pitch=-90;
            if(pitch > 90) pitch=90;
            if(yaw >= 360) yaw-=360;
            if(yaw < 0) yaw+=360;

            Vector3f front = new Vector3f();
            front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
            front.y = (float) Math.sin(Math.toRadians(pitch));
            front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
            playerLookAt = front.normalize();

            boolean keyWalkForward = engine.getIfKeyIsPressed(Key.WALK_FORWARD);
            boolean keyWalkBackward = engine.getIfKeyIsPressed(Key.WALK_BACKWARD);
            boolean keyWalkLeft = engine.getIfKeyIsPressed(Key.WALK_LEFT);
            boolean keyWalkRight = engine.getIfKeyIsPressed(Key.WALK_RIGHT);
            boolean keyCrouch = engine.getIfKeyIsPressed(Key.CROUCH);
            boolean keyJump = engine.getIfKeyIsPressed(Key.JUMP);

            float multiplier = deltaTime *= playerWalkSpeed;

            Vector3f ns = new Vector3f(playerLookAt.x, 0, playerLookAt.z).mul(keyWalkForward?1:(keyWalkBackward?-1:0));
            Vector3f ow = new Vector3f(playerLookAt.x, 0, playerLookAt.z).cross(worldUp).mul(keyWalkRight?0.5f:(keyWalkLeft?-0.5f:0));
            dir = ns.add(ow).normalize().mul(multiplier);
            playerPosition.add(dir);

            // Render Queue
            running = engine.shouldClose();
            engine.render(
                    playerPosition,
                    playerLookAt
            );
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
