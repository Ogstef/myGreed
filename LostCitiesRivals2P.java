import javax.swing.JOptionPane;

/**
 * The start of the game. It would select human vs computer, human vs human (using GUI or console), or playing in tournament mode
 * For tournament, each AI should be a separate execution and you need a 3rd execution that will just produce cards and keep stats (time used etc).
 * @author IV
 *
 */
public class LostCitiesRivals2P {

	public static void main(String[] args) throws CloneNotSupportedException {
		LostCitiesRivals2PCardShuffler shuffler = new LostCitiesRivals2PCardShuffler();
		LostCitiesRivals2PGameState gs = new LostCitiesRivals2PGameState();
		LostCitiesRivals2PInput console1 = new LostCitiesRivals2PGUI(gs);// new LostCitiesRivals2PInputConsole();
		LostCitiesRivals2PInput console2 = new greedyAI();
		/*
		gs.setDisplayCards(shuffler.nextCard(gs.cardsToFillDisplay()));
		gs.printState();
		System.out.println(Arrays.toString(gs.minExpValues()));
		System.out.println(Arrays.toString(gs.getCardsAvailableInDisplay()));
		System.out.println(Arrays.toString(gs.getFutureCardsInDisplay()));
		*/
		//TODO Tournament and options
		
		
		gs.setDisplayCards(shuffler.nextCards(LostCitiesRivals2PGameState.DISPLAY_SIZE, gs));
		LostCitiesRivals2PInput console;
		while (true)
		{
			if (gs.isP1turn())
				console = console1;
			else
				console = console2;
			int move = console.nextMove(gs);
			System.out.println();
			System.out.println("P"+(gs.isP1turn()?"1":"2") +" Move made:" + move);
			if (gs.executeMove(move))
				gs.setDisplayCards(shuffler.nextCards(gs.cardsToFillDisplay(),gs));
			if (gs.getRoundNo()>LostCitiesRivals2PGameState.DISTRIBUTE_GOLD_TIMES_FACTOR) // game has ended
				break;
		}
		int[] points = gs.printState();
		System.out.println("Game finished. Player 1:"+points[0]+" Player 2:"+points[1]);
		if (console instanceof LostCitiesRivals2PGUI)
		{
			((LostCitiesRivals2PGUI)console).setVisible(false);
			JOptionPane.showMessageDialog(null, "Game finished.\nPlayer 1:"+points[0]+"\nPlayer 2:"+points[1], "Final Scores", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
