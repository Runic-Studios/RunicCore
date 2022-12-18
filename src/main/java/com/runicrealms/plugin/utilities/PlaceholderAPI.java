/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.TitleData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion {

    @NotNull
    @Override
    public String getIdentifier() {
        return "core";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Skyfallin";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String arg) {

        if (player == null) return null;
        String lowerArg = arg.toLowerCase();

        TitleData titleData = RunicCore.getTitleManager().loadTitleData(player.getUniqueId());

        switch (lowerArg) {
            case "class":
                return RunicCore.getCharacterAPI().getPlayerClass(player);
            case "class_prefix":
                return RunicCore.getCharacterAPI().getPlayerClass(player).substring(0, 2);
            case "level":
                return player.getLevel() + "";
            case "prof":
                return "";
            case "prof_level":
                return "";
            case "prefix":
                return titleData.getPrefix();
            case "prefix_formatted":
                if (!titleData.getPrefix().equals("")) {
                    return "[" + titleData.getPrefix() + "] ";
                }
                return "";
            case "suffix":
                return titleData.getSuffix();
            case "suffix_formatted":
                if (!titleData.getSuffix().equals("")) {
                    return "[" + titleData.getSuffix() + "] ";
                }
                return "";
            default:
                return "";
        }
    }
}
