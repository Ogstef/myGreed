import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

// This class stores the game state
public class LostCitiesRivals2PGameState implements Cloneable, Serializable {
	// GAME CONSTANTS --- BEST NOT TO CHANGE THEM!
	byte[] currentExpedition;
	int maxExpedition = 0;
	int maxExpeditionNumber = 0;
	int expTotal = 0;

	// NOTE: to use byte, then it should be NUM_EXPEDITIONS*MAX_VALUE <= 127
	public static final int NUM_EXPEDITIONS = 5; 	// how many expeditions
	public static final int MAX_VALUE = 10;			// maximum value per expedition
	public static final int MAX_DOUBLECARD_VALUE = 5;// up to this value there are two cards but only one step in each one
	public static final int MIN_VALUE = 2;			// smallest value card (should be at least 2 because 1=bet card)
	public static final int MAX_BET_CARDS = 3;		// how many bet cards there are
	public static final int EXP_COST = 4;			// cost to start an expedition (subtract this value)
	public static final int EXP_LARGE_BONUS = 8;	// bonus value for large expedition (bonus added after bet multiplier, so not multiplied)
	public static final int EXP_LARGE_NUMCARDS = 7;	// min number cards (excluding bets) to get large expedition bonus
	public static final String EXP_COLORS = "YBKGR";// the colors are: Red Green Blue Yellow blacK
	public static final int DISPLAY_SIZE = 9;		// this will be how many cards are displayed (and max available) - WARNING: DON'T MAKE THIS 10 OR MORE
	public static final int DISPLAY_MIN = 3;		// this will be the min number of available cards in display (at beginning and after auction)
	public static final int GOLD_START = 13;		// starting gold for each player
	public static final int DISTRIBUTE_GOLD_TIMES_FACTOR = 4; // You should distribute paid gold to players when (total cards)/(this factor) cards have been played (this will also affect the displayed cards)
															  // So essentially it's the number of ROUNDS!
	public static final int SPARE_GOLD_IF_BANKRUPT=3; // give this much gold if both players are out of gold! (or the amount of cards left in the round - which ever is smaller)

	public static final int COINS_PER_FINAL_POINT=3;

	private byte[] lastCardsAddedToDisplay=null;
	public byte cardsAddedCounter = 2;
	// this is how many points you get for the leftover gold you have at the end (e.g. if 3 then each 3 gold is 1 point)

	// NOTE: Regarding card values  = value + expedition*MAX_VALUE (so should be 1...NUM_EXPEDITIONS*MAX_VALUE)

	public enum GameStatus {
		REVEAL, BET, SELECT
	}

	private byte[][] expeditionsP1 = new byte[NUM_EXPEDITIONS][MAX_VALUE]; 	// number of cards of each value in P1 expeditions (Note: position of value i is i-1)
	private byte[][] expeditionsP2 = new byte[NUM_EXPEDITIONS][MAX_VALUE]; 	// number of cards of each value in P2 expeditions
	private byte[] display = new byte[DISPLAY_SIZE];						// cards in display
	private byte displayCardsAvailable = 0;
	private byte goldP1 = GOLD_START, goldP2 = GOLD_START, goldPaid = 0;
	private byte cardsInRound, cardsLeftInRound, roundNo=1; // will compute these later: after how many cards money is distributed

	private boolean isP1turn=true;					// checks whos turn it is
	private int minBet=0;							// keeps track of the minimum bet (if it's a betting round)/ 0 otherwise (which is NOT a valid bet)

	private GameStatus gameStatus;

	/**
	 * Converts a card number into a string (XXC) where XX=card number and C=expedition color
	 * @param card card number
	 * @return null if incorrect card number, else card string
	 */
	public static String cardNum2Str(byte card)
	{
		if (card<1 || card>NUM_EXPEDITIONS*MAX_VALUE) return null;
		int exp = (card-1)/MAX_VALUE;
		int i = card-exp*MAX_VALUE;
		if (i>1) {
			String s = (i<10)?" ":"";
			return s+i+EXP_COLORS.charAt(exp);
		}
		else return " +"+EXP_COLORS.charAt(exp);
	}

