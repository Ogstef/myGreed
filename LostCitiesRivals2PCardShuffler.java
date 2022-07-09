import java.util.ArrayList;
import java.util.Random;

public class LostCitiesRivals2PCardShuffler {
	private byte[] deck;
	private int nextCardIndex=0;
	
	private int cardsInRound, cardsLeftInRound;
	
	/**
	 * Creates deck of cards (based on size constants defined in LostCitiesRivals2PGameState) and then shuffles deck
	 */
	public LostCitiesRivals2PCardShuffler() 
	{
		int count = LostCitiesRivals2PGameState.MAX_BET_CARDS + LostCitiesRivals2PGameState.MAX_VALUE + 
				LostCitiesRivals2PGameState.MAX_DOUBLECARD_VALUE - 2*LostCitiesRivals2PGameState.MIN_VALUE +2;
		//System.out.println(count);
		deck = new byte[count*LostCitiesRivals2PGameState.NUM_EXPEDITIONS]; // to store all cards
		
		int i=0;
		for (int exp=0; exp<LostCitiesRivals2PGameState.NUM_EXPEDITIONS; exp++)
		{
			// create cards for one expedition
			for (int k=1; k<=LostCitiesRivals2PGameState.MAX_BET_CARDS; k++)
			{
				deck[i++] = (byte)(exp*LostCitiesRivals2PGameState.MAX_VALUE+1);
			}
			for (int k=LostCitiesRivals2PGameState.MIN_VALUE; k<=LostCitiesRivals2PGameState.MAX_VALUE; k++)
			{
				deck[i++] = (byte)(exp*LostCitiesRivals2PGameState.MAX_VALUE+k);
				if (k<=LostCitiesRivals2PGameState.MAX_DOUBLECARD_VALUE)
					deck[i++] = (byte)(exp*LostCitiesRivals2PGameState.MAX_VALUE+k);
			}
		}
		
		// implements Fisher-Yates shuffle
		Random rng = new Random();
		for (i=deck.length-1; i>0; i--)
		{
			int index = rng.nextInt(i + 1);
			// Simple swap
			byte t = deck[index];
			deck[index] = deck[i];
			deck[i] = t;
		}
				
		//for (i=0; i<deck.length;i++) System.out.print(LostCitiesRivals2PGameState.cardNum2Str(deck[i])); System.out.println();
		
		cardsInRound = count * LostCitiesRivals2PGameState.NUM_EXPEDITIONS / LostCitiesRivals2PGameState.DISTRIBUTE_GOLD_TIMES_FACTOR;
		cardsLeftInRound = cardsInRound;
		
	}
	
	/**
	 * Returns the next (1) card from the deck
	 * @return next card number, or 0 (if deck has run out or round stack is over)
	 */
	public byte nextCard()
	{
		if (nextCardIndex<deck.length && cardsLeftInRound>0)
		{
			cardsLeftInRound--;
			return deck[nextCardIndex++];
		}
		else
		{
			cardsLeftInRound=cardsInRound; // refill for next round
			return 0;
		}
	}
	
	/**
	 * Returns the next n cards from the deck (and extra cards that would be discarded as they cannot be used by either player)
	 * Checks if this would exceed the array or if it's more than the display limit
	 * @param n number of desired cards
	 * @return array of cards
	 */
	public byte[] nextCards(int n, LostCitiesRivals2PGameState gamestate)
	{
		if (n>LostCitiesRivals2PGameState.DISPLAY_SIZE) n=LostCitiesRivals2PGameState.DISPLAY_SIZE;
		if (n+nextCardIndex>deck.length) n=deck.length-nextCardIndex; // no more cards left
		if (n<1) return new byte[0];
		
		// all OK, get n useful cards now (and maybe some that need to be discarded)
		byte[] minExpValues = gamestate.minExpValues();
		ArrayList<Byte> cards = new ArrayList<>();
		
		for (int i=0; i<n; i++)
		{
			byte card = nextCard();
			int exp = LostCitiesRivals2PGameState.cardNum2Exp(card);
			int val = LostCitiesRivals2PGameState.cardNum2Val(card);
			if (exp<0)
			{
				System.err.println("ZERO CARD in shuffler. Card number is:"+card);
				if (card==0) // deck has run out or round over
					break;
				i--; // get another card instead
				continue;
			}
			cards.add(card);
			if (minExpValues[exp]>val)
			{
				System.out.println("Generated a useless card:" + LostCitiesRivals2PGameState.cardNum2Str(card));
				i--; // need to generate an additional card - useless card is still added though
			}
		}
		byte[] finalCards = new byte[cards.size()];
		for (int i=0; i<finalCards.length; i++) finalCards[i] = cards.get(i);
		
		return finalCards;
	}
	
}
