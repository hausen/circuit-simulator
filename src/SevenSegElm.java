import java.awt.*;
import java.util.StringTokenizer;

    class SevenSegElm extends ChipElm {
	public SevenSegElm(int xx, int yy) { super(xx, yy); }
	public SevenSegElm(int xa, int ya, int xb, int yb, int f,
			   StringTokenizer st) {
	    super(xa, ya, xb, yb, f, st);
	}
	String getChipName() { return "7-segment driver/display"; }
	Color darkred;
	void setupPins() {
	    darkred = new Color(30, 0, 0);
	    sizeX = 4;
	    sizeY = 4;
	    pins = new Pin[7];
	    pins[0] = new Pin(0, SIDE_W, "a");
	    pins[1] = new Pin(1, SIDE_W, "b");
	    pins[2] = new Pin(2, SIDE_W, "c");
	    pins[3] = new Pin(3, SIDE_W, "d");
	    pins[4] = new Pin(1, SIDE_S, "e");
	    pins[5] = new Pin(2, SIDE_S, "f");
	    pins[6] = new Pin(3, SIDE_S, "g");
	}
	void draw(Graphics g) {
	    drawChip(g);
	    g.setColor(Color.red);
	    int xl = x+cspc*5;
	    int yl = y+cspc;
	    setColor(g, 0);
	    drawThickLine(g, xl, yl, xl+cspc, yl);
	    setColor(g, 1);
	    drawThickLine(g, xl+cspc, yl, xl+cspc, yl+cspc);
	    setColor(g, 2);
	    drawThickLine(g, xl+cspc, yl+cspc, xl+cspc, yl+cspc2);
	    setColor(g, 3);
	    drawThickLine(g, xl, yl+cspc2, xl+cspc, yl+cspc2);
	    setColor(g, 4);
	    drawThickLine(g, xl, yl+cspc, xl, yl+cspc2);
	    setColor(g, 5);
	    drawThickLine(g, xl, yl, xl, yl+cspc);
	    setColor(g, 6);
	    drawThickLine(g, xl, yl+cspc, xl+cspc, yl+cspc);
	}
	void setColor(Graphics g, int p) {
	    g.setColor(pins[p].value ? Color.red :
		       sim.printableCheckItem.getState() ? Color.white : darkred);
	}
	int getPostCount() { return 7; }
	int getVoltageSourceCount() { return 0; }
	int getDumpType() { return 157; }
    }
