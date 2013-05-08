    class NTransistorElm extends TransistorElm {
	public NTransistorElm(int xx, int yy) { super(xx, yy, false); }
	Class getDumpClass() { return TransistorElm.class; }
    }
