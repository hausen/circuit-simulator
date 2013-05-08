import java.awt.*;

class CircuitCanvas extends Canvas {
    CirSim pg;
    CircuitCanvas(CirSim p) {
	pg = p;
    }
    public Dimension getPreferredSize() {
	return new Dimension(300,400);
    }
    public void update(Graphics g) {
	pg.updateCircuit(g);
    }
    public void paint(Graphics g) {
	pg.updateCircuit(g);
    }
};
