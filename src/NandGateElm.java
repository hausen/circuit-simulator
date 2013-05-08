import java.awt.*;
import java.util.StringTokenizer;

    class NandGateElm extends AndGateElm {
	public NandGateElm(int xx, int yy) { super(xx, yy); }
	public NandGateElm(int xa, int ya, int xb, int yb, int f,
			   StringTokenizer st) {
	    super(xa, ya, xb, yb, f, st);
	}
	boolean isInverting() { return true; }
	String getGateName() { return "NAND gate"; }
	int getDumpType() { return 151; }
	int getShortcut() { return '@'; }
    }
