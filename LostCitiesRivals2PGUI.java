import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LostCitiesRivals2PGUI extends JFrame implements LostCitiesRivals2PInput {

	private static final long serialVersionUID = 1L;
	
	private LostCitiesRivals2PBoardComponent mainBoard;
	private int move;
	
	public LostCitiesRivals2PGUI(LostCitiesRivals2PGameState gs)
	{
		mainBoard = new LostCitiesRivals2PBoardComponent(this, gs);
		setTitle("Lost Cities Rivals 2P Board Game (modified by I. A. Vetsikas)");
		getContentPane().add(mainBoard);
		setSize(mainBoard.getSize().width+18,mainBoard.getSize().height+47);
		System.out.println("SIZE OF GUI: "+mainBoard.getSize().width+","+mainBoard.getSize().height);
		setVisible(true);
		
		ImageIcon icon = new ImageIcon("images/logo.jpg");
		JOptionPane.showMessageDialog(null, "You are only allowed to use this game for personal use.\nThe images are copyrighted by the owner of the Lost Cities game.\nBy using this game you assume full responsibility for its use.", "Lost Cities Rivals 2P", JOptionPane.INFORMATION_MESSAGE, icon);
	}

	/**
	 * Call this to give the new move
	 * @param newMove
	 */
	public void setMove(int newMove)
	{
		this.move = newMove;
	}
	
	public int nextMove(LostCitiesRivals2PGameState gs) {
		move = -1000;
		mainBoard.registerMove(gs);
		while (move<-10)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		String s;
		if (gs.isP1turn())
			s = "P1 ";
		else s= "P2 ";
		System.out.println(s+"Read move:"+move);
		return move;
	}

}
