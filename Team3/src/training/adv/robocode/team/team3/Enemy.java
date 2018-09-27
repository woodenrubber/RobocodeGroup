package training.adv.robocode.team.team3;
import java.awt.geom.Point2D;
import java.io.Serializable;

import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class Enemy implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * This class is about the enemy robot
	 * author: Jason Zhang
	 */	
	// enemy's Point2D
	public Point2D.Double position;
	
	// enemy's ScannedRobotEvent
	public ScannedRobotEvent scannedEvent;
	
	// my robot
	public Team3Leader me;
	
	// enemy's coordination
	public double x, y;
	
	// enemy's name
	public String name;
	
	// enemy's energy
	public double energy; 
	
	// heading radians
	public double headingRadians;
	
	// bearing radians
	public double bearingRadians;
	
	// enemy's distance
	public double distance;
	
	// enemy's velocity
	public volatile double velocity; 
	
	public double absoluteBearingRadians;
	
	// the direction of the enemy towards me
	// public double direction;
	
	//	the time when scan enemy, notice that the time is the current turn of the battle round 
	public long scanTime;
	
	// enemy's last heading radians
	double lastEnemyHeadingRadians;
	int thisStep;
	
	//	update the info of the target
	public void update(ScannedRobotEvent e, Team3Leader me) { // 这里是用那种leader记得改哈
		name = e.getName();
		energy = e.getEnergy();
		headingRadians = e.getHeadingRadians();
		bearingRadians = e.getBearingRadians();
		distance = e.getDistance();
		velocity = e.getVelocity();
		scanTime = e.getTime();
		
        absoluteBearingRadians = bearingRadians + me.getHeadingRadians();
		
		thisStep = encode(headingRadians - lastEnemyHeadingRadians, velocity);
		lastEnemyHeadingRadians = headingRadians;
		
	}
    
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBearing() {
        return bearingRadians;
    }

    public void setBearing(double bearing) {
        this.bearingRadians = bearing;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getHeadingRadians() {
        return headingRadians;
    }

    public void setHeadingRadians(double heading) {
        this.headingRadians = heading;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void reset() {
        bearingRadians = 0.0;
        distance = 0.0;
        energy = 0.0;
        headingRadians = 0.0;
        name = "";
        velocity = 0.0;
        x = 0.0;
        y = 0.0;
        System.out.printf("Reset tracked bot!\n");
    }


    public boolean none() {
        System.out.printf("None tracked!\n");
        return "".equals(name);
    }

    public void update(ScannedRobotEvent e, Robot robot) {
        bearingRadians = e.getBearingRadians();
        distance = e.getDistance();
        energy = e.getEnergy();
        headingRadians = e.getHeadingRadians();
        name = e.getName();
        velocity = e.getVelocity();
        scanTime = e.getTime();
        double absBearingDeg = (robot.getHeading() + e.getBearing());
        if (absBearingDeg < 0) absBearingDeg += 360;

        // yes, you use the _sine_ to get the X value because 0 deg is North
        x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();

        // yes, you use the _cosine_ to get the Y value because 0 deg is North
        y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
    }
    public void update(Enemy e, Robot robot) {
        bearingRadians = e.getBearingRadians();
        distance = e.getDistance();
        energy = e.getEnergy();
        headingRadians = e.getHeadingRadians();
        name = e.getName();
        velocity = e.getVelocity();
        scanTime = e.getTime();
        double absBearingDeg = (robot.getHeading() + e.getBearing());
        if (absBearingDeg < 0) absBearingDeg += 360;

        // yes, you use the _sine_ to get the X value because 0 deg is North
        x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();

        // yes, you use the _cosine_ to get the Y value because 0 deg is North
        y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
    }
    
    public long getTime() {
		return this.scanTime;
	}

	@Override
    public String toString() {
        return "EnemyBot{" +
                "bearing=" + bearingRadians +
                ", distance=" + distance +
                ", energy=" + energy +
                ", heading=" + headingRadians +
                ", name='" + name + '\'' +
                ", velocity=" + velocity +
                ", X=" + x +
                ", Y=" + y +
                '}';
    }
   

    public double getFutureX(long when){
        return x + Math.sin(getHeadingRadians()) * getVelocity() * when;
    }

    public double getFutureY(long when){
        return y + Math.cos(getHeadingRadians()) * getVelocity() * when;
    }

    public double getFutureT(Robot robot, double bulletVelocity){

        // enemy velocity
        double v_E = getVelocity();

        // temp variables
        double x_diff = x - robot.getX();
        double y_diff = y - robot.getY();

        // angles of enemy's heading
        double sin = Math.sin(getHeadingRadians());
        double cos = Math.cos(getHeadingRadians());

        // calculated time
        double T;
        double v_B = bulletVelocity;

        double xy = (x_diff*sin + y_diff*cos);

        T = ( (v_E*xy) + Math.sqrt(sqr(v_E)*sqr(xy) + (sqr(x_diff) + sqr(y_diff))*(sqr(v_B) + sqr(v_E))) ) / (sqr(v_B) - sqr(v_E));

        return T;

    }

    private static double sqr(double in){
        return in * in;
    }

	public double getBearingRadians() {
		return bearingRadians;
	}
	
	
	public static int encode(double dh, double v) {
        if (Math.abs(dh) > Rules.MAX_TURN_RATE_RADIANS) {
            return (char) -1;
        }
        //取正
        //-10<toDegrees(dh)<10 ; -8<v<8 ;
        //so we add with 10 and 8
        int dhCode = (int) Math.rint(Math.toDegrees(dh)) + 10;
        int vCode = (int) Math.rint(v + 8);
        return (char) (17 * dhCode + vCode);
    }

    public void decode(int symbol) {
        headingRadians += Math.toRadians(symbol / 17 - 10);
        velocity = symbol % 17 - 8;
    }

}
