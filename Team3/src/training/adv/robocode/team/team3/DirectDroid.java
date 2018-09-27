package training.adv.robocode.team.team3;

/**
 * Copyright (c) 2001-2016 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 */

import robocode.Droid;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.TeamRobot;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * SimpleDroid - a sample robot by Mathew Nelson.
 * <p/>
 * Follows orders of team leader.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */
public class DirectDroid extends TeamRobot implements Droid {
	
	double enemyDistance;
	double predictedX;
	double predictedY;
	Enemy enemy;
	double enemyBearingRadians;
	 
	boolean leaderDeath = false;
	double lastBulletSpeed = 15;
	long moveTime = 1;
	int sameDirectionCounter = 0;
	int moveDirection = 1;
	
	boolean enemyLeaderDeath = false;
	 String enemyLeaderName;
	 boolean getEnemyLeaderName = false;
	 String leaderName;
	
	
	
	/**
	 * run:  Droid's default behavior
	 */
	public void run() {
		setEventPriority("HitRobotEvent", 100);
		out.println("MyFirstDroid ready.");
	}
	
	// the direction of our droids
	int droidDirection = 1;

	/**
	 * onMessageReceived:  What to do when our leader sends a message
	 */
	public void onMessageReceived(MessageEvent e) {
		// Fire at a point
		if (e.getMessage() instanceof Enemy) {
			
//			turnRightRadians(Math.PI/4);
//			ahead(100);
			
			Enemy enemy = (Enemy) e.getMessage();
			double predictedX = enemy.x;
			double predictedY = enemy.y;
			leaderName = e.getSender();

			
//			System.out.println("the current target is " + enemy.name);
			
			enemyDistance = Point2D.distance(getX(), getY(), predictedX, predictedY);
			
			if(this.getDistanceRemaining() == 0) {
				droidDirection = droidDirection * -1;
				this.setAhead(enemyDistance/2);
			}
			
			enemyBearingRadians = enemy.headingRadians - this.getHeadingRadians();
			
			this.setTurnRightRadians(enemyBearingRadians + Math.PI/2 - 0.3 * droidDirection);
			
//			double direction = (enemy.headingRadians - this.getGunHeadingRadians()) % (2 * Math.PI);
			
			double theta = Utils.normalAbsoluteAngle( Math.atan2( predictedX - this.getX(), predictedY - this.getY() ) );
			this.setTurnRightRadians( Utils.normalRelativeAngle( theta - this.getGunHeadingRadians() ) );
//			this.setTurnRadarRightRadians( Utils.normalRelativeAngle( direction - this.getRadarHeadingRadians() ) );
			
			if(getGunHeat() == 0 && enemy.getDistance() < 300) {
				this.setFire(3);
				System.out.println("fire");
			}
			
			// if the droid is close enough too the enemy
//			if(enemyDistance < 100  &&  this.getGunTurnRemainingRadians() < Math.PI/15 && this.getGunHeat()==0 ) {
//				double firePower = 200/enemyDistance;
//				this.setFire(firePower);
//				System.out.println("2st fire! the power is " + firePower);
//			}
//			
//			// if the droid is far away from enemy
//			if(enemyDistance >= 100 &&  this.getGunTurnRemainingRadians() < Math.PI/30 && this.getGunHeat()==0 ) {
//				double firePower = Math.min( Rules.MAX_BULLET_POWER, Math.max( Rules.MIN_BULLET_POWER, this.getEnergy() / 30 ) );
//				this.setFire(firePower);
//				System.out.println("1st fire! the power is " + firePower);
//			}
			
			doNearWall();
			
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
	
	
	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		if(isTeammate(event.getName())){
			return;
		}
		if(leaderDeath == true) {
			this.setTurnRightRadians(event.getBearingRadians());
			this.setFire(Math.min( Rules.MAX_BULLET_POWER, Math.max( Rules.MIN_BULLET_POWER, this.getEnergy() / 30 ) ));
			execute();
		}
		
		super.onHitByBullet(event);
	}
	
	@Override
	  public void onRobotDeath(RobotDeathEvent event) {
	   String dieRobotName = event.getName();
	   
	   // if the died robot is the enemy leader
	   if(dieRobotName.equals(leaderName)) {
	    leaderDeath = true;
	    while (true) {
	     moveAfterLeaderDeath();
	     execute();
	    }
	   }
	// if out leader and enemy robot both dead
	   if (dieRobotName.equals(enemyLeaderName) && leaderDeath == true) {
	    enemyLeaderDeath = true;
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
	     moveAfterEnemyLeaderDeath();
	     execute();
	    }
	   }
	   
//	   if(!isTeammate(event.getName())) {
//		   setTurnRightRadians(Math.PI);
//		   setAhead(100);
//		   execute();
//	   }
//	   
	   super.onRobotDeath(event);
	  }
	  
