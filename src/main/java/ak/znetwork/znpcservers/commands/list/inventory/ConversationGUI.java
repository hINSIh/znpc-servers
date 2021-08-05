package ak.znetwork.znpcservers.commands.list.inventory;

import ak.znetwork.znpcservers.configuration.ConfigType;
import ak.znetwork.znpcservers.configuration.ConfigValue;
import ak.znetwork.znpcservers.manager.ConfigManager;
import ak.znetwork.znpcservers.npc.conversation.Conversation;
import ak.znetwork.znpcservers.npc.conversation.ConversationKey;
import ak.znetwork.znpcservers.npc.ZNPCAction;
import ak.znetwork.znpcservers.types.ConfigTypes;
import ak.znetwork.znpcservers.user.EventService;
import ak.znetwork.znpcservers.user.ZUser;
import ak.znetwork.znpcservers.utility.Utils;
import ak.znetwork.znpcservers.utility.inventory.ZInventory;
import ak.znetwork.znpcservers.utility.inventory.ZInventoryPage;
import ak.znetwork.znpcservers.utility.itemstack.ItemStackBuilder;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.List;

/**
 * <p>Copyright (c) ZNetwork, 2020.</p>
 *
 * @author ZNetwork
 * @since 2/8/2021
 */
public class ConversationGUI extends ZInventory {
    /**
     * A string whitespace.
     */
    private static final String WHITESPACE = " ";

    /**
     * Creates a new splitter instance for a whitespace (' ').
     */
    private static final Splitter SPACE_SPLITTER = Splitter.on(WHITESPACE);

    /**
     * Creates a new joiner instance for a whitespace (' ').
     */
    private static final Joiner SPACE_JOINER = Joiner.on(WHITESPACE);

    /**
     * Creates a new inventory for the given player.
     *
     * @param player The player to create the inventory for.
     */
    public ConversationGUI(Player player) {
        super(player);
        setCurrentPage(new MainPage(this));
    }

    /**
     * Default/main page for gui.
     */
    static class MainPage extends ZInventoryPage {
        /**
         * Creates a new page for the inventory.
         *
         * @param inventory The inventory to create the page for.
         */
        public MainPage(ZInventory inventory) {
            super(inventory, "Conversations", 5);
        }

