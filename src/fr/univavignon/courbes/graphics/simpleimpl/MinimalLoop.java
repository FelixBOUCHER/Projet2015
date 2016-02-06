package fr.univavignon.courbes.graphics.simpleimpl;

/*
 * Courbes
 * Copyright 2015-16 L3 Info UAPV 2015-16
 * 
 * This file is part of Courbes.
 * 
 * Courbes is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 2 of the License, or (at your option) any later version.
 * 
 * Courbes is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Courbes. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import fr.univavignon.courbes.common.Board;
import fr.univavignon.courbes.common.Constants;
import fr.univavignon.courbes.common.Direction;
import fr.univavignon.courbes.common.Player;
import fr.univavignon.courbes.common.Profile;
import fr.univavignon.courbes.common.Round;
import fr.univavignon.courbes.graphics.GraphicDisplay;
import fr.univavignon.courbes.physics.PhysicsEngine;
import fr.univavignon.courbes.physics.simpleimpl.PhysicsEngineImpl;

/**
 * Boucle minimale pour débugger le Moteur Physique et le Moteur Graphique.
 * 
 * @author	L3 Info UAPV 2015-16
 */
public class MinimalLoop extends JPanel implements KeyListener, Runnable
{	/** Numéro de série */
	private static final long serialVersionUID = 1L;

	/**
	 * Lance la boucle minimale.
	 * 
	 * @param args
	 * 		Inutilisé.
	 */
	public static void main(String[] args)
	{	JFrame window = new JFrame("Courbes -- Test");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		
		MinimalLoop loop = new MinimalLoop();
		window.add(loop);
		
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		
		loop.start();
	}
	
	private PhysicsEngine pe;
	private GraphicDisplay gd;
	private JPanel boardPanel;
	private JPanel scorePanel;
	private Board board;
	private Round round;
	private Thread loopThread;
	private boolean running;
	
	public MinimalLoop()
	{	Profile p1 = new Profile();
		p1.profileId = 1;
		p1.userName = "Joueur 1";
		Profile p2 = new Profile();
		p2.profileId = 2;
		p2.userName = "Joueur 2";
		
		Player pl1 = new Player();
		pl1.profile = p1;
		pl1.playerId = 0;
		pl1.totalScore = 0;
		pl1.roundScore = 0;
		pl1.currentRank = 1;
		Player pl2 = new Player();
		pl2.profile = p2;
		pl2.playerId = 1;
		pl2.totalScore = 0;
		pl2.roundScore = 0;
		pl2.currentRank = 2;
		Player players[] = {pl1,pl2};
		
		pe = new PhysicsEngineImpl();
		Board board = pe.initDemo(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT, players);
		
		round = new Round();
		round.board = board;
		round.players = players;
		round.pointLimit = 30;
		
		gd = new GraphicDisplayImpl();
		gd.init(round);
		boardPanel = gd.getBoardPanel();
		scorePanel = gd.getScorePanel();
		
		Dimension dim = new Dimension(Constants.BOARD_WIDTH,Constants.BOARD_HEIGHT);
		boardPanel.setPreferredSize(dim);
		boardPanel.setMinimumSize(dim);
		boardPanel.setMaximumSize(dim);
		
		add(boardPanel,BorderLayout.CENTER);
		add(scorePanel,BorderLayout.WEST);
		
		setFocusable(true);
		addKeyListener(this);
	}
	
	public synchronized void start()
	{	running = true;
		loopThread = new Thread(this,"Courbes");
		loopThread.start();
	}
	
	public synchronized void stop()
	{	running = false;
		System.exit(0);
	}
	
	/** Nombre désiré de mises à jour physiques par seconde */
	private final static int UPS = 60;
	/** Nombre désiré de mises à jour graphiques par seconde */
	private final static int FPS = 60;
	/** Afficher les stats dans la console, pour le dégugage */
	private final static boolean SHOW_STATS = true;
	/** Délai associé à une itération forcée */
	private final static long FORCED_ITERATION_STEP = 50;
	
