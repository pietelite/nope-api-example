package com.pietersvenson.nopeaddon;

import com.google.inject.Inject;
import com.minecraftonline.nope.common.setting.SettingKey;
import com.minecraftonline.nope.common.setting.manager.BooleanKeyManager;
import com.minecraftonline.nope.sponge.api.event.SettingEventListener;
import com.minecraftonline.nope.sponge.api.event.SettingListenerRegistration;
import com.minecraftonline.nope.sponge.api.event.SettingListenerRegistrationEvent;
import com.minecraftonline.nope.sponge.api.event.SettingValueLookupFunction;
import com.minecraftonline.nope.sponge.api.setting.SettingKeyRegistrationEvent;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

/**
 * The main class of your Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("nope-addon")
public class NopeAddon {

  private final PluginContainer container;
  private final Logger logger;

  private final SettingKey.Unary<Boolean> SNOW_TRAIL = SettingKey.Unary.builder(
      "snow-trail",
      false,
      new BooleanKeyManager()
  ).blurb("Makes snow trails for players like snow golems")
      .description("Creates a snow trail behind players as if they were a snow golem")
      .category(SettingKey.Category.ENTITIES)
      .build();

  @Inject
  NopeAddon(final PluginContainer container, final Logger logger) {
    this.container = container;
    this.logger = logger;
  }

  @Listener
  public void onConstructPlugin(final ConstructPluginEvent event) {
    // Perform any one-time setup
    this.logger.info("Constructing nope-addon");
  }

  @Listener
  public void onServerStarting(final StartingEngineEvent<Server> event) {
    // Any setup per-game instance. This can run multiple times when
    // using the integrated (singleplayer) server.
  }

  @Listener
  public void onServerStopping(final StoppingEngineEvent<Server> event) {
    // Any tear down per-game instance. This can run multiple times when
    // using the integrated (singleplayer) server.
  }

  @Listener
  public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
    // Register a simple command
    // When possible, all commands should be registered within a command register event
    final Parameter.Value<String> nameParam = Parameter.string().key("name").build();
    event.register(this.container, Command.builder()
        .addParameter(nameParam)
        .permission("nope-addon.command.greet")
        .executor(ctx -> {
          final String name = ctx.requireOne(nameParam);
          ctx.sendMessage(Identity.nil(), LinearComponents.linear(
              NamedTextColor.AQUA,
              Component.text("Hello "),
              Component.text(name, Style.style(TextDecoration.BOLD)),
              Component.text("!")
          ));

          return CommandResult.success();
        })
        .build(), "greet", "wave");
  }

  @Listener
  public void onSettingKeyRegistration(SettingKeyRegistrationEvent event) {
    event.registrar().register(SNOW_TRAIL);
  }

  @Listener
  public void onSettingListenerRegistration(SettingListenerRegistrationEvent event) {
    event.registrar().register(new SettingListenerRegistration<>(
        SNOW_TRAIL,
        MoveEntityEvent.class,
        this.container,
        (e, settingGetter) -> {
          if (e.entity().type().equals(EntityTypes.PLAYER.get())
          && settingGetter.lookup(null, e.entity().serverLocation())
          && e.entity().serverLocation()
              .add(Vector3i.UNIT_Y.negate())
              .block()
              .getOrElse(Keys.IS_SOLID, false)) {
              e.entity().serverLocation().setBlock(BlockState.builder()
                  .blockType(BlockTypes.SNOW)
                  .build(),
                  BlockChangeFlags.DEFAULT_PLACEMENT);
          }
        }
    ));
  }
}
