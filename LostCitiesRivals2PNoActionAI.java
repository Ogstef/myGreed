//import java.util.Arrays;
//
/////**
//// * This implements an AI that does nothing really
//// * @author IV
//// *
//// */
////public class LostCitiesRivals2PNoActionAI implements LostCitiesRivals2PInput {
////
////	@Override
////	public int nextMove(LostCitiesRivals2PGameState gs) {
////
////
////		if ( (gs.getCardsAvailableInDisplay()[i] -gs.minCardOfEachExpedition(2)[y]) <5 && (gs.categorizeCards(gs.getCardsAvailableInDisplay()[i]) == gs.categorizeCards(gs.minCardOfEachExpedition(2)[y])))
////		{
////
////			System.out.println("This is the category" + gs.categorizeCards(gs.getCardsAvailableInDisplay()[i]));
////			System.out.println("This is count select" + countSelect);
////			ourSelection[countSelect] += (i + 1);
////			countSelect++;
////			System.out.println("This is count select after add" + countSelect);
////		}
////
////	}
//
// if (gs.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.REVEAL) {
//         if(gs.getCardsAvailableInDisplay().length <4){
//        return 0;
//        }
//        else {
//        for (int y = 0; y < 5; y++) {
//        for (int i = 0; i < gs.getCardsAvailableInDisplay().length; i++) {
//        counter++;
//        System.out.println("Im in reveal and its still round 1" + counter);
//
//        }
//        else if ((gs.getCardsAvailableInDisplay()[i] >= gs.minCardOfEachExpedition(2)[y]) && (gs.categorizeCards(gs.getCardsAvailableInDisplay()[i]) == gs.categorizeCards(gs.minCardOfEachExpedition(2)[y]))) {
//
//        counter++;
//        System.out.println("Im in reveal and its not round1" + counter);
//
//        }
//        }
//        }
//        return counter;
//        }
//        }
////
////}
