package com.DestroTheGod.AxisRestrict;

import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AxisRestrict extends JavaPlugin {
	private static AxisRestrict plugin;

	private static Location restrictedLineBaseLocation = null;
	private static int restrictMode = 0;
	private static Player theOnePlayer = null;
	private static BukkitTask task = null;
	private static long updateRate = 4L;

	@Override
	public void onDisable() {
		AxisRestrict.task.cancel();;
	}

	@Override
	public void onEnable() {
		AxisRestrict.plugin = this;

		this.enableListeners();

		CommandGod cg = new CommandGod();

		this.getCommand("swap").setExecutor(cg);
		this.getCommand("fast_movement_checks").setExecutor(cg);
		this.getCommand("default_movement_checks").setExecutor(cg);
		this.getCommand("slow_movement_checks").setExecutor(cg);

		Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

		if ((players != null) && (players.size() > 0)) {
			AxisRestrict.theOnePlayer = players.iterator().next();
			AxisRestrict.updateLocation();
		}

		AxisRestrict.startTask();
	}

	private static void startTask() {
		if (AxisRestrict.task != null) {
			AxisRestrict.task.cancel();
		}

		AxisRestrict.task = Bukkit.getServer().getScheduler().runTaskTimer(AxisRestrict.plugin, new Runnable() {
			@Override
			public void run() {
				if (AxisRestrict.theOnePlayer == null) {
					AxisRestrict.task.cancel();
					return;
				}

				switch (AxisRestrict.restrictMode) {
					case 0:
						this.checkX();
						break;
					case 1:
						this.checkZ();
						break;
					case 2:
						this.checkY();
						break;
				}
			}

			private void checkX() {
				Location pLoc = AxisRestrict.theOnePlayer.getLocation();

				this.checkAny(AxisRestrict.restrictedLineBaseLocation.getBlockX(), pLoc.getBlockX(), (x) -> {
					pLoc.setX(x);
					AxisRestrict.theOnePlayer.teleport(pLoc);
				});
			}

			private void checkZ() {
				Location pLoc = AxisRestrict.theOnePlayer.getLocation();

				this.checkAny(AxisRestrict.restrictedLineBaseLocation.getBlockZ(), pLoc.getBlockZ(), (x) -> {
					pLoc.setZ(x);
					AxisRestrict.theOnePlayer.teleport(pLoc);
				});
			}

			private void checkY() {
				Location pLoc = AxisRestrict.theOnePlayer.getLocation();

				this.checkAny(AxisRestrict.restrictedLineBaseLocation.getBlockY(), pLoc.getBlockY(), (x) -> {
					pLoc.setY(x);
					AxisRestrict.theOnePlayer.teleport(pLoc);
				});
			}

			private void checkAny(int resDirVal, int curDirVal, Constants.checkTeleport func) {
				if (curDirVal != resDirVal) {
					func.run(resDirVal + 0.5);
				}
			}
		}, AxisRestrict.updateRate, AxisRestrict.updateRate);
	}

	private static class Constants {
		@FunctionalInterface
		public interface checkTeleport { void run(double x); }
	}

	public class CommandGod implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] argCommandArgs) {
			Logger l = AxisRestrict.plugin.getLogger();

			if (command.getName().equalsIgnoreCase("swap")) {
				AxisRestrict.restrictMode = (AxisRestrict.restrictMode + 1) % 3;

				switch (AxisRestrict.restrictMode) {
					case 0:
						l.info("X restricted.");
						break;
					case 1:
						l.info("Y Restricted.");
						break;
					case 2:
						l.info("Z Restricted.");
						break;
				}

				AxisRestrict.updateLocation();
				return true;
			} else if (command.getName().equalsIgnoreCase("fast_movement_checks")) {
				AxisRestrict.updateRate = 1L;
				AxisRestrict.startTask();
			} else if (command.getName().equalsIgnoreCase("default_movement_checks")) {
				AxisRestrict.updateRate = 4L;
				AxisRestrict.startTask();
			} else if (command.getName().equalsIgnoreCase("slow_movement_checks")) {
				AxisRestrict.updateRate = 10L;
				AxisRestrict.startTask();
			}

			return false;
		}
	}

	private void enableListeners() {
		PluginManager pm = this.getServer().getPluginManager();

		pm.registerEvents(new TheListeners(), AxisRestrict.plugin);
	}

	public class TheListeners implements Listener {
		@EventHandler
		public void onLogin(PlayerLoginEvent event) {
			AxisRestrict.theOnePlayer = event.getPlayer();
			AxisRestrict.updateLocation();

			AxisRestrict.startTask();
		}

		@EventHandler
		public void onWorldChange(PlayerChangedWorldEvent event) { AxisRestrict.updateLocation(); }
	}

	private static void updateLocation() {
		if (AxisRestrict.theOnePlayer == null) {
			return;
		}

		AxisRestrict.restrictedLineBaseLocation = AxisRestrict.theOnePlayer.getLocation();
	}
}
