package me.pietelite.nopeapiexamplesponge;

import com.google.inject.Inject;
import me.pietelite.nope.common.api.NopeServiceProvider;
import me.pietelite.nope.common.api.setting.SettingCategory;
import me.pietelite.nope.common.api.setting.SettingKeyBuilder;
import me.pietelite.nope.sponge.api.event.SettingListenerRegistration;
import me.pietelite.nope.sponge.api.event.SettingListenerRegistrationEvent;
import me.pietelite.nope.sponge.api.setting.SettingKeyRegistrationEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

/**
 * Standard Sponge plugin.
 */
@Plugin("nope-api-example-sponge")
public class NopeApiExampleSponge {

  private final PluginContainer container;
  private final Logger logger;

  @Inject
  NopeApiExampleSponge(final PluginContainer container, final Logger logger) {
    this.container = container;
    this.logger = logger;
  }

  // Other Sponge plugin code

  /**
   * Handler for Nope's {@link SettingKeyRegistrationEvent},
   * which allows us to register our special setting key using a
   * {@link SettingKeyBuilder}.
   *
   * @param event the event
   */
  @Listener
  public void onSettingKeyRegistration(SettingKeyRegistrationEvent event) {
    logger.info("Registering our custom setting key: 'snow-trail'");
    event.registrar().register(NopeServiceProvider.service()
        .toggleKeyBuilder("snow-trail")
        .defaultValue(false)
        .description("When enabled, players create trails of snow in their wake like snow golems!")
        .blurb("Players create snow trails")
        .category(SettingCategory.ENTITIES));
  }

  /**
   * Handler for Nope's {@link SettingListenerRegistrationEvent},
   * which allows us to register a listener to handle the behavior when
   * our setting key is set to a certain value.
   *
   * @param event the event
   */
  @Listener
  public void onSettingListenerRegistration(SettingListenerRegistrationEvent event) {
    logger.info("Registering a listener for our custom setting key: 'snow-trail'");
    event.registrar().register(new SettingListenerRegistration<>(
        "snow-trail", Boolean.class, MoveEntityEvent.class, this.container,
        (context) -> {
          Entity entity = context.event().entity();
          if (!entity.type().equals(EntityTypes.PLAYER.get())) {
            return;  // only players should create snow trails!
          }
          if (!context.lookup(entity.serverLocation())) {
            return;  // "snow-trails" needs to be set to "true"!
          }
          if (!entity.serverLocation()
              .add(Vector3i.UNIT_Y.negate())
              .block()
              .getOrElse(Keys.IS_SOLID, false)) {
            return;  // the ground cannot have snow on it!
          }
          entity.serverLocation().setBlock(BlockState.builder()
                  .blockType(BlockTypes.SNOW)
                  .build(),
              BlockChangeFlags.DEFAULT_PLACEMENT);
        }
    ));
  }
}
