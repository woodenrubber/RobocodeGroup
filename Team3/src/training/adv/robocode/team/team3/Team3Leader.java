package training.adv.robocode.team.team3;

import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import robocode.*;
import robocode.util.*;

public class Team3Leader extends TeamRobot {

    //initial
    static Boolean hithithit = false;
    Enemy enemy = new Enemy();
    //pattern match
    private static final int MAX_PATTERN_LENGTH = 30;
    private static Map<String, int[]> matcher = new HashMap<String, int[]>(40000);
    private static String enemyHistory;
    //predict
    private static double FIRE_POWER = 3;
    private static double FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
    private static List<Point2D.Double> predictions = new ArrayList<Point2D.Double>();
    //move
    static final double BASE_MOVEMENT = 180;
    static final double BASE_TURN = Math.PI / 1.5;
    static double movement;
    
	boolean enemyLeaderIsdied = false;
	String enemyLeaderName = null;

    public void run() {
    	setEventPriority("HitRobotEvent", 99);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    	RobotColors c = new RobotColors();

    	
		  c.bodyColor = new Color(55, 150, 199);
		  c.gunColor = new Color(255, 100, 0);
		  c.radarColor = new Color(100, 100, 190);
		  c.scanColor = new Color(55, 55, 255);
		  c.bulletColor = new Color(255, 0, 0);
		  
		  setBodyColor(c.bodyColor);
		  setGunColor(c.gunColor);
		  setRadarColor(c.radarColor);
		  setScanColor(c.scanColor);
		  setBulletColor(c.bulletColor);
    	
		  try {
				// Send RobotColors object to our entire team
				broadcastMessage(c);
			} catch (IOException ignored) {System.out.println("System error!");
			}
		  
        enemyHistory = "";
        movement = Double.POSITIVE_INFINITY;
        setTurnRadarRightRadians(2 * Math.PI);
        do {
            scan();

            if(getTime() - enemy.getTime() > 8) {
            	System.out.println("losing the target!");
            	setTurnRadarRightRadians(2 * Math.PI);
            	execute();
            }
            
            if (getDistanceRemaining() == 0) {
                setAhead(movement = -movement);
                setTurnRightRadians(BASE_TURN);
                hithithit = false;
            }
            doNearWall();
        } while (true);
    }

    ////////////////////////////**EVENT**/////////////////////////////////////
    public void onHitWall(HitWallEvent e) {
        if (Math.abs(movement) > BASE_MOVEMENT) {
            movement = BASE_MOVEMENT;
        }
    }

    public void onRobotDeath(RobotDeathEvent event) {
		String dieRobotName = event.getName();
		
		// if the died robot is the enemy leader
		if(dieRobotName.equals(enemyLeaderName)) {
			enemyLeaderIsdied = true;
			System.out.println("Enemy leader is died!");
		}
		
		// if enemy robot is dead
		  if (!isTeammate(event.getName())) {
//		   enemyLeaderIsdied = true;
		   setTurnLeft(getHeading() % 90);
		   if (getHeading() == 0) {
		    setAhead(getBattleFieldHeight() - getY() - getWidth());
		    System.out.println("Aha");
		    execute();
		   } else if (getHeading() == 90) {
		    setAhead(getBattleFieldWidth() - getX() - getWidth());
		    System.out.println("Aha");
		    execute();
		   } else if (getHeading() == 180) {
		    setAhead(getY() - getWidth());
		    System.out.println("Aha");
		    execute();
		   } else {
		    setAhead(getX() - getWidth());
		    System.out.println("Aha");
		    execute();
		   }
//
//		   // Turn the gun to turn right 90 degrees.
//		   turnRight(90);
//		   while (true) {
//		    moveAfterEnemyLeaderDeath();
//		    execute();
//		   }
		  }
		
		setTurnRadarRightRadians(2 * Math.PI);
		super.onRobotDeath(event);
	}

    public void onHitByBullet(HitByBulletEvent e) {
        setTurnRightRadians(Math.PI/2);
        setAhead(100);
    }

