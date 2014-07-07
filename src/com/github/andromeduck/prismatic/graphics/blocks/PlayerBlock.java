package com.github.andromeduck.prismatic.graphics.blocks;

import com.github.andromeduck.prismatic.graphics.platforms.Platform;

import java.util.List;

public class PlayerBlock extends BasicBlock {
    PlayerState state = PlayerState.STABLE;
    Platform currentPlatform = null;

    public PlayerBlock(){
        super();

        setScale(new float[]{1,2,1});
    }

    public void update(float deltaTime, float[] inputDir, List<Platform> platforms){
        // Broad phase collision on levels
        float[] playerSphere = getBoundingSphere();
        for (Platform p : platforms){
            float[] platformSphere = p.getBoundingSphere();
            if (colissionDetected(playerSphere, platformSphere)) {
                float[] plocalPlayerSphere = {
                        playerSphere[0] - platformSphere[0],
                        playerSphere[1] - platformSphere[1],
                        playerSphere[2] - platformSphere[2],
                        playerSphere[3]};

                // TODO: test algorithim and handle events
                for (Drawable d : p.getBlocks()){
                    if (colissionDetected(plocalPlayerSphere, d.getBoundingSphere())){
                        // TODO: box collision
                        setColor(getPosition());
                        switch (state) {
                            case STABLE: {

                            }
                            case FALLING: {

                            }
                            case ROLLING: {

                            }
                        }
                    }

                }

                //TODO: implement collision block vs decoration
                for (Drawable d : p.getDecorations()){
                    // TODO: create decoration class with onCollision method
                    // if collision then d.onCollision(this) else nothing;
                }
            }

            // TODO: implement roll mechanic, this is for testing purposes only
            float[] playerPos = getPosition();
            playerPos[0] += inputDir[0];
            playerPos[1] += inputDir[1];
            playerPos[2] += inputDir[2];
            setPosition(playerPos);
        }
    }


    boolean colissionDetected(float[] a, float[] b){
        float dist = (float) Math.sqrt(
                Math.pow(a[0] - b[0],2.f) +
                Math.pow(a[1] - b[1],2.f) +
                Math.pow(a[2] - b[2],2.f));

        return (dist <= (a[3] + b[3]));
    }



    public enum PlayerState {
        STABLE,
        FALLING,
        ROLLING,

    }
}
