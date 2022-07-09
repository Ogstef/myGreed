// This should be used either for AI or console
public interface LostCitiesRivals2PInput {
	
	public static final int REVEALCARD=-1; 	// any negative number actually will work for (not betting and) revealing the next card in the display
	public static final int MINIMUMBET=1;	// min bet amount
	
	/*
	 * When selecting cards, give the cards in the form 2453 would mean get cards 2, 4 and 5 and drop card 3 
	 * if you don't want to get/drop card set to 0, so 2450 would mean get cards 2, 4 and 5 and don't drop a card, whereas 3 would mean don't get cards and drop 3
	 */
	
	public int nextMove(LostCitiesRivals2PGameState gs) throws CloneNotSupportedException;
}
