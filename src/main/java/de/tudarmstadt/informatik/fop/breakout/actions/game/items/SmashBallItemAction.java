package de.tudarmstadt.informatik.fop.breakout.actions.game.items;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import de.tudarmstadt.informatik.fop.breakout.models.game.BallModel;
import de.tudarmstadt.informatik.fop.breakout.models.game.PlayerModel;
import de.tudarmstadt.informatik.fop.breakout.states.GameplayState;
import org.newdawn.slick.state.StateBasedGame;

import java.util.List;

/**
 * Triggers the smash attribute of all balls in game
 */
public class SmashBallItemAction extends AbstractItemAction {

    private List<BallModel> allBalls;

    @Override
    protected void init(StateBasedGame stateBasedGame, PlayerModel catchingPlayer) {
        allBalls = ((GameplayState) stateBasedGame.getState(GameParameters.GAMEPLAY_STATE)).getBalls();
    }

    @Override
    public void onEnable() {
        allBalls.forEach(ball -> ball.setSmashMode(true));
    }

    @Override
    public void onDisable() {
        allBalls.forEach(ball -> ball.setSmashMode(false));
    }
}
