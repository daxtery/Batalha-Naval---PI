package Common;

public class AttackResult {

    public final AttackResultStatus status;
    public final boolean destroyedShip;

    private AttackResult(AttackResultStatus status, boolean destroyedShip) {
        this.status = status;
        this.destroyedShip = destroyedShip;
    }

    static AttackResult OutsideBounds() {
        return new AttackResult(AttackResultStatus.OutsideBounds, false);
    }

    static AttackResult AlreadyVisible() {
        return new AttackResult(AttackResultStatus.AlreadyVisible, false);
    }

    static AttackResult HitWater() {
        return new AttackResult(AttackResultStatus.HitWater, false);
    }

    static AttackResult HitShipPiece(boolean destroyedShip) {
        return new AttackResult(AttackResultStatus.HitShipPiece, destroyedShip);
    }

    public boolean shouldPlayAgain() {
        return switch (this.status) {
            case OutsideBounds, AlreadyVisible, HitShipPiece -> true;
            case HitWater -> false;
        };
    }

    public boolean valid() {
        return switch (this.status) {
            case OutsideBounds, AlreadyVisible -> false;
            case HitWater, HitShipPiece -> true;
        };
    }

}
