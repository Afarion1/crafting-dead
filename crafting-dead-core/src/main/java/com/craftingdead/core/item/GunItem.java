/**
 * Crafting Dead
 * Copyright (C) 2020  Nexus Node
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.craftingdead.core.item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.craftingdead.core.capability.ModCapabilities;
import com.craftingdead.core.capability.SerializableCapabilityProvider;
import com.craftingdead.core.capability.animationprovider.gun.AnimationType;
import com.craftingdead.core.capability.animationprovider.gun.GunAnimation;
import com.craftingdead.core.capability.gun.AimableGun;
import com.craftingdead.core.capability.gun.DefaultGun;
import com.craftingdead.core.capability.gun.IGun;
import com.craftingdead.core.capability.scope.IScope;
import com.craftingdead.core.client.renderer.item.GunRenderer;
import com.craftingdead.core.client.renderer.item.IRendererProvider;
import com.craftingdead.core.util.Text;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.DistExecutor;

public class GunItem extends ShootableItem implements IRendererProvider {

  /**
   * Time between shots in milliseconds.
   */
  private final int fireRateMs;

  private final int damage;

  private final int reloadDurationTicks;

  /**
   * Accuracy as percentage.
   */
  private final float accuracy;

  /**
   * Amount of "pellets" to be fired in a single shot. It is used by shotguns.
   */
  private final int bulletAmountToFire;

  /**
   * Whether the player can aim with this gun or not
   */
  private final boolean aimable;

  /**
   * Whether the crosshair should be rendered or not while holding this item
   */
  private final boolean crosshair;

  /**
   * {@link FireMode}s the gun can cycle through.
   */
  private final List<FireMode> fireModes;

  private final Supplier<SoundEvent> shootSound;

  private final Supplier<SoundEvent> silencedShootSound;

  private final Supplier<SoundEvent> reloadSound;

  private final Map<AnimationType, Supplier<GunAnimation>> animations;

  private final Set<Supplier<MagazineItem>> acceptedMagazines;

  private final Supplier<MagazineItem> defaultMagazine;

  private final Set<Supplier<AttachmentItem>> acceptedAttachments;

  private final Set<Supplier<PaintItem>> acceptedPaints;

  private final Set<Supplier<AttachmentItem>> defaultAttachments;

  private final Supplier<DistExecutor.SafeCallable<GunRenderer>> rendererFactory;

  private final IGun.RightMouseActionTriggerType rightMouseActionTriggerType;

  private final Predicate<IGun> triggerPredicate;

  private final Supplier<SoundEvent> rightMouseActionSound;

  private final long rightMouseActionSoundRepeatDelayMs;

  public GunItem(Properties properties) {
    super(properties);
    this.fireRateMs = properties.fireRate;
    this.damage = properties.damage;
    this.reloadDurationTicks = properties.reloadDurationTicks;
    this.accuracy = properties.accuracy;
    this.bulletAmountToFire = properties.bulletAmountToFire;
    this.aimable = properties.aimable;
    this.crosshair = properties.crosshair;
    this.fireModes = properties.fireModes;
    this.shootSound = properties.shootSound;
    this.silencedShootSound = properties.silencedShootSound;
    this.reloadSound = properties.reloadSound;
    this.animations = properties.animations;
    this.acceptedMagazines = properties.acceptedMagazines;
    this.defaultMagazine = properties.defaultMagazine;
    this.acceptedAttachments = properties.acceptedAttachments;
    this.defaultAttachments = properties.defaultAttachments;
    this.acceptedPaints = properties.acceptedPaints;
    this.rendererFactory = properties.rendererFactory;
    this.rightMouseActionTriggerType = properties.rightMouseActionTriggerType;
    this.triggerPredicate = properties.triggerPredicate;
    this.rightMouseActionSound = properties.rightMouseActionSound;
    this.rightMouseActionSoundRepeatDelayMs = properties.rightMouseActionSoundRepeatDelayMs;
    this
        .addPropertyOverride(new ResourceLocation("aiming"),
            (itemStack, world, entity) -> entity != null ? itemStack
                .getCapability(ModCapabilities.GUN)
                .filter(gun -> gun instanceof IScope)
                .map(gun -> ((IScope) gun).isAiming(entity, itemStack) ? 1.0F : 0.0F)
                .orElse(0.0F) : 0.0F);
  }

  public int getFireRateMs() {
    return this.fireRateMs;
  }

  public int getFireRateRPM() {
    return 60000 / this.getFireRateMs();
  }

  public int getDamage() {
    return this.damage;
  }

  public int getReloadDurationTicks() {
    return this.reloadDurationTicks;
  }

  public float getAccuracy() {
    return this.accuracy;
  }

  public int getBulletAmountToFire() {
    return this.bulletAmountToFire;
  }

  public boolean hasCrosshair() {
    return this.crosshair;
  }

  public List<FireMode> getFireModes() {
    return this.fireModes;
  }

  public Supplier<SoundEvent> getShootSound() {
    return this.shootSound;
  }

  public Optional<SoundEvent> getSilencedShootSound() {
    return Optional.ofNullable(this.silencedShootSound.get());
  }

  public Optional<SoundEvent> getReloadSound() {
    return Optional.ofNullable(this.reloadSound.get());
  }

  public Map<AnimationType, Supplier<GunAnimation>> getAnimations() {
    return this.animations;
  }

  public Set<MagazineItem> getAcceptedMagazines() {
    return this.acceptedMagazines.stream().map(Supplier::get).collect(Collectors.toSet());
  }

  public Supplier<MagazineItem> getDefaultMagazine() {
    return this.defaultMagazine;
  }

  public Set<AttachmentItem> getAcceptedAttachments() {
    return this.acceptedAttachments.stream().map(Supplier::get).collect(Collectors.toSet());
  }

  public Set<PaintItem> getAcceptedPaints() {
    return this.acceptedPaints.stream().map(Supplier::get).collect(Collectors.toSet());
  }

  public Set<AttachmentItem> getDefaultAttachments() {
    return this.defaultAttachments.stream().map(Supplier::get).collect(Collectors.toSet());
  }

  public IGun.RightMouseActionTriggerType getRightMouseActionTriggerType() {
    return this.rightMouseActionTriggerType;
  }

  public Predicate<IGun> getTriggerPredicate() {
    return this.triggerPredicate;
  }

  public Supplier<SoundEvent> getRightMouseActionSound() {
    return this.rightMouseActionSound;
  }

  public long getRightMouseActionSoundRepeatDelayMs() {
    return this.rightMouseActionSoundRepeatDelayMs;
  }

  @Override
  public GunRenderer getRenderer() {
    return DistExecutor.safeCallWhenOn(Dist.CLIENT, this.rendererFactory);
  }

  @Override
  public Predicate<ItemStack> getInventoryAmmoPredicate() {
    return itemStack -> this.acceptedMagazines
        .stream()
        .map(Supplier::get)
        .anyMatch(itemStack.getItem()::equals);
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack itemStack, @Nullable CompoundNBT nbt) {
    return this.aimable
        ? new SerializableCapabilityProvider<>(new AimableGun(this),
            ImmutableSet.of(() -> ModCapabilities.ANIMATION_PROVIDER, () -> ModCapabilities.GUN,
                () -> ModCapabilities.SCOPE))
        : new SerializableCapabilityProvider<>(new DefaultGun(this),
            ImmutableSet.of(() -> ModCapabilities.ANIMATION_PROVIDER, () -> ModCapabilities.GUN));
  }

  @Override
  public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
    return true;
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
      boolean slotChanged) {
    return oldStack.getItem() != newStack.getItem();
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity playerEntity,
      Hand hand) {
    if (hand == Hand.MAIN_HAND) {
      playerEntity.getCapability(ModCapabilities.LIVING).ifPresent(living -> playerEntity
          .getHeldItem(hand)
          .getCapability(ModCapabilities.GUN)
          .filter(
              gun -> gun.getRightMouseActionTriggerType() == IGun.RightMouseActionTriggerType.CLICK)
          .ifPresent(gun -> gun.toggleRightMouseAction(living, false)));
    }
    return super.onItemRightClick(world, playerEntity, hand);
  }

  @Override
  public void addInformation(ItemStack stack, World world, List<ITextComponent> lines,
      ITooltipFlag tooltipFlag) {
    super.addInformation(stack, world, lines, tooltipFlag);

    stack.getCapability(ModCapabilities.GUN).ifPresent(gun -> {
      ITextComponent magazineSizeText =
          Text.of(gun.getMagazineSize()).applyTextStyle(TextFormatting.RED);
      ITextComponent damageText = Text.of(this.damage).applyTextStyle(TextFormatting.RED);
      ITextComponent headshotDamageText = Text
          .of((int) (this.damage * DefaultGun.HEADSHOT_MULTIPLIER))
          .applyTextStyle(TextFormatting.RED);
      ITextComponent accuracyText =
          Text.of((int) (this.accuracy * 100D) + "%").applyTextStyle(TextFormatting.RED);
      ITextComponent rpmText = Text.of(this.getFireRateRPM()).applyTextStyle(TextFormatting.RED);

      lines
          .add(Text
              .translate("item_lore.gun_item.ammo_amount")
              .applyTextStyle(TextFormatting.GRAY)
              .appendSibling(magazineSizeText));
      lines
          .add(Text
              .translate("item_lore.gun_item.damage")
              .applyTextStyle(TextFormatting.GRAY)
              .appendSibling(damageText));
      lines
          .add(Text
              .translate("item_lore.gun_item.headshot_damage")
              .applyTextStyle(TextFormatting.GRAY)
              .appendSibling(headshotDamageText));

      if (this.bulletAmountToFire > 1) {
        ITextComponent pelletsText =
            Text.of(this.bulletAmountToFire).applyTextStyle(TextFormatting.RED);

        lines
            .add(Text
                .translate("item_lore.gun_item.pellets_shot")
                .applyTextStyle(TextFormatting.GRAY)
                .appendSibling(pelletsText));
      }

      for (AttachmentItem attachment : gun.getAttachments()) {
        ITextComponent attachmentNameText = attachment.getName().applyTextStyle(TextFormatting.RED);
        lines
            .add(Text
                .translate("item_lore.gun_item.attachment")
                .applyTextStyle(TextFormatting.GRAY)
                .appendSibling(attachmentNameText));
      }

      lines
          .add(Text
              .translate("item_lore.gun_item.rpm")
              .applyTextStyle(TextFormatting.GRAY)
              .appendSibling(rpmText));
      lines
          .add(Text
              .translate("item_lore.gun_item.accuracy")
              .applyTextStyle(TextFormatting.GRAY)
              .appendSibling(accuracyText));
    });
  }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
    return enchantment == Enchantments.FLAME || enchantment == Enchantments.POWER
        || super.canApplyAtEnchantingTable(stack, enchantment);
  }

  @Override
  public int getItemEnchantability() {
    return 1;
  }

  @Override
  public void onCreated(ItemStack itemStack, World world, PlayerEntity playerEntity) {
    itemStack.getCapability(ModCapabilities.GUN).ifPresent(gun -> gun.setMagazineSize(0));
  }

  public static class Properties extends Item.Properties {

    private int fireRate;

    private int damage;

    private int reloadDurationTicks;

    private int bulletAmountToFire = 1;

    private float accuracy;

    private boolean aimable = true;

    private boolean crosshair = true;

    private final List<FireMode> fireModes = new ArrayList<>();

    private Supplier<SoundEvent> shootSound;

    private Supplier<SoundEvent> silencedShootSound = () -> null;

    private Supplier<SoundEvent> reloadSound = () -> null;

    private final Map<AnimationType, Supplier<GunAnimation>> animations =
        new EnumMap<>(AnimationType.class);

    private final Set<Supplier<MagazineItem>> acceptedMagazines = new HashSet<>();

    private Supplier<MagazineItem> defaultMagazine;

    private final Set<Supplier<AttachmentItem>> acceptedAttachments = new HashSet<>();

    private final Set<Supplier<PaintItem>> acceptedPaints = new HashSet<>();

    private final Set<Supplier<AttachmentItem>> defaultAttachments = new HashSet<>();

    private Supplier<DistExecutor.SafeCallable<GunRenderer>> rendererFactory;

    private IGun.RightMouseActionTriggerType rightMouseActionTriggerType =
        IGun.RightMouseActionTriggerType.CLICK;

    private Predicate<IGun> triggerPredicate = gun -> true;

    private Supplier<SoundEvent> rightMouseActionSound = () -> null;

    private long rightMouseActionSoundRepeatDelayMs = -1L;

    public Properties setFireRate(int fireRate) {
      this.fireRate = fireRate;
      return this;
    }

    public Properties setDamage(int damage) {
      this.damage = damage;
      return this;
    }

    public Properties setReloadDurationTicks(int reloadDurationTicks) {
      this.reloadDurationTicks = reloadDurationTicks;
      return this;
    }

    public Properties setBulletAmountToFire(int amount) {
      this.bulletAmountToFire = amount;
      return this;
    }

    public Properties setAimable(boolean aimable) {
      this.aimable = aimable;
      return this;
    }

    public Properties setCrosshair(boolean crosshair) {
      this.crosshair = crosshair;
      return this;
    }

    public Properties setAccuracy(float accuracy) {
      this.accuracy = accuracy;
      return this;
    }

    public Properties addFireMode(FireMode fireMode) {
      this.fireModes.add(fireMode);
      return this;
    }

    public Properties setShootSound(Supplier<SoundEvent> shootSound) {
      this.shootSound = shootSound;
      return this;
    }

    public Properties setSilencedShootSound(Supplier<SoundEvent> silencedShootSound) {
      this.silencedShootSound = silencedShootSound;
      return this;
    }

    public Properties setReloadSound(Supplier<SoundEvent> reloadSound) {
      this.reloadSound = reloadSound;
      return this;
    }

    public Properties addAnimation(AnimationType type, Supplier<GunAnimation> animation) {
      this.animations.put(type, animation);
      return this;
    }

    public Properties addAcceptedMagazine(Supplier<MagazineItem> acceptedMagazine) {
      this.acceptedMagazines.add(acceptedMagazine);
      return this;
    }

    public Properties setDefaultMagazine(Supplier<MagazineItem> defaultMagazine) {
      if (this.defaultMagazine != null) {
        throw new IllegalArgumentException("Default magazine already set");
      }
      this.defaultMagazine = defaultMagazine;
      return this.addAcceptedMagazine(defaultMagazine);
    }

    public Properties addAcceptedAttachment(Supplier<AttachmentItem> acceptedAttachment) {
      this.acceptedAttachments.add(acceptedAttachment);
      return this;
    }

    public Properties addDefaultAttachment(Supplier<AttachmentItem> defaultAttachment) {
      this.defaultAttachments.add(defaultAttachment);
      return this.addAcceptedAttachment(defaultAttachment);
    }

    public Properties addAcceptedPaint(Supplier<PaintItem> acceptedPaint) {
      this.acceptedPaints.add(acceptedPaint);
      return this;
    }

    public Properties setRendererFactory(
        Supplier<DistExecutor.SafeCallable<GunRenderer>> rendererFactory) {
      this.rendererFactory = rendererFactory;
      return this;
    }

    public Properties setRightMouseActionTriggerType(
        IGun.RightMouseActionTriggerType rightMouseActionTriggerType) {
      this.rightMouseActionTriggerType = rightMouseActionTriggerType;
      return this;
    }

    public Properties setTriggerPredicate(Predicate<IGun> triggerPredicate) {
      this.triggerPredicate = triggerPredicate;
      return this;
    }

    public Properties setRightMouseActionSound(Supplier<SoundEvent> rightMouseActionSound) {
      this.rightMouseActionSound = rightMouseActionSound;
      return this;
    }

    public Properties setRightMouseActionSoundRepeatDelayMs(
        long rightMouseActionSoundRepeatDelayMs) {
      this.rightMouseActionSoundRepeatDelayMs = rightMouseActionSoundRepeatDelayMs;
      return this;
    }
  }
}