	/**
	 * Returns the expedition number from card number
	 * @param card card number
	 * @return -1 if incorrect card number, else card expedition number
	 */
	public static byte cardNum2Exp(byte card)
	{
		if (card<1 || card>NUM_EXPEDITIONS*MAX_VALUE) return -1;
		return (byte)((card-1)/MAX_VALUE);
	}

	/**
	 * Returns the card value from card number
	 * @param card card number
	 * @return -1 if incorrect card number, else card value (1: for bet card, 2+: normal cards)
	 */
	public static byte cardNum2Val(byte card)
	{
		if (card<1 || card>NUM_EXPEDITIONS*MAX_VALUE) return -1;
		int exp = (card-1)/MAX_VALUE;
		return (byte)(card-exp*MAX_VALUE);
	}

	/**
	 * Returns an array of the cards values (per expedition) that can still be used by this player.
	 * @param cardsP list of cards in player expeditions
	 * @return
	 */
	public static byte[] minValuesPerExpedition(byte[][] cardsP)
	{
		byte [] minValues = new byte[NUM_EXPEDITIONS];
		for (int exp=0; exp<NUM_EXPEDITIONS; exp++)
		{
			for (int i=1; i<MAX_VALUE; i++)
			{
				if (cardsP[exp][i]>0)
					minValues[exp] = (byte)(i+1);
			}
		}
		return minValues;
	}

	/**
	 * Returns an array of the cards values (per expedition) that can still be used by at least one player.
	 * @param cardsP1 list of cards in player 1 expeditions
	 * @param cardsP2 list of cards in player 2 expeditions
	 * @return
	 */
	public static byte[] minValuesPerExpeditionBothPlayers(byte[][] cardsP1, byte[][] cardsP2)
	{
		byte[] minValues1 = minValuesPerExpedition(cardsP1);
		byte[] minValues2 = minValuesPerExpedition(cardsP2);
		byte[] minValues = new byte[NUM_EXPEDITIONS];
		for (int exp=0; exp<NUM_EXPEDITIONS; exp++)
		{
			if (minValues1[exp]<minValues2[exp])
				minValues[exp] = minValues1[exp];
			else
				minValues[exp] = minValues2[exp];
		}
		return minValues;
	}

	/**
	 * Returns an array of the cards values (per expedition) that can still be used by at least one player.
	 * Without parameters compared to minValuesPerExpeditionBothPlayers function
	 * @return
	 */
	public byte[] minExpValues()
	{
		return minValuesPerExpeditionBothPlayers(expeditionsP1, expeditionsP2);
	}

	/**
	 * Constructor of initial game state
	 */
	public LostCitiesRivals2PGameState()
	{
		int totalcards = MAX_BET_CARDS + MAX_VALUE + MAX_DOUBLECARD_VALUE - 2*MIN_VALUE +2;
		totalcards *= NUM_EXPEDITIONS;
		cardsInRound = (byte) (totalcards / DISTRIBUTE_GOLD_TIMES_FACTOR);
		cardsLeftInRound = cardsInRound;
		gameStatus = GameStatus.REVEAL;
	}

	/**
	 * Prints a single player expedition cards
	 * @param expeditionCards cards of the player
	 * @return
	 */
	private int printPlayerExpedition(byte[][] expeditionCards)
	{
		int totalPoints = 0;
		//private byte[][] expeditionsP1 = new byte[NUM_EXPEDITIONS][MAX_VALUE]; 	// number of cards of each value in P1 expeditions (Note: position of value i is i-1)
		for (int exp=0; exp<NUM_EXPEDITIONS; exp++)
		{
			System.out.print(EXP_COLORS.charAt(exp)+": ");
			int points = 0, totalCards=0;
			int mult = expeditionCards[exp][0];
			for (int k=0; k<expeditionCards[exp][0]; k++)
				System.out.print("+");
			for (int i=1; i<expeditionCards[exp].length; i++)
			{
				totalCards += expeditionCards[exp][i];
				if (i>=MAX_DOUBLECARD_VALUE) points += 2*expeditionCards[exp][i];
				else points += expeditionCards[exp][i];
				for (int k=0; k<expeditionCards[exp][i]; k++)
					System.out.print(" "+(i+1));
			}
			if (mult>0 || points>0)
			{
				points = (1+mult)*(points-EXP_COST);
				if (totalCards>=EXP_LARGE_NUMCARDS)
					points += EXP_LARGE_BONUS;
				System.out.print("   \t> "+points+" pts");
			}
			totalPoints += points;
			System.out.println();
		}
		return totalPoints;
	}


