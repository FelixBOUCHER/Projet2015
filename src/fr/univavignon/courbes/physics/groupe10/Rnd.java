package fr.univavignon.courbes.physics.groupe10;

import java.util.HashMap;
import java.util.Map;

import fr.univavignon.courbes.physics.PhysicsEngine;
import fr.univavignon.courbes.common.*;

public class Rnd implements PhysicsEngine {
	
	Board board;
	
	
	@Override
	public Board init(int width, int height, int[] profileIds)
	{
		
		board.width = width;
		
		board.height = height;
		
		board.snakes = new Snake[profileIds.length];
		
		board.snakesMap = new HashMap<Position, Integer>();
		
		board.itemsMap = new HashMap<Position, Item>();
		
		snakesCreator(board.snakes, profileIds);
		
		return board;
	}
	
	public void snakesCreator(Snake[] snakes, int[] profileIds)
	{
		for (int i = 0; i < profileIds.length; i++)
		{
			board.snakes[i] = new Snake();
			
			
		}
	}

	
	@Override
	public void update(long elapsedTime, Map<Integer,Direction> commands)
	{
		// Deplacer snakes "mettre à jour les snakes"
		
		//moveSnake(long, commands);
		
		//on trace le tracé et on test si il 

	}
	

	@Override
	public void forceUpdate(Board board) {
		// TODO Auto-generated method stub
		
	}
	
	public void moveSnake(int id, long elapsedTime, fr.univavignon.courbes.physics.groupe10.Direction command)
	{
		
		// On fais tourner le snake
		
		if (command == fr.univavignon.courbes.physics.groupe10.Direction.LEFT)
			board.snakes[id].setCurrentAngle(board.snakes[id].getCurrentAngle() * elapsedTime);

		if (command == fr.univavignon.courbes.physics.groupe10.Direction.RIGHT)
			board.snakes[id].setCurrentAngle( -1 * board.snakes[id].getCurrentAngle() * elapsedTime);
		
		// On regarde si l'angle sors des limites du certcle trigo
		
		// On determine les coordonnée du pixels objectif et la distance qui le separe
		
		//on fixe la position initiale de la tete
		double tmpX = board.snakes[id].currentX;
		double tmpY = board.snakes[id].currentY;
		//c'est egalement les coordonnée de la 'pointe du tracé'
		
		//on deplace la tete vers l'objectif
		board.snakes[id].currentX += (int) (Math.cos(board.snakes[id].currentAngle)*board.snakes[id].movingSpeed*elapsedTime);
		board.snakes[id].currentY += (int) (Math.sin(board.snakes[id].currentAngle)*board.snakes[id].movingSpeed*elapsedTime);
		
		//la 'pointe du tracé' va cherche a rejoindre en ligne droite la tete du Snake
		//en couvrant chaque pixel
		
		//on determine le pas de la tete du tracé en x et en y
		double interX = board.snakes[id].currentX - tmpX;
		double interY = board.snakes[id].currentY - tmpY;
		
		double distance = Math.sqrt( Math.pow(interX, 2) + Math.pow(interY, 2));
		
		double stepX = interX / distance;
		double stepY = interY / distance;
		

		//tant que la pointe du tracé n'a pas rejoint la tete,
		//on dessine le pixel et on incremente
		for (int i = 0; i <= (int) distance ; i++)
		{
			Position po = new Position((int) tmpX, (int) tmpY);
			System.out.println(po.x+" "+po.y);
			
			if(checkCollision(po, id) == 0 || checkCollision(po, id) == 2)	 // 0 Pas de collision --- 2 collision avec item
			{
				board.snakesMap.put(po, id);			// Je déssine la position sur la map
				
			}
			else
			{
				if(checkCollision(po, id) == -1 || checkCollision(po, id) == 1)
			 			// Si il y a une collision avec un snake ou un mur je sors de la boucle
				{					// **Collision */
				break;
				}
			
			}
				
			tmpX += stepX;
			tmpY += stepY;
		}
		
		// on trace le trace sur la HashMap pixel par pixel
		//si l'on tombe sur un pixel deja utilisé, on renvois un entier:
			//0 : pas de colision
			//1 : colision mortelle avec un autre snake
			//2 : colision avec une item
	}
	
