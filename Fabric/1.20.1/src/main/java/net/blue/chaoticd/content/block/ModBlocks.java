package net.blue.chaoticd.content.block;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Initial Fabric registration of the legacy blocks.
 *
 * <p>Each legacy block id is preserved. Vanilla block families are registered
 * with their matching state properties so the recovered blockstate models can
 * render correctly; custom properties and gameplay are ported next.</p>
 */
public final class ModBlocks {
    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    private static final Set<String> NEW_CONTENT_IDS = Set.of(
        "rosalita_leaves", "rosalita_stone", "deep_rosalita_stone", "rosaline_stone",
        "rosalita_granite", "rosalita_diorite", "rosalita_andesite", "rosalita_sandstone");
    private static boolean initialized;

    private ModBlocks() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        for (String id : "aluminium_block,aluminium_ore,bloco_folha_branca,bloco_grama_branco,bloco_madeira_branco,bloco_ruby,blocosombra,botao_madeira_sombra,cerca_madeira_sombra,cobblestone_negra,crystal_blue_plant,crystal_dirt,crystal_grass_block,crystal_green_plant,crystal_leaves_1,crystal_leaves_2,crystal_leaves_3,crystal_log,crystal_planks,crystal_red_plant,crystal_yellow_plant,escada_madeira_sombra,fire_button,fire_fence,fire_fence_gate,fire_leaves,fire_log,fire_planks,fire_pressure_plate,fire_slab,fire_stairs,fire_wood,folha_sombra,grama_sombra,madeira_bruta_branca,madeira_sombra,minerio_ruby,mineriosombra,pedra_sombra,placa_pressao_madeira_sombra,porta_madeira_sombra,portao_madeira_sombra,rosalitabloco,slab_madeira_sombra,tabua_sombra,terra_branca,terra_sombra,titanium_block,titanium_ore,toxic_block,toxic_ore,trap_door_madeira_sombra,vanilla_2_leaves".split(",")) {
            register(id);
        }

        for (String id : NEW_CONTENT_IDS) {
            register(id);
        }
    }

    public static Collection<Map.Entry<String, Block>> entries() {
        return BLOCKS.entrySet();
    }

    public static Block get(String id) {
        Block block = BLOCKS.get(id);
        if (block == null) {
            throw new IllegalArgumentException("Unknown Chaotic Dimensions block: " + id);
        }
        return block;
    }

    public static boolean isNewContent(String id) {
        return NEW_CONTENT_IDS.contains(id);
    }

    private static void register(String id) {
        Block block = createBlock(id);
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(ChaoticDimensions.MOD_ID, id), block);
        BLOCKS.put(id, block);
    }

    private static Block createBlock(String id) {
        BlockBehaviour.Properties wood = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS);

        return switch (id) {
            case "escada_madeira_sombra", "fire_stairs" ->
                new StairBlock(Blocks.OAK_PLANKS.defaultBlockState(), wood);
            case "slab_madeira_sombra", "fire_slab" -> new SlabBlock(wood);
            case "cerca_madeira_sombra", "fire_fence" -> new FenceBlock(wood);
            case "portao_madeira_sombra", "fire_fence_gate" -> new FenceGateBlock(wood, WoodType.OAK);
            case "porta_madeira_sombra" -> new DoorBlock(wood, BlockSetType.OAK);
            case "trap_door_madeira_sombra" -> new TrapDoorBlock(wood, BlockSetType.OAK);
            case "placa_pressao_madeira_sombra", "fire_pressure_plate" ->
                new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, wood, BlockSetType.OAK);
            case "botao_madeira_sombra", "fire_button" -> new ButtonBlock(wood, BlockSetType.OAK, 20, false);
            case "fire_log", "fire_wood" -> new RotatedPillarBlock(wood);
            case "rosalita_leaves" -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES));
            case "rosalita_stone", "rosaline_stone" -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
            case "deep_rosalita_stone" -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE));
            case "rosalita_granite" -> new Block(BlockBehaviour.Properties.copy(Blocks.GRANITE));
            case "rosalita_diorite" -> new Block(BlockBehaviour.Properties.copy(Blocks.DIORITE));
            case "rosalita_andesite" -> new Block(BlockBehaviour.Properties.copy(Blocks.ANDESITE));
            case "rosalita_sandstone" -> new Block(BlockBehaviour.Properties.copy(Blocks.SANDSTONE));
            default -> new Block(BlockBehaviour.Properties.of());
        };
    }
}
