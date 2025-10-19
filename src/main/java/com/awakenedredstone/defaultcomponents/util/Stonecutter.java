package com.awakenedredstone.defaultcomponents.util;

import com.mojang.authlib.GameProfile;

/**
 * Utility class to move common components that have stonecutter comments to make the code cleaner
 */
public class Stonecutter {
    public static /*$ WhitelistProfile >>*/net.minecraft.server.PlayerConfigEntry toPlayerProfile(GameProfile profile) {
        return /*? if <1.21.9 {*/ /*profile *//*?} else {*/ new net.minecraft.server.PlayerConfigEntry(profile.id(), profile.name()) /*?}*/;
    }
}