	/**Cette fonction detecte si la tete du snake rentre en collision avec des objets, soit avec un mur, un autre snake (ou son propre corps), 
	 * Retourne -1 s'il s'agit d'une collision avec le mur
	 * Retourne 1 s'il y a une collision avec snake
	 * Retourne 2 s'il s'agit d'un d'une collision avec item
	 * Retourne 0 pas de collision
	 * */
	
	public int checkCollision(Position p, int id)
	{
		

		if(p.x <= 0 || p.y <= 0 || p.x >= board.width-1 || p.y >= board.height-1) // Collision avec le mur
		{
			if(board.snakes[id].fly == false)					// mode avion n'est pas activé
			{
				board.snakes[id].state = false;			// Je change l'état du snake (mort)
				return -1; 
			}
		}
		

				
		
		if(board.snakesMap.containsKey(p) && board.snakes[id].fly == false)				// Si il y a une collision et le mode avion n'est pas activé
		{
			board.snakes[id].state = false;
			return 1;										// Collision avec un autre snake
		}
			
		

		
		for (HashMap.Entry<Position, Item> entry : board.itemsMap.entrySet())
		{	
				if(entry.getKey().x == p.x && entry.getKey().y == p.y)				// Collision avec item
				{
					board.snakes[id].currentItems.put(entry.getValue(), (long) entry.getValue().duration); 
					return 2;
				}
		}
		
		return 0;								// Pas de collision
	}
	
	
	
	public void itemEffect()
	{
			for(int j = 1; j < board.snakes.length; j++)
			{
				for (HashMap.Entry<Item, Long> item : board.snakes[j].currentItems.entrySet())
				{	
						if(item.getKey() == Item.USER_SPEED)
						{
							board.snakes[j].movingSpeed += 1 ;
						}
						
						if(item.getKey() == Item.USER_SLOW)
						{
							board.snakes[j].movingSpeed -= 1.5;
						}
						
						if(item.getKey() == Item.USER_BIG_HOLE)
						{
							board.snakes[j].holeRate += 0.5;
						}
						
						if(item.getKey() == Item.OTHERS_SPEED)
						{
							for( int i = 0; i < board.snakes.length; i++)
							{
								if(i != j)
								{
									board.snakes[i].movingSpeed += 1;
								}
							}
						}
						
						if(item.getKey() == Item.OTHERS_THICK)
						{
							for( int i = 0; i < board.snakes.length; i++)
							{
								if(i != j)
								{
									board.snakes[i].headRadius += 2;
								}
							}
						}
						
						if(item.getKey() == Item.OTHERS_SLOW)
						{
							for ( int i = 0; i < board.snakes.length; i++)
							{
								if(i != j)
								{
									board.snakes[i].movingSpeed -= 1;
								}
							}
						}
							
						if(item.getKey() == Item.OTHERS_REVERSE)
						{
							for ( int i = 0; i < board.snakes.length; i++)
							{
								if(i != j)
								{
									board.snakes[i].inversion = true;
								}
							}
						}
						
						if(item.getKey() == Item.COLLECTIVE_THREE_CIRCLES)
						{
							
						}
						
						if(item.getKey() == Item.COLLECTIVE_TRAVERSE_WALL)
						{
							
						}
						
						if(item.getKey() == Item.COLLECTIVE_ERASER)
						{
							// J'efface tout les tracés de snakes (Enlever le tracé de la map)
							board.snakesMap.clear();
							// Je mets la tete des snakes dans la map 
							for(int i = 0; i < board.snakes.length; i++)
							{
								board.snakesMap.put(new Position(board.snakes[i].currentX, board.snakes[i].currentY), board.snakes[i].playerId);
							}
						}
	
			}
	}
		
		
	}
	
	public void generateItem()
	{
		int wdt = (int) (Math.random() * board.width);	// generate a random x
		
		int hgt = (int) (Math.random() * board.height);	// generate a random y
		
		Position p = new Position( wdt , hgt );		// Create a new position
		
		int it = (int)(Math.random()*Item.values().length);				// Item.value is an array that containts different items
		
		Item item = Item.values()[it];		// Generate a random item 																			
		
		board.
		itemsMap.put(p, item);													// add the new item in the itemsMap
		
	}



}
