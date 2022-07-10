import java.util.Arrays;
import java.util.Random;

/**
 * This implements an AI that randomly bets and randomly picks up a card
 * @author IV
 *
 */
public class LostCitiesRivals2PRandomActionAI implements LostCitiesRivals2PInput {
	int count = 0;

	byte [] bestExp ;
	int maxExpedition = 0, expTotal = 0;



	private Random rng = new Random();

	int countExp = 0;
	
	@Override
	public int nextMove(LostCitiesRivals2PGameState gs) throws CloneNotSupportedException {
		LostCitiesRivals2PGameState cloned = gs.deepCopyUsingSerialization();


		count ++;


		byte [] currentCards = cloned.getLastCardsAddedToDisplay();
		if (count ==1 ){
			int [] cardsToTake = new int[5];
			for(int i =0; i<currentCards.length;i++){
				if (currentCards[i] <= 5)
					cardsToTake [0] ++;
				else if (currentCards[i] <=15 && currentCards[i] >10 )
					cardsToTake[1]++;
				else if (currentCards[i]<=25 && currentCards[i] >20)
					cardsToTake[2]++;
				else if (currentCards[i]<=35  && currentCards[i] >30)
					cardsToTake[3]++;
				else if (currentCards[i]<=45 && currentCards[i] >40)
					cardsToTake[4]++;

			}
			System.out.println(Arrays.toString(currentCards));
			int max = cardsToTake[0];
			for ( int i =0 ; i<5 ; i++){
				if (cardsToTake[i] > max ) max = cardsToTake[i];
				System.out.println(cardsToTake[i]);
			}
			System.out.println(max);
		}
		System.out.println("flag for my method \n");
		if ( cloned.checkExpeditionsForUs()!= null){
			System.out.println(Arrays.toString(cloned.checkExpeditionsForUs()));

		}



		if (cloned.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.REVEAL){


			return 0;
		}
			 // 50% uncover card 50% bet 1
		else if (cloned.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.BET){


			return -1;}// 50% increase bet / 50% drop out
		else if (cloned.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.SELECT)
			return 123;//pick up one card (randomly from the available ones) and don't discard any - does not check if card is useful or can be picked up by this player!
		else
			return 0; //This should not happen
	}

}
