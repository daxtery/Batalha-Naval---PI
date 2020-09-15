package Server;

import java.util.Arrays;
import java.util.Objects;

public class Lobby {

    public final int code;
    public final LobbyParticipant[] participants;

    public Lobby(int code, int count) {
        this.code = code;
        this.participants = new LobbyParticipant[count];
    }

    public int count() {
        return (int) Arrays.stream(this.participants).filter(Objects::nonNull).count();
    }

    public int capacity() {
        return this.participants.length;
    }

    public boolean full() {
        return Arrays.stream(this.participants).noneMatch(Objects::isNull);
    }

}
