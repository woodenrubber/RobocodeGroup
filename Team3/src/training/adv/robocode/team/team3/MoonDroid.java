package training.adv.robocode.team.team3;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.Droid;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

public class MoonDroid extends TeamRobot implements Droid {
	
	Color getRandomColor[] = new Color[4];
	Enemy enemy = new Enemy();
	double robotPositionX;
    double robotPositionY;
	private double wallStick = 120;
	private int sameDirectionCounter = 0;
	private long moveTime = 1;
	private static double lastBulletSpeed = 15;
	private static int moveDirection = 1;
	private boolean inPosition = false;
	private boolean turnYet = false;
	private int droidDirection = 1;
	
	boolean helpingLeader = false;
	String messageSender;
	
	
	public void run() {
		setAdjustGunForRobotTurn(true);	
		setAdjustRadarForGunTurn(true);
		this.setMaxVelocity(Rules.MAX_VELOCITY);
		this.setMaxTurnRate(Rules.MAX_TURN_RATE);
		enemy.reset();
		
//		setBodyColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
//        setGunColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
//        setRadarColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
//        setBulletColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
//        setScanColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
	 
	    while(true) { 
	        move();
	      	execute();
	    }
	}
	public void onMessageReceived(MessageEvent event) {

        if(event.getMessage() instanceof RobotColors) {
        	RobotColors c = (RobotColors) event.getMessage();
			setBodyColor(c.bodyColor);
			setGunColor(c.gunColor);
			setRadarColor(c.radarColor);
			setScanColor(c.scanColor);
			setBulletColor(c.bulletColor);
        } else if(event.getMessage() instanceof Enemy) {
			
			Enemy enemy = (Enemy)event.getMessage();
			double predictedX = enemy.x;
			double predictedY = enemy.y;
			
//			System.out.println("the current target is " + enemy.getName());
			
			double enemyDistance = Point2D.distance(getX(), getY(), predictedX, predictedY);
			
			if(this.getDistanceRemaining() == 0) {
				droidDirection  = droidDirection * -1;
				this.setAhead(enemyDistance/2 * droidDirection);
			}
			
			double enemyBearingRadians = enemy.getHeadingRadians() - this.getHeadingRadians();
			
			this.setTurnGunRightRadians(enemyBearingRadians + Math.PI/2 - 0.5 * droidDirection);
			
			double direction = (enemy.getHeadingRadians() - this.getGunHeadingRadians()) % (2 * Math.PI);
			
			double theta = Utils.normalAbsoluteAngle( Math.atan2( predictedX - this.getX(), predictedY - this.getY() ) );
			this.setTurnGunRightRadians( Utils.normalRelativeAngle( theta - this.getGunHeadingRadians() ) );
			this.setTurnRadarRightRadians( Utils.normalRelativeAngle( direction - this.getRadarHeadingRadians() ) );
			
			fire(3);
		}
        else if(event.getMessage() instanceof Integer) {
			
			int code;
			// if the leader's energy is too low
			
			code = (Integer)event.getMessage();
			if(code == 1 && helpingLeader == false) {
				System.out.println("out leader's energy is too low!");
				messageSender = event.getSender();
				helpingLeader = true;
				try {
					sendMessage(messageSender, new Point(getX(), getY()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}else if(code == 2){
				System.out.println("I am the choosing one!");
//				stop();

			}
		}
		
	}
	
	private void doGun (Enemy enemy2) {
		if (enemy2.none())
			return;

//		double firePower = Math.min(700 / enemy2.getDistance(), 3);
		double firePower = 3;
		double bulletSpeed = 20 - firePower * 3;
		long time = (long)(enemy2.getDistance() / bulletSpeed);

		double futureX = enemy2.getFutureX(time);
		double futureY = enemy2.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);

		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10 && getEnergy() > 1) {
			setFire(firePower);
		}
	}
	
	public void move () {
		if(getTime() % 16 == 0) {			
            // Change the wall stick distance, to make us even more unpredictable
            wallStick  = 120 + Math.random()*60;
        }
		double absBearing = enemy.getBearingRadians() + getHeadingRadians();
		double distance = enemy.getDistance() + (Math.random()-0.5)*5.0;

        /* Radar Turn */
        double radarTurn = Utils.normalRelativeAngle(absBearing
                // Subtract current radar heading to get turn required
                - getRadarHeadingRadians() );

        double baseScanSpan = (18.0 + 36.0*Math.random());
        // Distance we want to scan from middle of enemy to either side
        double extraTurn = Math.min(Math.atan(baseScanSpan / distance), Math.PI/4.0);
        setTurnRadarRightRadians(radarTurn + (radarTurn < 0 ? -extraTurn : extraTurn));
        /* Movement */
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

        /* This is too clean for crazy! Add some real randomness. */
        goalDirection += (Math.random()-0.5) * (Math.random()*1.0 + 1.0);

        /* Smooth around the walls, if we smooth too much, reverse direction! */
        double x = getX();
        double y = getY();
        double smooth = 0;

        Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
        while (!fieldRect.contains(x+Math.sin(goalDirection)*wallStick, y+ Math.cos(goalDirection)*wallStick)) {
            /* turn a little toward enemy and try again */
            goalDirection += moveDirection*0.1;
            smooth += 0.1;
        }

        /* If we smoothed to much, then reverse direction. */
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
        /* Adjust so we drive backwards if the turn is less to go backwards */
		if(getDistanceRemaining() == 0 && getTurnRemaining() == 0) {
			if (Math.abs(turn) > Math.PI/2) {
	            turn = Utils.normalRelativeAngle(turn + Math.PI);
	            setBack(100);
	        } else {
	            setAhead(100);
	        }

	        setTurnRightRadians(turn);
		}
		doNearWall();
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
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName().equals(enemy.getName())) {
			enemy.reset();
		}
	}   

	void doGun() {

		if (enemy.none())
			return;

		double firePower = Math.min(1200 / enemy.getDistance(), 3);
		double bulletSpeed = 20 - firePower * 3;
		long time = (long)(enemy.getDistance() / bulletSpeed);

		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);

		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10 && getEnergy() > 1) {
			setFire(firePower);
		}
	}

	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { 
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { 
			bearing = 360 + arcSin; 
		} else if (xo > 0 && yo < 0) { 
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { 
			bearing = 180 - arcSin; 
		}

		return bearing;
	}
	
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	public void onHitWall(HitWallEvent e) {
		setTurnLeft(180);
		if(0==getTurnRemaining())
		setAhead(100);
//		

	}
	
	public void onHitRobot(HitRobotEvent e) {
		if(!isTeammate(e.getName())) {
			double absBearing = e.getBearing() + getHeading();
			turnGunRight((absBearing - getGunHeading()) % 360);
			fire(3);
		}
	}
	
	public void onHitByBullet(HitByBulletEvent event) {
		if(isTeammate(event.getName())){
			return;
		}
		if(getTime() - enemy.scanTime > 30) {
			this.setTurnRightRadians(event.getBearingRadians());
			this.setFire(Math.min( Rules.MAX_BULLET_POWER, Math.max( Rules.MIN_BULLET_POWER, this.getEnergy() / 30 ) ));
			execute();
		}
		
		super.onHitByBullet(event);
	}
	
	
}
