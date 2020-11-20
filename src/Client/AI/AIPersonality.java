package Client.AI;

import Client.IClient;
import Common.BotPersonality;

public abstract class AIPersonality implements IClient {

    protected final BotPersonality personality;
    protected final BotBrain brain;
    protected AiClient client;

    public AIPersonality(BotPersonality personality) {
        this.personality = personality;
        this.brain = new BotBrain();
    }

    public void setClient(AiClient client) {
        this.client = client;
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
