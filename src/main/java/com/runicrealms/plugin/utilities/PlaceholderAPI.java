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
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "core";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Skyfallin_";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player pl, String arg) {

        if (pl == null)  return null;

        String lowerArg = arg.toLowerCase();

        switch (lowerArg) {
            case "class":
                return RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName();
            case "class_prefix":
                return RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName().substring(0, 2);
            case "level":
                return pl.getLevel() + "";
            case "prof":
                return RunicCore.getCacheManager().getPlayerCaches().get(pl).getProfName();
            case "prof_level":
                return RunicCore.getCacheManager().getPlayerCaches().get(pl).getProfLevel() + "";
            default:
                return "";
        }
    }
}
