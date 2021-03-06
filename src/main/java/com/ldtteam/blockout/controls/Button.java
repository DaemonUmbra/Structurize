package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Base button class.
 * Has a handler for when the button is clicked.
 */
public class Button extends Pane
{
    protected ButtonHandler handler;
    protected String label;

    /**
     * Default constructor.
     */
    public Button()
    {
        super();
    }

    /**
     * Constructor used when loading from xml.
     *
     * @param params PaneParams from xml file.
     */
    public Button(@NotNull final PaneParams params)
    {
        super(params);
        label = params.getLocalizedStringAttribute("label", label);
    }

    /**
     * Button textContent getter.
     *
     * @return button textContent.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Button textContent setter.
     *
     * @param s new textContent.
     */
    public void setLabel(final String s)
    {
        label = s;
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final ButtonHandler h)
    {
        handler = h;
    }

    /**
     * Play click sound and find the proper handler.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left
     * @param my mouse Y coordinate, relative to Pane's top-left
     */
    @Override
    public boolean handleClick(final double mx, final double my)
    {
        mc.getSoundHandler().play(SimpleSound.music(SoundEvents.UI_BUTTON_CLICK));

        ButtonHandler delegatedHandler = handler;

        if (delegatedHandler == null)
        {
            // If we do not have a designated handler, find the closest ancestor that is a Handler
            for (Pane p = parent; p != null; p = p.getParent())
            {
                if (p instanceof ButtonHandler)
                {
                    delegatedHandler = (ButtonHandler) p;
                    break;
                }
            }
        }

        if (delegatedHandler != null)
        {
            delegatedHandler.onButtonClicked(this);
        }
        return true;
    }
}
