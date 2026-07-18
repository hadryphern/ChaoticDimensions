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
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
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
        "rosalita_granite", "rosalita_diorite", "rosalita_andesite", "rosalita_sandstone",
        "rosalita_log", "stripped_rosalita_log", "rosalita_wood", "stripped_rosalita_wood",
        "rosalita_planks", "rosalita_stairs", "rosalita_slab", "rosalita_fence", "rosalita_fence_gate",
        "rosalita_door", "rosalita_trapdoor", "rosalita_pressure_plate", "rosalita_button",
        "rosalita_sign", "rosalita_wall_sign", "rosalita_hanging_sign", "rosalita_wall_hanging_sign",
        "rosalita_crafting_table", "rosalita_chest", "rosalita_trapped_chest", "rosalita_barrel",
        "rosalita_ladder");
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

        // Keep this explicit order: item registration and recipes remain stable for users.
        for (String id : "rosalita_leaves,rosalita_stone,deep_rosalita_stone,rosaline_stone,rosalita_granite,rosalita_diorite,rosalita_andesite,rosalita_sandstone,rosalita_log,stripped_rosalita_log,rosalita_wood,stripped_rosalita_wood,rosalita_planks,rosalita_stairs,rosalita_slab,rosalita_fence,rosalita_fence_gate,rosalita_door,rosalita_trapdoor,rosalita_pressure_plate,rosalita_button,rosalita_sign,rosalita_wall_sign,rosalita_hanging_sign,rosalita_wall_hanging_sign,rosalita_crafting_table,rosalita_chest,rosalita_trapped_chest,rosalita_barrel,rosalita_ladder".split(",")) {
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
            case "rosalita_log", "stripped_rosalita_log", "rosalita_wood", "stripped_rosalita_wood" ->
                new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG));
            case "rosalita_planks" -> new Block(wood);
            case "rosalita_stairs" -> new StairBlock(get("rosalita_planks").defaultBlockState(), wood);
            case "rosalita_slab" -> new SlabBlock(wood);
            case "rosalita_fence" -> new FenceBlock(wood);
            case "rosalita_fence_gate" -> new FenceGateBlock(wood, ModWoodTypes.ROSALITA);
            case "rosalita_door" -> new DoorBlock(wood, ModWoodTypes.ROSALITA_SET);
            case "rosalita_trapdoor" -> new TrapDoorBlock(wood, ModWoodTypes.ROSALITA_SET);
            case "rosalita_pressure_plate" ->
                new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, wood, ModWoodTypes.ROSALITA_SET);
            case "rosalita_button" -> new ButtonBlock(wood, ModWoodTypes.ROSALITA_SET, 20, false);
            case "rosalita_sign" -> new StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN), ModWoodTypes.ROSALITA);
            case "rosalita_wall_sign" -> new WallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN), ModWoodTypes.ROSALITA);
            case "rosalita_hanging_sign" ->
                new CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_HANGING_SIGN), ModWoodTypes.ROSALITA);
            case "rosalita_wall_hanging_sign" ->
                new WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_HANGING_SIGN), ModWoodTypes.ROSALITA);
            case "rosalita_crafting_table" -> new CraftingTableBlock(wood);
            case "rosalita_chest" -> new RosalitaChestBlock(wood,
                () -> net.minecraft.world.level.block.entity.BlockEntityType.CHEST);
            case "rosalita_trapped_chest" -> new RosalitaTrappedChestBlock(wood);
            case "rosalita_barrel" -> new RosalitaBarrelBlock(wood);
            case "rosalita_ladder" -> new LadderBlock(wood.noOcclusion());
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
