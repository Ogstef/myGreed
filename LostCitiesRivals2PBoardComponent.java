import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class LostCitiesRivals2PBoardComponent extends JComponent implements MouseListener {

	private static final long serialVersionUID = 1L;
	private LostCitiesRivals2PGameState gs = null;
	
	private static final int BOARD_HEIGHT = 1000;
	private static final int BOARD_WIDTH = 1800;
	
	private boolean canMakeMove = false;
	
	private LostCitiesRivals2PGUI gui;
	
	private BufferedImage imExpeditions, imCoin, imCoinPile, imCardB, imFeet1, imFeet2;
	private BufferedImage[] imCardExp, imBets;
	private Color[] cardColors;
	private Font cardFont, goldFont;
	
	private int cardHeight, cardWidth, cardNumberSize=40;
	
	private boolean[] isCardSelected;
	
	public LostCitiesRivals2PBoardComponent(LostCitiesRivals2PGUI gui, LostCitiesRivals2PGameState gs) {
		this.gui = gui;
		this.gs = gs;
		setSize(BOARD_WIDTH, BOARD_HEIGHT);
		addMouseListener(this);
		
		cardFont = new Font("Arial", Font.BOLD, 30);
		goldFont = new Font("Arial", Font.BOLD, 60);
		try { //"YBKGR";
			imExpeditions = ImageIO.read(new File("images/expeditions.jpg"));
			imCoin = ImageIO.read(new File("images/coin.png"));
			imCoinPile = ImageIO.read(new File("images/coinpile.png"));
			imFeet1 = ImageIO.read(new File("images/feet1.png"));
			imFeet2 = ImageIO.read(new File("images/feet2.png"));
			imCardB = ImageIO.read(new File("images/cardback.png"));
			cardHeight = imCardB.getHeight();
			cardWidth = imCardB.getWidth();
			imCardExp = new BufferedImage[5];
			imBets = new BufferedImage[5];
			cardColors = new Color[5];
			imCardExp[0] = ImageIO.read(new File("images/cardY.png"));
			imBets[0] = ImageIO.read(new File("images/0y.png"));
			cardColors[0] = new Color(240,200,0); //Color.YELLOW;
			imCardExp[1] = ImageIO.read(new File("images/cardB.png"));
			imBets[1] = ImageIO.read(new File("images/0b.png"));
			cardColors[1] = Color.BLUE;
			imCardExp[2] = ImageIO.read(new File("images/cardK.png"));
			imBets[2] = ImageIO.read(new File("images/0k.png"));
			cardColors[2] = Color.BLACK;
			imCardExp[3] = ImageIO.read(new File("images/cardG.png"));
			imBets[3] = ImageIO.read(new File("images/0g.png"));
			cardColors[3] = Color.GREEN;
			imCardExp[4] = ImageIO.read(new File("images/cardR.png"));
			imBets[4] = ImageIO.read(new File("images/0r.png"));
			cardColors[4] = Color.RED;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isCardSelected = new boolean[LostCitiesRivals2PGameState.DISPLAY_SIZE];
		clearSelectedCardsFromDisplay();
		repaint();
	}
	
	/**
	 * This clears the selection of cards after picking
	 */
	void clearSelectedCardsFromDisplay()
	{
		for (int i=0; i<isCardSelected.length; i++)
			isCardSelected[i]=false;
	}
	
	/**
	 * Call this method when the GUI should provide the next move
	 * It's a bit of a hack the way this is made!
	 */
	public void registerMove(LostCitiesRivals2PGameState gs)
	{
		this.gs = gs;
		canMakeMove = true;
		repaint();	
	}
	
	/**
	 * Paints the board
	 */
	public void paintComponent(Graphics g)
	{	
		Graphics2D g2 = (Graphics2D) g;
		//g2.translate(0, 0);
		Font oldFont = g2.getFont();
		Stroke oldStroke = g2.getStroke();
		Color oldColor = g2.getColor();
		
		g2.setColor(Color.BLUE);
		g2.setFont(goldFont);
		if (gs.getGoldPaid()>0)
		{
			g2.drawImage(imCoinPile, 208, 10, imCoinPile.getWidth(), imCoinPile.getHeight(), null);
			g2.drawString(""+gs.getGoldPaid(), 250, 70);
		}
		// draw display cards and left piles
		byte[] cards1 = gs.getCardsAvailableInDisplay();
		byte[] cards2 = gs.getFutureCardsInDisplay();
		int x=350;
		g2.setStroke(new BasicStroke(5));
		
		for (int i=0; i<cards1.length; i++)
		{
			drawCard(g2, x, 5, cards1[i], true, 0);
			if (isCardSelected[i])
			{
				g2.setColor(Color.CYAN);
				g2.drawRect(x, 5, cardWidth, cardHeight);
			}
			x += 100;
		}
		for (int i=0; i<cards2.length; i++)
		{
			drawCard(g2, x, 5, cards2[i], false, 0);
			x += 100;
		}
		for (int i=4-gs.getRoundNo(); i>0; i--)
			g2.drawImage(imCardB, x+50*i, 5-10*i, cardWidth, cardHeight, null);
		if (gs.getCardsLeftInRound()>0)
		{
			g2.drawImage(imCardB, x, 5, cardWidth, cardHeight, null);
			g2.setColor(Color.RED);
			g2.setFont(goldFont);
			g2.drawString(((gs.getCardsLeftInRound()<10)?" ":"")+gs.getCardsLeftInRound(), x+10, 70);
		}
		
		// status of game
		g2.setColor(Color.BLUE);
		g2.setFont(goldFont);
		if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.SELECT)
			g2.drawString("SELECT UP TO "+(gs.getMinAcceptableBet()-1)+" CARDS", 500, 220);
		else if (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.BET)
			g2.drawString("MINIMUM BET: "+gs.getMinAcceptableBet(), 650, 220);
		
		// Players and Gold
		g2.setColor(Color.RED);
		g2.setFont(goldFont);
		if (gs.isP1turn())
			g2.drawString("PLAYER 1", 100, 220);
		else
			g2.drawString("PLAYER 2", BOARD_WIDTH-400, 220);
		g2.setColor(Color.BLUE);
		g2.drawImage(imCoin, 5, 150, imCoin.getWidth(), imCoin.getHeight(), null);
		if (gs.getGoldP1()>0)
		{
			g2.drawString(((gs.getGoldP1()<10)?" ":"")+gs.getGoldP1(), 10, 210);
		}
		g2.drawImage(imCoin, BOARD_WIDTH-5-imCoin.getWidth(), 150, imCoin.getWidth(), imCoin.getHeight(), null);
		if (gs.getGoldP2()>0)
		{
			g2.drawString(((gs.getGoldP2()<10)?" ":"")+gs.getGoldP2(), BOARD_WIDTH-imCoin.getWidth(), 210);
		}		
		
		// cards being selected
		
		// draw expeditions
		g2.drawImage(imExpeditions, (BOARD_WIDTH-imExpeditions.getWidth())/2, 250, imExpeditions.getWidth(), imExpeditions.getHeight(), null);
		
		for (int i=0; i<LostCitiesRivals2PGameState.NUM_EXPEDITIONS; i++)
		{
			drawOneExpedition(g2, false, i, gs.getExpeditionP1(i));
			drawOneExpedition(g2, true, i, gs.getExpeditionP2(i));
		}
		
		//drawCard(g2, 0, 0, (byte)2, true, 0);
		
		//restore previous color, font and stroke
		g2.setStroke(oldStroke);
		g2.setColor(oldColor);
		g2.setFont(oldFont);
		
	}
	
	/**
	 * Draws the cards of one expedition
	 * @param g2 Graphics2D object used to draw
	 * @param toRightSide whether the expedition goes to the right (i.e. for player 2)
	 * @param exp number of the expedition (affects color and y coord)
	 * @param expeditionCards an array of how many cards of each value there are in the expedition
	 */
	private void drawOneExpedition(Graphics2D g2, boolean toRightSide, int exp, byte[] expeditionCards)
	{
		int totalCards=0;
		for (int i=0; i<expeditionCards.length; i++)
			totalCards += expeditionCards[i];
		if (totalCards<1) return;
		int cardSpacing = (BOARD_WIDTH-50-imExpeditions.getWidth())/(2*totalCards)-1;
		if (cardSpacing>100) cardSpacing=100;
		int x;
		if (toRightSide)
		{
			x=(BOARD_WIDTH+10+imExpeditions.getWidth())/2;
		}
		else
		{
			x=(BOARD_WIDTH-10-imExpeditions.getWidth())/2 - cardWidth;
			cardSpacing = -cardSpacing;
		}
		for (int i=0; i<expeditionCards.length; i++)
		{
			for (int k=0; k<expeditionCards[i]; k++)
			{
				drawCard(g2, x, 260+exp*135, (byte)(exp*LostCitiesRivals2PGameState.MAX_VALUE+i+1), true, toRightSide?-1:1);
				x += cardSpacing;
			}
		}		
	}
	
	/**
	 * Draws one cards 
	 * @param g2 the graphics2D object used to draw
	 * @param x coordinates of card
	 * @param y coordinates of card
	 * @param card the card number
	 * @param revealed whether the cards is revealed (it would not be revealed/available in the display)
	 * @param xoffset -/0/+ whether to draw feet/bet on left/center/right of card
	 */
	private void drawCard(Graphics2D g2, int x, int y, byte card, boolean revealed, int xoffset)
	{
		byte exp = LostCitiesRivals2PGameState.cardNum2Exp(card);
		byte val = LostCitiesRivals2PGameState.cardNum2Val(card);
		if (exp<0) return; // this should not happen!
		g2.drawImage(revealed?imCardExp[exp]:imCardB, x, y, cardWidth, cardHeight, null);
		if (val<=1)
		{
			int dx;
			if (xoffset<0) dx=1;
			else if (xoffset>0) dx=cardWidth-1-imBets[exp].getWidth();
			else dx=(cardWidth-imBets[exp].getWidth())/2;
			g2.drawImage(imBets[exp], x+dx, y+1, imBets[exp].getWidth(), imBets[exp].getHeight(), null);
		}
		else
		{
			g2.setColor(cardColors[exp]);
			g2.fillRect(x+(cardWidth-cardNumberSize)/2, y+cardHeight-cardNumberSize-1, cardNumberSize, cardNumberSize);
			g2.setColor(Color.WHITE);
			g2.setFont(cardFont);
			g2.drawString(((val>=10)?"":" ")+val, x+(cardWidth-cardNumberSize)/2+3, y+cardHeight-8);
			BufferedImage feet = (val>LostCitiesRivals2PGameState.MAX_DOUBLECARD_VALUE)?imFeet2:imFeet1;
			int dx;
			if (xoffset<0) dx=1;
			else if (xoffset>0) dx=cardWidth-1-feet.getWidth();
			else dx=(cardWidth-feet.getWidth())/2;
			g2.drawImage(feet, x+dx, y+1, feet.getWidth(), feet.getHeight(), null);
		}
	}
	
	/**
	 * Checks if a mouse gets pressed and implements the play functionality
	 */
	public void mouseClicked(MouseEvent e) {
		//System.out.printf("Mouse click: %d,%d\n", e.getX(), e.getY());
		if (gs==null || !canMakeMove) return;
		//gs.printState();
		int x = e.getX(), y = e.getY();
		if (y<145 && gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.REVEAL) // clicked on cards - then reveal one more card
		{
			canMakeMove=false;
			gui.setMove(0);
			return;
		}
		if (y>145 && y<230 && (gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.BET || gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.REVEAL))
		{
			if ((gs.isP1turn() && x<90) || (!gs.isP1turn() && x>BOARD_WIDTH-90)) // the player wants to bet
			{
				// bet here
				int plgold = (gs.isP1turn())?gs.getGoldP1():gs.getGoldP2();
				int currentBet = gs.getMinAcceptableBet();
				while (true)
				{
					Object[] options = new String[]{"-", ""+currentBet, "+"};
					if (currentBet==gs.getMinAcceptableBet())
						options[0]="X";
					if (currentBet==plgold)
						options[2]="|";
					int n = JOptionPane.showOptionDialog(null, "+/- bet amount, or select it to bid",
							"Select your bet", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
							null, options, options[1]);
					if (n==1) break; // bet is ok
					if (n==0)
						if (currentBet>gs.getMinAcceptableBet()) currentBet--;
						else {currentBet=0; break;} // drop out of betting
					if (n==2) 
						if (currentBet<plgold) currentBet++;
				}
				
				canMakeMove=false;
				gui.setMove(currentBet);
				return;
			}
		}
		// WARNING: the following will not check if you can pick up the cards etc. (the gamestate will and discard some part of the wrong move in that case)
		if (y<140 && gs.getGameStatus()==LostCitiesRivals2PGameState.GameStatus.SELECT) // clicked on cards - selection phase
		{
			int discard = -1;// i.e. don't discard
			boolean discarding=false;
			if (SwingUtilities.isRightMouseButton(e))
			{	// select card to drop and exit selection
				discarding=true;
				//System.out.println("EXITING!!!");
			}
			// which card was clicked?
			int i = (x-342)/100;
			//System.out.println("DEBUG:"+i);
			if (i>=0 && i<LostCitiesRivals2PGameState.DISPLAY_SIZE)
			{
				if (!discarding)
				{
					isCardSelected[i] = !isCardSelected[i];
					repaint();
					return;
				}
				else
					discard = i;
			}
			if (discarding)
			{
				int total = 0;
				byte[] cards1 = gs.getCardsAvailableInDisplay();
				for (int c=0; c<cards1.length; c++)
					if (isCardSelected[c])
						total = total*10+(c+1);
				total = total*10+discard+1;
				clearSelectedCardsFromDisplay();
				canMakeMove=false;
				gui.setMove(total);				
			}
			else return; // do nothing
		}
	/*	
		// just in case return a move of zero
		System.err.println("Error: should not arrive at this choice in the GUI!");
		canMakeMove=false;
		gui.setMove(0);
		*/
	}

	// We don't need these methods from the interface
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}
