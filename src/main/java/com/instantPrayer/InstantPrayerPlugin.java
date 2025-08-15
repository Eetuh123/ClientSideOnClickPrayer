package com.instantPrayer;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.MenuAction;
import net.runelite.api.SpriteID;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.client.game.SpriteManager;

@Slf4j
@PluginDescriptor(
        name = "Instant Overhead",
        description = "Shows the overhead prayer icon instantly on your client when activated, instead of waiting for the next tick."
)
public class InstantPrayerPlugin extends Plugin
{
    private int lastClickTick = -1;
    private boolean intentActive = false;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private InstantPrayerOverlay overlay;

    @Inject
    private SpriteManager spriteManager;

    private boolean drawMarker = false;
    private Integer overheadFrame = null;

    // Cached icon
    private BufferedImage cachedIcon = null;

    public boolean shouldDrawMarker()
    {
        return drawMarker;
    }

    public Integer getOverheadFrame()
    {
        return overheadFrame;
    }

    public BufferedImage getCachedIcon()
    {
        return cachedIcon;
    }

    // Over Head Prayer indexes
    private static final Map<String, Integer> OVERHEAD_FRAMES = new HashMap<>();
    static
    {
        OVERHEAD_FRAMES.put("Protect from Melee", 0);
        OVERHEAD_FRAMES.put("Protect from Missiles", 1);
        OVERHEAD_FRAMES.put("Protect from Magic", 2);
        OVERHEAD_FRAMES.put("Retribution", 3);
        OVERHEAD_FRAMES.put("Smite", 4);
        OVERHEAD_FRAMES.put("Redemption", 5);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        drawMarker = false;
        overheadFrame = null;
        cachedIcon = null;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if ((event.getMenuAction() == MenuAction.CC_OP
                || event.getMenuAction() == MenuAction.CC_OP_LOW_PRIORITY)
                && WidgetInfo.TO_GROUP(event.getParam1()) == WidgetID.PRAYER_GROUP_ID)
        {
            String option = event.getMenuOption();
            String cleanTarget = Text.removeTags(event.getMenuTarget());

            Integer frame = OVERHEAD_FRAMES.get(cleanTarget);
            if (frame != null)
            {
                handlePrayer(option, frame);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!intentActive)
        {
            // Hide only when server has actually turned it off
            if (client.getLocalPlayer() != null && client.getLocalPlayer().getOverheadIcon() == null)
            {
                overheadFrame = null;
                drawMarker = false;
                cachedIcon = null;
            }
        }
    }

    private void handlePrayer(String option, int frame)
    {
        int tickNow = client.getTickCount();

        if (option.equalsIgnoreCase("Activate"))
        {
            overheadFrame = frame;
            drawMarker = true;
            intentActive = true;
            lastClickTick = tickNow;

            // Cache the icon immediately
            cachedIcon = spriteManager.getSprite(SpriteID.OVERHEAD_PROTECT_FROM_MELEE, frame);
        }
        else if (option.equalsIgnoreCase("Deactivate"))
        {
            intentActive = false;

            // Only turn off instantly if tick has passed
            if (tickNow > lastClickTick)
            {
                overheadFrame = null;
                drawMarker = false;
                cachedIcon = null;
            }
            else
            {
                // Same tick → wait until GameTick to confirm
                lastClickTick = tickNow;
            }
        }
    }
}
