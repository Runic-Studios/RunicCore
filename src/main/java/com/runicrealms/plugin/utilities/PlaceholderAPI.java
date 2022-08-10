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

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.model.ClassData;
import com.runicrealms.plugin.model.ProfessionData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlaceholderAPI extends PlaceholderExpansion {

    @NotNull
    @Override
    public String getIdentifier() {
        return "core";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Skyfallin_";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String arg) {

        if (player == null) return null;
        String lowerArg = arg.toLowerCase();

        Map<String, String> classFields = RunicCoreAPI.getRedisValues(player, ClassData.getFIELDS());
        Map<String, String> professionFields = RunicCoreAPI.getRedisValues(player, ProfessionData.getFields());
        switch (lowerArg) {
            case "class":
                return classFields.get(CharacterField.CLASS_TYPE.getField());
            case "class_prefix":
                return classFields.get(CharacterField.CLASS_TYPE.getField()).substring(0, 2);
            case "level":
                return player.getLevel() + "";
            case "prof":
                return professionFields.get(CharacterField.PROF_NAME.getField());
            case "prof_level":
                return professionFields.get(CharacterField.PROF_LEVEL.getField());
            default:
                return "";
        }
    }
}
