    class ClockElm extends RailElm {
	public ClockElm(int xx, int yy) {
	    super(xx, yy, WF_SQUARE);
	    maxVoltage = 2.5;
	    bias = 2.5;
	    frequency = 100;
	    flags |= FLAG_CLOCK;
	}
	Class getDumpClass() { return RailElm.class; }
	int getShortcut() { return 0; }
    }
