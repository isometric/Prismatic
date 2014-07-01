package com.github.andromeduck.prismatic.graphics.platforms;

import com.github.andromeduck.prismatic.graphics.blocks.Cube;
import com.github.andromeduck.prismatic.graphics.blocks.Drawable;

public class DebugAxis extends Platform {

    public DebugAxis() {
        Drawable cube0 = new Cube();
        cube0.setColor(.1f, .1f, .1f);
        cube0.setPosition(0, 0, 0);
        cube0.setScale(0.2f);
        blocks.add(cube0);

        Drawable cube1 = new Cube();
        cube1.setPosition(4, 0, 0);
        cube1.setColor(0.9f, 0.2f, 0.2f);
        cube1.setScale(0.1f);
        blocks.add(cube1);

        Drawable cube2 = new Cube();
        cube2.setColor(.2f, .9f, .2f);
        cube2.setPosition(0, 4, 0);
        cube2.setScale(0.1f);
        blocks.add(cube2);

        Drawable cube3 = new Cube();
        cube3.setColor(.2f, .2f, .9f);
        cube3.setPosition(0, 0, 4);
        cube3.setScale(0.1f);
        blocks.add(cube3);
    }
}
