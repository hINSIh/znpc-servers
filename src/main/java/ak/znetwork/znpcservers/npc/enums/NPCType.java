/*
 *
 * ZNServersNPC
 * Copyright (C) 2019 Gaston Gonzalez (ZNetwork)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package ak.znetwork.znpcservers.npc.enums;

import ak.znetwork.znpcservers.cache.ClazzCache;
import ak.znetwork.znpcservers.utils.Utils;
import lombok.Getter;
import org.apache.commons.lang.math.NumberUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

public enum NPCType {

    PLAYER(ClazzCache.ENTITY_PLAYER_CLASS, "", -1, 0),
    ARMOR_STAND(ClazzCache.ENTITY_ARMOR_STAND_CLASS, "", -1, 0),
    CREEPER(ClazzCache.ENTITY_CREEPER_CLASS, "", -1, -0.15, "setPowered"),
    BAT(ClazzCache.ENTITY_BAT_CLASS, "", -1, -0.5, "setAsleep"),

    BLAZE(ClazzCache.ENTITY_BLAZE_CLASS, "", -1, 0, (Utils.isVersionNewestThan(8) ? "p" : (Utils.isVersionNewestThan(15) ? "l" : "eL"))),
    CAVE_SPIDER(ClazzCache.ENTITY_CAVE_SPIDER_CLASS, "", -1, -1),
    COW(ClazzCache.ENTITY_COW_CLASS, "", -1, -0.25, "setAge"),
    ENDER_DRAGON(ClazzCache.ENTITY_ENDER_DRAGON_CLASS, "", -1, 1.5),
    ENDERMAN(ClazzCache.ENTITY_ENDERMAN_CLASS, "", -1, 0.7),
    ENDERMITE(ClazzCache.ENTITY_ENDERMITE_CLASS, "", -1, -1.5),
    GHAST(ClazzCache.ENTITY_GHAST_CLASS, "", -1, 3),
    IRON_GOLEM(ClazzCache.ENTITY_IRON_GOLEM_CLASS, "", -1, 0.75),
    GIANT(ClazzCache.ENTITY_GIANT_ZOMBIE_CLASS, "", -1, 11),
    GUARDIAN(ClazzCache.ENTITY_GUARDIAN_CLASS, "", -1, -0.7),
    HORSE(ClazzCache.ENTITY_HORSE_CLASS, "", -1, 0, "setVariant", "setAge"),
    LLAMA(ClazzCache.ENTITY_LLAMA_CLASS, "", -1, 0, "setAge"),
    MAGMA_CUBE(ClazzCache.ENTITY_MAGMA_CUBE_CLASS, "", -1, -1.25, "setSize"),
    MOOSHROOM(ClazzCache.ENTITY_MUSHROOM_COW_CLASS, "", -1, -0.25, "setAge"),
    OCELOT(ClazzCache.ENTITY_OCELOT_CLASS, "", -1, -1, "setCatType", "setAge"),
    PARROT(ClazzCache.ENTITY_PARROT_CLASS, "", -1, -1.5),
    PIG(ClazzCache.ENTITY_PIG_CLASS, "", -1, -1, "setAge"),
    ZOMBIFIED_PIGLIN(ClazzCache.ENTITY_PIG_ZOMBIE_CLASS, "ZOMBIE_PIGMAN", -1, 0),
    POLAR_BEAR(ClazzCache.ENTITY_POLAR_BEAR_CLASS, "", -1, -0.5),
    SHEEP(ClazzCache.ENTITY_SHEEP_CLASS, "", -1, -0.5, "setAge", "setSheared", "setColor"),
    SILVERFISH(ClazzCache.ENTITY_SILVERFISH_CLASS, "", -1, -1.5),
    SKELETON(ClazzCache.ENTITY_SKELETON_CLASS, "", -1, 0),
    SLIME(ClazzCache.ENTITY_SLIME_CLASS, "", -1, -1.25, "setSize"),
    SPIDER(ClazzCache.ENTITY_SPIDER_CLASS, "", -1, -1),
    SQUID(ClazzCache.ENTITY_SQUID_CLASS, "", -1, -1),
    VILLAGER(ClazzCache.ENTITY_VILLAGER_CLASS, "", -1, 0, "setProfession", "setAge"),
    WITCH(ClazzCache.ENTITY_WITCH_CLASS, "", -1, 0.5),
    WITHER(ClazzCache.ENTITY_WITHER_CLASS, "", -1, 1.75, "g"),
    ZOMBIE(ClazzCache.ENTITY_ZOMBIE_CLASS, "", -1, 0, "setBaby"),
    WOLF(ClazzCache.ENTITY_WOLF_CLASS, "", -1, -1, "setAngry", "setAge"),
    END_CRYSTAL(ClazzCache.ENTITY_END_CRYSTAL_CLASS, "", 51, 0);

    @Getter private final double holoHeight;

    @Getter private final ClazzCache clazzCache;
    @Getter private Constructor<?> constructor = null;

    @Getter private Object entityType;

    @Getter private final String[] customization;
    @Getter private final HashMap<String, Method> customizationMethods;

    @Getter private final int id;

    private final String newName;

    NPCType(final ClazzCache clazzCache, final String newName, final int id, final double holoHeight, final String... customization) {
        this.clazzCache = clazzCache;

        this.newName = newName;

        this.id = id;

        this.holoHeight = holoHeight;

        this.customization = customization;
        this.customizationMethods = new HashMap<>();

        // Load customization
        for (String value : this.customization) {
            for (Method method : this.clazzCache.getCacheClass().getMethods()) {
                if (customizationMethods.containsKey(method.getName())) continue;

                if (method.getName().equalsIgnoreCase(value)) {
                    customizationMethods.put(method.getName(), method);
                }
            }
        }
    }

    /**
     * Load npc type
     */
    public void load() {
        try {
            this.constructor = (this != NPCType.PLAYER ? (Utils.isVersionNewestThan(13) ? this.getClazzCache().getCacheClass().getConstructor(this.getEntityType().getClass(), ClazzCache.WORLD_CLASS.getCacheClass()) : this.getClazzCache().getCacheClass().getConstructor(ClazzCache.WORLD_CLASS.getCacheClass())) : null);

            if (!Utils.isVersionNewestThan(13)) return;

            try {
                entityType = ClazzCache.ENTITY_TYPES_CLASS.getCacheClass().getField(name()).get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                try {
                    entityType = ClazzCache.ENTITY_TYPES_CLASS.getCacheClass().getField(newName).get(null);
                } catch (IllegalAccessException | NoSuchFieldException operationException) {
                    throw new AssertionError(operationException);
                }
            }
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new AssertionError(noSuchMethodException);
        }
    }

    /**
     * For the customization command a is necessary convert each value to
     * its primitive data type of the method
     * <p>
     * Example if the method parameter types are
     * boolean.class & double.class, and the input values are >
     * new String[]{"true", "10"} it will convert to its correct primitive data type
     * > (boolean.class) true, (double.class) 10.00
     *
     * @param strings array of strings to convert
     * @param method  customization method
     * @return converted array of primitive types
     */
    public static Object[] arrayToPrimitive(String[] strings, Method method) {
        Class<?>[] methodParameterTypes = method.getParameterTypes();

        Object[] newArray = new Object[methodParameterTypes.length];
        for (int i = 0; i < methodParameterTypes.length; i++) {
            if (methodParameterTypes[i] == boolean.class) newArray[i] = Boolean.parseBoolean(strings[i]);
            else if (methodParameterTypes[i] == float.class)
                newArray[i] = NumberUtils.createNumber(strings[i]).floatValue();
            else if (methodParameterTypes[i] == double.class)
                newArray[i] = NumberUtils.createNumber(strings[i]).doubleValue();
            else if (methodParameterTypes[i] == int.class)
                newArray[i] = NumberUtils.createNumber(strings[i]).intValue();
            else if (methodParameterTypes[i] == short.class)
                newArray[i] = NumberUtils.createNumber(strings[i]).shortValue();
            else if (methodParameterTypes[i] == long.class)
                newArray[i] = NumberUtils.createNumber(strings[i]).longValue();
            else newArray[i] = String.valueOf(strings[i]);
        }
        return newArray;
    }

    /**
     * Change/updates the npc customization
     *
     * @param name   the method name
     * @param entity the entity
     * @param values the method values
     * @throws Exception if method could not be invoked
     */
    public void invokeMethod(String name, Object entity, Object[] values) throws Exception {
        if (!customizationMethods.containsKey(name)) return;

        Method method = customizationMethods.get(name);
        if (this == NPCType.SHEEP && name.equalsIgnoreCase("setColor")) {
            method.invoke(entity, ClazzCache.ENUM_COLOR_CLASS.getCacheClass().getField(String.valueOf(values[0])).get(null));
        } else method.invoke(entity, values);
    }

    public static NPCType fromString(String text) {
        for (NPCType b : NPCType.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}