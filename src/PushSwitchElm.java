    class PushSwitchElm extends SwitchElm {
	public PushSwitchElm(int xx, int yy) { super(xx, yy, true); }
	Class getDumpClass() { return SwitchElm.class; }
	int getShortcut() { return 0; }
    }
