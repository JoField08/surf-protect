package dev.slne.protect.bukkit.gui.protection.flags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.surf.gui.api.SurfGui;
import dev.slne.surf.gui.api.chest.SurfChestGui;
import dev.slne.surf.gui.api.utils.ItemUtils;
import dev.slne.surf.gui.api.utils.pagination.PageController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionFlagsGui extends SurfChestGui {

    private final PaginatedPane paginatedPane;
    private final StaticPane navigationPane;

    /**
     * Creates a new protection flags gui
     *
     * @param region the region
     */
    public ProtectionFlagsGui(SurfGui parent, ProtectedRegion region, Player viewingPlayer) {
        super(parent, 5, Component.text(parent.getGui().getTitle() + " - Flags"), viewingPlayer);

        List<GuiItem> buttons = new ArrayList<>();

        for (ProtectionFlagsMap map : ProtectionFlagsMap.values()) {
            StateFlag flag = map.getFlag();
            State oldState = region.getFlag(flag);

            buttons.add(new ToggleButton(map, oldState, newState -> {

                // Quick fix that enables TNT when toggeling other explosions
                if (flag.equals(Flags.OTHER_EXPLOSION)) {
                    region.setFlag(Flags.TNT, newState);
                }

                region.setFlag(flag, newState);
                update();
            }));
        }

        paginatedPane = new PaginatedPane(0, 1, 9, 3);
        paginatedPane.populateWithGuiItems(buttons);

        navigationPane = new StaticPane(0, 4, 9, 1);

        addPane(paginatedPane);
        addPane(navigationPane);
    }

    @Override
    public void update() {
        ItemStack backgroundItem = ItemUtils.paneItem();
        navigationPane.addItem(
                PageController.PREVIOUS.toGuiItem(this, Component.text("Zurück", NamedTextColor.GREEN), paginatedPane,
                        backgroundItem),
                0, 0);
        navigationPane.addItem(
                PageController.NEXT.toGuiItem(this, Component.text("Weiter", NamedTextColor.GREEN), paginatedPane,
                        backgroundItem),
                8, 0);

        super.update();
    }
}