import java.util.StringTokenizer;

// contributed by Edward Calver

    class DeMultiplexerElm extends ChipElm {
	boolean hasReset() {return false;}
	public DeMultiplexerElm(int xx, int yy) { super(xx, yy); }
	public DeMultiplexerElm(int xa, int ya, int xb, int yb, int f,
			    StringTokenizer st) {
	    super(xa, ya, xb, yb, f, st);
	}
	String getChipName() { return "Multiplexer"; }

	void setupPins() {
	    sizeX = 3;
	    sizeY = 5;
	    pins = new Pin[getPostCount()];

	    pins[0] = new Pin(0, SIDE_E, "Q0");
	    pins[0].output=true;
	    pins[1] = new Pin(1, SIDE_E, "Q1");
	    pins[1].output=true;
	    pins[2] = new Pin(2, SIDE_E, "Q2");
	    pins[2].output=true;
	    pins[3] = new Pin(3, SIDE_E, "Q3");
	    pins[3].output=true;
		
	    pins[4] = new Pin(0, SIDE_S, "S0");
	    pins[5] = new Pin(1, SIDE_S, "S1");

	    pins[6] = new Pin(0, SIDE_W, "Q");


	}
	int getPostCount() {
	    return 7;
	}
	int getVoltageSourceCount() {return 4;}

	void execute() {
	 int selectedvalue=0;
	 if(pins[4].value)selectedvalue++;
	 if(pins[5].value)selectedvalue+=2;
	 for(int i=0;i<4;i++)pins[i].value=false;
	 pins[selectedvalue].value=pins[6].value;

	}
	int getDumpType() { return 185; }

    }