	@Override
	public void onHitRobot(HitRobotEvent event) {
//		if(isTeammate(event.getName())) {
//			return;
//		}
		double eventHeadingRadians = (this.getHeadingRadians() + event.getBearingRadians()) % (2 * Math.PI);
		
		this.setTurnGunRightRadians(normalizeRadians(eventHeadingRadians - this.getGunHeadingRadians()));
		
		if(this.getGunHeat() == 0 && this.getGunTurnRemainingRadians() < Math.PI/6) {
			double firePower = Rules.MAX_BULLET_POWER;
			this.setFire(firePower);
			System.out.println("For the Alliance!");
		}
		
		this.setTurnRightRadians(Math.PI/2);
		this.setAhead(100);
		this.setTurnLeftRadians(Math.PI/3);
		this.setBack(30);
//		execute();
		super.onHitRobot(event);
	}

    public void onScannedRobot(ScannedRobotEvent e) {
    	if (isTeammate(e.getName())) {
			return;
		}
    	
    	if(e.getEnergy() > 150 && enemyLeaderIsdied == false) {
			enemy.update(e, this);
			enemyLeaderName = enemy.name;
//			System.out.println("the current time is " + this.getTime());
		} else if(e.getName().equals(enemy.name) && enemyLeaderIsdied == false){
			enemy.update(e, this);
			System.out.println("update the leader's info");
//			System.out.println("x " + enemy.x + "y " +  enemy.y);
		} else if(enemyLeaderIsdied == true){
			enemy.update(e, this);
			// set the most distant enemy to be the target
//			if(e.getDistance() > enemy.getDistance()) {
//				enemy.update(e, this);
				System.out.println("change the target!");
//			}
		}
//    	System.out.println("current time is " +  getTime());
    	System.out.println(enemy.name +  " x " + enemy.x + "y " +  enemy.y);
    	
        //update
//        enemy.update(e, this);
        //fire
        if (getGunTurnRemaining() == 0 && getEnergy() > 1) {
            smartFire();
        }
        //track
        trackHim();
        // memorize.
        if (enemy.thisStep == (char) -1) {
            return;
        }
        record(enemy.thisStep);
        enemyHistory = (char) enemy.thisStep + enemyHistory;
        // aim
        predictions.clear();
        Point2D.Double myP = new Point2D.Double(getX(), getY());
        Point2D.Double enemyP = project(myP, enemy.absoluteBearingRadians, e.getDistance());
        String pattern = enemyHistory;
        for (double d = 0; d < myP.distance(enemyP); d += FIRE_SPEED) {
            int nextStep = predict(pattern);
            enemy.decode(nextStep);
            enemyP = project(enemyP, enemy.headingRadians, enemy.velocity);
            predictions.add(enemyP);
            pattern = (char) nextStep + pattern;
        }

        enemy.absoluteBearingRadians = Math.atan2(enemyP.x - myP.x, enemyP.y - myP.y);
        double gunTurn = enemy.absoluteBearingRadians - getGunHeadingRadians();
        setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
        
		// Calculate enemy bearing
		double enemyBearingRadians = (this.getHeadingRadians() + enemy.bearingRadians) % (2 * Math.PI); 
		// Calculate enemy's position
		int lastScannedX = (int)(getX() + enemy.distance * Math.sin(enemyBearingRadians));
		int lastScannedY = (int)(getY() + enemy.distance * Math.cos(enemyBearingRadians));

		double enemyHeading = enemy.headingRadians;
		double enemyVelocity = enemy.velocity;

		
		// the possible x, y for enemy in the next time
		double predictedX = lastScannedX + Math.sin(enemyHeading) * enemyVelocity;
		double predictedY = lastScannedY + Math.cos(enemyHeading) * enemyVelocity;

//		System.out.println("x " + predictedX + "y " +  predictedY);
		
		enemy.x = predictedX;
		enemy.y = predictedY;
        
		try {
			// Send enemy position to teammates
			broadcastMessage(enemy);
		} catch (IOException ex) {
			out.println("Unable to send order: ");
			ex.printStackTrace(out);
		}
        
    }
    ////////////////////////////////**MYFUNCTION**/////////////////////////////

