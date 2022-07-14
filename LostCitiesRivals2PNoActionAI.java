import java.util.Arrays;

/**
 * This implements an AI that does nothing really
 * @author IV
 *
 */
public class LostCitiesRivals2PNoActionAI implements LostCitiesRivals2PInput {

	@Override
	public int nextMove(LostCitiesRivals2PGameState gs) {
		if ( (gs.getCardsAvailableInDisplay()[i] -gs.minCardOfEachExpedition(2)[y]) <5 && (gs.categorizeCards(gs.getCardsAvailableInDisplay()[i]) == gs.categorizeCards(gs.minCardOfEachExpedition(2)[y])))
		{

			System.out.println("This is the category" + gs.categorizeCards(gs.getCardsAvailableInDisplay()[i]));
			System.out.println("This is count select" + countSelect);
			ourSelection[countSelect] += (i + 1);
			countSelect++;
			System.out.println("This is count select after add" + countSelect);
		}

	}

}
