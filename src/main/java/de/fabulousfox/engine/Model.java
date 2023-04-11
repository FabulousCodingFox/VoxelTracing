package de.fabulousfox.engine;

import de.fabulousfox.engine.wrapper.Shader;
import de.fabulousfox.engine.wrapper.Texture3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;

public class Model {
    public static final float VOXEL_SIZE = 0.05f;

    private static int idCounter = 0;
    private final int id;

    private final Vector3f position;
    private final Vector3f rotation;

    private final Texture3D data;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    public Model(Texture3D data, Vector3f position, Vector3f rotation, int sizeX, int sizeY, int sizeZ) {
        this.position = position;
        this.rotation = rotation;

        this.id = idCounter;
        idCounter++;

        this.data = data;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public Model(Texture3D data, Vector3f position, int sizeX, int sizeY, int sizeZ){
        this(data, position, new Vector3f(0, 0, 0), sizeX, sizeY, sizeZ);
    }

    public void prepareShader(Shader shader) {
        shader.setMatrix4f("model", new Matrix4f().translate(this.position));
        shader.setInt("dataContainer", 5);
        shader.setInt("sizeX", this.sizeX);
        shader.setInt("sizeY", this.sizeY);
        shader.setInt("sizeZ", this.sizeZ);
        shader.setVector3f("modelPosition", this.position);
        shader.setVector3f("modelRotation", this.rotation);
        shader.setFloat("voxelSize", VOXEL_SIZE);
    }

    public int getId() {
        return id;
    }

    public Vector3f getPosition() {
        return position;
    }

    public int getTextureId() {
        return data.get();
    }

    public void remove() {
        data.remove();
    }

    public Vector3f getSize() {
        return new Vector3f(sizeX, sizeY, sizeZ);
    }

    public void syncCreateData() {
        data.create();
    }

    public double getDistance(Vector3f cam, boolean nearestPoint) {
        float sX = sizeX * VOXEL_SIZE;
        float sY = sizeY * VOXEL_SIZE;
        float sZ = sizeZ * VOXEL_SIZE;

        Vector3f nearestPointOnBoxToCam = new Vector3f(
                Math.max(position.x, Math.min(cam.x, position.x + sX)),
                Math.max(position.y, Math.min(cam.y, position.y + sY)),
                Math.max(position.z, Math.min(cam.z, position.z + sZ))
        );

        Vector3f furthestPointOnBoxToCam = new Vector3f(
                (position.x + sX) - nearestPointOnBoxToCam.x,
                (position.y + sY) - nearestPointOnBoxToCam.y,
                (position.z + sZ) - nearestPointOnBoxToCam.z
        );

        Vector3f comparePoint = nearestPoint ? nearestPointOnBoxToCam : furthestPointOnBoxToCam;
        return Math.abs(comparePoint.distance(cam));
    }

    /*public static void sortModelList(Vector3f cameraPosition, List<Model> models) {
        models.sort(Comparator.comparing(model -> model.getDistance(cameraPosition, true)));
    }*/

    public static void getSortedModelLists(Vector3f cameraPosition, List<Model> models, List<Model> modelsOutside, List<Model> modelsInside) {
        for (Model model : models) {
            if (cameraPosition.x > model.position.x && cameraPosition.x < model.position.x + model.sizeX * VOXEL_SIZE && cameraPosition.y > model.position.y && cameraPosition.y < model.position.y + model.sizeY * VOXEL_SIZE && cameraPosition.z > model.position.z && cameraPosition.z < model.position.z + model.sizeZ * VOXEL_SIZE) {
                modelsInside.add(model);
            } else {
                modelsOutside.add(model);
            }
        }

        modelsOutside.sort(Comparator.comparing(model -> model.getDistance(cameraPosition, true)));
        modelsInside.sort(Comparator.comparing(model -> model.getDistance(cameraPosition, false)));
    }
}
