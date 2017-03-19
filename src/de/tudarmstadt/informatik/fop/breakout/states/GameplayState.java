package de.tudarmstadt.informatik.fop.breakout.states;

import de.tudarmstadt.informatik.fop.breakout.actions.PauseToggleAction;
import de.tudarmstadt.informatik.fop.breakout.actions.StartGameAction;
import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import de.tudarmstadt.informatik.fop.breakout.controllers.*;
import de.tudarmstadt.informatik.fop.breakout.events.KeyPressedEvent;
import de.tudarmstadt.informatik.fop.breakout.factories.BorderFactory;
import de.tudarmstadt.informatik.fop.breakout.models.*;
import de.tudarmstadt.informatik.fop.breakout.models.gui.BackButton;
import de.tudarmstadt.informatik.fop.breakout.ui.Breakout;
import de.tudarmstadt.informatik.fop.breakout.views.*;

import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Showing the actual game where you can start playing
 */
public class GameplayState extends AbstractGameState {

    private final Logger logger = LogManager.getLogger();

    private final RamBlockMovementController ramBlockMovementController = new RamBlockMovementController();
    private final List<BallModel> balls = new ArrayList<>();

    private GameContainer gameContainer;
    private StateBasedGame stateBasedGame;

    private MapController mapController;
    private PlayerModel player;
    private ClockModel clock;
    private ClockController clockController;

    private float gameSpeedFactor = 1;
    private float gameSpeedFactorGoal = 1;
    private int ballIdCounter = 0;

    public GameplayState(int id) throws SlickException {
        // Load dynamic background
        super(id, new Animation(new SpriteSheet(GameParameters.BACKGROUND_SPRITESHEET
                , GameParameters.WINDOW_WIDTH, GameParameters.WINDOW_HEIGHT), 70));
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
        if (isTesting()) {
            return;
        }

        this.gameContainer = gameContainer;
        this.stateBasedGame = stateBasedGame;

        //load sound effects
        Breakout breakout = (Breakout) stateBasedGame;
        SoundController soundController = breakout.getSoundController();
        soundController.load(SoundType.BLOCK_HIT, SoundType.ITEM_PICKUP, SoundType.STICK_HIT);

        // Basic player implementation
        String playerId = GameParameters.PLAYER_ID + "_" + 0;
        player = new PlayerModel(playerId);
        PlayerStatsRenderComponent playerView = new PlayerStatsRenderComponent(playerId + GameParameters.EXT_VIEW, false);
        player.addComponent(playerView);
        playerView.init();
    }

    /**
     * Load the level with the given id and initialize all related component
     *
     * @param mapId map id
     * @throws SlickException happens if images cannot be loaded
     */
    private void loadLevel(int mapId) throws SlickException {
        // TODO Move some parts to 'init(...)' to avoid double calculations
        // Pause game
        gameContainer.setPaused(true);

        // Reset speed
        gameSpeedFactorGoal = 1;

        // Delete all previous entities
        clearEntities();

        // TODO Change default positions
        // Add stick & ball to state
        balls.clear();
        addStick(stateBasedGame, gameContainer.getWidth() / 2);
        addBall(stateBasedGame);

        mapController = new MapController(stateBasedGame, this);
        mapController.loadMap(mapId);
        addBorders();

        addEntity(clock);
        addEntity(player);

        addStartGameEntity(gameContainer.getWidth() / 2);
        addPauseEntities(gameContainer);
    }

    /**
     * Starts a new game.
     *
     * @param player the player model
     * @throws SlickException if images cannot be loaded
     */
    private void newGame(PlayerModel player) throws SlickException {
        gameContainer.setPaused(true);

        // Reset player
        player.reset();

        // Initialize clock
        clock = new ClockModel(GameParameters.STOP_WATCH_ID);
        clockController = new ClockController(GameParameters.STOP_WATCH_ID + GameParameters.EXT_CONTROLLER);
        clock.addComponent(clockController);
        ClockRenderComponent clockView = new ClockRenderComponent(GameParameters.STOP_WATCH_ID + GameParameters.EXT_VIEW);
        clock.addComponent(clockView);
        clockView.init();
        clockController.init(stateBasedGame);

        // Load initial map
        loadLevel(GameParameters.MAP_INITIAL_ID);
    }

    /**
     * Switches to the next level or ends the game if there are no more levels.
     */
    public void nextLevel() {
        int currentMapId = mapController.getMapId();
        if (currentMapId < GameParameters.MAP_COUNT) {
            try {
                loadLevel(++currentMapId);
            } catch (SlickException e) {
                logger.error("Some error occurred while loading map" + currentMapId + ": " + e);
            }
        } else {
            // TODO Victory screen?!
        }
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int delta)
            throws SlickException {
        super.update(gameContainer, stateBasedGame, delta);

        if(!gameContainer.isPaused())ramBlockMovementController.update(this, delta);

        // Check if game speed fade is needed
        if (gameSpeedFactorGoal != gameSpeedFactor) {
            // Update game speed
            if (gameSpeedFactor < gameSpeedFactorGoal)
                gameSpeedFactor += GameParameters.GAME_SLOMO_ANIMATION_SPEED * delta;
            else
                gameSpeedFactor -= GameParameters.GAME_SLOMO_ANIMATION_SPEED * delta;

            // If gameSpeed is near it's goal, abort the animation
            if (Math.abs(gameSpeedFactor - gameSpeedFactorGoal) <= 0.01)
                gameSpeedFactor = gameSpeedFactorGoal;

            // Update pitch
            ((Breakout) stateBasedGame).getSoundController().setMusicPitch(gameSpeedFactor);
        }
    }

