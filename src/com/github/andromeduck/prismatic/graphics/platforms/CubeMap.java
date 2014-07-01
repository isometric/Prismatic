package com.github.andromeduck.prismatic.graphics.platforms;

import com.github.andromeduck.prismatic.graphics.blocks.Cube;
import com.github.andromeduck.prismatic.graphics.blocks.Drawable;

/**
 * Created by t-jadeng on 7/1/2014.
 */
public class CubeMap extends Platform {

    public CubeMap() {
        //fake skybox made out of six cubes
        float skyboxTone = .5f;
        float skyboxScale = 30f;

        Drawable cube0 = new Cube();
        cube0.setColor(skyboxTone, skyboxTone, skyboxTone);
        cube0.setPosition(-skyboxScale, 0, 0);
        cube0.setScale(skyboxScale);
        blocks.add(cube0);

        Drawable cube1 = new Cube();
        cube1.setColor(skyboxTone, skyboxTone, skyboxTone);
        cube1.setPosition(skyboxScale, 0, 0);
        cube1.setScale(skyboxScale);
        blocks.add(cube1);

        Drawable cube2 = new Cube();
        cube2.setColor(skyboxTone, skyboxTone, skyboxTone);
        cube2.setPosition(0, -skyboxScale, 0);
        cube2.setScale(skyboxScale);
        blocks.add(cube2);

        Drawable cube3 = new Cube();
        cube3.setColor(skyboxTone, skyboxTone, skyboxTone);
        cube3.setPosition(0, skyboxScale, 0);
        cube3.setScale(skyboxScale);
        blocks.add(cube3);

        Drawable cube4 = new Cube();
        cube4.setColor(skyboxTone, skyboxTone, skyboxTone);
        cube4.setPosition(0, 0, -skyboxScale);
        cube4.setScale(skyboxScale);
        blocks.add(cube4);

        Drawable cube5 = new Cube();
        cube5.setColor(skyboxTone, skyboxTone, skyboxTone);
        cube5.setPosition(0, 0, skyboxScale);
        cube5.setScale(skyboxScale);
        blocks.add(cube5);
    }
}
