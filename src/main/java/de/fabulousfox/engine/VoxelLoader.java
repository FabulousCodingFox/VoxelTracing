package de.fabulousfox.engine;

import de.fabulousfox.engine.wrapper.Texture3D;
import de.fabulousfox.gvox_java.GVOX;
import de.fabulousfox.gvox_java.consumerapi.GvoxAdapterContext;
import de.fabulousfox.gvox_java.consumerapi.GvoxContext;
import de.fabulousfox.gvox_java.consumerapi.config.input.GvoxFileInputAdapterConfig;
import de.fabulousfox.gvox_java.consumerapi.config.output.GvoxByteBufferOutputAdapterConfig;
import de.fabulousfox.gvox_java.consumerapi.config.serialize.GvoxColoredTextSerializeAdapterConfig;
import de.fabulousfox.gvox_java.enums.GvoxChannelBit;
import de.fabulousfox.gvox_java.structs.GvoxExtent3D;
import de.fabulousfox.gvox_java.structs.GvoxOffset3D;
import de.fabulousfox.gvox_java.structs.GvoxRegionRange;
import de.fabulousfox.libs.voxfileparser.*;
import de.fabulousfox.libs.voxfileparser.chunk.VoxRGBAChunk;
import org.joml.Vector3f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

        try (VoxReader reader = new VoxReader(new FileInputStream(path.startsWith("C:") ? path : "C:/Users/fabif/IdeaProjects/VoxelTracing/src/main/resources" + path))) {
            VoxFile voxFile = reader.read();
            ConcurrentLinkedQueue<VoxModelInstance> modelInstances = new ConcurrentLinkedQueue<>(voxFile.getModelInstances());
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
        } catch (InvalidVoxException e) {
            System.out.println("[VOXLOADER] Invalid vox file");
        }

        int counter = 0;
        for (Model model : models) {
            model.syncCreateData();
            counter++;

            if (counter % 10 == 0)
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
        GvoxContext gvox_ctx = GVOX.create_context();

        GvoxFileInputAdapterConfig input_config = new GvoxFileInputAdapterConfig() {{
            filepath = "C:/Users/fabif/IdeaProjects/VoxelTracing/src/main/resources/models/menger.vox";
        }};

        GvoxColoredTextSerializeAdapterConfig serialize_config = new GvoxColoredTextSerializeAdapterConfig() {{
            non_color_max_value = 255;
        }};

        GvoxByteBufferOutputAdapterConfig output_config = new GvoxByteBufferOutputAdapterConfig();

        GvoxAdapterContext input_ctx = GVOX.create_adapter_context(gvox_ctx, GVOX.get_input_adapter(gvox_ctx, "file"), input_config);
        GvoxAdapterContext output_ctx = GVOX.create_adapter_context(gvox_ctx, GVOX.get_output_adapter(gvox_ctx, "buffer"), output_config);
        GvoxAdapterContext parse_ctx = GVOX.create_adapter_context(gvox_ctx, GVOX.get_parse_adapter(gvox_ctx, "magicavoxel"), null);
        GvoxAdapterContext serialize_ctx = GVOX.create_adapter_context(gvox_ctx, GVOX.get_serialize_adapter(gvox_ctx, "colored_text"), serialize_config);

        GvoxRegionRange range = new GvoxRegionRange(
                new GvoxOffset3D(0, 0, 0),
                new GvoxExtent3D(8, 8, 8)
        );

        GVOX.blit_region(input_ctx, output_ctx, parse_ctx, serialize_ctx, range, List.of(
                GvoxChannelBit.COLOR,
                GvoxChannelBit.MATERIAL_ID,
                GvoxChannelBit.ROUGHNESS,
                GvoxChannelBit.TRANSPARENCY,
                GvoxChannelBit.EMISSIVITY
        ));

        GVOX.destroy_adapter_context(input_ctx);
        GVOX.destroy_adapter_context(output_ctx);
        GVOX.destroy_adapter_context(parse_ctx);
        GVOX.destroy_adapter_context(serialize_ctx);

        GVOX.destroy_context(gvox_ctx);

        GVOX.close();

        return List.of();
    }

    private static Node firstNodeListElementOrNull(NodeList nodeList) {
        if (nodeList.getLength() > 0) {
            return nodeList.item(0);
        } else {
            return null;
        }
    }

    private static int totalVoxBoxes = 0;
    private static int totalVoxBoxesCount = 0;

    private static void traverseNodes(String rootfolder, Vector3f posOffset, Vector3f rotationOffset, Node thisNode, List<Model> models, HashMap<String, List<Model>> voxStorage) throws NumberFormatException {
        if (thisNode.getNodeType() != Node.ELEMENT_NODE) return;
        Element thisElement = (Element) thisNode;

        Vector3f pOffset = new Vector3f(posOffset);
        Vector3f rOffset = new Vector3f(rotationOffset);
        String[] pOffsetString = thisElement.getAttribute("pos").split(" ");
        String[] rOffsetString = thisElement.getAttribute("rot").split(" ");
        String[] sizeString = thisElement.getAttribute("size").split(" ");
        if (pOffsetString.length == 3) {
            pOffset.x += Float.parseFloat(pOffsetString[0]);
            pOffset.y += Float.parseFloat(pOffsetString[1]);
            pOffset.z += Float.parseFloat(pOffsetString[2]);
        }
        if (rOffsetString.length == 3) {
            rOffset.x += Float.parseFloat(rOffsetString[0]);
            rOffset.y += Float.parseFloat(rOffsetString[1]);
            rOffset.z += Float.parseFloat(rOffsetString[2]);
        }

        /*if (thisElement.getNodeName().equalsIgnoreCase("voxbox")) {
            Texture3D texture = new Texture3D(size[0], size[1], size[2]);
            texture.fill();
            texture.create();

            models.add(new Model(texture, pOffset.mul(Model.VOXEL_SIZE), rOffset, size[0], size[1], size[2]));

            totalVoxBoxesCount++;
            System.out.println("Progress: " + totalVoxBoxesCount + "/" + totalVoxBoxes + " -> " + 100f * ((float) totalVoxBoxesCount / (float) totalVoxBoxes) + "%");
        }*/

        if (thisElement.getNodeName().equalsIgnoreCase("vox") ||
                thisElement.getNodeName().equalsIgnoreCase("voxbox") ||
                thisElement.getNodeName().equalsIgnoreCase("instance")
        ) {
            String voxPath = thisElement.getAttribute("file").replace("MOD/", "");
            if(!voxStorage.containsKey(voxPath)) {
                voxStorage.put(voxPath, load(rootfolder + voxPath));
            }

            for(Model mdl: voxStorage.get(voxPath)){
                models.add(mdl.getNewCopy(pOffset.mul(Model.VOXEL_SIZE).mul(10)));
            }

            totalVoxBoxesCount++;
            System.out.println("Progress: " + totalVoxBoxesCount + "/" + totalVoxBoxes + " -> " + 100f * ((float) totalVoxBoxesCount / (float) totalVoxBoxes) + "%");
        }

        if (thisNode.getChildNodes().getLength() != 0) {
            for (int i = 0; i < thisNode.getChildNodes().getLength(); i++) {
                traverseNodes(rootfolder, pOffset, rOffset, thisNode.getChildNodes().item(i), models, voxStorage);
            }
        }
    }

    public static List<Model> loadTeardown(Vector3f spawnPoint, String pathToXML) throws ParserConfigurationException, IOException, SAXException, NumberFormatException {
        List<Model> models = new ArrayList<>();

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new File(pathToXML));
        doc.getDocumentElement().normalize();

        Node sceneNode = doc.getElementsByTagName("scene").item(0);

        Node spawnPointNode = firstNodeListElementOrNull(doc.getElementsByTagName("spawnpoint"));
        if (spawnPointNode != null) {
            Node tempNode = spawnPointNode.getAttributes().getNamedItem("pos");
            String[] tempStringArray = tempNode.getTextContent().split(" ");
            spawnPoint.x = Float.parseFloat(tempStringArray[0]);
            spawnPoint.y = Float.parseFloat(tempStringArray[1]);
            spawnPoint.z = Float.parseFloat(tempStringArray[2]);
        }

        totalVoxBoxes = doc.getElementsByTagName("voxbox").getLength() + doc.getElementsByTagName("vox").getLength() + doc.getElementsByTagName("instance").getLength();
        totalVoxBoxesCount = 0;

        System.out.println("Traversing nodes... (" + totalVoxBoxes + ")");
        traverseNodes(pathToXML.substring(0, pathToXML.lastIndexOf("/") + 1), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), sceneNode, models, new HashMap<>());
        System.out.println("Done traversing nodes (" + totalVoxBoxes + ")");

        return models;
    }
}
