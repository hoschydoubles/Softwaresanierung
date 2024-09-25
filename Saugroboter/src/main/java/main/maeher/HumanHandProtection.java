package main.maeher;

public class HumanHandProtection {

    private static final float DISTANCE_THRESHOLD = 25;

    public boolean checkHumanHandDistance(Callback callback, float distanceToHumanHand) {
        System.out.println("Passed distance to Hand value:"+distanceToHumanHand);
        if (distanceToHumanHand <= DISTANCE_THRESHOLD) {
            System.out.println("Distance to Hand lower than threshold. Stopping motor.");
            callback.execute();
            return true;
        }
        return false;
    }
}