    public void smartFire() {
//        FIRE_POWER = Math.min( Rules.MAX_BULLET_POWER, Math.max( Rules.MIN_BULLET_POWER, this.getEnergy() / 60 ) );
        
		if(enemy.getDistance() < 150 && this.getGunTurnRemainingRadians() < Math.PI/12 && this.getGunHeat() == 0) {
			FIRE_POWER = Math.min(300/enemy.getDistance(), Rules.MAX_BULLET_POWER);
			this.setFire(FIRE_POWER);
			System.out.println("too close to the enemy! For the Alliance! Fire is " + FIRE_POWER);
			System.out.println("Enemy distance is " + enemy.getDistance());
			if(enemy.getEnergy() < 10 && this.getEnergy() > enemy.getEnergy()) {
				this.setTurnRightRadians(enemy.getBearingRadians());
				this.setAhead(enemy.getDistance());
			}
		}
		
		if(enemy.getDistance() >= 150 &&  this.getGunTurnRemainingRadians() < Math.PI/30 && this.getGunHeat()==0) {
			FIRE_POWER = Math.min( Rules.MAX_BULLET_POWER, Math.max( Rules.MIN_BULLET_POWER, this.getEnergy() / 70 ) );
			this.setFire(FIRE_POWER);
			System.out.println("1st fire! the power is " + FIRE_POWER);
		}
		
		FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
		
    }

    public void trackHim() {
        double RadarOffset;
        RadarOffset = Utils.normalRelativeAngle(enemy.absoluteBearingRadians - getRadarHeadingRadians());
        setTurnRadarRightRadians(RadarOffset * 1.2);
    }

    private void record(int thisStep) {
        int maxLength = Math.min(MAX_PATTERN_LENGTH, enemyHistory.length());
        for (int i = 0; i <= maxLength; ++i) {
            String pattern = enemyHistory.substring(0, i);
            int[] frequencies = matcher.get(pattern);
            if (frequencies == null) {
                // frequency tables need to hold 21 possible dh values times 17 possible v values
                frequencies = new int[21 * 17];
                matcher.put(pattern, frequencies);
            }
            ++frequencies[thisStep];
        }

    }

    private int predict(String pattern) {
        int[] frequencies = null;
        for (int patternLength = Math.min(pattern.length(), MAX_PATTERN_LENGTH); frequencies == null; --patternLength) {
            frequencies = matcher.get(pattern.substring(0, patternLength));
        }
        int nextTick = 0;
        for (int i = 1; i < frequencies.length; ++i) {
            if (frequencies[nextTick] < frequencies[i]) {
                nextTick = i;
            }
        }
        return nextTick;
    }

    private static Point2D.Double project(Point2D.Double p, double angle,
            double distance) {
        double x = p.x + distance * Math.sin(angle);
        double y = p.y + distance * Math.cos(angle);
        return new Point2D.Double(x, y);
    }
    
	/**
	  * Avoid hitting the walls
	  */
	public void doNearWall() {
	    if(this.getX() > (getBattleFieldWidth() - 50) || this.getX() < 50 || this.getY() > (getBattleFieldHeight() - 50) || this.getY() < 50) {
	     double turnAngle = Math.atan2(this.getBattleFieldWidth()/2 - this.getX(), this.getBattleFieldHeight()/2 - this.getY());
	        turnAngle = normalizeRadians(turnAngle - getHeadingRadians()); 
	        double moveDistance = Point2D.distance(this.getX(),this.getY(),getBattleFieldWidth()/2,getBattleFieldHeight()); 
	        double moveDirection = 1; 
	     if (Math.abs(turnAngle) > Math.PI/2) 
	        {         
	            turnAngle = normalizeRadians( turnAngle + Math.PI/2 );     
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