        @Override
        public void update() {
            for (int i = 0; i < ConfigTypes.NPC_CONVERSATIONS.size(); i++) {
                Conversation conversation = ConfigTypes.NPC_CONVERSATIONS.get(i);
                addItem(ItemStackBuilder.forMaterial(Material.PAPER)
                                .setName(ChatColor.GREEN + conversation.getName())
                                .setLore("&7this conversation has &b" + conversation.getTexts().size() + " &7texts,"
                                        , "&7it will activate when a player is on a &b" + conversation.getRadius() + "x" + conversation.getRadius() + " &7radius,"
                                        , "&7or when a player interacts with an npc."
                                        , "&7when the conversation is finish, there is a &b" + conversation.getDelay() + "s &7delay to start again."
                                        , "&f&lUSES"
                                        , " &bLeft-click &7to manage texts.", " &bRight-click &7to add a new text."
                                        , " &bQ &7to change the radius.", " &bMiddle-click &7to change the cooldown.")
                                .build(),
                        i, clickEvent -> {
                            if (clickEvent.getClick() == ClickType.DROP) {
                                Utils.sendTitle(getPlayer(), "&b&lCHANGE RADIUS", "&7Type the new radius...");
                                EventService.addService(ZUser.find(getPlayer()), AsyncPlayerChatEvent.class,
                                        event -> {
                                            if (!ConfigTypes.NPC_CONVERSATIONS.contains(conversation)) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.NO_CONVERSATION_FOUND);
                                                return;
                                            }
                                            Integer radius = Ints.tryParse(event.getMessage());
                                            if (radius == null) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_NUMBER);
                                                return;
                                            }
                                            // delay must be >0
                                            if (radius < 0) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_NUMBER);
                                                return;
                                            }
                                            conversation.setRadius(radius);
                                            ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                        }
                                );
                            } else if (clickEvent.isRightClick()) {
                                Utils.sendTitle(getPlayer(), "&e&lADD LINE", "&7Type the new line...");
                                EventService.addService(ZUser.find(getPlayer()), AsyncPlayerChatEvent.class,
                                        event -> {
                                            if (!ConfigTypes.NPC_CONVERSATIONS.contains(conversation)) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.NO_CONVERSATION_FOUND);
                                                return;
                                            }
                                            conversation.getTexts().add(new ConversationKey(event.getMessage()));
                                            ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                        }
                                );
                            } else if (clickEvent.isLeftClick()) {
                                getPlayer().openInventory(getInventory().build(new EditConversationPage(getInventory(), conversation)));
                            } else if (clickEvent.getClick() == ClickType.MIDDLE) {
                                Utils.sendTitle(getPlayer(), "&6&lCHANGE COOLDOWN", "&7Type the new cooldown...");
                                EventService.addService(ZUser.find(getPlayer()), AsyncPlayerChatEvent.class,
                                        event -> {
                                            if (!ConfigTypes.NPC_CONVERSATIONS.contains(conversation)) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.NO_CONVERSATION_FOUND);
                                                return;
                                            }
                                            Integer cooldown = Ints.tryParse(event.getMessage());
                                            if (cooldown == null) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_NUMBER);
                                                return;
                                            }
                                            // cooldown must be >0
                                            if (cooldown < 0) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_NUMBER);
                                                return;
                                            }
                                            conversation.setDelay(cooldown);
                                            ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                        }
                                );
                            }
                        }
                );
            }
        }
    }

    /**
     * Manages the conversations.
     */
    static class EditConversationPage extends ZInventoryPage {
        /**
         * The conversation key to edit.
         */
        private final Conversation conversation;

        /**
         * Creates a new page for the inventory.
         *
         * @param inventory    The inventory to create the page for.
         * @param conversation The conversation to edit in the page.
         */
        public EditConversationPage(ZInventory inventory,
                                    Conversation conversation) {
            super(inventory, "Editing conversation " + conversation.getName(), 5);
            this.conversation = conversation;
        }

        @Override
        public void update() {
            for (int i = 0; i < conversation.getTexts().size(); i++) {
                ConversationKey conversationKey = conversation.getTexts().get(i);
                addItem(ItemStackBuilder.forMaterial(Material.NAME_TAG)
                                .setName(ChatColor.AQUA + conversationKey.getFirstTextFormatted() + "....")
                                .setLore("&7this conversation text has a delay of &b" + conversationKey.getDelay() + "s &7to be executed,"
                                        , "&7the sound for the text is &b" + (conversationKey.getSoundName() == null ? "NONE" : conversationKey.getSoundName()) + "&7,"
                                        , "&7before sending the text there is a delay of &b" + conversationKey.getDelay() + "s"
                                        , "&7the index for the text is &b" + i + "&7,"
                                        , "&7and the conversation has currently &b" + conversationKey.getActions().size() + " actions&7."
                                        , "&f&lUSES"
                                        , " &bLeft-click &7to change the position.", " &bRight-click &7to remove text."
                                        , " &bShift-click &7to change the sound.", " &bMiddle-click &7to change the delay.",
                                        " &bQ &7to manage actions.")
                                .build(),
                        i, clickEvent -> {
                            if (clickEvent.isShiftClick()) {
                                Utils.sendTitle(getPlayer(), "&c&lCHANGE SOUND", "&7Type the new sound...");
                                EventService.addService(ZUser.find(getPlayer()), AsyncPlayerChatEvent.class,
                                        event -> {
                                            if (!ConfigTypes.NPC_CONVERSATIONS.contains(conversation) ||
                                                    !conversation.getTexts().contains(conversationKey)) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.NO_CONVERSATION_FOUND);
                                                return;
                                            }
                                            String sound = event.getMessage().trim();
                                            conversationKey.setSoundName(sound);
                                            ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                        }
                                );
                            } else if (clickEvent.isLeftClick()) {
                                Utils.sendTitle(getPlayer(), "&e&lCHANGE POSITION &a>=" + 0 + "&c<=" + conversation.getTexts().size(),
                                        "&7Type the new position...");
                                EventService.addService(ZUser.find(getPlayer()), AsyncPlayerChatEvent.class,
                                        event -> {
                                            if (!ConfigTypes.NPC_CONVERSATIONS.contains(conversation) ||
                                                    !conversation.getTexts().contains(conversationKey)) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.NO_CONVERSATION_FOUND);
                                                return;
                                            }
                                            Integer position = Ints.tryParse(event.getMessage());
                                            if (position == null) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_NUMBER);
                                                return;
                                            }
                                            // check if position is within conversation texts size
                                            if (position < 0 || position > conversation.getTexts().size()-1) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_SIZE);
                                                return;
                                            }
                                            Collections.swap(conversation.getTexts(), conversation.getTexts().indexOf(conversationKey), position);
                                            ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                        }
                                );
                            } else if (clickEvent.isRightClick()) {
                                conversation.getTexts().remove(conversationKey);
                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                // update gui
                                openInventory();
                            } else if (clickEvent.getClick() == ClickType.MIDDLE) {
                                Utils.sendTitle(getPlayer(), "&d&lCHANGE DELAY", "&7Type the new delay...");
                                EventService.addService(ZUser.find(getPlayer()), AsyncPlayerChatEvent.class,
                                        event -> {
                                            if (!ConfigTypes.NPC_CONVERSATIONS.contains(conversation) ||
                                                    !conversation.getTexts().contains(conversationKey)) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.NO_CONVERSATION_FOUND);
                                                return;
                                            }
                                            Integer delay = Ints.tryParse(event.getMessage());
                                            if (delay == null) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_NUMBER);
                                                return;
                                            }
                                            // delay must be >0
                                            if (delay < 0) {
                                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INVALID_NUMBER);
                                                return;
                                            }
                                            conversationKey.setDelay(delay);
                                            ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                        }
                                );
                            } else if (clickEvent.getClick() == ClickType.DROP) {
                                getPlayer().openInventory(getInventory().build(new ActionManagementPage(getInventory(), conversation, conversationKey)));
                            }
                        }
                );
            }
        }
    }

    /**
     * Manages a conversation actions.
     */
    static class ActionManagementPage extends ZInventoryPage {
        /**
         * The conversation.
         */
        private final Conversation conversation;

        /**
         * The conversation text to edit.
         */
        private final ConversationKey conversationKey;

        /**
         * Creates a new page for the inventory.
         *
         * @param inventory       The inventory to create the page for.
         * @param conversation    The conversation.
         * @param conversationKey The conversation text to edit in the page.
         */
        public ActionManagementPage(ZInventory inventory,
                                    Conversation conversation,
                                    ConversationKey conversationKey) {
            super(inventory, "Editing " + conversationKey.getFirstTextFormatted(), 5);
            this.conversation = conversation;
            this.conversationKey = conversationKey;
        }

        @Override
        public void update() {
            for (int i = 0; i < conversationKey.getActions().size(); i++) {
                ZNPCAction znpcAction = conversationKey.getActions().get(i);
                addItem(ItemStackBuilder.forMaterial(Material.ANVIL)
                                .setName(ChatColor.AQUA + znpcAction.getAction().substring(0, Math.min(znpcAction.getAction().length(), 24)) + "....")
                                .setLore("&7this action type is &b" + znpcAction.getActionType()
                                        , "&f&lUSES"
                                        , " &bRight-click &7to remove text.")
                                .build(),
                        i, clickEvent -> {
                            if (clickEvent.isRightClick()) {
                                conversationKey.getActions().remove(znpcAction);
                                ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                openInventory();
                            }
                        }
                );
            }
            // item for creating a new action for the conversation
            addItem(ItemStackBuilder.forMaterial(Material.EMERALD)
                            .setName(ChatColor.AQUA + "ADD A NEW ACTION")
                            .setLore("&7click here...")
                            .build(),
                    (getRows() * 9) - 5, clickEvent -> {
                        Utils.sendTitle(getPlayer(), "&d&lADD ACTION", "&7Type the action...");
                        EventService.addService(ZUser.find(getPlayer()), AsyncPlayerChatEvent.class,
                                event -> {
                                    if (!ConfigTypes.NPC_CONVERSATIONS.contains(conversation) ||
                                            !conversation.getTexts().contains(conversationKey)) {
                                        ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.NO_CONVERSATION_FOUND);
                                        return;
                                    }
                                    List<String> stringList = SPACE_SPLITTER.splitToList(event.getMessage());
                                    if (stringList.size() < 2) {
                                        ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.INCORRECT_USAGE);
                                        return;
                                    }
                                    conversationKey.getActions().add(new ZNPCAction(stringList.get(0).toUpperCase(), SPACE_JOINER.join(Iterables.skip(stringList, 1))));
                                    ConfigManager.getByType(ConfigType.MESSAGES).sendMessage(getPlayer(), ConfigValue.SUCCESS);
                                }
                        );
                    }
            );
        }
    }
}