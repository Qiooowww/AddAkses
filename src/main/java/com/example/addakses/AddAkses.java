package com.example.addakses;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.UUID;

public class AddAkses extends JavaPlugin {

    private final HashMap<UUID, Long> aksesSementara = new HashMap<>();
    private final HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getScheduler().runTaskTimer(this, this::cekKadaluarsa, 20 * 60, 20 * 60); // cek tiap 1 menit
        getLogger().info("AddAkses plugin aktif!");
    }

    @Override
    public void onDisable() {
        aksesSementara.clear();
        attachments.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("addakses")) {
            if (args.length < 2) {
                sender.sendMessage("§cGunakan: /addakses <username> <durasi>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer tidak ditemukan atau sedang offline.");
                return true;
            }

            long durasiMs = parseDurasi(args[1]);
            if (durasiMs <= 0) {
                sender.sendMessage("§cFormat durasi salah. Gunakan h=jam, m=menit, s=detik (contoh: 10h, 30m).");
                return true;
            }

            long expireAt = System.currentTimeMillis() + durasiMs;
            aksesSementara.put(target.getUniqueId(), expireAt);

            PermissionAttachment attachment = target.addAttachment(this);
            attachment.setPermission("addakses.temp", true);
            attachments.put(target.getUniqueId(), attachment);

            sender.sendMessage("§aAkses sementara diberikan ke " + target.getName() + " selama " + args[1]);
            target.sendMessage("§aKamu mendapatkan akses sementara selama " + args[1]);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("delakses")) {
            if (args.length < 1) {
                sender.sendMessage("§cGunakan: /delakses <username>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer tidak ditemukan atau sedang offline.");
                return true;
            }

            hapusAkses(target.getUniqueId());
            sender.sendMessage("§aAkses player " + target.getName() + " telah dicabut.");
            target.sendMessage("§cAkses sementara kamu dicabut.");
            return true;
        }

        return false;
    }

    private long parseDurasi(String input) {
        try {
            if (input.endsWith("h")) {
                return Long.parseLong(input.replace("h", "")) * 60L * 60L * 1000L;
            } else if (input.endsWith("m")) {
                return Long.parseLong(input.replace("m", "")) * 60L * 1000L;
            } else if (input.endsWith("s")) {
                return Long.parseLong(input.replace("s", "")) * 1000L;
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private void cekKadaluarsa() {
        long now = System.currentTimeMillis();
        for (UUID uuid : aksesSementara.keySet().toArray(new UUID[0])) {
            if (aksesSementara.get(uuid) < now) {
                hapusAkses(uuid);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage("§cAkses sementara kamu telah berakhir!");
                }
            }
        }
    }

    private void hapusAkses(UUID uuid) {
        aksesSementara.remove(uuid);
        if (attachments.containsKey(uuid)) {
            PermissionAttachment att = attachments.get(uuid);
            if (att != null) {
                att.unsetPermission("addakses.temp");
            }
            attachments.remove(uuid);
        }
    }
}
