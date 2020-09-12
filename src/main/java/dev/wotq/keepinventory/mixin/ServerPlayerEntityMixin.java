package dev.wotq.keepinventory.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.wotq.keepinventory.bridge.PlayerEntityBridge.bridge;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    /**
     * Redirects call to getBoolean(GameRules.KEEP_INVENTORY) in copyFrom.
     *
     * Only interferes if key is KEEP_INVENTORY.
     *
     * @param rules object getBoolean is being called on
     * @param key argument getBoolean is being called with
     *
     * @return $wotq_keepInventory if it is true or false, otherwise the gamerule's value
     */
    @Redirect(method = "copyFrom", at = @At(value = "INVOKE", target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean onCopyFrom(GameRules rules, GameRules.Key<GameRules.BooleanRule> key, ServerPlayerEntity oldPlayer) {
        if (key.getName().equals("keepInventory")) {
            return bridge(oldPlayer).$wotq_getKeepInventory().orElseGet(() -> rules.getBoolean(key));
        } else {
            return rules.getBoolean(key);
        }
    }

    /**
     * Injected at the end of copyFrom.
     *
     * Copies the per-player keepInventory value.
     *
     * @param oldPlayer the old player to copy from
     * @param alive
     * @param callback
     */
    @Inject(method = "copyFrom", at = @At(value = "RETURN"))
    public void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo callback) {
        bridge(this).$wotq_setKeepInventory(bridge(oldPlayer).$wotq_getKeepInventory());
    }
}
