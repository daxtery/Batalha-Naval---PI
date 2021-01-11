package Client.AI.Personality;

import Client.AI.AiClient;
import Common.BotPersonality;
import Common.Network;
import javafx.util.Pair;
import util.Point;

public interface AIPersonality {

    BotPersonality getPersonality();

    String introductionMessage();

    void onCanStart(Network.StartGameResponse startGameResponse, AiClient aiClient);

    void onWhoseTurn(Network.WhoseTurnResponse whoseTurnResponse, AiClient aiClient);

    void onYourBoardToPaint(Network.YourBoardResponse object, AiClient aiClient);

    void onEnemyBoardToPaint(Network.EnemyBoardResponse object, AiClient aiClient);

    void onAnAttackResponse(Network.AnAttackResponse object, AiClient aiClient);

    Pair<Integer, Point> onYourTurn(AiClient aiClient);

    void onYouDead(AiClient aiClient);

    void onPlayerDied(Network.PlayerDiedResponse object, AiClient aiClient);

    void onYouWon(AiClient aiClient);

    void onChatMessage(Network.ChatMessageResponse object, AiClient aiClient);
}
