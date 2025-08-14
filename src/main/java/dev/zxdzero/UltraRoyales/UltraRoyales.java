package dev.zxdzero.UltraRoyales;

import dev.zxdzero.UltraRoyales.commands.BingoResetCommand;
import dev.zxdzero.UltraRoyales.commands.WithdrawHeartCommand;
import dev.zxdzero.UltraRoyales.listeners.*;
import dev.zxdzero.UltraRoyales.listeners.scenarios.MaceRoyale;
import dev.zxdzero.UltraRoyales.listeners.scenarios.SpeedRoyale;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import dev.zxdzero.ZxdzeroEvents.registries.RecipeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.AxisAngle4f;

import java.util.List;

public final class UltraRoyales extends JavaPlugin {

    private static UltraRoyales plugin;

    public static UltraRoyales getPlugin() { return plugin; }

    public static NamespacedKey saddleCooldown;
    public static NamespacedKey staffCooldown;
    public static NamespacedKey saberCooldown;
    public static NamespacedKey conchCooldown;

    @Override
    public void onEnable() {
        plugin = this;
        saddleCooldown = new NamespacedKey(plugin, "knights_saddle_cooldown");
        staffCooldown = new NamespacedKey(plugin, "spider_staff_cooldown");
        saberCooldown = new NamespacedKey(plugin, "sponge_saber_cooldown");
        conchCooldown = new NamespacedKey(plugin, "electric_conch_cooldown");

        ItemsMenuManager.registerMenus();
        Items.registerBehavior();
        registerRecipes();

        getServer().getPluginManager().registerEvents(new KnightsSaddleListener(), this);
        getServer().getPluginManager().registerEvents(new DwarvenBowListener(), this);
        getServer().getPluginManager().registerEvents(new GhlochesterListener(), this);
        getServer().getPluginManager().registerEvents(new SpiderStaffListener(), this);
        getServer().getPluginManager().registerEvents(new SkeletalBarberListener(), this);
        getServer().getPluginManager().registerEvents(new ElectricConchListener(), this);
        getServer().getPluginManager().registerEvents(new BingoTheClownListener(), this);
        getServer().getPluginManager().registerEvents(new ScenarioManager(), this);

        ScenarioManager.registerScenario("speed_royale", new SpeedRoyale());
        ScenarioManager.registerScenario("mace_royale", new MaceRoyale());

        getCommand("arrowaura").setExecutor(new ArrowAuraCommand());
        getCommand("bingoreset").setExecutor(new BingoResetCommand());
        getCommand("withdrawheart").setExecutor(new WithdrawHeartCommand());
        getCommand("scenario").setExecutor(new ScenarioManager());

        CooldownRegistry.registerCooldown(saddleCooldown, Material.SADDLE);
        CooldownRegistry.registerCooldown(staffCooldown, Material.NAUTILUS_SHELL);
        CooldownRegistry.registerCooldown(saberCooldown, Material.DIAMOND_SWORD);
        CooldownRegistry.registerCooldown(conchCooldown, Material.TRIDENT);

        Bukkit.removeRecipe(NamespacedKey.minecraft("mace"));
    }

    @Override
    public void onDisable() {
        SpiderAIController.shutdown();
        ScenarioManager.endScenario();
    }

    private static void registerRecipes() {
        RecipeManager.registerRecipe(plugin, "spider_staff", new RecipeManager.PedestalRecipe(
                Items.spiderStaff(),
                List.of(
                        ItemStack.of(Material.NETHERITE_INGOT, 1),
                        ItemStack.of(Material.GOLD_BLOCK, 8),
                        ItemStack.of(Material.FERMENTED_SPIDER_EYE, 8)
                ),
                0.75f, -0.4f
        ));
        RecipeManager.registerRecipe(plugin, "dwarven_bow", new RecipeManager.PedestalRecipe(
                Items.dwarvenBow(),
                List.of(
                        ItemStack.of(Material.EMERALD_BLOCK, 8),
                        ItemStack.of(Material.DIAMOND_PICKAXE, 8),
                        ItemStack.of(Material.AMETHYST_SHARD, 16),
                        ItemStack.of(Material.BOW, 1)

                ),
                0.5f, new AxisAngle4f((float)(Math.PI/2), 1f, 1f, 1f),
                0
        ));
        RecipeManager.registerRecipe(plugin, "sponge_saber", new RecipeManager.PedestalRecipe(
                Items.spongeSaber(),
                List.of(
                        ItemStack.of(Material.SPONGE, 8),
                        ItemStack.of(Material.BUCKET, 64),
                        ItemStack.of(Material.BLAZE_ROD, 4),
                        ItemStack.of(Material.DIAMOND_SWORD, 1)
                )
        ));
        RecipeManager.registerRecipe(plugin, "ghlochester", new RecipeManager.PedestalRecipe(
                Items.ghlochester(),
                List.of(
                        ItemStack.of(Material.DIAMOND_AXE, 1),
                        ItemStack.of(Material.BREEZE_ROD, 16),
                        ItemStack.of(Material.LEAD, 8),
                        ItemStack.of(Material.HEAVY_CORE, 1)
                ),
                0.6f, 0.15f
        ));
        RecipeManager.registerRecipe(plugin, "knights_saddle", new RecipeManager.PedestalRecipe(
                Items.knightsSaddle(),
                List.of(
                        ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE, 1),
                        ItemStack.of(Material.SADDLE, 1),
                        ItemStack.of(Material.ANVIL, 8),
                        ItemStack.of(Material.GOLDEN_CARROT, 16)
                )
        ));
        RecipeManager.registerRecipe(plugin, "electric_conch", new RecipeManager.PedestalRecipe(
                Items.electricConch(),
                List.of(
                        ItemStack.of(Material.NAUTILUS_SHELL, 4),
                        ItemStack.of(Material.HEART_OF_THE_SEA, 1),
                        ItemStack.of(Material.COPPER_BLOCK, 32),
                        ItemStack.of(Material.GUNPOWDER, 16)
                )
        ));
        RecipeManager.registerRecipe(plugin, "skeletal_barber", new RecipeManager.PedestalRecipe(
                Items.skeletalBarber(),
                List.of(
                        ItemStack.of(Material.WITHER_SKELETON_SKULL, 1),
                        ItemStack.of(Material.NETHERITE_SCRAP, 2),
                        ItemStack.of(Material.DIAMOND_SWORD, 1),
                        ItemStack.of(Material.BONE, 32)
                ),
                0.5f, -0.1f
        ));
    }
}
