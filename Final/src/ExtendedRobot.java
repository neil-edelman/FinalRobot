
public ExtendedRobot extends Robot {

   public float angleTo;

   public ExtendedRobot() {
      this.super();
   }
   
	/** this is used to rotate the robot at a constant speed */
	public void turnConstantlyTo(final float angleTo,final int speed) {
		status = Status.LOCALISING;
		this.setLeftSpeed(-speed);
		this.setRightSpeed(speed);
      this.angleTo = angleTo;
	}

}
