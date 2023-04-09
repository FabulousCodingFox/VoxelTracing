package de.fabulousfox.engine;

import de.fabulousfox.engine.wrapper.Texture3D;
import de.fabulousfox.gvox_java.Gvox;
import de.fabulousfox.gvox_java.consumerapi.GvoxAdapterContext;
import de.fabulousfox.gvox_java.consumerapi.GvoxContext;
import de.fabulousfox.gvox_java.consumerapi.config.input.GvoxFileInputAdapterConfig;
import de.fabulousfox.gvox_java.consumerapi.config.serialize.GvoxColoredTextSerializeAdapterConfig;
import de.fabulousfox.gvox_java.enums.GvoxChannelBit;
import de.fabulousfox.gvox_java.structs.GvoxExtent3D;
import de.fabulousfox.gvox_java.structs.GvoxOffset3D;
import de.fabulousfox.gvox_java.structs.GvoxRegionRange;
import de.fabulousfox.libs.voxfileparser.*;
import de.fabulousfox.libs.voxfileparser.chunk.VoxRGBAChunk;
import org.joml.Vector3f;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VoxelLoader {
    private static void refreshThreadPool(Thread[] threadPool, ConcurrentLinkedQueue<VoxModelInstance> modelInstances, VoxFile voxFile, ArrayList<Model> models) {
        for (int i = 0; i < threadPool.length; i++) {
            Thread thread = threadPool[i];
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(() -> {
                    VoxModelInstance model_instance = modelInstances.poll();
                    if (model_instance == null) return;

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

                    models.add(new Model(
                            texture,
                            new Vector3f(
                                    world_Offset.x - (int) (model.getSize().x / 2f),
                                    world_Offset.z - (int) (model.getSize().z / 2f),
                                    -world_Offset.y - (int) (model.getSize().y / 2f)
                            ).mul(Model.VOXEL_SIZE),
                            sizeX, sizeY, sizeZ
                    ));
                });
                threadPool[i] = thread;
                thread.start();
            }
        }
    }

    public static ArrayList<Model> load(String path) {
        ArrayList<Model> models = new ArrayList<>();
        Thread[] threadPool = new Thread[4];

        System.out.println("[VOXLOADER] Loading models from " + path);

        try (VoxReader reader = new VoxReader(new FileInputStream("C:/Users/fabif/IdeaProjects/VoxelTracing/src/main/resources" + path))) {
            VoxFile voxFile = reader.read();
            ConcurrentLinkedQueue<VoxModelInstance> modelInstances = new ConcurrentLinkedQueue<>(voxFile.getModelInstances());
            System.out.println("[VOXLOADER] Found " + modelInstances.size() + " model instances");
            while (!modelInstances.isEmpty()) {
                refreshThreadPool(threadPool, modelInstances, voxFile, models);
            }
            for (Thread thread : threadPool) {
                if (thread != null) {
                    thread.join();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("[VOXLOADER] Loaded " + models.size() + " models");

        int counter = 0;
        for (Model model : models) {
            model.syncCreateData();
            counter++;

            if (counter % 100 == 0)
                System.out.println("[VOXLOADER] Loaded texture data " + counter + "/" + models.size());
        }

        return models;




        /*ArrayList<Model> models = new ArrayList<>();

        try (VoxReader reader = new VoxReader(new FileInputStream("C:/Users/fabif/IdeaProjects/VoxelTracing/src/main/resources"+ path))) {
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

                if(counter % 100 == 0) System.out.println("Loaded model " + counter + "/" + voxFile.getModelInstances().size());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return models;*/
    }

    public static List<Model> loadGVOX(String path) {
        GvoxContext gvox_ctx = Gvox.create_context();

        GvoxFileInputAdapterConfig input_config = new GvoxFileInputAdapterConfig() {{
            filepath = "C:/Users/fabif/IdeaProjects/VoxelTracing/src/main/resources/models/menger.vox";
        }};

        GvoxColoredTextSerializeAdapterConfig serialize_config = new GvoxColoredTextSerializeAdapterConfig() {{
            non_color_max_value = 255;
        }};

        GvoxAdapterContext input_ctx = Gvox.create_adapter_context(gvox_ctx, Gvox.get_input_adapter(gvox_ctx, "file"), input_config);
        GvoxAdapterContext output_ctx = Gvox.create_adapter_context(gvox_ctx, Gvox.get_output_adapter(gvox_ctx, "stdout"), null);
        GvoxAdapterContext parse_ctx = Gvox.create_adapter_context(gvox_ctx, Gvox.get_parse_adapter(gvox_ctx, "magicavoxel"), null);
        GvoxAdapterContext serialize_ctx = Gvox.create_adapter_context(gvox_ctx, Gvox.get_serialize_adapter(gvox_ctx, "colored_text"), serialize_config);

        GvoxRegionRange range = new GvoxRegionRange(
                new GvoxOffset3D(0, 0, 0),
                new GvoxExtent3D(8, 8, 8)
        );

        Gvox.blit_region(input_ctx, output_ctx, parse_ctx, serialize_ctx, range, List.of(
                GvoxChannelBit.COLOR,
                GvoxChannelBit.MATERIAL_ID,
                GvoxChannelBit.ROUGHNESS,
                GvoxChannelBit.TRANSPARENCY,
                GvoxChannelBit.EMISSIVITY
        ));

        Gvox.destroy_adapter_context(input_ctx);
        Gvox.destroy_adapter_context(output_ctx);
        Gvox.destroy_adapter_context(parse_ctx);
        Gvox.destroy_adapter_context(serialize_ctx);

        Gvox.destroy_context(gvox_ctx);

        Gvox.close();

        return List.of();
    }
}
