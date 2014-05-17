// stub ThermistorElm based on SparkGapElm
// FIXME need to uncomment ThermistorElm line from CirSim.java
// FIXME need to add ThermistorElm.java to srclist

import java.awt.*;
import java.util.StringTokenizer;

class ThermistorElm extends CircuitElm {
    double minresistance, maxresistance;
    double resistance;
    Scrollbar slider;
    Label label;
    public ThermistorElm(int xx, int yy) {
	super(xx, yy);
	maxresistance = 1e9;
	minresistance = 1e3;
	createSlider();
    }
    public ThermistorElm(int xa, int ya, int xb, int yb, int f,
		       StringTokenizer st) {
	super(xa, ya, xb, yb, f);
	minresistance = new Double(st.nextToken()).doubleValue();
	maxresistance = new Double(st.nextToken()).doubleValue();
	createSlider();
    }
    boolean nonLinear() {return true;}
    int getDumpType() { return 188; }
    String dump() {
	return super.dump() + " " + minresistance + " " + maxresistance;
    }
    Point ps3, ps4;
    void createSlider() {
	CirSim.main.add(label = new Label("Temperature", Label.CENTER));
	int value = 50;
	CirSim.main.add(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
	CirSim.main.validate();
    }
    void setPoints() {
	super.setPoints();
	calcLeads(32);
	ps3 = new Point();
	ps4 = new Point();
    }
    void delete() {
	CirSim.main.remove(label);
	CirSim.main.remove(slider);
    }
    
    void draw(Graphics g) {
	int i;
	double v1 = volts[0];
	double v2 = volts[1];
	setBbox(point1, point2, 6);
	draw2Leads(g);
	// FIXME need to draw properly, see ResistorElm.java
	setPowerColor(g, true);
	doDots(g);
	drawPosts(g);
    }
    
    void calculateCurrent() {
	double vd = volts[0] - volts[1];
	current = vd/resistance;
    }
    void startIteration() {
	double vd = volts[0] - volts[1];
	// FIXME set resistance as appropriate, using slider.getValue()
	resistance = minresistance;
	//System.out.print(this + " res current set to " + current + "\n");
    }
    void doStep() {
	sim.stampResistor(nodes[0], nodes[1], resistance);
    }
    void stamp() {
	sim.stampNonLinear(nodes[0]);
	sim.stampNonLinear(nodes[1]);
    }
    void getInfo(String arr[]) {
	// FIXME
	arr[0] = "spark gap";
	getBasicInfo(arr);
	arr[3] = "R = " + getUnitText(resistance, CirSim.ohmString);
	arr[4] = "Ron = " + getUnitText(minresistance, CirSim.ohmString);
	arr[5] = "Roff = " + getUnitText(maxresistance, CirSim.ohmString);
    }
    public EditInfo getEditInfo(int n) {
	// ohmString doesn't work here on linux
	if (n == 0)
	    return new EditInfo("Min resistance (ohms)", minresistance, 0, 0);
	if (n == 1)
	    return new EditInfo("Max resistance (ohms)", maxresistance, 0, 0);
	return null;
    }
    public void setEditValue(int n, EditInfo ei) {
	if (ei.value > 0 && n == 0)
	    minresistance = ei.value;
	if (ei.value > 0 && n == 1)
	    maxresistance = ei.value;
    }
}

