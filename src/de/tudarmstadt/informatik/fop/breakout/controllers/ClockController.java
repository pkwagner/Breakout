package de.tudarmstadt.informatik.fop.breakout.controllers;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import de.tudarmstadt.informatik.fop.breakout.states.GameplayState;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;
import de.tudarmstadt.informatik.fop.breakout.models.ClockModel;
import eea.engine.component.Component;

import java.util.ArrayList;

public class ClockController extends Component {

	private ClockModel clock;

	private ArrayList<Timeout> timeouts = new ArrayList<>();
	
	public ClockController(String componentID) {
		super(componentID);
	}

    @Override
    public ClockModel getOwnerEntity() {
        return (ClockModel) super.getOwnerEntity();
    }

    public void setOwnerEntity(ClockModel owningEntity) {
        super.setOwnerEntity(owningEntity);
    }

    public void init(StateBasedGame stateBasedGame){
    	clock = getOwnerEntity();
    	int xOffset = 50;
    	int yOffset = 20;
    	clock.setPosition(new Vector2f(stateBasedGame.getContainer().getWidth() - xOffset, yOffset));
    }

	@Override
	public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int delta) {
		if(!clock.isPaused()){
			GameplayState gameplayState = (GameplayState) stateBasedGame.getState(GameParameters.GAMEPLAY_STATE);
			clock.addSeconds(delta * gameplayState.getGameSpeedFactor() / 1000F);

			if (!timeouts.isEmpty() && (timeouts.get(0).getWakeupTime() <= clock.getSeconds())) {
				timeouts.get(0).getCallback().accept((int) timeouts.get(0).getWakeupTime());
				timeouts.remove(0);
			}
		}		
	}

	public void addTimeout(Timeout timeout) {
    	timeout.setStartedAt(clock.getSeconds());
    	timeouts.add(timeout);
    	timeouts.sort(((o1, o2) -> (o1.getWakeupTime() > o2.getWakeupTime()) ? 1 : -1));
	}
}
