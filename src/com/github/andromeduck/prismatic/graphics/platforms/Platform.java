package com.github.andromeduck.prismatic.graphics.platforms;

import com.github.andromeduck.prismatic.graphics.blocks.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by t-jadeng on 7/1/2014.
 */
public abstract class Platform {

    public final List<Drawable> blocks = new ArrayList<Drawable>();
    public final List<Drawable> decorations = new ArrayList<Drawable>();

    public List<Drawable> getDrawables() {
        List<Drawable> drawables = new ArrayList<Drawable>();
        drawables.addAll(blocks);
        drawables.addAll(decorations);
        return drawables;
    }


    public void update() {
    }

    ;

}
