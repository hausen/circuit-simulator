import java.util.StringTokenizer;

    class HalfAdderElm extends ChipElm {
	boolean hasReset() {return false;}
	public HalfAdderElm(int xx, int yy) { super(xx, yy); }
	public HalfAdderElm(int xa, int ya, int xb, int yb, int f,
			    StringTokenizer st) {
	    super(xa, ya, xb, yb, f, st);
	}
	String getChipName() { return "Half Adder"; }

	void setupPins() {
	    sizeX=2;
	    sizeY=2;
	    pins=new Pin[getPostCount()];

	    pins[0] = new Pin(0, SIDE_E, "S");
	    pins[0].output=true;
	    pins[1] = new Pin(1, SIDE_E, "C");
	    pins[1].output=true;		
	    pins[2] = new Pin(0, SIDE_W, "A");
	    pins[3] = new Pin(1, SIDE_W, "B");

	}
	int getPostCount() {
	    return 4;
	}
	int getVoltageSourceCount() {return 2;}

	void execute() {
	
	pins[0].value=pins[2].value^pins[3].value;
	pins[1].value=pins[2].value&&pins[3].value;
	}
	int getDumpType() { return 195; }

    }