	/**
	 * Prints the state of the game
	 * ADDED: Printing scores if end of game
	 * ADDED: return points of players
	 */
	public int[] printState()
	{
		System.out.print("\nCommon Gold:"+getGoldPaid()+" - DISPLAY:");
		for (int i=0; i<DISPLAY_SIZE; i++)
		{
			if (i==displayCardsAvailable) System.out.print("[...] ");
			if (display[i]>0) System.out.print("#"+(i+1)+":"+cardNum2Str(display[i])+" ");
		}
		System.out.println("\nCards Left in Round "+getRoundNo()+" : "+cardsLeftInRound+"/"+cardsInRound);
		System.out.println("Player 1 - Gold:"+getGoldP1());
		int pointsP1 = printPlayerExpedition(expeditionsP1);
		System.out.println("Player 2 - Gold:"+getGoldP2());
		int pointsP2 = printPlayerExpedition(expeditionsP2);
		pointsP1 += goldP1/COINS_PER_FINAL_POINT;
		pointsP2 += goldP2/COINS_PER_FINAL_POINT;
		if (roundNo>DISTRIBUTE_GOLD_TIMES_FACTOR) // should be end of game actually
		{
			System.out.printf("FINAL SCORES - P1:%d - P2:%d\n", pointsP1, pointsP2);
		}
		else switch (gameStatus)
		{
		case BET:
			System.out.println("---Status: BETTING (current bet="+minBet+")---");
			break;
		case REVEAL:
			System.out.println("---Status: REVEALING (or choosing to bet)---");
			break;
		case SELECT:
			System.out.println("---Status: SELECTING CARDS (max selected="+minBet+")---");
			break;
		default:
			System.out.println("***Status: ??? (Should not happen!)***");
			break;
		}
		return new int[] {pointsP1, pointsP2};
	}

	/**
	 * How many cards still to fill the display (if fewer for end of round, then only go up to end of round!)
	 * @return
	 */
	public int cardsToFillDisplay()
	{
		if (cardsLeftInRound<0) // this should not happen
			System.err.println("ERROR: in cards left:"+cardsLeftInRound);
		int cards = 0;
		for (int i=0; i<DISPLAY_SIZE; i++)
		{
			if (display[i]<=0) cards++;
		}
		if (cards<cardsLeftInRound)
			return cards;
		else
			return cardsLeftInRound;
	}

	/**
	 * Remove cards from display that players can no longer use
	 * WARNING: does not condense display (just throws away cards)
	 */
	private void removeUselessCardsFromDisplay()
	{
		// stage 1: remove useless cards
		byte[] minExpValues = minExpValues();
		for (int i=0; i<9; i++)
		{
			if (display[i]==0) continue; // there is no card here
			int exp = cardNum2Exp(display[i]);
			int val = cardNum2Val(display[i]);
			if (exp<0)
			{
				System.err.println("ERROR in display. Card number is:"+display[i]);
				display[i]=0;
				continue;
			}
			if (minExpValues[exp]>val)
			{
				System.out.println("Throwing away useless card:" + cardNum2Str(display[i]));
				display[i]=0;
			}
		}
	}

