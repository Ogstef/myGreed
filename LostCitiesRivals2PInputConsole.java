import java.util.InputMismatchException;
import java.util.Scanner;

public class LostCitiesRivals2PInputConsole implements LostCitiesRivals2PInput {

	private Scanner keyb;
	public LostCitiesRivals2PInputConsole() {
		keyb = new Scanner(System.in);
	}

	@Override
	public int nextMove(LostCitiesRivals2PGameState gs) {
		gs.printState();
		String plstr = (gs.isP1turn())?"Player 1":"Player 2";
		int plgold = (gs.isP1turn())?gs.getGoldP1():gs.getGoldP2();
		System.out.println("Your gold is: "+ plgold);
		while (true)
		{
			if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.REVEAL)
				System.out.println(plstr+" input your move (-1 to add card to display or >0 to bet): ");
			else if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.BET)
				System.out.println(plstr+" input your move (type bet>="+ gs.getMinAcceptableBet() +"): ");
			else if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.SELECT)
				System.out.println(plstr+" input your move (select cards from display as XYZ etc where X,Y,Z are the positions 1-9 last one is card to discard): ");
			else
				System.err.println("This should not happen....!");
			try {
				int inp=keyb.nextInt();
				// check input
				if (inp>plgold && gs.getGameStatus()!=LostCitiesRivals2PGameState.GameStatus.SELECT) {
					System.out.println("You don't have so much gold....");
					continue;
				}
				if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.BET && inp<gs.getMinAcceptableBet() && inp>0) {
					System.out.println("You need to bet more than "+gs.getMinAcceptableBet()+" or 0 to indicate that you drop out....");
					continue;
				}
				return inp;
			}
			catch (InputMismatchException ex)
			{
				System.out.println("Incorrect input...");
			}
		}
	}

}
