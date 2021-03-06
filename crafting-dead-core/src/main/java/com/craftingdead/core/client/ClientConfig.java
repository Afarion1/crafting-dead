/**
 * Crafting Dead Copyright (C) 2020 Nexus Node
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.craftingdead.core.client;

import com.craftingdead.core.client.gui.HitMarker;
import com.craftingdead.core.client.tutorial.ModTutorialSteps;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

  public final ForgeConfigSpec.BooleanValue displayBlood;

  public final ForgeConfigSpec.EnumValue<HitMarker.Mode> hitMarkerMode;

  public final ForgeConfigSpec.BooleanValue playKillSound;

  public final ForgeConfigSpec.EnumValue<ModTutorialSteps> tutorialStep;

  public ClientConfig(ForgeConfigSpec.Builder builder) {
    builder.push("client");
    {
      this.displayBlood = builder
          .translation("options.craftingdead.client.display_blood")
          .define("displayBlood", true);
      this.hitMarkerMode = builder.translation("options.craftingdead.client.hit_marker_mode")
          .defineEnum("hitMarkerMode", HitMarker.Mode.HIT_AND_KILL);
      this.playKillSound = builder.translation("options.craftingdead.client.play_kill_sound")
          .define("playKillSound", true);
      this.tutorialStep =
          builder.comment("Internal").defineEnum("tutorialStep", ModTutorialSteps.OPEN_INVENTORY);
    }
    builder.pop();
  }
}
