package Client.AI;

import Client.IClient;
import Common.BotPersonality;

public abstract class AIPersonality implements IClient {

    public final BotPersonality personality;
    protected AiClient ai;

    public AIPersonality(BotPersonality personality) {
        this.personality = personality;
    }

    public void setAi(AiClient ai) {
        this.ai = ai;
    }

    @Override
    public void OnIsFull() {
    }

    @Override
    public void OnAbort() {
    }

    @Override
    public void OnReadyForShips() {
    }
}
