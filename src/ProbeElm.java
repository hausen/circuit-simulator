import java.awt.*;
import java.util.StringTokenizer;

class ProbeElm extends CircuitElm {
    static final int FLAG_SHOWVOLTAGE = 1;
    public ProbeElm(int xx, int yy) { super(xx, yy); }
    public ProbeElm(int xa, int ya, int xb, int yb, int f,
		    StringTokenizer st) {
	super(xa, ya, xb, yb, f);
    }
    int getDumpType() { return 'p'; }
	
    Point center;
    void setPoints() {
	super.setPoints();
	// swap points so that we subtract higher from lower
	if (point2.y < point1.y) {
	    Point x = point1;
	    point1 = point2;
	    point2 = x;
	}
	center = interpPoint(point1, point2, .5);
    }
    void draw(Graphics g) {
	int hs = 8;
	setBbox(point1, point2, hs);
	boolean selected = (needsHighlight() || sim.plotYElm == this);
	double len = (selected || sim.dragElm == this) ? 16 : dn-32;
	calcLeads((int) len);
	setVoltageColor(g, volts[0]);
	if (selected)
	    g.setColor(selectColor);
	drawThickLine(g, point1, lead1);
	setVoltageColor(g, volts[1]);
	if (selected)
	    g.setColor(selectColor);
	drawThickLine(g, lead2, point2);
	Font f = new Font("SansSerif", Font.BOLD, 14);
	g.setFont(f);
	if (this == sim.plotXElm)
	    drawCenteredText(g, "X", center.x, center.y, true);
	if (this == sim.plotYElm)
	    drawCenteredText(g, "Y", center.x, center.y, true);
	if (mustShowVoltage()) {
	    String s = getShortUnitText(volts[0], "V");
	    drawValues(g, s, 4);
	}
	drawPosts(g);
    }
	
    boolean mustShowVoltage() {
	return (flags & FLAG_SHOWVOLTAGE) != 0;
    }

    void getInfo(String arr[]) {
	arr[0] = "scope probe";
	arr[1] = "Vd = " + getVoltageText(getVoltageDiff());
    }
    boolean getConnection(int n1, int n2) { return false; }

	public EditInfo getEditInfo(int n) {
	    if (n == 0) {
		EditInfo ei = new EditInfo("", 0, -1, -1);
		ei.checkbox = new Checkbox("Show Voltage", mustShowVoltage());
		return ei;
	    }
	    return null;
	}
	public void setEditValue(int n, EditInfo ei) {
	    if (n == 0) {
		if (ei.checkbox.getState())
		    flags = FLAG_SHOWVOLTAGE;
		else
		    flags &= ~FLAG_SHOWVOLTAGE;
	    }
	}
}

