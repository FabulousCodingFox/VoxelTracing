package de.fabulousfox.engine;

import de.fabulousfox.engine.wrapper.Texture3D;
import de.fabulousfox.libs.voxfileparser.*;
import de.fabulousfox.libs.voxfileparser.chunk.VoxRGBAChunk;
import org.joml.Vector3f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class VoxelLoader {
    private static int counter = 0;

    public static ArrayList<Model> load(String path) {
        ArrayList<Model> models = new ArrayList<>();

        try (VoxReader reader = new VoxReader(VoxelLoader.class.getResourceAsStream(path))) {
            VoxFile voxFile = reader.read();

            for (VoxModelInstance model_instance : voxFile.getModelInstances()) {

                GridPoint3 world_Offset = model_instance.worldOffset;
                VoxModelBlueprint model = model_instance.model;

                int sizeX = model.getSize().x + 1;
                int sizeY = model.getSize().z + 1;
                int sizeZ = model.getSize().y + 1;

                Texture3D texture = new Texture3D(sizeX, sizeY, sizeZ);

                for (Voxel voxel : model.getVoxels()) {
                    int color;
                    if (voxel.getColourIndex() < 0 || voxel.getColourIndex() >= voxFile.getPalette().length) {
                        if (voxel.getColourIndex() < 0 || voxel.getColourIndex() >= VoxRGBAChunk.DEFAULT_PALETTE.length) {
                            color = Color.WHITE.getRGB();
                        } else {
                            color = VoxRGBAChunk.DEFAULT_PALETTE[voxel.getColourIndex()];
                        }
                    } else {
                        color = voxFile.getPalette()[voxel.getColourIndex()];
                    }

                    if (voxel.getPosition().x < 0 || voxel.getPosition().x >= sizeX || voxel.getPosition().y < 0 || voxel.getPosition().y >= sizeZ || voxel.getPosition().z < 0 || voxel.getPosition().z >= sizeY)
                        continue;

                    texture.setPixel(voxel.getPosition().x, voxel.getPosition().z, voxel.getPosition().y, color);
                }

                texture.create();

                models.add(new Model(
                        texture,
                        new Vector3f(
                                world_Offset.x - (int) (model.getSize().x / 2f),
                                world_Offset.z - (int) (model.getSize().z / 2f),
                                -world_Offset.y - (int) (model.getSize().y / 2f)
                        ).mul(Model.VOXEL_SIZE),
                        sizeX, sizeY, sizeZ
                ));

                counter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return models;
    }
}
