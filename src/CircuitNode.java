import java.util.Vector;

class CircuitNode {
    int x, y;
    Vector<CircuitNodeLink> links;
    boolean internal;
    CircuitNode() { links = new Vector<CircuitNodeLink>(); }
}
