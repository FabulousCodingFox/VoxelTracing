package de.fabulousfox.engine;

import de.fabulousfox.libs.voxfileparser.*;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
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

        try (VoxReader reader = new VoxReader(VoxelLoader.class.getResourceAsStream(path))) {
            VoxFile voxFile = reader.read();
            for (VoxModelInstance model_instance : voxFile.getModelInstances()) {

                if (counter>=32) break;

                GridPoint3 world_Offset = model_instance.worldOffset;
                VoxModelBlueprint model = model_instance.model;

                int sizeX = model.getSize().x;
                int sizeY = model.getSize().z;
                int sizeZ = model.getSize().y;

                BufferedImage image = new BufferedImage(sizeZ, sizeX * sizeY, BufferedImage.TYPE_INT_ARGB);

                System.out.println(image.getWidth() + "x" + image.getHeight());

                for (Voxel voxel : model.getVoxels()) {
                    if(voxel.getColourIndex() < 0 || voxel.getColourIndex() >= voxFile.getPalette().length) continue;
                    int posX = voxel.getPosition().y;
                    int posY = image.getHeight() - (voxel.getPosition().x + voxel.getPosition().z * sizeX);

                    if(posX < 0 || posX >= image.getWidth() || posY < 0 || posY >= image.getHeight()) continue;

                    image.setRGB(
                            posX,
                            posY,
                            voxFile.getPalette()[voxel.getColourIndex()]
                    );
                }

                ImageIO.write(image, "png", new File("C:/tmp/voxel/" + counter + ".png"));

                models.add(new Model(
                        new Texture("C:/tmp/voxel/" + counter + ".png", intToGL_TEXTURE(counter)),
                        new Vector3f(world_Offset.x, world_Offset.y, world_Offset.z),
                        sizeX, sizeY, sizeZ
                ));

                counter++;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return models;
    }
}