	/**
	 * Condenses the cards from the display to fill cards that have been removed
	 */
	private void condenseDisplay()
	{
		// condense display now
		for (int i=0; i<DISPLAY_SIZE-1; i++)
		{
			if (display[i]<=0 && display[i+1]>0) // move card to previous slot and repeat - inefficient but works
			{
				display[i] = display[i+1];
				display[i+1] = 0;
				if (displayCardsAvailable == i+1)
					displayCardsAvailable--;
				condenseDisplay();
				return;
			}
		}
		// check value of which cards are available (it could have all cards available before moving them - then the value is incorrect)
		if (displayCardsAvailable>DISPLAY_SIZE) displayCardsAvailable=DISPLAY_SIZE;
		while (displayCardsAvailable>0 && display[displayCardsAvailable-1]<=0) displayCardsAvailable--;
	}

	/**
	 * Gets from an external source (e.g. file or the shuffler) the new cards to be used in the display
	 * These cards could have useless cards that should be discarded immediately
	 * First condenses the display to remove empty spots
	 * Need to makes some more cards available (if needed - as it throws away useless cards)
	 * @param newCards an array of the new cards to add
	 */
	public void setDisplayCards(byte[] newCards)
	{
		lastCardsAddedToDisplay = newCards;
		cardsAddedCounter  = 2;
		condenseDisplay();
		byte[] minExpValues = minExpValues();
		int displayIndex=0;
		// find first empty display spot
		for (;displayIndex<DISPLAY_SIZE && display[displayIndex]>0; displayIndex++) ;
		//System.out.println(">>>"+displayIndex);
		for (int i=0; i<newCards.length; i++)
		{
			// the next two checks are to confirm that given cards have correct values (if not they are ignored!)
			if (newCards[i]<=0)
				continue;
			int exp = cardNum2Exp(newCards[i]);
			int val = cardNum2Val(newCards[i]);
			if (exp<0)
				continue;
			cardsLeftInRound--;
			System.out.println(cardsLeftInRound+": card="+newCards[i]);
			if (minExpValues[exp]>val)
			{
				System.out.println("Throwing away card:" + cardNum2Str(newCards[i]));
			}
			else if (displayIndex>=DISPLAY_SIZE)
			{
				System.err.println("DISPLAY SIZE EXCEEDED!!!! SHOULD NOT HAPPEN!");
				continue;
			}
			else // everything ok
			{
				display[displayIndex++]=newCards[i];
			}
		}
		if (displayCardsAvailable<DISPLAY_MIN)	// make extra cards available up to DISPLAY_MIN
			displayCardsAvailable = (byte)Math.min(DISPLAY_MIN,displayIndex);
	}

	/**
	 * Moves the selected cards from the display to the expedition of the player (in optimal order)
	 * ADDITIONALLY: removes gaps from the display (so it can then be filled in) - if you did not do anything then first cards is auto-discarded
	 * @param move list of numbers of selected cards to take (and one - the last one - to discard)
	 * @param cardLimit max number of cards allowed to take
	 */
	private void moveSelectedCardsToExpedition(int move, int cardLimit)
	{
		byte[][] refPlayerExpeditions = isP1turn?expeditionsP1:expeditionsP2;
		if (cardLimit<1) {
			System.err.println("Card Limit paremeter:" + cardLimit);
			cardLimit=1; // this should not happen ever
		}
		if (cardLimit>displayCardsAvailable) cardLimit = displayCardsAvailable;
		// which cards can be picked up
		byte[] minExpValues = minValuesPerExpedition(refPlayerExpeditions);
		// select the cards now (from last digit to first digit) - if you have put too many it will ignore the rest - any errors in move might create problems for the player as fewer cards might be picked or the wrong cards
		byte[] selectedCards = new byte[cardLimit];
		byte cardToDiscard = (byte)(move % 10 - 1);
		move /= 10;
		while (move>0)
		{
			byte pickCard = (byte)(move % 10 - 1);
			move /= 10;
			if (pickCard>=displayCardsAvailable || pickCard<0) continue; // wrong card number
			int exp = cardNum2Exp(display[pickCard]);
			int val = cardNum2Val(display[pickCard]);
			if (cardLimit>0 && exp>=0 && exp<NUM_EXPEDITIONS && val>=minExpValues[exp]) // card can be picked up
			{
				selectedCards[--cardLimit] = display[pickCard];
				System.out.println("Picking up card: " + cardNum2Str(display[pickCard]));
				display[pickCard] = 0;
			}
		}
		if (cardToDiscard<displayCardsAvailable && cardToDiscard>=0 && display[cardToDiscard]>0)
		{
			System.out.println("Discarding card: " + cardNum2Str(display[cardToDiscard]));
			display[cardToDiscard]=0;
		}
		// move cards to expedition in order (that's why we sort it)
		Arrays.sort(selectedCards);
		for (int i=0; i<selectedCards.length; i++)
		{
			if (selectedCards[i]<=0) continue;
			int exp = cardNum2Exp(selectedCards[i]);
			int val = cardNum2Val(selectedCards[i]);
			if (val>0)
				refPlayerExpeditions[exp][val-1]++;
			else
				refPlayerExpeditions[exp][val]++;
		}

		condenseDisplay();
		// TODO: added this (needs more testing)
		removeUselessCardsFromDisplay();

		if (cardsToFillDisplay()<=0 && displayCardsAvailable>=DISPLAY_SIZE)
		{	// autodrop first card -- probably no cards were picked or dropped!
			System.out.println("CHECK CARDS TAKEN - AUTODROPPING FIRST CARD FROM DISPLAY");
			display[0] = 0;
			condenseDisplay();
		}
	}

