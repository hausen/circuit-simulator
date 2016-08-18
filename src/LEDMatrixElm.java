import java.awt.*;
import java.util.StringTokenizer;

class LEDMatrixElm extends ChipElm {

    private static final int size=8;
    //private static final double resistance=100;

    public LEDMatrixElm(int xx, int yy) { super(xx, yy); }
    public LEDMatrixElm(int xa, int ya, int xb, int yb, int f,
		    StringTokenizer st) {
	super(xa, ya, xb, yb, f, st);
	negateRows=new Boolean(st.nextToken()).booleanValue();
	negateColumns=new Boolean(st.nextToken()).booleanValue();
	colorR=new Double(st.nextToken()).doubleValue();
	colorG=new Double(st.nextToken()).doubleValue();
	colorB=new Double(st.nextToken()).doubleValue();
    }

    private boolean negateRows=false;
    private boolean negateColumns=false;
    private double colorR=1.0;
    private double colorG=0.0;
    private double colorB=0.0;

    String getChipName() { return "LED Matrix"; }
    void setupPins() {
	sizeX = 8;
	sizeY = 8;
	pins = new Pin[16];
	pins[0] = new Pin(0, SIDE_W, "");
	pins[1] = new Pin(1, SIDE_W, "");
	pins[2] = new Pin(2, SIDE_W, "");
	pins[3] = new Pin(3, SIDE_W, "");
	pins[4] = new Pin(4, SIDE_W, "");
	pins[5] = new Pin(5, SIDE_W, "");
	pins[6] = new Pin(6, SIDE_W, "");
	pins[7] = new Pin(7, SIDE_W, "");

	pins[8] = new Pin(0, SIDE_S, "");
	pins[9] = new Pin(1, SIDE_S, "");
	pins[10] =new Pin(2, SIDE_S, "");
	pins[11] =new Pin(3, SIDE_S, "");
	pins[12] =new Pin(4, SIDE_S, "");
	pins[13] =new Pin(5, SIDE_S, "");
	pins[14] =new Pin(6, SIDE_S, "");
	pins[15] =new Pin(7, SIDE_S, "");
    }

    void draw(Graphics g){
    drawChip(g);
    Color color=new Color((int)(colorR*255),(int)(colorG*255),(int)(colorB*255));
    	for(int col=0;col<size;col++)
	for(int row=0;row<size;row++)
	{
	int centreX=x+2*(col+1)*cspc;
	int centreY=y+2*row*cspc;
	int radius=cspc/2;
		if((negateRows^pins[row].value)&&(negateColumns^pins[col+8].value))
		{
		g.setColor(color);
		g.fillOval(centreX-radius,centreY-radius,radius*2,radius*2);
		}
	g.setColor(Color.gray);
	radius=(3*cspc)/4;
    	drawThickCircle(g,centreX,centreY,radius);
	}
    }
	
    public EditInfo getEditInfo(int n){
	    if (n == 2)
		{
		EditInfo ei=new EditInfo("",0,-1,-1);
		ei.checkbox=new Checkbox("Negate rows",negateRows);
		return ei;
		}
	    if (n == 3)
		{
		EditInfo ei=new EditInfo("",0,-1,-1);
		ei.checkbox=new Checkbox("Negate columns",negateColumns);
		return ei;
		}
	    if(n==4)
	  	{
		return new EditInfo("Red Value (0-1)",colorR,0,1).setDimensionless();
		} 
	    if(n==5)
	  	{
		return new EditInfo("Green Value (0-1)",colorG,0,1).setDimensionless();
		}	 
	    if(n==6)
	  	{
		return new EditInfo("Blue Value (0-1)",colorB,0,1).setDimensionless();
		}			    
       return super.getEditInfo(n);
	}
	public void setEditValue(int n, EditInfo ei) {
	super.setEditValue(n,ei);
		if (n == 2)
		negateRows=ei.checkbox.getState();
	  	if (n == 3)
		negateColumns=ei.checkbox.getState();
	    	if (n == 4)
		colorR=ei.value;
		if(n == 5)
		colorG=ei.value;
		if(n == 6)
		colorB=ei.value;
	}

    int getPostCount() {return 16;}
    int getVoltageSourceCount() { return 0; }
    int getDumpType() { return 207;}
    String  dump()
	{
	return super.dump() + " "+negateRows+" "+negateColumns+" "+colorR+" "+colorG+" "+colorB;
	}
}
    
