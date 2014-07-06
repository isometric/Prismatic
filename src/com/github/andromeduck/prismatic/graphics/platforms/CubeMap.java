package com.github.andromeduck.prismatic.graphics.platforms;

import com.github.andromeduck.prismatic.graphics.blocks.Cube;
import com.github.andromeduck.prismatic.graphics.blocks.Drawable;


public class CubeMap extends Platform {

    @Override
    public void init() {
        //fake skybox made out of six cubes
        float[] skyboxColor = {0.5f,0.5f,0.5f};
        float skyboxScale = 30f;

        Drawable cube0 = new Cube();
        cube0.setColor(skyboxColor);
        cube0.setPosition(new float[]{-skyboxScale, 0, 0});
        cube0.setScale(skyboxScale);
        blocks.add(cube0);

        Drawable cube1 = new Cube();
        cube1.setColor(skyboxColor);
        cube1.setPosition(new float[]{skyboxScale, 0, 0});
        cube1.setScale(skyboxScale);
        blocks.add(cube1);

        Drawable cube2 = new Cube();
        cube2.setColor(skyboxColor);
        cube2.setPosition(new float[]{0, -skyboxScale, 0});
        cube2.setScale(skyboxScale);
        blocks.add(cube2);

        Drawable cube3 = new Cube();
        cube3.setColor(skyboxColor);
        cube3.setPosition(new float[]{0, skyboxScale, 0});
        cube3.setScale(skyboxScale);
        blocks.add(cube3);

        Drawable cube4 = new Cube();
        cube4.setColor(skyboxColor);
        cube4.setPosition(new float[]{0, 0, -skyboxScale});
        cube4.setScale(skyboxScale);
        blocks.add(cube4);

        Drawable cube5 = new Cube();
        cube5.setColor(skyboxColor);
        cube5.setPosition(new float[]{0, 0, skyboxScale});
        cube5.setScale(skyboxScale);
        blocks.add(cube5);
    }
}
