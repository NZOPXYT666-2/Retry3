package com.enzo.enzomodkit;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class EnzoModKit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            // /nzosave <name>
            dispatcher.register(ClientCommandManager.literal("nzosave")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .executes(context -> {

                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    String name = StringArgumentType.getString(context, "name");
                    List<String> commands = new ArrayList<>();

                    for (int i = 0; i < client.player.getInventory().size(); i++) {
                        ItemStack stack = client.player.getInventory().getStack(i);
                        if (!stack.isEmpty()) {
                            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                            int count = stack.getCount();

                            commands.add(
                                "item replace entity @s container." + i +
                                " with " + itemId + " " + count
                            );
                        }
                    }

                    try {
                        File file = new File(getKitDir(), name + ".txt");
                        Files.write(file.toPath(), commands);

                        context.getSource().sendFeedback(
                            Text.literal("§a[NZO] Kit '" + name + "' saved!")
                        );
                    } catch (Exception e) {
                        context.getSource().sendFeedback(
                            Text.literal("§c[NZO] Error saving kit!")
                        );
                        e.printStackTrace();
                    }

                    return 1;
                }))
            );

            // /nzoload <name>
            dispatcher.register(ClientCommandManager.literal("nzoload")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .executes(context -> {

                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) return 0;

                    String name = StringArgumentType.getString(context, "name");
                    File file = new File(getKitDir(), name + ".txt");

                    if (!file.exists()) {
                        context.getSource().sendFeedback(
                            Text.literal("§c[NZO] Kit not found!")
                        );
                        return 0;
                    }

                    try {
                        List<String> lines = Files.readAllLines(file.toPath());
                        for (String cmd : lines) {
                            client.player.networkHandler.sendChatCommand(cmd);
                        }

                        context.getSource().sendFeedback(
                            Text.literal("§6[NZO] Kit '" + name + "' loaded!")
                        );
                    } catch (Exception e) {
                        context.getSource().sendFeedback(
                            Text.literal("§c[NZO] Error loading kit!")
                        );
                        e.printStackTrace();
                    }

                    return 1;
                }))
            );
        });
    }

    // Best-practice config path: .minecraft/config/enzomodkit/kits/
    private static File getKitDir() {
        File dir = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "enzomodkit/kits"
        );
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
