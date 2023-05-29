package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.weaponskin.WeaponSkin;
import org.bukkit.entity.Player;

@CommandAlias("weaponskin")
@Conditions("is-op")
@Private
public class WeaponSkinCMD extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        player.sendMessage(ColorUtil.format("&aUse: /weaponskin equip|unequip <skin-1> <skin-2> ..."));
    }

    @Subcommand("equip")
    @CommandCompletion("@weapon-skins")
    public void onCommandEquip(Player player, String[] args) {
        for (String skinName : args) {
            WeaponSkin weaponSkin = RunicCore.getWeaponSkinAPI().getAllSkins()
                    .stream()
                    .filter((skin) -> skin.customName().equalsIgnoreCase(skinName))
                    .findFirst().orElse(null);
            if (weaponSkin == null) {
                player.sendMessage(ColorUtil.format("&cFailed to find skin: " + skinName));
                continue;
            }
            if (!RunicCore.getWeaponSkinAPI().hasWeaponSkin(player, weaponSkin)) {
                player.sendMessage(ColorUtil.format("&cPlayer does not have permission for skin: " + skinName));
                continue;
            }
            RunicCore.getWeaponSkinAPI().activateSkin(player, weaponSkin);
            player.sendMessage(ColorUtil.format("&aLoaded weapon skin: " + skinName));
        }
    }

    @Subcommand("unequip")
    @CommandCompletion("@weapon-skins")
    public void onCommandUnequip(Player player, String[] args) {
        for (String skinName : args) {
            WeaponSkin weaponSkin = RunicCore.getWeaponSkinAPI().getAllSkins()
                    .stream()
                    .filter((skin) -> skin.customName().equalsIgnoreCase(skinName))
                    .findFirst().orElse(null);
            if (weaponSkin == null) {
                player.sendMessage(ColorUtil.format("&cFailed to find skin: " + skinName));
                continue;
            }
            RunicCore.getWeaponSkinAPI().deactivateSkin(player, weaponSkin);
            player.sendMessage(ColorUtil.format("&aUnequiped weapon skin: " + skinName));
        }
    }

}
