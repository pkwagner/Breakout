package de.tudarmstadt.informatik.fop.breakout.controllers.blocks;

import de.tudarmstadt.informatik.fop.breakout.models.blocks.AbstractBlockModel;

import eea.engine.component.Component;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;

public class AbstractBlockController extends Component {

    public AbstractBlockController(String componentID) {
        super(componentID);
    }

    @Override
    public AbstractBlockModel getOwnerEntity() {
        return (AbstractBlockModel) super.getOwnerEntity();
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int delta) {

    }
}
