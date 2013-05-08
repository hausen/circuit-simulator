import java.awt.*;
import java.util.StringTokenizer;

class ADCElm extends ChipElm {
    public ADCElm(int xx, int yy) { super(xx, yy); }
    public ADCElm(int xa, int ya, int xb, int yb, int f,
		  StringTokenizer st) {
	super(xa, ya, xb, yb, f, st);
    }
    String getChipName() { return "ADC"; }
    boolean needsBits() { return true; }
    void setupPins() {
	sizeX = 2;
	sizeY = bits > 2 ? bits : 2;
	pins = new Pin[getPostCount()];
	int i;
	for (i = 0; i != bits; i++) {
	    pins[i] = new Pin(bits-1-i, SIDE_E, "D" + i);
	    pins[i].output = true;
	}
	pins[bits]   = new Pin(0, SIDE_W, "In");
	pins[bits+1] = new Pin(sizeY-1, SIDE_W, "V+");
	allocNodes();
    }
    void execute() {
	int imax = (1<<bits)-1;
	// if we round, the half-flash doesn't work
	double val = imax*volts[bits]/volts[bits+1]; // + .5;
	int ival = (int) val;
	ival = min(imax, max(0, ival));
	int i;
	for (i = 0; i != bits; i++)
	    pins[i].value = ((ival & (1<<i)) != 0);
    }
    int getVoltageSourceCount() { return bits; }
    int getPostCount() { return bits+2; }
    int getDumpType() { return 167; }
}
    
