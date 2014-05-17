import java.awt.*;

class CircuitLayout implements LayoutManager {
	public CircuitLayout() {
	}

	public void addLayoutComponent(String name, Component c) {
	}

	public void removeLayoutComponent(Component c) {
	}

	public Dimension preferredLayoutSize(Container target) {
		return new Dimension(500, 500);
	}

	public Dimension minimumLayoutSize(Container target) {
		return new Dimension(100, 100);
	}

	public void layoutContainer(Container target) {
		Insets insets = target.getInsets();
		int targetw = target.getSize().width - insets.left - insets.right;
		int cw = targetw * 8 / 10;
		int targeth = target.getSize().height - (insets.top + insets.bottom);
		target.getComponent(0).setLocation(insets.left, insets.top);
		target.getComponent(0).setSize(cw, targeth);
		int barwidth = targetw - cw;
		cw += insets.left;
		int i;
		int h = insets.top;
		for (i = 1; i < target.getComponentCount(); i++) {
			Component m = target.getComponent(i);
			if (m.isVisible()) {
				Dimension d = m.getPreferredSize();
				if (m instanceof Scrollbar)
					d.width = barwidth;
				if (m instanceof Choice && d.width > barwidth)
					d.width = barwidth;
				if (m instanceof Label) {
					h += d.height / 5;
					d.width = barwidth;
				}
				m.setLocation(cw, h);
				m.setSize(d.width, d.height);
				h += d.height;
			}
		}
	}
};
