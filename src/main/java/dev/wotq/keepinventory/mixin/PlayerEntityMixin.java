package dev.wotq.keepinventory.mixin;

import dev.wotq.keepinventory.bridge.PlayerEntityBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityBridge {
    /**
     * The per-player keepInventory flag.
     */
    private Optional<Boolean> $wotq_keepInventory;

    /**
     * Getter for $wotq_keepInventory.
     *
     * @return value of $wotq_keepInventory
     */
    public Optional<Boolean> $wotq_getKeepInventory() {
        return $wotq_keepInventory;
    }

    /**
     * Setter for $wotq_keepInventory
     *
     * @param value value of $wotq_keepInventory
     */
    public void $wotq_setKeepInventory(Optional<Boolean> value) {
        $wotq_keepInventory = value;
    }

    /**
     * Injected at the end of the constructor.
     *
     * @param callback
     */
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo callback) {
        $wotq_keepInventory = Optional.empty();
    }

    /**
     * Injected at the end of readCustomDataFromTag
     *
     * @param tag tag to read the data from
     * @param callback
     */
    @Inject(method = "readCustomDataFromTag", at = @At(value = "RETURN"))
    public void onReadCustomDataFromTag(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("$wotq_keepInventory")) {
            $wotq_keepInventory = Optional.of(tag.getBoolean("$wotq_keepInventory"));
        } else {
            $wotq_keepInventory = Optional.empty();
        }
    }

    /**
     * Injected at the end of writeCustomDataToTag.
     *
     * @param tag tag to write the data to
     * @param callback
     */
    @Inject(method = "writeCustomDataToTag", at = @At(value = "RETURN"))
    public void onWriteCustomDataToTag(CompoundTag tag, CallbackInfo callback) {
        $wotq_keepInventory.ifPresent(value -> tag.putBoolean("$wotq_keepInventory", value));
    }

    /**
     * Redirects call to getBoolean(GameRules.KEEP_INVENTORY) in dropInventory.
     *
     * Only interferes if key is KEEP_INVENTORY.
     *
     * @param rules object getBoolean is being called on
     * @param key argument getBoolean is being called with
     *
     * @return $wotq_keepInventory if it is true or false, otherwise the gamerule's value
     */
    @Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
    public boolean onDropInventory(GameRules rules, GameRules.RuleKey<GameRules.BooleanRule> key) {
        System.out.println("onDropInventory");

        if (key == GameRules.KEEP_INVENTORY) {
            return this.$wotq_keepInventory.orElseGet(() -> rules.getBoolean(key));
        } else {
            return rules.getBoolean(key);
        }
    }

    /**
     * Redirects call to getBoolean(GameRules.KEEP_INVENTORY) in getCurrentExperience (more like getDroppedExperience).
     *
     * Only interferes if key is KEEP_INVENTORY.
     *
     * @param rules object getBoolean is being called on
     * @param key argument getBoolean is being called with
     *
     * @return $wotq_keepInventory if it is true or false, otherwise the gamerule's value
     */
    @Redirect(method = "getCurrentExperience", at = @At(value = "INVOKE", target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
    public boolean onGetCurrentExperience(GameRules rules, GameRules.RuleKey<GameRules.BooleanRule> key) {
        System.out.println("onGetCurrentExperience");

        if (key == GameRules.KEEP_INVENTORY) {
            return this.$wotq_keepInventory.orElseGet(() -> rules.getBoolean(key));
        } else {
            return rules.getBoolean(key);
        }
    }
}
