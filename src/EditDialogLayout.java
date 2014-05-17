import java.awt.*;

class EditDialogLayout implements LayoutManager {
    public EditDialogLayout() {}
    public void addLayoutComponent(String name, Component c) {}
    public void removeLayoutComponent(Component c) {}
    public Dimension preferredLayoutSize(Container target) {
	return new Dimension(500, 500);
    }
    public Dimension minimumLayoutSize(Container target) {
	return new Dimension(100,100);
    }
    public void layoutContainer(Container target) {
	Insets insets = target.getInsets();
	int targetw = target.getSize().width - insets.left - insets.right;
	int targeth = target.getSize().height - (insets.top+insets.bottom);
	int i;
	int h = insets.top;
	int pw = 300;
	int x = 0;
	for (i = 0; i < target.getComponentCount(); i++) {
	    Component m = target.getComponent(i);
	    boolean newline = true;
	    if (m.isVisible()) {
		Dimension d = m.getPreferredSize();
		if (pw < d.width)
		    pw = d.width;
		if (m instanceof Scrollbar) {
		    h += 10;
		    d.width = targetw-x;
		}
		if (m instanceof Choice && d.width > targetw)
		    d.width = targetw-x;
		if (m instanceof Label) {
		    Dimension d2 =
			target.getComponent(i+1).getPreferredSize();
		    if (d.height < d2.height)
			d.height = d2.height;
		    h += d.height/5;
		    newline = false;
		}
		if (m instanceof Button) {
		    if (x == 0)
			h += 20;
		    if (i != target.getComponentCount()-1)
			newline = false;
		}
		m.setLocation(insets.left+x, h);
		m.setSize(d.width, d.height);
		if (newline) {
		    h += d.height;
		    x = 0;
		} else
		    x += d.width;
	    }
	}
	if (target.getSize().height < h)
	    target.setSize(pw + insets.right, h + insets.bottom);
    }
};

