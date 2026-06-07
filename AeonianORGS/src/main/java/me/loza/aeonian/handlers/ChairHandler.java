package me.loza.aeonian.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class ChairHandler implements Listener {

    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(getClass());

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        if (player.isSneaking()) return;
        if (player.isInsideVehicle()) return;

        Block block = e.getClickedBlock();

        double playerY = player.getLocation().getY();
        double blockY = block.getLocation().getY();

        if (blockY - playerY > 2) return;

        if (!isSeatBlock(block)) return;
        if (!hasTwoBlocksClearAbove(block)) return;

        Location seatLoc = getSeatLocation(block, player);

        ArmorStand stand = seatLoc.getWorld().spawn(seatLoc, ArmorStand.class, as -> {
            as.setMarker(false);
            as.setInvisible(true);
            as.setSmall(true);
            as.setBasePlate(false);
            as.setArms(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setSilent(true);
            as.setCollidable(false);
        });

        stand.addPassenger(player);
        e.setCancelled(true);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        Entity vehicle = e.getDismounted();
        if (!(vehicle instanceof ArmorStand stand)) return;

        Bukkit.getScheduler().runTaskLater(plugin, stand::remove, 1L);
    }

    private boolean isSeatBlock(Block block) {
        Material type = block.getType();

        if (isStairs(block)) return true;
        if (isGroundBottomSlab(block)) return true;
        if (isCarpet(type) && hasSolidBelow(block)) return true;

        return false;
    }

    private boolean isStairs(Block block) {
        Material type = block.getType();
        if (!type.name().endsWith("_STAIRS")) return false;
        if (!(block.getBlockData() instanceof Stairs stairs)) return false;
        if (stairs.getHalf() == Bisected.Half.TOP) return false;
        return true;
    }

    private boolean isCarpet(Material type) {
        return type.name().endsWith("_CARPET");
    }

    private boolean hasSolidBelow(Block block) {
        return block.getRelative(BlockFace.DOWN).getType().isSolid();
    }

    private boolean hasTwoBlocksClearAbove(Block block) {
        return block.getRelative(BlockFace.UP).isEmpty()
                && block.getRelative(BlockFace.UP, 2).isEmpty();
    }

    private boolean isGroundBottomSlab(Block block) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Slab slab)) return false;

        if (slab.getType() != Slab.Type.BOTTOM) return false;
        return hasSolidBelow(block);
    }

    private Location getSeatLocation(Block block, Player player) {
        BlockData data = block.getBlockData();
        Location loc = block.getLocation().add(0.5, 0.0, 0.5);

        float yaw;

        if (data instanceof Stairs stairs) {
            loc.add(0, -0.5, 0);

            BlockFace facing = stairs.getFacing();
            yaw = faceToYaw(facing.getOppositeFace());

            double edgeOffset = 0.12;
            switch (facing) {
                case NORTH -> loc.add(0, 0, edgeOffset);
                case SOUTH -> loc.add(0, 0, -edgeOffset);
                case EAST  -> loc.add(-edgeOffset, 0, 0);
                case WEST  -> loc.add(edgeOffset, 0, 0);
                default -> {}
            }
        } else if (data instanceof Slab) {
            loc.add(0, -0.5, 0);
            yaw = player.getLocation().getYaw();
        } else if (isCarpet(block.getType())) {
            loc.add(0, -0.9, 0);
            yaw = player.getLocation().getYaw();
        } else {
            loc.add(0, 0, 0);
            yaw = player.getLocation().getYaw();
        }

        loc.setYaw(yaw);
        loc.setPitch(0f);
        return loc;
    }

    private float faceToYaw(BlockFace face) {
        return switch (face) {
            case SOUTH -> 0f;
            case WEST  -> 90f;
            case NORTH -> 180f;
            case EAST  -> -90f;
            default -> 0f;
        };
    }
}
