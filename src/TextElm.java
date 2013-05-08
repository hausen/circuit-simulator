import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

class TextElm extends GraphicElm {
    String text;
    Vector<String> lines;
    int size;
    final int FLAG_CENTER = 1;
    final int FLAG_BAR = 2;
    public TextElm(int xx, int yy) {
	super(xx, yy);
	text = "hello";
	lines = new Vector<String>();
	lines.add(text);
	size = 24;
    }
    public TextElm(int xa, int ya, int xb, int yb, int f,
		   StringTokenizer st) {
	super(xa, ya, xb, yb, f);
	size = new Integer(st.nextToken()).intValue();
	text = st.nextToken();
	while (st.hasMoreTokens())
	    text += ' ' + st.nextToken();
	split();
    }
    void split() {
	int i;
	lines = new Vector<String>();
	StringBuffer sb = new StringBuffer(text);
	for (i = 0; i < sb.length(); i++) {
	    char c = sb.charAt(i);
	    if (c == '\\') {
		sb.deleteCharAt(i);
		c = sb.charAt(i);
		if (c == 'n') {
		    lines.add(sb.substring(0, i));
		    sb.delete(0, i+1);
		    i = -1;
		    continue;
		}
	    }
	}
	lines.add(sb.toString());
    }
    String dump() {
	return super.dump() + " " + size + " " + text;
    }
    int getDumpType() { return 'x'; }
    void drag(int xx, int yy) {
	x = xx;
	y = yy;
	x2 = xx+16;
	y2 = yy;
    }
    void draw(Graphics g) {
	//Graphics2D g2 = (Graphics2D)g;
	//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	//	RenderingHints.VALUE_ANTIALIAS_ON);
	g.setColor(needsHighlight() ? selectColor : lightGrayColor);
	Font f = new Font("SansSerif", 0, size);
	g.setFont(f);
	FontMetrics fm = g.getFontMetrics();
	int i;
	int maxw = -1;
	for (i = 0; i != lines.size(); i++) {
	    int w = fm.stringWidth((String) (lines.elementAt(i)));
	    if (w > maxw)
		maxw = w;
	}
	int cury = y;
	setBbox(x, y, x, y);
	for (i = 0; i != lines.size(); i++) {
	    String s = (String) (lines.elementAt(i));
	    if ((flags & FLAG_CENTER) != 0)
		x = (sim.winSize.width-fm.stringWidth(s))/2;
	    g.drawString(s, x, cury);
	    if ((flags & FLAG_BAR) != 0) {
		int by = cury-fm.getAscent();
		g.drawLine(x, by, x+fm.stringWidth(s)-1, by);
	    }
	    adjustBbox(x, cury-fm.getAscent(),
		       x+fm.stringWidth(s), cury+fm.getDescent());
	    cury += fm.getHeight();
	}
	x2 = boundingBox.x + boundingBox.width;
	y2 = boundingBox.y + boundingBox.height;
    }
    public EditInfo getEditInfo(int n) {
	if (n == 0) {
	    EditInfo ei = new EditInfo("Text", 0, -1, -1);
	    ei.text = text;
	    return ei;
	}
	if (n == 1)
	    return new EditInfo("Size", size, 5, 100);
	if (n == 2) {
	    EditInfo ei = new EditInfo("", 0, -1, -1);
	    ei.checkbox =
		new Checkbox("Center", (flags & FLAG_CENTER) != 0);
	    return ei;
	}
	if (n == 3) {
	    EditInfo ei = new EditInfo("", 0, -1, -1);
	    ei.checkbox =
		new Checkbox("Draw Bar On Top", (flags & FLAG_BAR) != 0);
	    return ei;
	}
	return null;
    }
    public void setEditValue(int n, EditInfo ei) {
	if (n == 0) {
	    text = ei.textf.getText();
	    split();
	}
	if (n == 1)
	    size = (int) ei.value;
	if (n == 3) {
	    if (ei.checkbox.getState())
		flags |= FLAG_BAR;
	    else
		flags &= ~FLAG_BAR;
	}
	if (n == 2) {
	    if (ei.checkbox.getState())
		flags |= FLAG_CENTER;
	    else
		flags &= ~FLAG_CENTER;
	}
    }
    boolean isCenteredText() { return (flags & FLAG_CENTER) != 0; }
    void getInfo(String arr[]) {
	arr[0] = text;
    }
    @Override
    int getShortcut() { return 't'; }
}