	/**
	 * This function executes the move input by the user or AI
	 * WARNING: if the move has an error it could be executed in an unfavorable manner (e.g. assume you dropped out of bet etc)
	 * @param move the action made by the player (e.g. bet or not bet, or select cards - see input class for more details)
	 * @return whether a display refill is needed (from the shuffler used or from center)
	 */
	public boolean executeMove(int move)
	{
		cardsAddedCounter--;
		if (cardsAddedCounter<=0) lastCardsAddedToDisplay = null;
		switch (gameStatus)
		{
		case REVEAL:
			if (move>0 && move <= (isP1turn?goldP1:goldP2)) {
				gameStatus = GameStatus.BET;
				minBet = move;
			}
			else {
				displayCardsAvailable++;
				if (displayCardsAvailable >= DISPLAY_SIZE || display[displayCardsAvailable]<=0)
					gameStatus = GameStatus.BET;	// need to bet as display is full or it's the end of a round
			}
			isP1turn = !isP1turn;
			return false;
		case BET:
			byte currentplayergold = isP1turn?goldP1:goldP2;
			if (move>minBet && move <= currentplayergold) {	// increase bet
				minBet = move;
			}
			else if (minBet<=0)
			{	// forced to bet at least one for first bet (if you actually have gold)!!!
				if (currentplayergold>=1) minBet = 1;
				// else bet remains 0
			}
			else {	// drop out of the betting - other player wins
				gameStatus = GameStatus.SELECT;
			}
			isP1turn = !isP1turn;
			return false;
		case SELECT:
			if (isP1turn)
				goldP1 -= minBet;
			else
				goldP2 -= minBet;
			goldPaid += minBet;
			// get cards
			moveSelectedCardsToExpedition(move, minBet);
			minBet=0;
			isP1turn = !isP1turn;
			gameStatus = GameStatus.REVEAL;
			// check if a round is over and gold should be distributed (and then maybe game ends if all rounds are over - but this should be checked outside this method)
			if (cardsLeftInRound<=0)
			{
				int goldSplit = goldPaid/2;
				goldPaid -= 2*goldSplit;
				goldP1 += goldSplit;
				goldP2 += goldSplit;
				roundNo++;
				cardsLeftInRound += cardsInRound;
			}
			// check if both player gold has run out - if so give them a few coins each from the pool of gold
			if (goldP1<=0 && goldP2<=0)
			{
				int goldSpare = Math.min(SPARE_GOLD_IF_BANKRUPT, cardsLeftInRound);
				System.out.println("Both players have no gold - give them some to continue the game:"+goldSpare);
				goldP1+=goldSpare;
				goldP2+=goldSpare;
				goldPaid-=2*goldSpare;
			}
			return true;

		default:
			break;

		}
		return false;
	}
	public LostCitiesRivals2PGameState deepCopyUsingSerialization()
	{
		try
		{
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(bo);
			o.writeObject(this);

			ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
			ObjectInputStream i = new ObjectInputStream(bi);

			return (LostCitiesRivals2PGameState) i.readObject();
		}
		catch(Exception e)
		{
			return null;
		}
	}

