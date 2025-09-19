package com.kamu.warpmanager;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.ResourceKey;

import com.flowpowered.math.vector.Vector3i;

import java.util.HashMap;
import java.util.Map;

@Plugin(id = "warpmanager", name = "WarpManager", version = "1.0", authors = {"Kamu"})
public class WarpManager {

    private final Map<String, Warp> warps = new HashMap<>();

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event) {

        Parameter.Value<String> warpName = Parameter.string().key("name").build();

        // /setwarp <name>
        Command.Parameterized setwarp = Command.builder()
            .addParameter(warpName)
            .executor(ctx -> {
                if (!(ctx.cause().root() instanceof ServerPlayer player)) {
                    return CommandResult.success();
                }
                String name = ctx.requireOne(warpName);
                ServerWorld world = player.world();
                ServerLocation loc = player.location();
                warps.put(name, new Warp(name, world.key(), loc.blockPosition()));
                player.sendMessage(org.spongepowered.api.text.Text.of("Warp '" + name + "' disimpan."));
                return CommandResult.success();
            })
            .build();

        event.register(this, setwarp, "setwarp");

        // /warp <name>
        Command.Parameterized warpCmd = Command.builder()
            .addParameter(warpName)
            .executor(ctx -> {
                if (!(ctx.cause().root() instanceof ServerPlayer player)) {
                    return CommandResult.success();
                }
                String name = ctx.requireOne(warpName);
                Warp warp = warps.get(name);
                if (warp == null) {
                    player.sendMessage(org.spongepowered.api.text.Text.of("Warp tidak ditemukan: " + name));
                    return CommandResult.success();
                }
                ServerWorld world = Sponge.server().worldManager().world(warp.worldKey()).orElseThrow();
                player.setLocation(ServerLocation.of(world, warp.position()));
                player.sendMessage(org.spongepowered.api.text.Text.of("Teleport ke warp '" + name + "'."));
                return CommandResult.success();
            })
            .build();

        event.register(this, warpCmd, "warp");
    }

    private static class Warp {
        private final String name;
        private final ResourceKey worldKey;
        private final Vector3i position;

        Warp(String name, ResourceKey worldKey, Vector3i pos) {
            this.name = name;
            this.worldKey = worldKey;
            this.position = pos;
        }

        public ResourceKey worldKey() {
            return worldKey;
        }

        public Vector3i position() {
            return position;
        }
    }
}
