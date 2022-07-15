import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Random;

public class updateAi implements LostCitiesRivals2PInput {
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
            if (gs.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.REVEAL) {
                if (gs.getCardsAvailableInDisplay().length < 4) {
                    return 0;
                } else {
                    for (int y = 0; y < 5; y++) {
                        for (int i = 0; i < gs.getCardsAvailableInDisplay().length; i++) {
                            if (gs.getRoundNo() >0 ) {
                                if (gs.getCardsAvailableInDisplay()[i] - gs.minCardOfEachExpedition(1)[y] > 0 && gs.getCardsAvailableInDisplay()[i] - gs.minCardOfEachExpedition(1)[y] < 3 && gs.categorizeCards(gs.getCardsAvailableInDisplay()[i]) == gs.categorizeCards(gs.minCardOfEachExpedition(1)[y])) {
                                    counter++;
                                    System.out.println("Im in reveal and its still round 1" + counter);
                                }
                            }
                        }
                    }

                    return counter;
                }
            }
            else if (gs.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.BET) {
                if ((float) (counter / gs.getMinAcceptableBet()) > 1.0) {
                    return counter;
                } else if ((float) (counter / cloned.getMinAcceptableBet()) > 0.6) {
                    return cloned.getMinAcceptableBet();
                } else return -1;

            }
            else if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.SELECT){
                int category;
                int []ourSelection = new int[10];//random value shouldn't take it.
                int selection;
                int countSelect =0;
                int f=0;

                for (int y = 0; y < 5; y++){
                    for(int i = 0; i < gs.getCardsAvailableInDisplay().length; i++) {
                        if ((gs.getCardsAvailableInDisplay()[i]!=gs.minCardOfEachExpedition(1)[y]) && (gs.getCardsAvailableInDisplay()[i]- gs.minCardOfEachExpedition(1)[y]<3 && gs.getCardsAvailableInDisplay()[i]- gs.minCardOfEachExpedition(1)[y]>0) && (gs.categorizeCards(gs.getCardsAvailableInDisplay()[i]) == gs.categorizeCards(gs.minCardOfEachExpedition(1)[y]))) {
                            System.out.println("IN SELECT AND ROUND =1 \n");
                            ourSelection[countSelect] += (i + 1);
                            countSelect++;
                        }
                    }

                }
                Arrays.sort(ourSelection);
                int retval=0;
                for (int digit: ourSelection){
                    retval *=10;
                    retval+=digit;
                }
                int lastcard=0;
                retval=Integer.parseInt(String.valueOf(retval)+String.valueOf(lastcard));
                System.out.println("OUR SELECTION:" + retval);
                return retval;
            }
            return 0;
        }
    }

