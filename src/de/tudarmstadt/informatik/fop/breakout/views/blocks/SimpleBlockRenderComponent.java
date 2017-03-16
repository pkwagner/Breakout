package de.tudarmstadt.informatik.fop.breakout.views.blocks;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class SimpleBlockRenderComponent extends AbstractBlockRenderComponent {

    public SimpleBlockRenderComponent(int remainingHits) throws SlickException {
        super(new Image(getImagePath(remainingHits)));

    }

    private static String getImagePath(int remainingHits) {
        switch (remainingHits) {
            case 1:
                return GameParameters.BLOCK_1_IMAGE;
            case 2:
                return GameParameters.BLOCK_2_IMAGE;
            case 3:
                return GameParameters.BLOCK_3_IMAGE;
            default:
                return GameParameters.BLOCK_1_IMAGE;
        }
    }
}
