import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Random;

public class myAi implements LostCitiesRivals2PInput {
    int count = 0;
    byte [] bestExp ;
    int counter ;
    int maxExpedition = 0, expTotal = 0;



    private Random rng = new Random();

    int countExp = 0;
    @Override
    public int nextMove(LostCitiesRivals2PGameState gs) throws CloneNotSupportedException {

        System.out.println("COUNT: " + count);
        System.out.println("Checking Expeditions: " + Arrays.toString(gs.checkExpeditionsForUs()));
        ///System.out.println("Next Best Expedition: " + Arrays.toString(gs.checkNextBestExpedition()));
        System.out.println("EXP WITH MOST CARDS after rounds:  " + gs.expeditionWithMostCardsAfterRounds());
        System.out.println("Cards available in disp:  " + Arrays.toString(gs.getCardsAvailableInDisplay()));
        System.out.println("Future Cards available in disp:  " + Arrays.toString(gs.getFutureCardsInDisplay()));
        System.out.println("All Cards available in disp:  " + Arrays.toString(gs.getAllCardsShown()));
        System.out.println("Min Card of each Expedition:  " + Arrays.toString(gs.minCardOfEachExpedition(1)));
        System.out.println("Totals of Expeditions BLUE:  " + Arrays.toString(gs.getExpeditionP1(1)));

        count++;
        LostCitiesRivals2PGameState cloned = gs.deepCopyUsingSerialization();
        counter = 0;

        //Reveal phase -- Needs more information added
        if (cloned.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.REVEAL) {
            if (gs.getCardsAvailableInDisplay().length < 4) {
                return 0;
            } else {
                return gs.heuristicFunction(2)[0];
            }
        }
        else if (cloned.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.BET) {
            if ((float) (gs.heuristicFunction(2)[0] / gs.getMinAcceptableBet()) > 1.0) {
                return counter;
            } else if ((float) (gs.heuristicFunction(2)[0] / cloned.getMinAcceptableBet()) > 0.6) {
                return cloned.getMinAcceptableBet();
            } else return -1;

        }
        else if (cloned.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.SELECT) {

            return gs.heuristicFunction(2)[1];
        }
        return 0;
    }
}
