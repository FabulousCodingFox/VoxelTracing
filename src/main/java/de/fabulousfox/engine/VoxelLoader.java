package de.fabulousfox.engine;

import de.fabulousfox.libs.voxfileparser.*;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL33.*;

public class VoxelLoader {
    private static int counter = 1;

    public static int intToGL_TEXTURE(int n){
        return switch (n) {
            case 1 -> GL_TEXTURE1;
            case 2 -> GL_TEXTURE2;
            case 3 -> GL_TEXTURE3;
            case 4 -> GL_TEXTURE4;
            case 5 -> GL_TEXTURE5;
            case 6 -> GL_TEXTURE6;
            case 7 -> GL_TEXTURE7;
            case 8 -> GL_TEXTURE8;
            case 9 -> GL_TEXTURE9;
            case 10 -> GL_TEXTURE10;
            case 11 -> GL_TEXTURE11;
            case 12 -> GL_TEXTURE12;
            case 13 -> GL_TEXTURE13;
            case 14 -> GL_TEXTURE14;
            case 15 -> GL_TEXTURE15;
            case 16 -> GL_TEXTURE16;
            case 17 -> GL_TEXTURE17;
            case 18 -> GL_TEXTURE18;
            case 19 -> GL_TEXTURE19;
            case 20 -> GL_TEXTURE20;
            case 21 -> GL_TEXTURE21;
            case 22 -> GL_TEXTURE22;
            case 23 -> GL_TEXTURE23;
            case 24 -> GL_TEXTURE24;
            case 25 -> GL_TEXTURE25;
            case 26 -> GL_TEXTURE26;
            case 27 -> GL_TEXTURE27;
            case 28 -> GL_TEXTURE28;
            case 29 -> GL_TEXTURE29;
            case 30 -> GL_TEXTURE30;
            case 31 -> GL_TEXTURE31;
            default -> throw new IllegalStateException("Unexpected value: " + n);
        };
    }

    public static ArrayList<Model> load(String path) {
        ArrayList<Model> models = new ArrayList<>();

        if (counter>=32) return models;

        try (VoxReader reader = new VoxReader(VoxelLoader.class.getResourceAsStream(path))) {
            VoxFile voxFile = reader.read();

            System.out.println("Voxel File: " + path + " contains " + voxFile.getModelInstances().size() + " models.");

            for (VoxModelInstance model_instance : voxFile.getModelInstances()) {

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
                        new Vector3f(world_Offset.x, world_Offset.y, world_Offset.z),
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
