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
package com.craftingdead.core.action;

import org.apache.commons.lang3.tuple.Pair;
import com.craftingdead.core.CraftingDead;
import com.craftingdead.core.action.item.BlockActionEntry;
import com.craftingdead.core.action.item.EntityActionEntry;
import com.craftingdead.core.action.item.UseItemAction;
import com.craftingdead.core.item.ModItems;
import com.craftingdead.core.potion.ModEffects;
import com.craftingdead.core.util.ModDamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class ActionTypes {

  @SuppressWarnings("unchecked")
  public static final DeferredRegister<ActionType<?>> ACTION_TYPES =
      DeferredRegister.create((Class<ActionType<?>>) (Class<?>) ActionType.class, CraftingDead.ID);

  public static final RegistryObject<ActionType<?>> RELOAD = ACTION_TYPES
      .register("reload",
          () -> new ActionType<>((actionType, performer, target) -> new ReloadAction(performer),
              true));

  public static final RegistryObject<ActionType<?>> REMOVE_MAGAZINE =
      ACTION_TYPES.register("remove_magazine",
          () -> new ActionType<>(
              (actionType, performer, target) -> new RemoveMagazineAction(performer),
              true));

  public static final RegistryObject<ActionType<UseItemAction>> USE_CURE_SYRINGE = ACTION_TYPES
      .register("use_cure_syringe",
          () -> new ActionType<>(
              (actionType, performer, target) -> UseItemAction
                  .builder(actionType, performer, target)
                  .setHeldItemPredicate(item -> item == ModItems.CURE_SYRINGE.get())
                  .setTotalDurationTicks(16)
                  .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                      .setTargetSelector(EntityActionEntry.TargetSelector.SELF_AND_OTHERS)
                      .setReturnItem(ModItems.SYRINGE)))
                  .build(),
              false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_CLEAN_RAG = ACTION_TYPES
      .register("use_clean_rag",
          () -> new ActionType<>(
              (actionType, performer, target) -> UseItemAction
                  .builder(actionType, performer, target)
                  .setHeldItemPredicate(item -> item == ModItems.CLEAN_RAG.get())
                  .setTotalDurationTicks(16)
                  .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                      .setTargetSelector(EntityActionEntry.TargetSelector.SELF_AND_OTHERS
                          .andThen(t -> (t == null
                              || !t.getEntity().isPotionActive(ModEffects.BLEEDING.get())) ? null
                                  : t))
                      .setReturnItem(ModItems.BLOODY_RAG)))
                  .build(),
              false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_SPLINT = ACTION_TYPES
      .register("use_splint",
          () -> new ActionType<>(
              (actionType, performer, target) -> UseItemAction
                  .builder(actionType, performer, target)
                  .setHeldItemPredicate(item -> item == ModItems.SPLINT.get())
                  .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                      .setTargetSelector(EntityActionEntry.TargetSelector.SELF_AND_OTHERS
                          .andThen(t -> (t == null
                              || !t.getEntity().isPotionActive(ModEffects.BROKEN_LEG.get())) ? null
                                  : t))))
                  .build(),
              false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_SYRINGE = ACTION_TYPES
      .register("use_syringe", () -> new ActionType<>(
          (actionType, performer, target) -> UseItemAction.builder(actionType, performer, target)
              .setHeldItemPredicate(item -> item == ModItems.SYRINGE.get())
              .setTotalDurationTicks(16)
              .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                  .setTargetSelector(
                      EntityActionEntry.TargetSelector.OTHERS_ONLY.ofType(ZombieEntity.class))
                  .setCustomAction(Pair.of(
                      t -> t.getEntity().attackEntityFrom(ModDamageSource.BLEEDING,
                          2.0F),
                      0.25F))
                  .setReturnItem(ModItems.RBI_SYRINGE)))
              .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                  .setTargetSelector((p, t) -> {
                    if (t == null) {
                      return null;
                    }
                    LivingEntity targetEntity = t.getEntity();
                    if (!(targetEntity instanceof ZombieEntity
                        || targetEntity instanceof SkeletonEntity)) {
                      if (targetEntity.getHealth() > 4) {
                        return t;
                      } else if (p.getEntity() instanceof PlayerEntity) {
                        ((PlayerEntity) p.getEntity()).sendStatusMessage(
                            new TranslationTextComponent("message.low_health",
                                targetEntity.getDisplayName())
                                    .setStyle(new Style().setColor(TextFormatting.RED)),
                            true);
                      }
                    }
                    return null;
                  })
                  .setCustomAction(Pair.of(
                      t -> t.getEntity().attackEntityFrom(ModDamageSource.BLEEDING, 2.0F), 1.0F))
                  .setReturnItem(ModItems.BLOOD_SYRINGE)))
              .build(),
          false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_RBI_SYRINGE = ACTION_TYPES
      .register("use_rbi_syringe",
          () -> new ActionType<>(
              (actionType, performer, target) -> UseItemAction
                  .builder(actionType, performer, target)
                  .setHeldItemPredicate(item -> item == ModItems.RBI_SYRINGE.get())
                  .setTotalDurationTicks(16)
                  .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                      .setTargetSelector(EntityActionEntry.TargetSelector.SELF_AND_OTHERS)
                      .addEffect(
                          Pair.of(new EffectInstance(ModEffects.INFECTION.get(), 9999999),
                              1.0F))
                      .setReturnItem(ModItems.SYRINGE)))
                  .build(),
              false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_FIRST_AID_KIT = ACTION_TYPES
      .register("use_first_aid_kit",
          () -> new ActionType<>(
              (actionType, performer, target) -> UseItemAction
                  .builder(actionType, performer, target)
                  .setHeldItemPredicate(item -> item == ModItems.FIRST_AID_KIT.get())
                  .setFreezeMovement(true)
                  .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                      .setTargetSelector(EntityActionEntry.TargetSelector.SELF_AND_OTHERS)
                      .addEffect(
                          Pair.of(new EffectInstance(Effects.INSTANT_HEALTH, 1, 1), 1.0F))))
                  .build(),
              false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_ADRENALINE_SYRINGE =
      ACTION_TYPES
          .register("use_adrenaline_syringe", () -> new ActionType<>(
              (actionType, performer, target) -> UseItemAction
                  .builder(actionType, performer, target)
                  .setHeldItemPredicate(item -> item == ModItems.ADRENALINE_SYRINGE.get())
                  .setTotalDurationTicks(16)
                  .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                      .setReturnItem(ModItems.SYRINGE)
                      .addEffect(
                          Pair.of(new EffectInstance(ModEffects.ADRENALINE.get(), (20 * 20), 1),
                              1.0F))))
                  .build(),
              false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_BLOOD_SYRINGE = ACTION_TYPES
      .register("use_blood_syringe", () -> new ActionType<>(
          (actionType, performer, target) -> UseItemAction.builder(actionType, performer, target)
              .setHeldItemPredicate(item -> item == ModItems.BLOOD_SYRINGE.get())
              .setTotalDurationTicks(16)
              .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                  .setReturnItem(ModItems.SYRINGE)
                  .addEffect(
                      Pair.of(new EffectInstance(Effects.INSTANT_HEALTH, 1, 0), 1.0F))))
              .build(),
          false));

  public static final RegistryObject<ActionType<UseItemAction>> USE_BANDAGE = ACTION_TYPES
      .register("use_bandage", () -> new ActionType<>(
          (actionType, performer, target) -> UseItemAction.builder(actionType, performer, target)
              .setHeldItemPredicate(item -> item == ModItems.BANDAGE.get())
              .setTotalDurationTicks(16)
              .addEntry(new EntityActionEntry(new EntityActionEntry.Properties()
                  .setTargetSelector(EntityActionEntry.TargetSelector.SELF_AND_OTHERS)
                  .addEffect(
                      Pair.of(new EffectInstance(Effects.INSTANT_HEALTH, 1, 0), 1.0F))))
              .build(),
          false));

  public static final RegistryObject<ActionType<UseItemAction>> WASH_RAG = ACTION_TYPES
      .register("wash_rag", () -> new ActionType<>(
          (actionType, performer, target) -> UseItemAction.builder(actionType, performer, target)
              .setHeldItemPredicate(
                  item -> item == ModItems.DIRTY_RAG.get() || item == ModItems.BLOODY_RAG.get())
              .addEntry(new BlockActionEntry(new BlockActionEntry.Properties()
                  .setReturnItem(ModItems.CLEAN_RAG)
                  .setFinishSound(SoundEvents.ITEM_BUCKET_FILL)
                  .setPredicate(
                      blockState -> blockState.getFluidState().getFluid() == Fluids.WATER)))
              .build(),
          false));
}
