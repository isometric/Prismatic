package com.github.andromeduck.prismatic.graphics.platforms;

import com.github.andromeduck.prismatic.graphics.blocks.BasicBlock;
import com.github.andromeduck.prismatic.graphics.blocks.Drawable;

public class DebugAxis extends Platform {

    @Override
    public void init() {
        Drawable cube0 = new BasicBlock();
        cube0.setColor(new float[]{.1f, .1f, .1f});
        cube0.setPosition(new float[]{0,0,0});
        cube0.setScale(new float[]{0.2f,0.2f,0.2f});
        blocks.add(cube0);

        Drawable cube1 = new BasicBlock();
        cube1.setPosition(new float[]{2,0,0});
        cube1.setColor(new float[]{0.9f, 0.2f, 0.2f});
        cube1.setScale(new float[]{0.1f,0.1f,0.1f});
        blocks.add(cube1);

        Drawable cube2 = new BasicBlock();
        cube2.setColor(new float[]{.2f, .9f, .2f});
        cube2.setPosition(new float[]{0,2,0});
        cube2.setScale(new float[]{0.1f,0.1f,0.1f});
        blocks.add(cube2);

        Drawable cube3 = new BasicBlock();
        cube3.setColor(new float[]{.2f, .2f, .9f});
        cube3.setPosition(new float[]{0,0,2});
        cube3.setScale(new float[]{0.1f,0.1f,0.1f});
        blocks.add(cube3);
    }
}
