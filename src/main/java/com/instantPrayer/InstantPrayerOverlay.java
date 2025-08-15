package com.instantPrayer;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class InstantPrayerOverlay extends Overlay
{
    private final Client client;
    private final InstantPrayerPlugin plugin;

    @Inject
    private InstantPrayerOverlay(Client client, InstantPrayerPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        if (!plugin.shouldDrawMarker())
        {
            return null;
        }

        BufferedImage icon = plugin.getCachedIcon();
        if (icon == null)
        {
            return null;
        }

        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return null;
        }

        if (player.getOverheadIcon() != null)
        {
            return null;
        }

        int height = player.getLogicalHeight() + 22;
        Point canvasPoint = Perspective.localToCanvas(client, player.getLocalLocation(), client.getPlane(), height);
        if (canvasPoint == null)
        {
            return null;
        }

        OverlayUtil.renderImageLocation(
                g,
                new Point(canvasPoint.getX() - icon.getWidth() / 2, canvasPoint.getY() - icon.getHeight()),
                icon
        );

        return null;
    }
}
