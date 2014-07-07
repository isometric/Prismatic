package com.github.andromeduck.prismatic.graphics.platforms;

import com.github.andromeduck.prismatic.graphics.blocks.BasicBlock;
import com.github.andromeduck.prismatic.graphics.blocks.Drawable;


public class CubeMap extends Platform {

    @Override
    public void init() {
        //fake skybox made out of six cubes
        float[] skyboxColor = {0.5f,0.5f,0.5f};
        float skyboxSize = 30f;
        float[] skyboxScale = {skyboxSize,skyboxSize,skyboxSize};

        Drawable cube0 = new BasicBlock();
        cube0.setColor(skyboxColor);
        cube0.setPosition(new float[]{-skyboxSize, 0, 0});
        cube0.setScale(skyboxScale);
        blocks.add(cube0);

        Drawable cube1 = new BasicBlock();
        cube1.setColor(skyboxColor);
        cube1.setPosition(new float[]{skyboxSize, 0, 0});
        cube1.setScale(skyboxScale);
        blocks.add(cube1);

        Drawable cube2 = new BasicBlock();
        cube2.setColor(skyboxColor);
        cube2.setPosition(new float[]{0, -skyboxSize, 0});
        cube2.setScale(skyboxScale);
        blocks.add(cube2);

        Drawable cube3 = new BasicBlock();
        cube3.setColor(skyboxColor);
        cube3.setPosition(new float[]{0, skyboxSize, 0});
        cube3.setScale(skyboxScale);
        blocks.add(cube3);

        Drawable cube4 = new BasicBlock();
        cube4.setColor(skyboxColor);
        cube4.setPosition(new float[]{0, 0, -skyboxSize});
        cube4.setScale(skyboxScale);
        blocks.add(cube4);

        Drawable cube5 = new BasicBlock();
        cube5.setColor(skyboxColor);
        cube5.setPosition(new float[]{0, 0, skyboxSize});
        cube5.setScale(skyboxScale);
        blocks.add(cube5);
    }
}
