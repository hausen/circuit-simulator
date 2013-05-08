import java.awt.*;

class EditInfo {
    EditInfo(String n, double val, double mn, double mx) {
	name = n;
	value = val;
	if (mn == 0 && mx == 0 && val > 0) {
	    minval = 1e10;
	    while (minval > val/100)
		minval /= 10.;
	    maxval = minval * 1000;
	} else {
	    minval = mn;
	    maxval = mx;
	}
	forceLargeM = name.indexOf("(ohms)") > 0 ||
	    name.indexOf("(Hz)") > 0;
	dimensionless = false;
    }
    EditInfo setDimensionless() { dimensionless = true; return this; }
    String name, text;
    double value, minval, maxval;
    TextField textf;
    Scrollbar bar;
    Choice choice;
    Checkbox checkbox;
    boolean newDialog;
    boolean forceLargeM;
    boolean dimensionless;
}
    