	@Override
	public void run()
	{	final double physDelay = 1000 / UPS;			// délai entre deux màj physiques en ms
		final double graphDelay = 1000 / FPS;			// délai entre deux màj graphiques en ms
		int phyUpdateNbr = 0;							// dernier nombre de màj physiques (stats)						
		int graphUpdateNbr = 0;							// dernier nombre de màj graphiques (stats)
		long elapsedStatTime = 0;						// temps écoulé depuis le dernier affichage des stats
		long elapsedPhysTime = 0;						// temps écoulé depuis la dernière màj physique
		long elapsedGraphTime = 0;						// temps écoulé depuis la dernière màj graphique
		long previousTime = System.currentTimeMillis();	// date de l'itération précédente

		while(running)
		{	long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - previousTime;
			previousTime = currentTime;
			
			// si on est en pause, on se contente de dessiner 
			if(getPause() && !getPassIteration())
			{	gd.update(round);
			}
			
			else
			{	// si on passe simplement une itération
				if(getPassIteration())
				{	setPassIteration(false);
					elapsedTime = FORCED_ITERATION_STEP;	// on fait simplement un pas de FORCED_ITERATION_STEP ms
				}
				
				elapsedPhysTime = elapsedPhysTime + elapsedTime;
				elapsedGraphTime = elapsedGraphTime + elapsedTime;
				elapsedStatTime = elapsedStatTime + elapsedTime;
				
				if(elapsedPhysTime/physDelay >= 1)
				{	Direction[] directions = retrieveDirections();
					pe.update(elapsedPhysTime, directions);
					phyUpdateNbr++;
					elapsedPhysTime = 0;
				}
	
				if(elapsedGraphTime/graphDelay >= 1)
				{	gd.update(round);
					graphUpdateNbr++;
					elapsedGraphTime = 0;
				}
	
				if(elapsedStatTime >= 1000)
				{	if(SHOW_STATS)
						System.out.println("UPS: "+phyUpdateNbr+" -- FPS: "+graphUpdateNbr);
					graphUpdateNbr = 0;
					phyUpdateNbr = 0;
					elapsedStatTime = 0;
				}
			}
		}
	}

	private Direction currentDirection = Direction.NONE;
	
	private synchronized void setDirection(Direction direction)
	{	currentDirection = direction;
	}
	
	private synchronized void unsetDirection(Direction direction)
	{	if(currentDirection == direction)
			currentDirection = Direction.NONE;
	}
	
	private synchronized Direction[] retrieveDirections()
	{	Direction[] result = {currentDirection,Direction.NONE};
		return result;
	}
	
	private boolean pause = true;
	private boolean passIteration = false;
	
	boolean keyState[] = new boolean[1000];
	boolean prevKeyState[] = new boolean[1000];
	
	private synchronized void switchPause()
	{	pause = !pause;
	}
	
	private synchronized boolean getPause()
	{	boolean result = pause;
		return result;
	}
	
	private synchronized void setPassIteration(boolean passIteration)
	{	this.passIteration = passIteration;
	}
	
	private synchronized boolean getPassIteration()
	{	boolean result = passIteration;
		return result;
	}

	@Override
	public void keyPressed(KeyEvent e)
	{	int keyCode = e.getKeyCode();
		if(keyCode==KeyEvent.VK_LEFT)
			setDirection(Direction.LEFT);
		else if(keyCode==KeyEvent.VK_RIGHT)
			setDirection(Direction.RIGHT);
		else if(keyCode==KeyEvent.VK_P)
		{	if(!keyState[KeyEvent.VK_P])
			{	keyState[KeyEvent.VK_P] = true;
				switchPause();
			}
		}
		else if(keyCode==KeyEvent.VK_O)
		{	if(!keyState[KeyEvent.VK_O])
			{	keyState[KeyEvent.VK_O] = true;
				setPassIteration(true);
			}
		}
//System.out.println("Input:"+keyCode);
	}

	@Override
	public void keyReleased(KeyEvent e)
	{	int keyCode = e.getKeyCode();
		if(keyCode==KeyEvent.VK_LEFT)
			unsetDirection(Direction.LEFT);
		else if(keyCode==KeyEvent.VK_RIGHT)
			unsetDirection(Direction.RIGHT);
		else if(keyCode==KeyEvent.VK_P)
			keyState[KeyEvent.VK_P] = false;
		else if(keyCode==KeyEvent.VK_O)
			keyState[KeyEvent.VK_O] = false;
//System.out.println("Input:"+keyCode);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{	//
	}
}

// TODO
// problèmes :
//	- pointillés reprennent pas en rectangle (à cause du cercle final dessiné sur l'ancienne position)
//		>> faudra juste ne pas dessiner l'un des deux disques quand on sort du mode fly ou entrance ou pointillé
//	- le serpent ne bouge pas quand on sort de pause
