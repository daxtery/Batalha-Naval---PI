package Common;

import java.util.ArrayList;
import java.util.List;

public class Conversations {

    private final int slotsCount;
    private final Conversation[] conversations;

    public Conversations(int slotsCount) {
        this.slotsCount = slotsCount;

        final int totalPairs = ((slotsCount - 1) * slotsCount) / 2;

        conversations = new Conversation[totalPairs];

        for (int base = 0, i = 0; base < slotsCount; base++) {
            for (int offset = base + 1; offset < slotsCount; offset++, i++) {
                conversations[i] = new Conversation();
            }
        }

    }

    public Conversation getConversationBetweenSlots(int slot1, int slot2) {
        final int min = Math.min(slot1, slot2);
        final int diff = this.slotsCount - 1 - min;
        final int pairsCount = (diff * (diff + 1)) / 2;
        final int minBase = conversations.length - pairsCount;

        final int offset = Math.abs(slot1 - slot2) - 1;
        return conversations[minBase + offset];
    }

    public static class Conversation {

        List<Line> lines;

        public Conversation() {
            lines = new ArrayList<>();
        }

        public Line add(int talking, String s) {
            Line line = new Line(talking, s);
            lines.add(line);
            return line;
        }

    }

    public static class Line {
        int id;
        String message;

        public Line(int _id, String _message) {
            id = _id;
            message = _message;
        }

        public String decode(String name) {
            return name + ": " + message;
        }
    }

}
