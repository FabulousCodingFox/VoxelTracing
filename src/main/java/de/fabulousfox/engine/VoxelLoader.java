package de.fabulousfox.engine;

import de.fabulousfox.libs.voxfileparser.*;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VoxelLoader {
    private static int counter = 1;

    public static int intToGL_TEXTURE(int n){
        if(n < 1 || n > 31) throw new IllegalArgumentException("n must be between 1 and 31 (inclusive).");
        return 33984 + n;
    }

    public static ArrayList<Model> load(String path) {
        ArrayList<Model> models = new ArrayList<>();

        try (VoxReader reader = new VoxReader(VoxelLoader.class.getResourceAsStream(path))) {
            VoxFile voxFile = reader.read();

            System.out.println("Voxel File: " + path + " contains " + voxFile.getModelInstances().size() + " models.");

            for (VoxModelInstance model_instance : voxFile.getModelInstances()) {

                if (counter>=32) return models;

                GridPoint3 world_Offset = model_instance.worldOffset;
                VoxModelBlueprint model = model_instance.model;

                int sizeX = model.getSize().x + 1;
                int sizeY = model.getSize().z + 1;
                int sizeZ = model.getSize().y + 1;

                BufferedImage[] images = new BufferedImage[sizeY];

                for (Voxel voxel : model.getVoxels()) {
                    int color;
                    if(voxel.getColourIndex() < 0 || voxel.getColourIndex() >= voxFile.getPalette().length){ color = Color.WHITE.getRGB(); }
                    else{ color = voxFile.getPalette()[voxel.getColourIndex()]; }

                    if (images[voxel.getPosition().z] == null) {
                        images[voxel.getPosition().z] = new BufferedImage(sizeX, sizeZ, BufferedImage.TYPE_INT_ARGB);
                    }

                    if(voxel.getPosition().x < 0 || voxel.getPosition().x >= sizeX || voxel.getPosition().y < 0 || voxel.getPosition().y >= sizeZ || voxel.getPosition().z < 0 || voxel.getPosition().z >= sizeY) continue;
                    images[voxel.getPosition().z].setRGB(voxel.getPosition().x, voxel.getPosition().y, color);
                }

                for(int i = 0; i < images.length; i++){
                    if(images[i] == null) {
                        images[i] = new BufferedImage(sizeX, sizeZ, BufferedImage.TYPE_INT_ARGB);
                    }
                    ImageIO.write(images[i], "png", new File("C:/tmp/voxel/" + counter + "_" + i + ".png"));
                }

                models.add(new Model(
                        new Texture3D("C:/tmp/voxel/" + counter + "_%s.png", intToGL_TEXTURE(counter), counter, sizeX, sizeY, sizeZ),
                        new Vector3f(world_Offset.x, world_Offset.y, world_Offset.z).mul(0.0f),
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
