class ACRailElm extends RailElm {
	public ACRailElm(int xx, int yy) {
		super(xx, yy, WF_AC);
	}

	Class<RailElm> getDumpClass() {
		return RailElm.class;
	}

	int getShortcut() {
		return 0;
	}
}
