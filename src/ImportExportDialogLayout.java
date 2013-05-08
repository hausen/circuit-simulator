import java.awt.*;

class ImportExportDialogLayout implements LayoutManager {
    public ImportExportDialogLayout() {}
    public void addLayoutComponent(String name, Component c) {}
    public void removeLayoutComponent(Component c) {}
    public Dimension preferredLayoutSize(Container target) {
	return new Dimension(500, 500);
    }
    public Dimension minimumLayoutSize(Container target) {
	return new Dimension(100,100);
    }
    public void layoutContainer(Container target) {
	Insets insets = target.insets();
	int targetw = target.size().width - insets.left - insets.right;
	int targeth = target.size().height - (insets.top+insets.bottom);
	int i;
	int pw = 300;
	if (target.getComponentCount() == 0)
	    return;
	Component cl = target.getComponent(target.getComponentCount()-1);
	Dimension dl = cl.getPreferredSize();
	target.getComponent(0).move(insets.left, insets.top);
	int cw = target.size().width - insets.left - insets.right;
	int ch = target.size().height - insets.top - insets.bottom -
	    dl.height;
	target.getComponent(0).resize(cw, ch);
	int h = ch + insets.top;
	int x = 0;
	for (i = 1; i < target.getComponentCount(); i++) {
	    Component m = target.getComponent(i);
	    if (m.isVisible()) {
		Dimension d = m.getPreferredSize();
		m.move(insets.left+x, h);
		m.resize(d.width, d.height);
		x += d.width;
	    }
	}
    }
};

