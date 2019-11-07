package com.craftingdead.mod.server;

import com.craftingdead.mod.CraftingDead;
import com.craftingdead.mod.IModDist;
import com.craftingdead.mod.masterserver.handshake.packet.HandshakePacket;
import com.craftingdead.mod.masterserver.modserverlogin.ModServerLoginSession;
import com.craftingdead.network.pipeline.NetworkManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ServerDist implements IModDist {

  private DedicatedServer dedicatedServer;

  public ServerDist() {
    FMLJavaModLoadingContext.get().getModEventBus().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public boolean isUsingNativeTransport() {
    return this.dedicatedServer.shouldUseNativeTransport();
  }

  @Override
  public void handleConnect(NetworkManager networkManager) {
    networkManager.sendMessage(
        new HandshakePacket(CraftingDead.MASTER_SERVER_VERSION, HandshakePacket.MOD_SERVER_LOGIN));
    networkManager.setSession(new ModServerLoginSession(networkManager, this.dedicatedServer));
  }

  // ================================================================================
  // Mod Events
  // ================================================================================

  @SubscribeEvent
  public void handleDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
    this.dedicatedServer = event.getServerSupplier().get();
  }

  // ================================================================================
  // Forge Events
  // ================================================================================

  @SubscribeEvent
  public void handleServerTick(TickEvent.ServerTickEvent event) {
    switch (event.phase) {
      case END:
        CraftingDead.getInstance().tickConnection();
        break;
      default:
        break;
    }
  }
}
