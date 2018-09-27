package training.adv.robocode.team.team3;

import java.awt.geom.Point2D;

/**
 * Copyright (c) 2001-2016 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 */

import robocode.Droid;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

import robocode.HitRobotEvent;

/**
 * MaggieDroid - a robot like walls by Maggie.
 * <p/>
 * Follows orders of team leader.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */
public class MaggieDroid extends TeamRobot implements Droid {

	double moveAmount; // How much to move
	boolean leaderDeath = false;
	boolean enemyLeaderDeath = false;
	boolean getEnemyLeaderName = false;
	double enemyDistance;
	String leaderName;
	String enemyLeaderName;

	/**
	 * run:  Droid's default behavior
	 */
	public void run() {
		
		// turnLeft to face a wall.
		// getHeading() % 90 means the remainder of
		// getHeading() divided by 90.
		turnLeft(getHeading() % 90);
		if (getHeading() == 0) {
			ahead(getBattleFieldHeight() - getY() - getWidth());
		} else if (getHeading() == 90) {
			ahead(getBattleFieldWidth() - getX() - getWidth());
		} else if (getHeading() == 180) {
			ahead(getY() - getWidth());
		} else {
			ahead(getX() - getWidth());
		}

		// Turn the gun to turn right 90 degrees.
		turnRight(90);

		while (true) {
			// Move at corner
			while (getX() > getBattleFieldWidth()/2 && getY() < getBattleFieldHeight()/3 && (leaderDeath == false || enemyLeaderDeath == true)) {
				if (getHeading() == 0) {
					ahead(300 - getWidth());
					back(getY() - getWidth());
					turnRight(90);
				} else if (getHeading() == 90) {
					back(400 - getWidth());
					ahead(getBattleFieldWidth() - getX() - getWidth());
					turnLeft(90);
				} else if (getHeading() == 180) {
					back(300 - getWidth());
					ahead(getY() - getWidth());
					turnRight(90);
				} else if (getHeading() == 270) {
					ahead(400 - getWidth());
					back(getBattleFieldWidth() - getX() - getWidth());
					turnLeft(90);
				}
			}
			
			// Move up the wall
			if (getHeading() == 0) {
				ahead(getBattleFieldHeight() - getY() - getWidth());
			} else if (getHeading() == 90) {
				ahead(getBattleFieldWidth() - getX() - getWidth());
			} else if (getHeading() == 180) {
				ahead(getY() - getWidth());
			} else {
				ahead(getX() - getWidth());
			}
			
			// Turn to the next wall
			turnRight(90);
		}
	}

	/**
	 * onMessageReceived:  What to do when our leader sends a message
	 */
	public void onMessageReceived(MessageEvent e) {
		// Fire at a point
		if (e.getMessage() instanceof Enemy) {
			
			Enemy enemy = (Enemy) e.getMessage();
			double predictedX = enemy.x;
			double predictedY = enemy.y;
			leaderName = e.getSender();
			
			if (enemy.getEnergy() > 150 && enemy.getTime() < 50 && getEnemyLeaderName == false) {
				enemyLeaderName = enemy.getName();
				getEnemyLeaderName = true;
			}
												
			double theta = Utils.normalAbsoluteAngle( Math.atan2( predictedX - this.getX(), predictedY - this.getY() ) );
			this.setTurnGunRightRadians( Utils.normalRelativeAngle( theta - this.getGunHeadingRadians() ) );
			
			enemyDistance = Point2D.distance(getX(), getY(), predictedX, predictedY);
			if (enemyDistance < 800) {
				fire(3);
			}
		} 
		else if (e.getMessage() instanceof RobotColors) {
			// Set our colors
			RobotColors c = (RobotColors) e.getMessage();

			setBodyColor(c.bodyColor);
			setGunColor(c.gunColor);
			setRadarColor(c.radarColor);
			setScanColor(c.scanColor);
			setBulletColor(c.bulletColor);
		}

	}

	/**
	 * onHitRobot: fire!
	 */
	public void onHitRobot(HitRobotEvent e) {
		if(!isTeammate(e.getName())) {
			double absBearing = e.getBearing() + getHeading();
			turnGunRight((absBearing - getGunHeading()) % 360);
			fire(3);
		}
	}
	
	/**
	 * onRobotDeath: after enemy leader 
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		String dieRobotName = event.getName();
	  
		// if the died robot is the enemy leader
		if(dieRobotName.equals(leaderName)) {
			System.out.println("Our leader " + leaderName + "is died!");
			leaderDeath = true;
		}
	 
		if (dieRobotName.equals(enemyLeaderName)) {
			enemyLeaderDeath = true;
		}
	  
		super.onRobotDeath(event);
	}

}