	  @Override
	  public void onHitRobot(HitRobotEvent event) {
	   double firePower = 0;
	   if(!isTeammate(event.getName())) {
	    System.out.println("Enemy hits me!");
	    double eventHeadingRadians = (this.getHeadingRadians() + event.getBearingRadians()) % (2 * Math.PI);
	   
	    this.turnRightRadians(eventHeadingRadians - this.getHeadingRadians());
	   
	    if(getGunHeat() == 0) {
	     firePower = Rules.MAX_BULLET_POWER;
	     this.fire(firePower); 
	     System.out.println("For the Alliance!");
	    }
	    execute();
	   
	    super.onHitRobot(event);
	   } else {
		   turnRightRadians(Math.PI/2);
		   ahead(50);
	   }
	  }
	
	
	public void doNearWall() {
	     if(this.getX() > (getBattleFieldWidth() - 50) || this.getX() < 50 || this.getY() > (getBattleFieldHeight() - 50) || this.getY() < 50) {
	    	 System.out.println("danger!");
	    	 double turnAngle = Math.atan2(this.getBattleFieldWidth()/2 - this.getX(), this.getBattleFieldHeight()/2 - this.getY());
	         turnAngle = normalizeRadians(turnAngle - getHeadingRadians()); 
	         double moveDistance = Point2D.distance(this.getX(),this.getY(),getBattleFieldWidth()/2,getBattleFieldHeight()); 
	         double moveDirection = 1; 
	      if (Math.abs(turnAngle) > Math.PI/2) 
	         {         
	             turnAngle = normalizeRadians( turnAngle + Math.PI );     
	             moveDirection = -1; 
	         }
	      setTurnRightRadians(turnAngle); 
	      setAhead(moveDirection * moveDistance * 0.3);
	     }
	}
	public double normalizeRadians (double angle) { 
	     if (angle < -Math.PI) 
	         angle += 2 * Math.PI; 
	     if (angle > Math.PI) 
	         angle -= 2 * Math.PI; 
	     return angle; 
	 }
	
	
	public void moveAfterLeaderDeath() {
		  double wallStick = 120;
		  if(getTime() % 16 == 0) {    
		   // Change the wall stick distance, to make us even more unpredictable
		   wallStick  = 120 + Math.random() * 60;
		  }
		  double absBearing = enemyBearingRadians + getHeadingRadians();
		  double distance = enemyDistance + (Math.random()-0.5)*5.0;

		  // Radar Turn
		  double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians() );

		  double baseScanSpan = (18.0 + 36.0*Math.random());
		  // Distance we want to scan from middle of enemy to either side
		  double extraTurn = Math.min(Math.atan(baseScanSpan / distance), Math.PI/4.0);
		  setTurnRadarRightRadians(radarTurn + (radarTurn < 0 ? -extraTurn : extraTurn));
		  // Movement
		  if(--moveTime  <= 0) {
		            distance = Math.max(distance, 100 + Math.random()*50) * 1.25;
		            moveTime = 50 + (long)(distance / lastBulletSpeed );

		            ++sameDirectionCounter;
		            if(Math.random() < 0.5 || sameDirectionCounter > 5) { //fate or too long no change
		                moveDirection = -moveDirection;
		                sameDirectionCounter = 0;
		            }
		        }
		  double goalDirection = absBearing-Math.PI/2.0*moveDirection;

		        // This is too clean for crazy! Add some real randomness.
		        goalDirection += (Math.random()-0.5) * (Math.random()*1.0 + 1.0);

		        // Smooth around the walls, if we smooth too much, reverse direction!
		        double x = getX();
		        double y = getY();
		        double smooth = 0;

		        Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
		        while (!fieldRect.contains(x+Math.sin(goalDirection)*wallStick, y+ Math.cos(goalDirection)*wallStick)) {
		            // turn a little toward enemy and try again
		            goalDirection += moveDirection * 0.1;
		            smooth += 0.1;
		        }

		        // If we smoothed to much, then reverse direction.
		        if(smooth > 0.5 + Math.random()*0.125) {
		            moveDirection  = -moveDirection;
		            sameDirectionCounter  = 0;
		        }
		        double turn = Utils.normalRelativeAngle(goalDirection - getHeadingRadians());
		  
		        if(getX() == getBattleFieldWidth() - getHeight()/2 - 10 || getBattleFieldWidth() - getX() == getBattleFieldWidth() - getHeight()/2 - 10 ||
		          getY() == getBattleFieldHeight() - getHeight()/2 - 10 || getBattleFieldHeight() - getY() == getBattleFieldHeight() - getHeight()/2 - 10) {
		         if(getDistanceRemaining() < 0) {
		          stop();
		          setAhead(100);
		          while(getDistanceRemaining() != 0) {}
		         } else {
		          stop();
		          setBack(100);
		          while(getDistanceRemaining() != 0) {}
		         }
		        }
		        // Adjust so we drive backwards if the turn is less to go backwards
		        if(getDistanceRemaining() == 0 && getTurnRemaining() == 0) {
		         if (Math.abs(turn) > Math.PI/2) {
		             turn = Utils.normalRelativeAngle(turn + Math.PI);
		             setBack(100);
		         } else {
		          setAhead(100);
		         }

		         setTurnRightRadians(turn);
		        }
		        
		        if(getTime() % 30 == 0) {
		        	setFire(1);
		        }
		        
		        doNearWall();
		    }
	
	
	
	public void moveAfterEnemyLeaderDeath() {
		  while (getX() < getBattleFieldWidth()/3 && getY() > getBattleFieldHeight()/2) {
		   if (getHeading() == 0) {
		    back(300 - getWidth());
		    ahead(getBattleFieldHeight() - getY() - getWidth());
		    turnRight(90);
		   } else if (getHeading() == 90) {
		    ahead(400 - getWidth());
		    back(getX() - getWidth());
		    turnLeft(90);
		   } else if (getHeading() == 180) {
		    ahead(300 - getWidth());
		    back(getBattleFieldHeight() - getY() - getWidth());
		    turnRight(90);
		   } else if (getHeading() == 270) {
		    back(400 - getWidth());
		    ahead(getX() - getWidth());
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