	//////  ---------------------    GETTER METHODS    --------------------------------------------------------

	/**
	 * Checks if it's P1 turn
	 * @return false if P2 turn
	 */
	public boolean isP1turn() {
		return isP1turn;
	}

	/**
	 * Returns the minimum amount that can be bet (one more than last best placed)
	 * @return
	 */
	public int getMinAcceptableBet() {
		return minBet+1;
	}

	/**
	 * Returns all cards that can be bet on
	 * @return
	 */
	public byte[] getCardsAvailableInDisplay()
	{
		return Arrays.copyOfRange(display, 0, displayCardsAvailable);
	}

	/**
	 * Returns all cards that are to be in the display in the future
	 * @return
	 */
	public byte[] getFutureCardsInDisplay()
	{
		return Arrays.copyOfRange(display, displayCardsAvailable, DISPLAY_SIZE);
	}

	/**
	 * P1 gold
	 * @return
	 */
	public byte getGoldP1() {
		return goldP1;
	}

	/**
	 * P2 gold
	 * @return
	 */
	public byte getGoldP2() {
		return goldP2;
	}

	/**
	 * Gold in common pool (to be distributed at the end of round equally to two players)
	 * @return
	 */
	public byte getGoldPaid() {
		return goldPaid;
	}

	/**
	 * How many cards until end of round
	 * @return
	 */
	public byte getCardsLeftInRound() {
		return cardsLeftInRound;
	}

	/**
	 * Round number
	 * @return
	 */
	public byte getRoundNo() {
		return roundNo;
	}

	/**
	 * Game Status (three choices: revealing cards adding to the display, or betting, or bet has concluded and the winner selects cards
	 * @return
	 */
	public GameStatus getGameStatus() {
		return gameStatus;
	}

	/**
	 * Returns the specific expedition cards of Player 1
	 * @param exp which expedition
	 * @return if incorrect exp number then returns empty array
	 */
	public byte[] getExpeditionP1(int exp)
	{
		if (exp<0 || exp>=NUM_EXPEDITIONS)
			return new byte[0];
		return Arrays.copyOf(expeditionsP1[exp], expeditionsP1[exp].length);
	}

	/**
	 * Returns the specific expedition cards of Player 2
	 * @param exp which expedition
	 * @return if incorrect exp number then returns empty array
	 */
	public byte[] getExpeditionP2(int exp)
	{
		if (exp<0 || exp>=NUM_EXPEDITIONS)
			return new byte[0];
		return Arrays.copyOf(expeditionsP2[exp], expeditionsP2[exp].length);
	}
	/**
	 * Returns all the cards that were just drawn to fill the display (even useless cards that were thrown)
	 * This is the same information that gamestate uses to update the display cards (and is useful for every AI)
	 * @return array of cards numbers, or null if no cards were added before the last two moves
	 */
	public byte[] getLastCardsAddedToDisplay()
	{
		if (lastCardsAddedToDisplay==null)
			return null;
		return Arrays.copyOf(lastCardsAddedToDisplay, lastCardsAddedToDisplay.length);
	}

	public int [] checkExpeditionsForUs(){
		int [] best = new int[2];
		for (int i = 0; i <= 5; i++) {
			currentExpedition = getExpeditionP2(i);
			if(currentExpedition.length == 0){
				continue;
			}
			expTotal = 0;
			for (int j = 0; j < 10; j++) {
				expTotal += currentExpedition[j];
			}
			if (expTotal > maxExpedition){
				maxExpedition = expTotal;
				maxExpeditionNumber = i;
			}
		}
		//System.out.println("max expedition:" + maxExpedition);
		//System.out.println("max expedition num:" + maxExpeditionNumber);
		best[0] = maxExpedition;
		best[1] = maxExpeditionNumber;

		return best;

	}

