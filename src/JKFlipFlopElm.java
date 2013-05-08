import java.awt.*;
import java.util.StringTokenizer;

    class JKFlipFlopElm extends ChipElm {
	public JKFlipFlopElm(int xx, int yy) { super(xx, yy); }
	public JKFlipFlopElm(int xa, int ya, int xb, int yb, int f,
			    StringTokenizer st) {
	    super(xa, ya, xb, yb, f, st);
	    pins[4].value = !pins[3].value;
	}
	String getChipName() { return "JK flip-flop"; }
	void setupPins() {
	    sizeX = 2;
	    sizeY = 3;
	    pins = new Pin[5];
	    pins[0] = new Pin(0, SIDE_W, "J");
	    pins[1] = new Pin(1, SIDE_W, "");
	    pins[1].clock = true;
	    pins[1].bubble = true;
	    pins[2] = new Pin(2, SIDE_W, "K");
	    pins[3] = new Pin(0, SIDE_E, "Q");
	    pins[3].output = pins[3].state = true;
	    pins[4] = new Pin(2, SIDE_E, "Q");
	    pins[4].output = true;
	    pins[4].lineOver = true;
	}
	int getPostCount() { return 5; }
	int getVoltageSourceCount() { return 2; }
	void execute() {
	    if (!pins[1].value && lastClock) {
		boolean q = pins[3].value;
		if (pins[0].value) {
		    if (pins[2].value)
			q = !q;
		    else
			q = true;
		} else if (pins[2].value)
		    q = false;
		pins[3].value = q;
		pins[4].value = !q;
	    }
	    lastClock = pins[1].value;
	}
	int getDumpType() { return 156; }
    }