    @Override
    public void enter(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
        newGame(player);
    }

    /**
     * Adds a entity showing how to start the game.
     *
     * @param midX the middle x position of the screen
     */
    private void addStartGameEntity(float midX) {
        Entity startGameEntity = new Entity(GameParameters.GAMESTART_ENTITY_ID);
        //center text
        startGameEntity.setPosition(new Vector2f(midX, gameContainer.getHeight() / 2));
        startGameEntity.setSize(new Vector2f(100, 100));
        startGameEntity.addComponent(new StartGameRenderComponent());

        KeyPressedEvent startGameEvent = new KeyPressedEvent(KeyBinding.START_GAME);
        startGameEvent.addAction(new StartGameAction());
        startGameEntity.addComponent(startGameEvent);

        startGameEntity.setPassable(true);
        addEntity(startGameEntity);
    }

    /**
     * Add left, right and top borders.
     */
    private void addBorders() {
        Entity leftBorder = new BorderFactory(GameParameters.BorderType.LEFT).createEntity();
        Entity rightBorder = new BorderFactory(GameParameters.BorderType.RIGHT).createEntity();
        Entity topBorder = new BorderFactory(GameParameters.BorderType.TOP).createEntity();

        addEntity(leftBorder);
        addEntity(rightBorder);
        addEntity(topBorder);
    }

    /**
     * Adds an entity for pausing the game.
     * <p>
     * It will toggle the pause state on keyboard input and will show up an pause-image on pause.
     *
     * @param gameContainer game instance container
     * @throws SlickException if the pause cannot be found
     */
    private void addPauseEntities(GameContainer gameContainer) throws SlickException {
        //show the back to main menu too on pausing the game
        BackButton backButton = new BackButton();
        backButton.setVisible(false);

        Entity pauseImage = new Entity(GameParameters.PAUSE_IMAGE_ID);

        //default hides the entity and make it passable so it won't effect the gameplay
        pauseImage.setVisible(false);


        //center the entity
        pauseImage.setPosition(new Vector2f(gameContainer.getWidth() / 2, gameContainer.getHeight() / 2));

        //view component
        pauseImage.addComponent(new ImageRenderComponent(new Image(GameParameters.PAUSE_IMAGE)));
        pauseImage.setPassable(true);
        //key listener
        Entity pauseEntity = new Entity(GameParameters.PAUSE_ID);
        KeyPressedEvent escapeKeyEvent = new KeyPressedEvent(KeyBinding.PAUSE);
        escapeKeyEvent.addAction(new PauseToggleAction(backButton, pauseImage));
        pauseEntity.addComponent(escapeKeyEvent);

        pauseEntity.setPassable(true);
        addEntity(pauseEntity);
        addEntity(pauseImage);
        addEntity(backButton);
    }

    /**
     * Creates and adds a new ball to the game
     *
     * @param stateBasedGame this game instance
     * @return the created ball
     * @throws SlickException if the image cannot be loaded
     */
    public BallModel addBall(StateBasedGame stateBasedGame) throws SlickException {
        // FORMAT: BALL_[ID][/_VIEW/_CONTROLLER]
        BallModel ballModel = new BallModel(GameParameters.BALL_ID + "_" + ballIdCounter, player);
        BallController ballController = new BallController(GameParameters.BALL_ID + "_" + ballIdCounter + GameParameters.EXT_CONTROLLER);
        ballModel.addComponent(ballController);
        BallRenderComponent ballView = new BallRenderComponent(GameParameters.BALL_ID + "_" + ballIdCounter + GameParameters.EXT_VIEW);
        ballModel.addComponent(ballView);
        balls.add(ballModel);

        ballView.init();
        ballController.init(gameContainer, stateBasedGame);
        addEntity(ballModel);

        ballIdCounter++;

        return ballModel;
    }

    /**
     * Adds a player stick to the game
     *
     * @param stateBasedGame game instance
     * @param position start position
     * @return the created stick
     * @throws SlickException if the stick image cannot be loaded
     */
    private StickModel addStick(StateBasedGame stateBasedGame, int position) throws SlickException {
        StickModel stickModel = new StickModel(player);
        StickController stickController = new StickController(GameParameters.STICK_ID + GameParameters.EXT_CONTROLLER);
        stickModel.addComponent(stickController);
        stickModel.setView(new StickRenderComponent());

        stickController.init(stateBasedGame, position);
        addEntity(stickModel);

        return stickModel;
    }

    /**
     * @return the shared ram block movement controller
     */
    public RamBlockMovementController getRBMC(){
        return ramBlockMovementController;
    }

    /**
     * Get all playing player. In case of multiplayer this list will be bigger than 1.
     *
     * @return all currently playing players
     */
    public List<PlayerModel> getPlayers() {
        return Arrays.asList(new PlayerModel[]{player});
    }

    /**
     * @return the shared map controller
     */
    public MapController getMapController() {
        return mapController;
    }

    /**
     * Gets the current game speed which can be slower and faster than the default.
     *
     * @return < 1f slower >1f faster
     */
    public float getGameSpeedFactor() {
        return gameSpeedFactor;
    }

    /**
     * Sets the current game speed which can be slower and faster than the default.
     *
     * @param gameSpeedFactor < 1f slower >1f faster
     */
    public void setGameSpeedFactor(float gameSpeedFactor) {
        this.gameSpeedFactorGoal = gameSpeedFactor;
    }

    /**
     * @return all balls that are ingame.
     */
    public List<BallModel> getBalls() {
        return balls;
    }

    /**
     * @return shared clock controller
     */
    public ClockController getClockController() {
        return clockController;
    }
}
