package dev.xf3d3.ultimatereports.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Rewards {

    public static final String REWARDS_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃    UltimateReports Rewards   ┃
            ┃      Developed by xF3d3      ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
            ┣╸ Information: https://modrinth.com/plugin/ultimate-reports
            ┗╸ Documentation: https://ultimatereports.gitbook.io/ultimatereports-docs/documentation/basics/rewards""";

    @Comment("The reward item. You can specify material, amount, and customModelData")
    private RewardItem rewardItem = new RewardItem(Material.DIAMOND, 2, 0);

    @Comment("Commands executed from the console when a player claims a reward")
    private List<String> rewardCommands = List.of(
            "give %PLAYER% diamond 1",
            "tellraw %PLAYER% {\"text\":\"You also received an extra diamond\", \"color\":\"blue\", \"bold\":true}"
    );

    @Getter
    @Configuration
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RewardItem {
        private Material material;
        private int amount;
        private int customModelData;


        public ItemStack toItemStack() {
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {

                if (customModelData != 0) {
                    meta.setCustomModelData(customModelData);
                }


                item.setItemMeta(meta);
            }
            return item;
        }
    }

}