	public int worthBetting() {
		byte [] cardsAvail = getCardsAvailableInDisplay();
		int bet =0;
		int [] positions = new int[10];
		int category=0;
		int [] myExp = minCardOfEachExpedition(1);
		for (int y=0; y<getCardsAvailableInDisplay().length;y++) {
			if ( getCardsAvailableInDisplay()[y] %10 !=0 ){
				category = getCardsAvailableInDisplay()[y] /10;
			}
			else {
				if ( getCardsAvailableInDisplay()[y] >10) category= getCardsAvailableInDisplay()[y]/10-1;
				else category=0;
			}
			for (int i =0; i <5; i++){
				if (category==myExp[i]/10 && getCardsAvailableInDisplay()[y]-myExp[i]>0){
					bet++;
				}
			}
		}
		return bet;
	}


	//returns which # expedition has the most cards with values of 5 and under. To decide when to start betting and for what.
	public int expeditionWithMostCardsStartingRounds() {
		byte[] currentCards = getCardsAvailableInDisplay();
		int [] cardsToTake = new int[5];
		for(int i = 0; i<currentCards.length; i++){
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
		int max = -1;
		for ( int i =0 ; i<5 ; i++){
			if (cardsToTake[i] > max ) max = i;
		}
		return max;
	}

	public int expeditionWithMostCardsAfterRounds() {
		byte[] currentCards = getAllCardsShown();
		int [] cardsToTake = new int[5];
		for(int i = 0; i<currentCards.length; i++){
			if (currentCards[i] <= 10 && currentCards[i]>5)
				cardsToTake [0] ++;
			else if (currentCards[i] <=20 && currentCards[i] >15 )
				cardsToTake[1]++;
			else if (currentCards[i]<=30 && currentCards[i] >25)
				cardsToTake[2]++;
			else if (currentCards[i]<=40  && currentCards[i] >35)
				cardsToTake[3]++;
			else if (currentCards[i]<=50 && currentCards[i] >45)
				cardsToTake[4]++;

		}
		System.out.println(Arrays.toString(currentCards));
		int max = 0;
		for ( int i =0 ; i<5 ; i++){
			if (cardsToTake[i] > max ) max = i;
		}
		return max;
	}



//	public int numberOfCardsIshouldBetOn(){
//		byte [] avail = getCardsAvailableInDisplay();
//
//
//	}


	//returns an array of all the cards shown on the board
	public byte [] getAllCardsShown(){
		byte[] addcards = new byte[getCardsAvailableInDisplay().length + getFutureCardsInDisplay().length];
		ByteBuffer buff = ByteBuffer.wrap(addcards);
		buff.put(getCardsAvailableInDisplay());
		buff.put(getFutureCardsInDisplay());
		return buff.array();
	}

	public int[] minCardOfEachExpedition(int player){
		int [] x = new int[5];
		byte [] currentExpedition = new byte[10];
		x = new int[]{1, 11, 21, 31, 41};
		for (int y =0; y<5; y++) {
			if (player==1){
				currentExpedition = getExpeditionP1(y);
			}
			else if ( player ==2){
				currentExpedition = getExpeditionP2(y);

			}

			for (int i = 9; i >= 0; i--) {
				if (currentExpedition[i] == 1) {
					x[y] = i + (y * 10 + 1);
					break;
				}
			}
		}
		return x;
	}

	public int categorizeCards (int x){
		if (x >0 && x<=10){
			return 0;
		}
		else if (x>10 && x<=20){
			return 1;
		}
		else if (x>20 && x<=30){
			return 2;
		}
		else if ( x>30 && x<=40){
			return 3;
		}
		else if ( x>40 && x<=50){
			return 4;
		}
		return 0; // shouldnt reach this.
	}

}


