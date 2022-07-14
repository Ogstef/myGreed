import java.util.Random;

/**
 * This implements an AI that randomly bets and randomly picks up a card
 * @author IV
 *
 */
public class LostCitiesRivals2PRandomActionAI implements LostCitiesRivals2PInput {

	private Random rng = new Random();

	@Override
	public int nextMove(LostCitiesRivals2PGameState gs) {
		if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.REVEAL)
			return rng.nextInt(2); // 50% uncover card 50% bet 1
		else if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.BET)
			return (rng.nextDouble()<0.5)?0:gs.getMinAcceptableBet(); // 50% increase bet / 50% drop out
		else if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.SELECT)
			return 10*rng.nextInt(gs.getCardsAvailableInDisplay().length); //pick up one card (randomly from the available ones) and don't discard any - does not check if card is useful or can be picked up by this player!
		else
			return 0; //This should not happen
	}

}
