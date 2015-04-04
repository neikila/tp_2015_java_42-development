package mechanics;

import Interface.AccountService;
import Interface.GameMechanics;
import Interface.Resource;
import Interface.WebSocketService;
import MBean.AccountServiceController;
import main.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import resource.GameMechanicsSettings;
import resource.LoggerMessages;
import resource.ResourceFactory;
import utils.JsonInterpreterFromRequest;
import utils.TimeHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameMechanicsImpl implements GameMechanics {
    final private Logger logger = LogManager.getLogger(GameMechanics.class.getName());
    final private LoggerMessages loggerMessages = (LoggerMessages) ResourceFactory.instance().getResource("loggerMessages");

    private static final int STEP_TIME = 100;

    final private int gameTime;
    final private int numAmount;

    final private WebSocketService webSocketService;
    final private AccountService accountService;

    final private Map<String, GameSession> nameToGame = new HashMap<>();

    final private Set<GameSession> allSessions = new HashSet<>();

    private String waiter;

    public GameMechanicsImpl(Context context) {
        this.webSocketService = (WebSocketService) context.get(WebSocketService.class);
        this.accountService = (AccountService) context.get(AccountService.class);
        gameTime = ((GameMechanicsSettings)ResourceFactory.instance().getResource("gameMechanicsSettings")).getTimeLimit() * 1000;
        numAmount = ((GameMechanicsSettings)ResourceFactory.instance().getResource("gameMechanicsSettings")).getNumAmount();
    }

    public void addUser(String user) {
        if (waiter != null) {
            logger.info(loggerMessages.secondPlayer());
            logger.info(loggerMessages.startGame());
            starGame(user);
            waiter = null;
        } else {
            logger.info(loggerMessages.firstPlayer());
            waiter = user;
        }
    }

    public void incrementScore(String userName) {
        GameSession myGameSession = nameToGame.get(userName);
        GameUser myUser = myGameSession.getSelf(userName);
        myUser.incrementMyScore();
        GameUser enemyUser = myGameSession.getEnemy(userName);
        enemyUser.incrementEnemyScore();
        webSocketService.notifyMyNewScore(myUser);
        webSocketService.notifyEnemyNewScore(enemyUser);
    }

    public boolean checkSequence(String userName, String sequence) {
        GameSession myGameSession = nameToGame.get(userName);
        GameUser myUser = myGameSession.getSelf(userName);
        boolean result = myGameSession.isCorrect(userName, JsonInterpreterFromRequest.getJsonFromString(sequence).get("sequence").toString());
        String resultStr = result ? "Correct" : "Failed";
        webSocketService.notifyResult(myUser, resultStr);
        return result;
    }

    @Override
    public void run() {
        while (true) {
            gmStep();
            TimeHelper.sleep(STEP_TIME);
        }
    }

    private void gmStep() {
        for (GameSession session : allSessions) {
            if (session.getSessionTime() > gameTime) {
                finishGame(session);
            }
        }
    }

    private void finishGame(GameSession session) {
        String firstName = session.getFirst().getMyName();
        String secondName = session.getSecond().getMyName();
        int firstResult = session.getWinner();
        int secondResult = -1 * firstResult;
        if (firstResult == 0) {
            logger.info(loggerMessages.draw(), firstName, secondName);
        } else {
            logger.info(loggerMessages.isWinner(), firstResult > 0 ? firstName : secondName);
            logger.info(loggerMessages.isLoser(), firstResult < 0 ? firstName : secondName);
        }
        int deltaScore = firstResult * (7 + Math.abs(session.getFirst().getMyScore() - session.getFirst().getEnemyScore() ) );
        accountService.getUser(firstName).increaseScoreOnValue(deltaScore);
        accountService.getUser(secondName).increaseScoreOnValue(-1 * deltaScore);
        webSocketService.notifyGameOver(session.getFirst(), firstResult);
        webSocketService.notifyGameOver(session.getSecond(), secondResult);
        allSessions.remove(session);
        logger.info(loggerMessages.sessionFinished());
    }

    private void starGame(String first) {
        String second = waiter;
        GameSession gameSession = new GameSession(first, second, numAmount);
        allSessions.add(gameSession);
        nameToGame.put(first, gameSession);
        nameToGame.put(second, gameSession);

        webSocketService.notifyStartGame(gameSession.getSelf(first), gameSession.getSequence());
        webSocketService.notifyStartGame(gameSession.getSelf(second), gameSession.getSequence());
    }
}