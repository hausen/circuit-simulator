// CirSim.java (c) 2010 by Paul Falstad

// For information about the theory behind this, see Electronic Circuit & System Simulation Methods by Pillage

import java.awt.*;
import java.util.Vector;
import java.io.File;
import java.util.Random;
import java.lang.Math;
import java.net.URL;
import java.awt.event.*;
import java.io.FilterInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.net.URLEncoder;

/*
 import java.lang.reflect.Method;		#unused imports
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.awt.image.*;
 import java.applet.Applet;
 import java.io.InputStream;
 */

public class CirSim extends Frame implements ComponentListener, ActionListener,
		AdjustmentListener, MouseMotionListener, MouseListener, ItemListener,
		KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1182255949956498126L;

	Thread engine = null;

	Dimension winSize;
	Image dbimage;

	Random random;
	public static final int sourceRadius = 7;
	public static final double freqMult = 3.14159265 * 2 * 4;

	public String getAppletInfo() {
		return "Circuit by Paul Falstad";
	}

	static Container main;
	Label titleLabel;
	Button resetButton;
	Button dumpMatrixButton;
	MenuItem exportItem, exportLinkItem, importItem, exitItem, undoItem,
			redoItem, cutItem, copyItem, pasteItem, selectAllItem, optionsItem;
	Menu optionsMenu;
	Checkbox stoppedCheck;
	CheckboxMenuItem dotsCheckItem;
	CheckboxMenuItem voltsCheckItem;
	CheckboxMenuItem powerCheckItem;
	CheckboxMenuItem smallGridCheckItem;
	CheckboxMenuItem showValuesCheckItem;
	CheckboxMenuItem conductanceCheckItem;
	CheckboxMenuItem euroResistorCheckItem;
	CheckboxMenuItem printableCheckItem;
	CheckboxMenuItem conventionCheckItem;
	CheckboxMenuItem idealWireCheckItem;
	Scrollbar speedBar;
	Scrollbar currentBar;
	Label powerLabel;
	Scrollbar powerBar;
	PopupMenu elmMenu;
	MenuItem elmEditMenuItem;
	MenuItem elmCutMenuItem;
	MenuItem elmCopyMenuItem;
	MenuItem elmDeleteMenuItem;
	MenuItem elmScopeMenuItem;
	PopupMenu scopeMenu;
	PopupMenu transScopeMenu;
	PopupMenu mainMenu;
	CheckboxMenuItem scopeVMenuItem;
	CheckboxMenuItem scopeIMenuItem;
	CheckboxMenuItem scopeMaxMenuItem;
	CheckboxMenuItem scopeMinMenuItem;
	CheckboxMenuItem scopeFreqMenuItem;
	CheckboxMenuItem scopePowerMenuItem;
	CheckboxMenuItem scopeIbMenuItem;
	CheckboxMenuItem scopeIcMenuItem;
	CheckboxMenuItem scopeIeMenuItem;
	CheckboxMenuItem scopeVbeMenuItem;
	CheckboxMenuItem scopeVbcMenuItem;
	CheckboxMenuItem scopeVceMenuItem;
	CheckboxMenuItem scopeVIMenuItem;
	CheckboxMenuItem scopeXYMenuItem;
	CheckboxMenuItem scopeResistMenuItem;
	CheckboxMenuItem scopeVceIcMenuItem;
	MenuItem scopeSelectYMenuItem;
	Class<?> addingClass;
	int mouseMode = MODE_SELECT;
	int tempMouseMode = MODE_SELECT;
	String mouseModeStr = "Select";
	static final double pi = 3.14159265358979323846;
	static final int MODE_ADD_ELM = 0;
	static final int MODE_DRAG_ALL = 1;
	static final int MODE_DRAG_ROW = 2;
	static final int MODE_DRAG_COLUMN = 3;
	static final int MODE_DRAG_SELECTED = 4;
	static final int MODE_DRAG_POST = 5;
	static final int MODE_SELECT = 6;
	static final int infoWidth = 120;
	int dragX, dragY, initDragX, initDragY;
	int selectedSource;
	Rectangle selectedArea;
	int gridSize, gridMask, gridRound;
	boolean dragging;
	boolean analyzeFlag;
	boolean dumpMatrix;
	boolean useBufferedImage;
	boolean isMac;
	String ctrlMetaKey;
	double t;
	int pause = 10;
	int scopeSelected = -1;
	int menuScope = -1;
	int hintType = -1, hintItem1, hintItem2;
	String stopMessage;
	double timeStep;
	static final int HINT_LC = 1;
	static final int HINT_RC = 2;
	static final int HINT_3DB_C = 3;
	static final int HINT_TWINT = 4;
	static final int HINT_3DB_L = 5;
	Vector<CircuitElm> elmList;
	// Vector setupList;
	CircuitElm dragElm, menuElm, mouseElm, stopElm;
	boolean didSwitch = false;
	int mousePost = -1;
	CircuitElm plotXElm, plotYElm;
	int draggingPost;
	SwitchElm heldSwitchElm;
	double circuitMatrix[][], circuitRightSide[], origRightSide[],
			origMatrix[][];
	RowInfo circuitRowInfo[];
	int circuitPermute[];
	boolean circuitNonLinear;
	int voltageSourceCount;
	int circuitMatrixSize, circuitMatrixFullSize;
	boolean circuitNeedsMap;
	public boolean useFrame;
	int scopeCount;
	Scope scopes[];
	int scopeColCount[];
	static EditDialog editDialog;
	static ImportExportDialog impDialog, expDialog;
	Class<?> dumpTypes[], shortcuts[];
	static String muString = "u";
	static String ohmString = "ohm";
	String clipboard;
	Rectangle circuitArea;
	int circuitBottom;
	Vector<String> undoStack, redoStack;

	int getrand(int x) {
		int q = random.nextInt();
		if (q < 0)
			q = -q;
		return q % x;
	}

	CircuitCanvas cv;
	Circuit applet;

	CirSim(Circuit a) {
		super("Circuit Simulator v1.6.1a");
		applet = a;
		useFrame = false;
	}

	String startCircuit = null;
	String startLabel = null;
	String startCircuitText = null;
	String baseURL = "http://www.falstad.com/circuit/";

	public void init() {
		String euroResistor = null;
		String useFrameStr = null;
		boolean printable = true; // hausen: changed from false to true
		boolean convention = true;

		CircuitElm.initClass(this);

		try {
			baseURL = applet.getDocumentBase().getFile();
			// look for circuit embedded in URL
			String doc = applet.getDocumentBase().toString();
			int in = doc.indexOf('#');
			if (in > 0) {
				String x = null;
				try {
					x = doc.substring(in + 1);
					x = URLDecoder.decode(x, "UTF-8");
					startCircuitText = x;
				} catch (Exception e) {
					System.out.println("can't decode " + x);
					e.printStackTrace();
				}
			}
			in = doc.lastIndexOf('/');
			if (in > 0)
				baseURL = doc.substring(0, in + 1);

			String param = applet.getParameter("PAUSE");
			if (param != null)
				pause = Integer.parseInt(param);
			startCircuit = applet.getParameter("startCircuit");
			startLabel = applet.getParameter("startLabel");
			euroResistor = applet.getParameter("euroResistors");
			useFrameStr = applet.getParameter("useFrame");
			String x = applet.getParameter("whiteBackground");
			if (x != null && x.equalsIgnoreCase("true"))
				printable = true;
			x = applet.getParameter("conventionalCurrent");
			if (x != null && x.equalsIgnoreCase("true"))
				convention = false;
		} catch (Exception e) {
		}

		boolean euro = (euroResistor != null && euroResistor
				.equalsIgnoreCase("true"));
		useFrame = (useFrameStr == null || !useFrameStr
				.equalsIgnoreCase("false"));
		if (useFrame)
			main = this;
		else
			main = applet;

		String os = System.getProperty("os.name");
		isMac = (os.indexOf("Mac ") == 0);
		ctrlMetaKey = (isMac) ? "\u2318" : "Ctrl";
		String jv = System.getProperty("java.class.version");
		double jvf = new Double(jv).doubleValue();
		if (jvf >= 48) {
			muString = "\u03bc";
			ohmString = "\u03a9";
			useBufferedImage = true;
		}

		dumpTypes = new Class[300];
		shortcuts = new Class[127];

		// these characters are reserved
		dumpTypes[(int) 'o'] = Scope.class;
		dumpTypes[(int) 'h'] = Scope.class;
		dumpTypes[(int) '$'] = Scope.class;
		dumpTypes[(int) '%'] = Scope.class;
		dumpTypes[(int) '?'] = Scope.class;
		dumpTypes[(int) 'B'] = Scope.class;

		main.setLayout(new CircuitLayout());
		cv = new CircuitCanvas(this);
		cv.addComponentListener(this);
		cv.addMouseMotionListener(this);
		cv.addMouseListener(this);
		cv.addKeyListener(this);
		main.add(cv);

		mainMenu = new PopupMenu();
		MenuBar mb = null;
		if (useFrame)
			mb = new MenuBar();
		Menu m = new Menu("File");
		if (useFrame)
			mb.add(m);
		else
			mainMenu.add(m);
		m.add(importItem = getMenuItem("Import"));
		m.add(exportItem = getMenuItem("Export"));
		// m.add(exportLinkItem = getMenuItem("Export Link"));
		m.addSeparator();
		m.add(exitItem = getMenuItem("Exit"));

		m = new Menu("Edit");
		m.add(undoItem = getMenuItem("Undo"));
		undoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z));
		m.add(redoItem = getMenuItem("Redo"));
		redoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z, true));
		m.addSeparator();
		m.add(cutItem = getMenuItem("Cut"));
		cutItem.setShortcut(new MenuShortcut(KeyEvent.VK_X));
		m.add(copyItem = getMenuItem("Copy"));
		copyItem.setShortcut(new MenuShortcut(KeyEvent.VK_C));
		m.add(pasteItem = getMenuItem("Paste"));
		pasteItem.setShortcut(new MenuShortcut(KeyEvent.VK_V));
		pasteItem.setEnabled(false);
		m.add(selectAllItem = getMenuItem("Select All"));
		selectAllItem.setShortcut(new MenuShortcut(KeyEvent.VK_A));
		if (useFrame)
			mb.add(m);
		else
			mainMenu.add(m);

		m = new Menu("Scope");
		if (useFrame)
			mb.add(m);
		else
			mainMenu.add(m);
		m.add(getMenuItem("Stack All", "stackAll"));
		m.add(getMenuItem("Unstack All", "unstackAll"));

		optionsMenu = m = new Menu("Options");
		if (useFrame)
			mb.add(m);
		else
			mainMenu.add(m);
		m.add(dotsCheckItem = getCheckItem("Show Current"));
		dotsCheckItem.setState(false); // hausen: changed from true to false
		m.add(voltsCheckItem = getCheckItem("Show Voltage"));
		voltsCheckItem.setState(true);
		m.add(powerCheckItem = getCheckItem("Show Power"));
		m.add(showValuesCheckItem = getCheckItem("Show Values"));
		showValuesCheckItem.setState(true);
		// m.add(conductanceCheckItem = getCheckItem("Show Conductance"));
		m.add(smallGridCheckItem = getCheckItem("Small Grid"));
		m.add(euroResistorCheckItem = getCheckItem("European Resistors"));
		euroResistorCheckItem.setState(euro);
		m.add(printableCheckItem = getCheckItem("White Background"));
		printableCheckItem.setState(printable);
		m.add(conventionCheckItem = getCheckItem("Conventional Current Motion"));
		conventionCheckItem.setState(convention);
		m.add(idealWireCheckItem = getCheckItem("Ideal Wires"));
		idealWireCheckItem.setState(WireElm.ideal);
		idealWireCheckItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					WireElm.ideal = true;
				} else {
					WireElm.ideal = false;
				}
				System.err.println("ideal wires: " + WireElm.ideal);
			}
		});

		m.add(optionsItem = getMenuItem("Other Options..."));

		Menu circuitsMenu = new Menu("Circuits");
		if (useFrame)
			mb.add(circuitsMenu);
		else
			mainMenu.add(circuitsMenu);

		mainMenu.add(getClassCheckItem("Add Wire", "WireElm"));
		mainMenu.add(getClassCheckItem("Add Resistor", "ResistorElm"));

		Menu passMenu = new Menu("Passive Components");
		mainMenu.add(passMenu);
		passMenu.add(getClassCheckItem("Add Capacitor", "CapacitorElm"));
		passMenu.add(getClassCheckItem("Add Inductor", "InductorElm"));
		passMenu.add(getClassCheckItem("Add Switch", "SwitchElm"));
		passMenu.add(getClassCheckItem("Add Push Switch", "PushSwitchElm"));
		passMenu.add(getClassCheckItem("Add SPDT Switch", "Switch2Elm"));
		passMenu.add(getClassCheckItem("Add Potentiometer", "PotElm"));
		passMenu.add(getClassCheckItem("Add Transformer", "TransformerElm"));
		passMenu.add(getClassCheckItem("Add Tapped Transformer",
				"TappedTransformerElm"));
		passMenu.add(getClassCheckItem("Add Transmission Line", "TransLineElm"));
		passMenu.add(getClassCheckItem("Add Relay", "RelayElm"));
		passMenu.add(getClassCheckItem("Add Memristor", "MemristorElm"));
		passMenu.add(getClassCheckItem("Add Spark Gap", "SparkGapElm"));

		Menu inputMenu = new Menu("Inputs/Outputs");
		mainMenu.add(inputMenu);
		inputMenu.add(getClassCheckItem("Add Ground", "GroundElm"));
		inputMenu.add(getClassCheckItem("Add Voltage Source (2-terminal)",
				"DCVoltageElm"));
		inputMenu.add(getClassCheckItem("Add A/C Source (2-terminal)",
				"ACVoltageElm"));
		inputMenu.add(getClassCheckItem("Add Voltage Source (1-terminal)",
				"RailElm"));
		inputMenu.add(getClassCheckItem("Add A/C Source (1-terminal)",
				"ACRailElm"));
		inputMenu.add(getClassCheckItem("Add Square Wave (1-terminal)",
				"SquareRailElm"));
		inputMenu.add(getClassCheckItem("Add Analog Output", "OutputElm"));
		inputMenu.add(getClassCheckItem("Add Logic Input", "LogicInputElm"));
		inputMenu.add(getClassCheckItem("Add Logic Output", "LogicOutputElm"));
		inputMenu.add(getClassCheckItem("Add Clock", "ClockElm"));
		inputMenu.add(getClassCheckItem("Add A/C Sweep", "SweepElm"));
		inputMenu.add(getClassCheckItem("Add Var. Voltage", "VarRailElm"));
		inputMenu.add(getClassCheckItem("Add Antenna", "AntennaElm"));
		inputMenu.add(getClassCheckItem("Add Current Source", "CurrentElm"));
		inputMenu.add(getClassCheckItem("Add LED", "LEDElm"));
		inputMenu.add(getClassCheckItem("Add Lamp (beta)", "LampElm"));

		Menu activeMenu = new Menu("Active Components");
		mainMenu.add(activeMenu);
		activeMenu.add(getClassCheckItem("Add Diode", "DiodeElm"));
		activeMenu.add(getClassCheckItem("Add Zener Diode", "ZenerElm"));
		activeMenu.add(getClassCheckItem("Add Transistor (bipolar, NPN)",
				"NTransistorElm"));
		activeMenu.add(getClassCheckItem("Add Transistor (bipolar, PNP)",
				"PTransistorElm"));
		activeMenu.add(getClassCheckItem("Add Op Amp (- on top)", "OpAmpElm"));
		activeMenu.add(getClassCheckItem("Add Op Amp (+ on top)",
				"OpAmpSwapElm"));
		activeMenu
				.add(getClassCheckItem("Add MOSFET (n-channel)", "NMosfetElm"));
		activeMenu
				.add(getClassCheckItem("Add MOSFET (p-channel)", "PMosfetElm"));
		activeMenu.add(getClassCheckItem("Add JFET (n-channel)", "NJfetElm"));
		activeMenu.add(getClassCheckItem("Add JFET (p-channel)", "PJfetElm"));
		activeMenu.add(getClassCheckItem("Add Analog Switch (SPST)",
				"AnalogSwitchElm"));
		activeMenu.add(getClassCheckItem("Add Analog Switch (SPDT)",
				"AnalogSwitch2Elm"));
		activeMenu.add(getClassCheckItem("Add SCR", "SCRElm"));
		// activeMenu.add(getClassCheckItem("Add Varactor/Varicap",
		// "VaractorElm"));
		activeMenu.add(getClassCheckItem("Add Tunnel Diode", "TunnelDiodeElm"));
		activeMenu.add(getClassCheckItem("Add Triode", "TriodeElm"));
		// activeMenu.add(getClassCheckItem("Add Diac", "DiacElm"));
		// activeMenu.add(getClassCheckItem("Add Triac", "TriacElm"));
		// activeMenu.add(getClassCheckItem("Add Photoresistor",
		// "PhotoResistorElm"));
		// activeMenu.add(getClassCheckItem("Add Thermistor", "ThermistorElm"));
		activeMenu.add(getClassCheckItem("Add CCII+", "CC2Elm"));
		activeMenu.add(getClassCheckItem("Add CCII-", "CC2NegElm"));

		Menu gateMenu = new Menu("Logic Gates");
		mainMenu.add(gateMenu);
		gateMenu.add(getClassCheckItem("Add Inverter", "InverterElm"));
		gateMenu.add(getClassCheckItem("Add NAND Gate", "NandGateElm"));
		gateMenu.add(getClassCheckItem("Add NOR Gate", "NorGateElm"));
		gateMenu.add(getClassCheckItem("Add AND Gate", "AndGateElm"));
		gateMenu.add(getClassCheckItem("Add OR Gate", "OrGateElm"));
		gateMenu.add(getClassCheckItem("Add XOR Gate", "XorGateElm"));

		Menu chipMenu = new Menu("Chips");
		mainMenu.add(chipMenu);
		chipMenu.add(getClassCheckItem("Add D Flip-Flop", "DFlipFlopElm"));
		chipMenu.add(getClassCheckItem("Add JK Flip-Flop", "JKFlipFlopElm"));
		chipMenu.add(getClassCheckItem("Add 7 Segment LED", "SevenSegElm"));
		chipMenu.add(getClassCheckItem("Add VCO", "VCOElm"));
		chipMenu.add(getClassCheckItem("Add Phase Comparator", "PhaseCompElm"));
		chipMenu.add(getClassCheckItem("Add Counter", "CounterElm"));
		chipMenu.add(getClassCheckItem("Add Decade Counter", "DecadeElm"));
		chipMenu.add(getClassCheckItem("Add 555 Timer", "TimerElm"));
		chipMenu.add(getClassCheckItem("Add DAC", "DACElm"));
		chipMenu.add(getClassCheckItem("Add ADC", "ADCElm"));
		chipMenu.add(getClassCheckItem("Add Latch", "LatchElm"));

		Menu otherMenu = new Menu("Other");
		mainMenu.add(otherMenu);
		otherMenu.add(getClassCheckItem("Add Text", "TextElm"));
		otherMenu.add(getClassCheckItem("Add Box", "BoxElm"));
		otherMenu.add(getClassCheckItem("Add Scope Probe", "ProbeElm"));
		otherMenu.add(getCheckItem("Drag All (Alt-drag)", "DragAll"));
		otherMenu.add(getCheckItem(isMac ? "Drag Row (Alt-S-drag, S-right)"
				: "Drag Row (S-right)", "DragRow"));
		otherMenu.add(getCheckItem(
				isMac ? "Drag Column (Alt-\u2318-drag, \u2318-right)"
						: "Drag Column (C-right)", "DragColumn"));
		otherMenu.add(getCheckItem("Drag Selected", "DragSelected"));
		otherMenu.add(getCheckItem("Drag Post (" + ctrlMetaKey + "-drag)",
				"DragPost"));

		mainMenu.add(getCheckItem("Select/Drag Selected (space or Shift-drag)",
				"Select"));
		main.add(mainMenu);

		main.add(resetButton = new Button("Reset"));
		resetButton.addActionListener(this);
		dumpMatrixButton = new Button("Dump Matrix");
		// main.add(dumpMatrixButton);
		dumpMatrixButton.addActionListener(this);
		stoppedCheck = new Checkbox("Stopped");
		stoppedCheck.addItemListener(this);
		main.add(stoppedCheck);

		main.add(new Label("Simulation Speed", Label.CENTER));

		// was max of 140
		main.add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 3, 1, 0, 260));
		speedBar.addAdjustmentListener(this);

		main.add(new Label("Current Speed", Label.CENTER));
		currentBar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 1, 100);
		currentBar.addAdjustmentListener(this);
		main.add(currentBar);

		main.add(powerLabel = new Label("Power Brightness", Label.CENTER));
		main.add(powerBar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 1, 100));
		powerBar.addAdjustmentListener(this);
		powerBar.setEnabled(false);
		powerLabel.setEnabled(false);

		main.add(new Label("www.falstad.com"));

		if (useFrame)
			main.add(new Label(""));
		Font f = new Font("SansSerif", 0, 10);
		Label l;
		l = new Label("Current Circuit:");
		l.setFont(f);
		titleLabel = new Label("Label");
		titleLabel.setFont(f);
		if (useFrame) {
			main.add(l);
			main.add(titleLabel);
		}

		setGrid();
		elmList = new Vector<CircuitElm>();
		// setupList = new Vector();
		undoStack = new Vector<String>();
		redoStack = new Vector<String>();

		scopes = new Scope[20];
		scopeColCount = new int[20];
		scopeCount = 0;

		random = new Random();
		cv.setBackground(Color.black);
		cv.setForeground(Color.lightGray);

		elmMenu = new PopupMenu();
		elmMenu.add(elmEditMenuItem = getMenuItem("Edit"));
		elmMenu.add(elmScopeMenuItem = getMenuItem("View in Scope"));
		elmMenu.add(elmCutMenuItem = getMenuItem("Cut"));
		elmMenu.add(elmCopyMenuItem = getMenuItem("Copy"));
		elmMenu.add(elmDeleteMenuItem = getMenuItem("Delete"));
		main.add(elmMenu);

		scopeMenu = buildScopeMenu(false);
		transScopeMenu = buildScopeMenu(true);

		getSetupList(circuitsMenu, false);
		if (useFrame)
			setMenuBar(mb);
		if (startCircuitText != null)
			readSetup(startCircuitText);
		else if (stopMessage == null && startCircuit != null)
			readSetupFile(startCircuit, startLabel);
		else
			readSetup(null, 0, false);

		if (useFrame) {
			Dimension screen = getToolkit().getScreenSize();
			setSize(860, 640);
			handleResize();
			Dimension x = getSize();
			setLocation((screen.width - x.width) / 2,
					(screen.height - x.height) / 2);
			setVisible(true);
		} else {
			if (!powerCheckItem.getState()) {
				main.remove(powerBar);
				main.remove(powerLabel);
				main.validate();
			}
			setVisible(false);
			handleResize();
			applet.validate();
		}
		requestFocus();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				destroyFrame();
			}
		});
	}

	boolean shown = false;

	public void triggerShow() {
		if (!shown)
			setVisible(true);
		shown = true;
	}

	public void requestFocus() {
		super.requestFocus();
		cv.requestFocus();
	}

	PopupMenu buildScopeMenu(boolean t) {
		PopupMenu m = new PopupMenu();
		m.add(getMenuItem("Remove", "remove"));
		m.add(getMenuItem("Speed 2x", "speed2"));
		m.add(getMenuItem("Speed 1/2x", "speed1/2"));
		m.add(getMenuItem("Scale 2x", "scale"));
		m.add(getMenuItem("Max Scale", "maxscale"));
		m.add(getMenuItem("Stack", "stack"));
		m.add(getMenuItem("Unstack", "unstack"));
		m.add(getMenuItem("Reset", "reset"));
		if (t) {
			m.add(scopeIbMenuItem = getCheckItem("Show Ib"));
			m.add(scopeIcMenuItem = getCheckItem("Show Ic"));
			m.add(scopeIeMenuItem = getCheckItem("Show Ie"));
			m.add(scopeVbeMenuItem = getCheckItem("Show Vbe"));
			m.add(scopeVbcMenuItem = getCheckItem("Show Vbc"));
			m.add(scopeVceMenuItem = getCheckItem("Show Vce"));
			m.add(scopeVceIcMenuItem = getCheckItem("Show Vce vs Ic"));
		} else {
			m.add(scopeVMenuItem = getCheckItem("Show Voltage"));
			m.add(scopeIMenuItem = getCheckItem("Show Current"));
			m.add(scopePowerMenuItem = getCheckItem("Show Power Consumed"));
			m.add(scopeMaxMenuItem = getCheckItem("Show Peak Value"));
			m.add(scopeMinMenuItem = getCheckItem("Show Negative Peak Value"));
			m.add(scopeFreqMenuItem = getCheckItem("Show Frequency"));
			m.add(scopeVIMenuItem = getCheckItem("Show V vs I"));
			m.add(scopeXYMenuItem = getCheckItem("Plot X/Y"));
			m.add(scopeSelectYMenuItem = getMenuItem("Select Y", "selecty"));
			m.add(scopeResistMenuItem = getCheckItem("Show Resistance"));
		}
		main.add(m);
		return m;
	}

	MenuItem getMenuItem(String s) {
		MenuItem mi = new MenuItem(s);
		mi.addActionListener(this);
		return mi;
	}

	MenuItem getMenuItem(String s, String ac) {
		MenuItem mi = new MenuItem(s);
		mi.setActionCommand(ac);
		mi.addActionListener(this);
		return mi;
	}

	CheckboxMenuItem getCheckItem(String s) {
		CheckboxMenuItem mi = new CheckboxMenuItem(s);
		mi.addItemListener(this);
		mi.setActionCommand("");
		return mi;
	}

	CheckboxMenuItem getClassCheckItem(String s, String t) {
		try {
			Class<?> c = Class.forName(t);
			CircuitElm elm = constructElement(c, 0, 0);
			register(c, elm);
			if (elm.needsShortcut()) {
				s += " (" + (char) elm.getShortcut() + ")";
			}
			elm.delete();
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return getCheckItem(s, t);
	}

	CheckboxMenuItem getCheckItem(String s, String t) {
		CheckboxMenuItem mi = new CheckboxMenuItem(s);
		mi.addItemListener(this);
		mi.setActionCommand(t);
		return mi;
	}

	void register(Class<?> c, CircuitElm elm) {
		int t = elm.getDumpType();
		if (t == 0) {
			System.out.println("no dump type: " + c);
			return;
		}

		int s = elm.getShortcut();
		if (elm.needsShortcut() && s == 0) {
			if (s == 0) {
				System.err.println("no shortcut " + c + " for " + c);
				return;
			} else if (s <= ' ' || s >= 127) {
				System.err.println("invalid shortcut " + c + " for " + c);
				return;
			}
		}

		Class<?> dclass = elm.getDumpClass();

		if (dumpTypes[t] != null && dumpTypes[t] != dclass) {
			System.out.println("dump type conflict: " + c + " " + dumpTypes[t]);
			return;
		}
		dumpTypes[t] = dclass;

		Class<? extends CircuitElm> sclass = elm.getClass();

		if (elm.needsShortcut() && shortcuts[s] != null
				&& shortcuts[s] != sclass) {
			System.err.println("shortcut conflict: " + c
					+ " (previously assigned to " + shortcuts[s] + ")");
		} else {
			shortcuts[s] = sclass;
		}
	}

	void handleResize() {
		winSize = cv.getSize();
		if (winSize.width == 0)
			return;
		dbimage = main.createImage(winSize.width, winSize.height);
		int h = winSize.height / 5;
		/*
		 * if (h < 128 && winSize.height > 300) h = 128;
		 */
		circuitArea = new Rectangle(0, 0, winSize.width, winSize.height - h);
		int i;
		int minx = 1000, maxx = 0, miny = 1000, maxy = 0;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			// centered text causes problems when trying to center the circuit,
			// so we special-case it here
			if (!ce.isCenteredText()) {
				minx = min(ce.x, min(ce.x2, minx));
				maxx = max(ce.x, max(ce.x2, maxx));
			}
			miny = min(ce.y, min(ce.y2, miny));
			maxy = max(ce.y, max(ce.y2, maxy));
		}
		// center circuit; we don't use snapGrid() because that rounds
		int dx = gridMask & ((circuitArea.width - (maxx - minx)) / 2 - minx);
		int dy = gridMask & ((circuitArea.height - (maxy - miny)) / 2 - miny);
		if (dx + minx < 0)
			dx = gridMask & (-minx);
		if (dy + miny < 0)
			dy = gridMask & (-miny);
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.move(dx, dy);
		}
		// after moving elements, need this to avoid singular matrix probs
		needAnalyze();
		circuitBottom = 0;
	}

	void destroyFrame() {
		if (applet == null) {
			dispose();
			System.exit(0);
		} else {
			applet.destroyFrame();
		}
	}

	public boolean handleEvent(Event ev) {
		if (ev.id == Event.WINDOW_DESTROY) {
			destroyFrame();
			return true;
		}
		return super.handleEvent(ev);
	}

	public void paint(Graphics g) {
		cv.repaint();
	}

	static final int resct = 6;
	long lastTime = 0, lastFrameTime, lastIterTime, secTime = 0;
	int frames = 0;
	int steps = 0;
	int framerate = 0, steprate = 0;

	public void updateCircuit(Graphics realg) {
		CircuitElm realMouseElm;
		if (winSize == null || winSize.width == 0)
			return;
		if (analyzeFlag) {
			analyzeCircuit();
			analyzeFlag = false;
		}
		if (editDialog != null && editDialog.elm instanceof CircuitElm)
			mouseElm = (CircuitElm) (editDialog.elm);
		realMouseElm = mouseElm;
		if (mouseElm == null)
			mouseElm = stopElm;
		setupScopes();
		Graphics2D g = null; // hausen: changed to Graphics2D
		g = (Graphics2D) dbimage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		CircuitElm.selectColor = Color.cyan;
		if (printableCheckItem.getState()) {
			CircuitElm.whiteColor = Color.black;
			CircuitElm.lightGrayColor = Color.black;
			g.setColor(Color.white);
		} else {
			CircuitElm.whiteColor = Color.white;
			CircuitElm.lightGrayColor = Color.lightGray;
			g.setColor(Color.black);
		}
		g.fillRect(0, 0, winSize.width, winSize.height);
		if (!stoppedCheck.getState()) {
			try {
				runCircuit();
			} catch (Exception e) {
				e.printStackTrace();
				analyzeFlag = true;
				cv.repaint();
				return;
			}
		}
		if (!stoppedCheck.getState()) {
			long sysTime = System.currentTimeMillis();
			if (lastTime != 0) {
				int inc = (int) (sysTime - lastTime);
				double c = currentBar.getValue();
				c = java.lang.Math.exp(c / 3.5 - 14.2);
				CircuitElm.currentMult = 1.7 * inc * c;
				if (!conventionCheckItem.getState())
					CircuitElm.currentMult = -CircuitElm.currentMult;
			}
			if (sysTime - secTime >= 1000) {
				framerate = frames;
				steprate = steps;
				frames = 0;
				steps = 0;
				secTime = sysTime;
			}
			lastTime = sysTime;
		} else
			lastTime = 0;
		CircuitElm.powerMult = Math.exp(powerBar.getValue() / 4.762 - 7);

		int i;
		Font oldfont = g.getFont();
		for (i = 0; i != elmList.size(); i++) {
			if (powerCheckItem.getState())
				g.setColor(Color.gray);
			/*
			 * else if (conductanceCheckItem.getState())
			 * g.setColor(Color.white);
			 */
			getElm(i).draw(g);
		}
		if (tempMouseMode == MODE_DRAG_ROW || tempMouseMode == MODE_DRAG_COLUMN
				|| tempMouseMode == MODE_DRAG_POST
				|| tempMouseMode == MODE_DRAG_SELECTED)
			for (i = 0; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				ce.drawPost(g, ce.x, ce.y);
				ce.drawPost(g, ce.x2, ce.y2);
			}
		int badnodes = 0;
		// find bad connections, nodes not connected to other elements which
		// intersect other elements' bounding boxes
		// debugged by hausen: nullPointerException
		if (nodeList != null)
			for (i = 0; i != nodeList.size(); i++) {
				CircuitNode cn = getCircuitNode(i);
				if (!cn.internal && cn.links.size() == 1) {
					int bb = 0, j;
					CircuitNodeLink cnl = cn.links.elementAt(0);
					for (j = 0; j != elmList.size(); j++) { // TODO: (hausen)
															// see if this
															// change does not
															// break stuff
						CircuitElm ce = getElm(j);
						if (ce instanceof GraphicElm)
							continue;
						if (cnl.elm != ce
								&& getElm(j).boundingBox.contains(cn.x, cn.y))
							bb++;
					}
					if (bb > 0) {
						g.setColor(Color.red);
						g.fillOval(cn.x - 3, cn.y - 3, 7, 7);
						badnodes++;
					}
				}
			}
		/*
		 * if (mouseElm != null) { g.setFont(oldfont); g.drawString("+",
		 * mouseElm.x+10, mouseElm.y); }
		 */
		if (dragElm != null
				&& (dragElm.x != dragElm.x2 || dragElm.y != dragElm.y2))
			dragElm.draw(g);
		g.setFont(oldfont);
		int ct = scopeCount;
		if (stopMessage != null)
			ct = 0;
		for (i = 0; i != ct; i++)
			scopes[i].draw(g);
		g.setColor(CircuitElm.whiteColor);
		if (stopMessage != null) {
			g.drawString(stopMessage, 10, circuitArea.height);
		} else {
			if (circuitBottom == 0)
				calcCircuitBottom();
			String info[] = new String[10];
			if (mouseElm != null) {
				if (mousePost == -1)
					mouseElm.getInfo(info);
				else
					info[0] = "V = "
							+ CircuitElm.getUnitText(
									mouseElm.getPostVoltage(mousePost), "V");
				/*
				 * //shownodes for (i = 0; i != mouseElm.getPostCount(); i++)
				 * info[0] += " " + mouseElm.nodes[i]; if
				 * (mouseElm.getVoltageSourceCount() > 0) info[0] += ";" +
				 * (mouseElm.getVoltageSource()+nodeList.size());
				 */

			} else {
				CircuitElm.showFormat.setMinimumFractionDigits(2);
				info[0] = "t = " + CircuitElm.getUnitText(t, "s");
				CircuitElm.showFormat.setMinimumFractionDigits(0);
			}
			if (hintType != -1) {
				for (i = 0; info[i] != null; i++)
					;
				String s = getHint();
				if (s == null)
					hintType = -1;
				else
					info[i] = s;
			}
			int x = 0;
			if (ct != 0)
				x = scopes[ct - 1].rightEdge() + 20;
			x = max(x, winSize.width * 2 / 3);

			// count lines of data
			for (i = 0; info[i] != null; i++)
				;
			if (badnodes > 0)
				info[i++] = badnodes
						+ ((badnodes == 1) ? " bad connection"
								: " bad connections");

			// find where to show data; below circuit, not too high unless we
			// need it
			int ybase = winSize.height - 15 * i - 5;
			ybase = min(ybase, circuitArea.height);
			ybase = max(ybase, circuitBottom);
			for (i = 0; info[i] != null; i++)
				g.drawString(info[i], x, ybase + 15 * (i + 1));
		}
		if (selectedArea != null) {
			g.setColor(CircuitElm.selectColor);
			g.drawRect(selectedArea.x, selectedArea.y, selectedArea.width,
					selectedArea.height);
		}
		mouseElm = realMouseElm;
		frames++;
		/*
		 * g.setColor(Color.white); g.drawString("Framerate: " + framerate, 10,
		 * 10); g.drawString("Steprate: " + steprate, 10, 30);
		 * g.drawString("Steprate/iter: " + (steprate/getIterCount()), 10, 50);
		 * g.drawString("iterc: " + (getIterCount()), 10, 70);
		 */

		realg.drawImage(dbimage, 0, 0, this);
		if (!stoppedCheck.getState() && circuitMatrix != null) {
			// Limit to 50 fps (thanks to Jurgen Klotzer for this)
			long delay = 1000 / 50 - (System.currentTimeMillis() - lastFrameTime);
			// realg.drawString("delay: " + delay, 10, 90);
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
			}

			cv.repaint(0);
		}
		lastFrameTime = lastTime;
	}

	void setupScopes() {
		int i;

		// check scopes to make sure the elements still exist, and remove
		// unused scopes/columns
		int pos = -1;
		for (i = 0; i < scopeCount; i++) {
			if (locateElm(scopes[i].elm) < 0)
				scopes[i].setElm(null);
			if (scopes[i].elm == null) {
				int j;
				for (j = i; j != scopeCount; j++)
					scopes[j] = scopes[j + 1];
				scopeCount--;
				i--;
				continue;
			}
			if (scopes[i].position > pos + 1)
				scopes[i].position = pos + 1;
			pos = scopes[i].position;
		}
		while (scopeCount > 0 && scopes[scopeCount - 1].elm == null)
			scopeCount--;
		int h = winSize.height - circuitArea.height;
		pos = 0;
		for (i = 0; i != scopeCount; i++)
			scopeColCount[i] = 0;
		for (i = 0; i != scopeCount; i++) {
			pos = max(scopes[i].position, pos);
			scopeColCount[scopes[i].position]++;
		}
		int colct = pos + 1;
		int iw = infoWidth;
		if (colct <= 2)
			iw = iw * 3 / 2;
		int w = (winSize.width - iw) / colct;
		int marg = 10;
		if (w < marg * 2)
			w = marg * 2;
		pos = -1;
		int colh = 0;
		int row = 0;
		int speed = 0;
		for (i = 0; i != scopeCount; i++) {
			Scope s = scopes[i];
			if (s.position > pos) {
				pos = s.position;
				colh = h / scopeColCount[pos];
				row = 0;
				speed = s.speed;
			}
			if (s.speed != speed) {
				s.speed = speed;
				s.resetGraph();
			}
			Rectangle r = new Rectangle(pos * w, winSize.height - h + colh
					* row, w - marg, colh);
			row++;
			if (!r.equals(s.rect))
				s.setRect(r);
		}
	}

	String getHint() {
		CircuitElm c1 = getElm(hintItem1);
		CircuitElm c2 = getElm(hintItem2);
		if (c1 == null || c2 == null)
			return null;
		if (hintType == HINT_LC) {
			if (!(c1 instanceof InductorElm))
				return null;
			if (!(c2 instanceof CapacitorElm))
				return null;
			InductorElm ie = (InductorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "res.f = "
					+ CircuitElm.getUnitText(
							1 / (2 * pi * Math.sqrt(ie.inductance
									* ce.capacitance)), "Hz");
		}
		if (hintType == HINT_RC) {
			if (!(c1 instanceof ResistorElm))
				return null;
			if (!(c2 instanceof CapacitorElm))
				return null;
			ResistorElm re = (ResistorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "RC = "
					+ CircuitElm.getUnitText(re.resistance * ce.capacitance,
							"s");
		}
		if (hintType == HINT_3DB_C) {
			if (!(c1 instanceof ResistorElm))
				return null;
			if (!(c2 instanceof CapacitorElm))
				return null;
			ResistorElm re = (ResistorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "f.3db = "
					+ CircuitElm
							.getUnitText(
									1 / (2 * pi * re.resistance * ce.capacitance),
									"Hz");
		}
		if (hintType == HINT_3DB_L) {
			if (!(c1 instanceof ResistorElm))
				return null;
			if (!(c2 instanceof InductorElm))
				return null;
			ResistorElm re = (ResistorElm) c1;
			InductorElm ie = (InductorElm) c2;
			return "f.3db = "
					+ CircuitElm.getUnitText(re.resistance
							/ (2 * pi * ie.inductance), "Hz");
		}
		if (hintType == HINT_TWINT) {
			if (!(c1 instanceof ResistorElm))
				return null;
			if (!(c2 instanceof CapacitorElm))
				return null;
			ResistorElm re = (ResistorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "fc = "
					+ CircuitElm
							.getUnitText(
									1 / (2 * pi * re.resistance * ce.capacitance),
									"Hz");
		}
		return null;
	}

	public void toggleSwitch(int n) {
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce instanceof SwitchElm) {
				n--;
				if (n == 0) {
					((SwitchElm) ce).toggle();
					analyzeFlag = true;
					cv.repaint();
					return;
				}
			}
		}
	}

	void needAnalyze() {
		analyzeFlag = true;
		cv.repaint();
	}

	Vector<CircuitNode> nodeList;
	CircuitElm voltageSources[];

	public CircuitNode getCircuitNode(int n) {
		if (n >= nodeList.size())
			return null;
		return nodeList.elementAt(n);
	}

	public CircuitElm getElm(int n) {
		if (n >= elmList.size())
			return null;
		return elmList.elementAt(n);
	}

	void analyzeCircuit() {
		calcCircuitBottom();
		if (elmList.isEmpty())
			return;
		stopMessage = null;
		stopElm = null;
		int i, j;
		int vscount = 0;
		nodeList = new Vector<CircuitNode>();
		boolean gotGround = false;
		boolean gotRail = false;
		CircuitElm volt = null;

		// System.out.println("ac1");
		// look for voltage or ground element
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce instanceof GroundElm) {
				gotGround = true;
				break;
			}
			if (ce instanceof RailElm)
				gotRail = true;
			if (volt == null && ce instanceof VoltageElm)
				volt = ce;
		}

		// if no ground, and no rails, then the voltage elm's first terminal
		// is ground
		if (!gotGround && volt != null && !gotRail) {
			CircuitNode cn = new CircuitNode();
			Point pt = volt.getPost(0);
			cn.x = pt.x;
			cn.y = pt.y;
			nodeList.addElement(cn);
		} else {
			// otherwise allocate extra node for ground
			CircuitNode cn = new CircuitNode();
			cn.x = cn.y = -1;
			nodeList.addElement(cn);
		}
		// System.out.println("ac2");

		// allocate nodes and voltage sources
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			int inodes = ce.getInternalNodeCount();
			int ivs = ce.getVoltageSourceCount();
			int posts = ce.getPostCount();

			// allocate a node for each post and match posts to nodes
			for (j = 0; j != posts; j++) {
				Point pt = ce.getPost(j);
				int k;
				for (k = 0; k != nodeList.size(); k++) {
					CircuitNode cn = getCircuitNode(k);
					if (pt.x == cn.x && pt.y == cn.y)
						break;
				}
				if (k == nodeList.size()) {
					CircuitNode cn = new CircuitNode();
					cn.x = pt.x;
					cn.y = pt.y;
					CircuitNodeLink cnl = new CircuitNodeLink();
					cnl.num = j;
					cnl.elm = ce;
					cn.links.addElement(cnl);
					ce.setNode(j, nodeList.size());
					nodeList.addElement(cn);
				} else {
					CircuitNodeLink cnl = new CircuitNodeLink();
					cnl.num = j;
					cnl.elm = ce;
					getCircuitNode(k).links.addElement(cnl);
					ce.setNode(j, k);
					// if it's the ground node, make sure the node voltage is 0,
					// cause it may not get set later
					if (k == 0)
						ce.setNodeVoltage(j, 0);
				}
			}
			for (j = 0; j != inodes; j++) {
				CircuitNode cn = new CircuitNode();
				cn.x = cn.y = -1;
				cn.internal = true;
				CircuitNodeLink cnl = new CircuitNodeLink();
				cnl.num = j + posts;
				cnl.elm = ce;
				cn.links.addElement(cnl);
				ce.setNode(cnl.num, nodeList.size());
				nodeList.addElement(cn);
			}
			vscount += ivs;
		}
		voltageSources = new CircuitElm[vscount];
		vscount = 0;
		circuitNonLinear = false;
		// System.out.println("ac3");

		// determine if circuit is nonlinear
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce.nonLinear())
				circuitNonLinear = true;
			int ivs = ce.getVoltageSourceCount();
			for (j = 0; j != ivs; j++) {
				voltageSources[vscount] = ce;
				ce.setVoltageSource(j, vscount++);
			}
		}
		voltageSourceCount = vscount;

		int matrixSize = nodeList.size() - 1 + vscount;
		circuitMatrix = new double[matrixSize][matrixSize];
		circuitRightSide = new double[matrixSize];
		origMatrix = new double[matrixSize][matrixSize];
		origRightSide = new double[matrixSize];
		circuitMatrixSize = circuitMatrixFullSize = matrixSize;
		circuitRowInfo = new RowInfo[matrixSize];
		circuitPermute = new int[matrixSize];
		//int vs = 0;	#unused
		for (i = 0; i != matrixSize; i++)
			circuitRowInfo[i] = new RowInfo();
		circuitNeedsMap = false;

		// stamp linear circuit elements
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.stamp();
		}
		// System.out.println("ac4");

		// determine nodes that are unconnected
		boolean closure[] = new boolean[nodeList.size()];
		// boolean tempclosure[] = new boolean[nodeList.size()]; #unused
		boolean changed = true;
		closure[0] = true;
		while (changed) {
			changed = false;
			for (i = 0; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				// loop through all ce's nodes to see if they are connected
				// to other nodes not in closure
				for (j = 0; j < ce.getPostCount(); j++) {
					if (!closure[ce.getNode(j)]) {
						if (ce.hasGroundConnection(j))
							closure[ce.getNode(j)] = changed = true;
						continue;
					}
					int k;
					for (k = 0; k != ce.getPostCount(); k++) {
						if (j == k)
							continue;
						int kn = ce.getNode(k);
						if (ce.getConnection(j, k) && !closure[kn]) {
							closure[kn] = true;
							changed = true;
						}
					}
				}
			}
			if (changed)
				continue;

			// connect unconnected nodes
			for (i = 0; i != nodeList.size(); i++)
				if (!closure[i] && !getCircuitNode(i).internal) {
					System.out.println("node " + i + " unconnected");
					stampResistor(0, i, 1e8);
					closure[i] = true;
					changed = true;
					break;
				}
		}
		// System.out.println("ac5");

		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			// look for inductors with no current path
			if (ce instanceof InductorElm) {
				FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce,
						ce.getNode(1));
				// first try findPath with maximum depth of 5, to avoid
				// slowdowns
				if (!fpi.findPath(ce.getNode(0), 5)
						&& !fpi.findPath(ce.getNode(0))) {
					System.out.println(ce + " no path");
					ce.reset();
				}
			}
			// look for current sources with no current path
			if (ce instanceof CurrentElm) {
				FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce,
						ce.getNode(1));
				if (!fpi.findPath(ce.getNode(0))) {
					stop("No path for current source!", ce);
					return;
				}
			}
			// look for voltage source loops
			if ((ce instanceof VoltageElm && ce.getPostCount() == 2)
					|| ce instanceof WireElm) {
				FindPathInfo fpi = new FindPathInfo(FindPathInfo.VOLTAGE, ce,
						ce.getNode(1));
				if (fpi.findPath(ce.getNode(0))) {
					stop("Voltage source/wire loop with no resistance!", ce);
					return;
				}
			}
			// look for shorted caps, or caps w/ voltage but no R
			if (ce instanceof CapacitorElm) {
				FindPathInfo fpi = new FindPathInfo(FindPathInfo.SHORT, ce,
						ce.getNode(1));
				if (fpi.findPath(ce.getNode(0))) {
					System.out.println(ce + " shorted");
					ce.reset();
				} else {
					fpi = new FindPathInfo(FindPathInfo.CAP_V, ce,
							ce.getNode(1));
					if (fpi.findPath(ce.getNode(0))) {
						stop("Capacitor loop with no resistance!", ce);
						return;
					}
				}
			}
		}
		// System.out.println("ac6");

		// simplify the matrix; this speeds things up quite a bit
		for (i = 0; i != matrixSize; i++) {
			int qm = -1, qp = -1;
			double qv = 0;
			RowInfo re = circuitRowInfo[i];
			/*
			 * System.out.println("row " + i + " " + re.lsChanges + " " +
			 * re.rsChanges + " " + re.dropRow);
			 */
			if (re.lsChanges || re.dropRow || re.rsChanges)
				continue;
			double rsadd = 0;

			// look for rows that can be removed
			for (j = 0; j != matrixSize; j++) {
				double q = circuitMatrix[i][j];
				if (circuitRowInfo[j].type == RowInfo.ROW_CONST) {
					// keep a running total of const values that have been
					// removed already
					rsadd -= circuitRowInfo[j].value * q;
					continue;
				}
				if (q == 0)
					continue;
				if (qp == -1) {
					qp = j;
					qv = q;
					continue;
				}
				if (qm == -1 && q == -qv) {
					qm = j;
					continue;
				}
				break;
			}
			// System.out.println("line " + i + " " + qp + " " + qm + " " + j);
			/*
			 * if (qp != -1 && circuitRowInfo[qp].lsChanges) {
			 * System.out.println("lschanges"); continue; } if (qm != -1 &&
			 * circuitRowInfo[qm].lsChanges) { System.out.println("lschanges");
			 * continue; }
			 */
			if (j == matrixSize) {
				if (qp == -1) {
					stop("Matrix error", null);
					return;
				}
				RowInfo elt = circuitRowInfo[qp];
				if (qm == -1) {
					// we found a row with only one nonzero entry; that value
					// is a constant
					int k;
					for (k = 0; elt.type == RowInfo.ROW_EQUAL && k < 100; k++) {
						// follow the chain
						/*
						 * System.out.println("following equal chain from " + i
						 * + " " + qp + " to " + elt.nodeEq);
						 */
						qp = elt.nodeEq;
						elt = circuitRowInfo[qp];
					}
					if (elt.type == RowInfo.ROW_EQUAL) {
						// break equal chains
						// System.out.println("Break equal chain");
						elt.type = RowInfo.ROW_NORMAL;
						continue;
					}
					if (elt.type != RowInfo.ROW_NORMAL) {
						System.out.println("type already " + elt.type + " for "
								+ qp + "!");
						continue;
					}
					elt.type = RowInfo.ROW_CONST;
					elt.value = (circuitRightSide[i] + rsadd) / qv;
					circuitRowInfo[i].dropRow = true;
					// System.out.println(qp + " * " + qv + " = const " +
					// elt.value);
					i = -1; // start over from scratch
				} else if (circuitRightSide[i] + rsadd == 0) {
					// we found a row with only two nonzero entries, and one
					// is the negative of the other; the values are equal
					if (elt.type != RowInfo.ROW_NORMAL) {
						// System.out.println("swapping");
						int qq = qm;
						qm = qp;
						qp = qq;
						elt = circuitRowInfo[qp];
						if (elt.type != RowInfo.ROW_NORMAL) {
							// we should follow the chain here, but this
							// hardly ever happens so it's not worth worrying
							// about
							System.out.println("swap failed");
							continue;
						}
					}
					elt.type = RowInfo.ROW_EQUAL;
					elt.nodeEq = qm;
					circuitRowInfo[i].dropRow = true;
					// System.out.println(qp + " = " + qm);
				}
			}
		}
		// System.out.println("ac7");

		// find size of new matrix
		int nn = 0;
		for (i = 0; i != matrixSize; i++) {
			RowInfo elt = circuitRowInfo[i];
			if (elt.type == RowInfo.ROW_NORMAL) {
				elt.mapCol = nn++;
				// System.out.println("col " + i + " maps to " + elt.mapCol);
				continue;
			}
			if (elt.type == RowInfo.ROW_EQUAL) {
				RowInfo e2 = null;
				// resolve chains of equality; 100 max steps to avoid loops
				for (j = 0; j != 100; j++) {
					e2 = circuitRowInfo[elt.nodeEq];
					if (e2.type != RowInfo.ROW_EQUAL)
						break;
					if (i == e2.nodeEq)
						break;
					elt.nodeEq = e2.nodeEq;
				}
			}
			if (elt.type == RowInfo.ROW_CONST)
				elt.mapCol = -1;
		}
		for (i = 0; i != matrixSize; i++) {
			RowInfo elt = circuitRowInfo[i];
			if (elt.type == RowInfo.ROW_EQUAL) {
				RowInfo e2 = circuitRowInfo[elt.nodeEq];
				if (e2.type == RowInfo.ROW_CONST) {
					// if something is equal to a const, it's a const
					elt.type = e2.type;
					elt.value = e2.value;
					elt.mapCol = -1;
					// System.out.println(i + " = [late]const " + elt.value);
				} else {
					elt.mapCol = e2.mapCol;
					// System.out.println(i + " maps to: " + e2.mapCol);
				}
			}
		}
		// System.out.println("ac8");

		/*
		 * System.out.println("matrixSize = " + matrixSize);
		 * 
		 * for (j = 0; j != circuitMatrixSize; j++) { System.out.println(j +
		 * ": "); for (i = 0; i != circuitMatrixSize; i++)
		 * System.out.print(circuitMatrix[j][i] + " "); System.out.print("  " +
		 * circuitRightSide[j] + "\n"); } System.out.print("\n");
		 */

		// make the new, simplified matrix
		int newsize = nn;
		double newmatx[][] = new double[newsize][newsize];
		double newrs[] = new double[newsize];
		int ii = 0;
		for (i = 0; i != matrixSize; i++) {
			RowInfo rri = circuitRowInfo[i];
			if (rri.dropRow) {
				rri.mapRow = -1;
				continue;
			}
			newrs[ii] = circuitRightSide[i];
			rri.mapRow = ii;
			// System.out.println("Row " + i + " maps to " + ii);
			for (j = 0; j != matrixSize; j++) {
				RowInfo ri = circuitRowInfo[j];
				if (ri.type == RowInfo.ROW_CONST)
					newrs[ii] -= ri.value * circuitMatrix[i][j];
				else
					newmatx[ii][ri.mapCol] += circuitMatrix[i][j];
			}
			ii++;
		}

		circuitMatrix = newmatx;
		circuitRightSide = newrs;
		matrixSize = circuitMatrixSize = newsize;
		for (i = 0; i != matrixSize; i++)
			origRightSide[i] = circuitRightSide[i];
		for (i = 0; i != matrixSize; i++)
			for (j = 0; j != matrixSize; j++)
				origMatrix[i][j] = circuitMatrix[i][j];
		circuitNeedsMap = true;

		/*
		 * System.out.println("matrixSize = " + matrixSize + " " +
		 * circuitNonLinear); for (j = 0; j != circuitMatrixSize; j++) { for (i
		 * = 0; i != circuitMatrixSize; i++)
		 * System.out.print(circuitMatrix[j][i] + " "); System.out.print("  " +
		 * circuitRightSide[j] + "\n"); } System.out.print("\n");
		 */

		// if a matrix is linear, we can do the lu_factor here instead of
		// needing to do it every frame
		if (!circuitNonLinear) {
			if (!lu_factor(circuitMatrix, circuitMatrixSize, circuitPermute)) {
				stop("Singular matrix!", null);
				return;
			}
		}
	}

	void calcCircuitBottom() {
		int i;
		circuitBottom = 0;
		for (i = 0; i != elmList.size(); i++) {
			Rectangle rect = getElm(i).boundingBox;
			int bottom = rect.height + rect.y;
			if (bottom > circuitBottom)
				circuitBottom = bottom;
		}
	}

	class FindPathInfo {
		static final int INDUCT = 1;
		static final int VOLTAGE = 2;
		static final int SHORT = 3;
		static final int CAP_V = 4;
		boolean used[];
		int dest;
		CircuitElm firstElm;
		int type;

		FindPathInfo(int t, CircuitElm e, int d) {
			dest = d;
			type = t;
			firstElm = e;
			used = new boolean[nodeList.size()];
		}

		boolean findPath(int n1) {
			return findPath(n1, -1);
		}

		boolean findPath(int n1, int depth) {
			if (n1 == dest)
				return true;
			if (depth-- == 0)
				return false;
			if (used[n1]) {
				// System.out.println("used " + n1);
				return false;
			}
			used[n1] = true;
			int i;
			for (i = 0; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				if (ce == firstElm)
					continue;
				if (type == INDUCT) {
					if (ce instanceof CurrentElm)
						continue;
				}
				if (type == VOLTAGE) {
					if (!(ce.isWire() || ce instanceof VoltageElm))
						continue;
				}
				if (type == SHORT && !ce.isWire())
					continue;
				if (type == CAP_V) {
					if (!(ce.isWire() || ce instanceof CapacitorElm || ce instanceof VoltageElm))
						continue;
				}
				if (n1 == 0) {
					// look for posts which have a ground connection;
					// our path can go through ground
					int j;
					for (j = 0; j != ce.getPostCount(); j++)
						if (ce.hasGroundConnection(j)
								&& findPath(ce.getNode(j), depth)) {
							used[n1] = false;
							return true;
						}
				}
				int j;
				for (j = 0; j != ce.getPostCount(); j++) {
					// System.out.println(ce + " " + ce.getNode(j));
					if (ce.getNode(j) == n1)
						break;
				}
				if (j == ce.getPostCount())
					continue;
				if (ce.hasGroundConnection(j) && findPath(0, depth)) {
					// System.out.println(ce + " has ground");
					used[n1] = false;
					return true;
				}
				if (type == INDUCT && ce instanceof InductorElm) {
					double c = ce.getCurrent();
					if (j == 0)
						c = -c;
					// System.out.println("matching " + c + " to " +
					// firstElm.getCurrent());
					// System.out.println(ce + " " + firstElm);
					if (Math.abs(c - firstElm.getCurrent()) > 1e-10)
						continue;
				}
				int k;
				for (k = 0; k != ce.getPostCount(); k++) {
					if (j == k)
						continue;
					// System.out.println(ce + " " + ce.getNode(j) + "-" +
					// ce.getNode(k));
					if (ce.getConnection(j, k)
							&& findPath(ce.getNode(k), depth)) {
						// System.out.println("got findpath " + n1);
						used[n1] = false;
						return true;
					}
					// System.out.println("back on findpath " + n1);
				}
			}
			used[n1] = false;
			// System.out.println(n1 + " failed");
			return false;
		}
	}

	void stop(String s, CircuitElm ce) {
		stopMessage = s;
		circuitMatrix = null;
		stopElm = ce;
		stoppedCheck.setState(true);
		analyzeFlag = false;
		cv.repaint();
	}

	// control voltage source vs with voltage from n1 to n2 (must
	// also call stampVoltageSource())
	void stampVCVS(int n1, int n2, double coef, int vs) {
		int vn = nodeList.size() + vs;
		stampMatrix(vn, n1, coef);
		stampMatrix(vn, n2, -coef);
	}

	// stamp independent voltage source #vs, from n1 to n2, amount v
	void stampVoltageSource(int n1, int n2, int vs, double v) {
		int vn = nodeList.size() + vs;
		stampMatrix(vn, n1, -1);
		stampMatrix(vn, n2, 1);
		stampRightSide(vn, v);
		stampMatrix(n1, vn, 1);
		stampMatrix(n2, vn, -1);
	}

	// use this if the amount of voltage is going to be updated in doStep()
	void stampVoltageSource(int n1, int n2, int vs) {
		int vn = nodeList.size() + vs;
		stampMatrix(vn, n1, -1);
		stampMatrix(vn, n2, 1);
		stampRightSide(vn);
		stampMatrix(n1, vn, 1);
		stampMatrix(n2, vn, -1);
	}

	void updateVoltageSource(int n1, int n2, int vs, double v) {
		int vn = nodeList.size() + vs;
		stampRightSide(vn, v);
	}

	void stampResistor(int n1, int n2, double r) {
		double r0 = 1 / r;
		if (Double.isNaN(r0) || Double.isInfinite(r0)) {
			System.out.print("bad resistance " + r + " " + r0 + "\n");
			int a = 0;
			a /= a;
		}
		stampMatrix(n1, n1, r0);
		stampMatrix(n2, n2, r0);
		stampMatrix(n1, n2, -r0);
		stampMatrix(n2, n1, -r0);
	}

	void stampConductance(int n1, int n2, double r0) {
		stampMatrix(n1, n1, r0);
		stampMatrix(n2, n2, r0);
		stampMatrix(n1, n2, -r0);
		stampMatrix(n2, n1, -r0);
	}

	// current from cn1 to cn2 is equal to voltage from vn1 to 2, divided by g
	void stampVCCurrentSource(int cn1, int cn2, int vn1, int vn2, double g) {
		stampMatrix(cn1, vn1, g);
		stampMatrix(cn2, vn2, g);
		stampMatrix(cn1, vn2, -g);
		stampMatrix(cn2, vn1, -g);
	}

	void stampCurrentSource(int n1, int n2, double i) {
		stampRightSide(n1, -i);
		stampRightSide(n2, i);
	}

	// stamp a current source from n1 to n2 depending on current through vs
	void stampCCCS(int n1, int n2, int vs, double gain) {
		int vn = nodeList.size() + vs;
		stampMatrix(n1, vn, gain);
		stampMatrix(n2, vn, -gain);
	}

	// stamp value x in row i, column j, meaning that a voltage change
	// of dv in node j will increase the current into node i by x dv.
	// (Unless i or j is a voltage source node.)
	void stampMatrix(int i, int j, double x) {
		if (i > 0 && j > 0) {
			if (circuitNeedsMap) {
				i = circuitRowInfo[i - 1].mapRow;
				RowInfo ri = circuitRowInfo[j - 1];
				if (ri.type == RowInfo.ROW_CONST) {
					// System.out.println("Stamping constant " + i + " " + j +
					// " " + x);
					circuitRightSide[i] -= x * ri.value;
					return;
				}
				j = ri.mapCol;
				// System.out.println("stamping " + i + " " + j + " " + x);
			} else {
				i--;
				j--;
			}
			circuitMatrix[i][j] += x;
		}
	}

	// stamp value x on the right side of row i, representing an
	// independent current source flowing into node i
	void stampRightSide(int i, double x) {
		if (i > 0) {
			if (circuitNeedsMap) {
				i = circuitRowInfo[i - 1].mapRow;
				// System.out.println("stamping " + i + " " + x);
			} else
				i--;
			circuitRightSide[i] += x;
		}
	}

	// indicate that the value on the right side of row i changes in doStep()
	void stampRightSide(int i) {
		// System.out.println("rschanges true " + (i-1));
		if (i > 0)
			circuitRowInfo[i - 1].rsChanges = true;
	}

	// indicate that the values on the left side of row i change in doStep()
	void stampNonLinear(int i) {
		if (i > 0)
			circuitRowInfo[i - 1].lsChanges = true;
	}

	double getIterCount() {
		if (speedBar.getValue() == 0)
			return 0;
		// return (Math.exp((speedBar.getValue()-1)/24.) + .5);
		return .1 * Math.exp((speedBar.getValue() - 61) / 24.);
	}

	boolean converged;
	int subIterations;

	void runCircuit() {
		if (circuitMatrix == null || elmList.size() == 0) {
			circuitMatrix = null;
			return;
		}
		int iter;
		// int maxIter = getIterCount();
		boolean debugprint = dumpMatrix;
		dumpMatrix = false;
		long steprate = (long) (160 * getIterCount());
		long tm = System.currentTimeMillis();
		long lit = lastIterTime;
		if (1000 >= steprate * (tm - lastIterTime))
			return;
		for (iter = 1;; iter++) {
			int i, j, k, subiter;
			for (i = 0; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				ce.startIteration();
			}
			steps++;
			final int subiterCount = 5000;
			for (subiter = 0; subiter != subiterCount; subiter++) {
				converged = true;
				subIterations = subiter;
				for (i = 0; i != circuitMatrixSize; i++)
					circuitRightSide[i] = origRightSide[i];
				if (circuitNonLinear) {
					for (i = 0; i != circuitMatrixSize; i++)
						for (j = 0; j != circuitMatrixSize; j++)
							circuitMatrix[i][j] = origMatrix[i][j];
				}
				for (i = 0; i != elmList.size(); i++) {
					CircuitElm ce = getElm(i);
					ce.doStep();
				}
				if (stopMessage != null)
					return;
				boolean printit = debugprint;
				debugprint = false;
				for (j = 0; j != circuitMatrixSize; j++) {
					for (i = 0; i != circuitMatrixSize; i++) {
						double x = circuitMatrix[i][j];
						if (Double.isNaN(x) || Double.isInfinite(x)) {
							stop("nan/infinite matrix!", null);
							return;
						}
					}
				}
				if (printit) {
					for (j = 0; j != circuitMatrixSize; j++) {
						for (i = 0; i != circuitMatrixSize; i++)
							System.out.print(circuitMatrix[j][i] + ",");
						System.out.print("  " + circuitRightSide[j] + "\n");
					}
					System.out.print("\n");
				}
				if (circuitNonLinear) {
					if (converged && subiter > 0)
						break;
					if (!lu_factor(circuitMatrix, circuitMatrixSize,
							circuitPermute)) {
						stop("Singular matrix!", null);
						return;
					}
				}
				lu_solve(circuitMatrix, circuitMatrixSize, circuitPermute,
						circuitRightSide);

				for (j = 0; j != circuitMatrixFullSize; j++) {
					RowInfo ri = circuitRowInfo[j];
					double res = 0;
					if (ri.type == RowInfo.ROW_CONST)
						res = ri.value;
					else
						res = circuitRightSide[ri.mapCol];
					/*
					 * System.out.println(j + " " + res + " " + ri.type + " " +
					 * ri.mapCol);
					 */
					if (Double.isNaN(res)) {
						converged = false;
						// debugprint = true;
						break;
					}
					if (j < nodeList.size() - 1) {
						CircuitNode cn = getCircuitNode(j + 1);
						for (k = 0; k != cn.links.size(); k++) {
							CircuitNodeLink cnl = (CircuitNodeLink) cn.links
									.elementAt(k);
							cnl.elm.setNodeVoltage(cnl.num, res);
						}
					} else {
						int ji = j - (nodeList.size() - 1);
						// System.out.println("setting vsrc " + ji + " to " +
						// res);
						voltageSources[ji].setCurrent(ji, res);
					}
				}
				if (!circuitNonLinear)
					break;
			}
			if (subiter > 5)
				System.out
						.print("converged after " + subiter + " iterations\n");
			if (subiter == subiterCount) {
				stop("Convergence failed!", null);
				break;
			}
			t += timeStep;
			for (i = 0; i != scopeCount; i++)
				scopes[i].timeStep();
			tm = System.currentTimeMillis();
			lit = tm;
			if (iter * 1000 >= steprate * (tm - lastIterTime)
					|| (tm - lastFrameTime > 500))
				break;
		}
		lastIterTime = lit;
		// System.out.println((System.currentTimeMillis()-lastFrameTime)/(double)
		// iter);
	}

	int min(int a, int b) {
		return (a < b) ? a : b;
	}

	int max(int a, int b) {
		return (a > b) ? a : b;
	}

	void editFuncPoint(int x, int y) {
		// XXX
		cv.repaint(pause);
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
		cv.repaint();
	}

	public void componentResized(ComponentEvent e) {
		handleResize();
		cv.repaint(100);
	}

	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		if (e.getSource() == resetButton) {
			int i;

			// on IE, drawImage() stops working inexplicably every once in
			// a while. Recreating it fixes the problem, so we do that here.
			dbimage = main.createImage(winSize.width, winSize.height);

			for (i = 0; i != elmList.size(); i++)
				getElm(i).reset();
			for (i = 0; i != scopeCount; i++)
				scopes[i].resetGraph();
			analyzeFlag = true;
			t = 0;
			stoppedCheck.setState(false);
			cv.repaint();
		}
		if (e.getSource() == dumpMatrixButton)
			dumpMatrix = true;
		if (e.getSource() == exportItem)
			doExport(false);
		if (e.getSource() == optionsItem)
			doEdit(new EditOptions(this));
		if (e.getSource() == importItem)
			doImport();
		if (e.getSource() == exportLinkItem)
			doExport(true);
		if (e.getSource() == undoItem)
			doUndo();
		if (e.getSource() == redoItem)
			doRedo();
		if (ac.compareTo("Cut") == 0) {
			if (e.getSource() != elmCutMenuItem)
				menuElm = null;
			doCut();
		}
		if (ac.compareTo("Copy") == 0) {
			if (e.getSource() != elmCopyMenuItem)
				menuElm = null;
			doCopy();
		}
		if (ac.compareTo("Paste") == 0)
			doPaste();
		if (e.getSource() == selectAllItem)
			doSelectAll();
		if (e.getSource() == exitItem) {
			destroyFrame();
			return;
		}
		if (ac.compareTo("stackAll") == 0)
			stackAll();
		if (ac.compareTo("unstackAll") == 0)
			unstackAll();
		if (e.getSource() == elmEditMenuItem)
			doEdit(menuElm);
		if (ac.compareTo("Delete") == 0) {
			if (e.getSource() != elmDeleteMenuItem)
				menuElm = null;
			doDelete();
		}
		if (e.getSource() == elmScopeMenuItem && menuElm != null) {
			int i;
			for (i = 0; i != scopeCount; i++)
				if (scopes[i].elm == null)
					break;
			if (i == scopeCount) {
				if (scopeCount == scopes.length)
					return;
				scopeCount++;
				scopes[i] = new Scope(this);
				scopes[i].position = i;
				handleResize();
			}
			scopes[i].setElm(menuElm);
		}
		if (menuScope != -1) {
			if (ac.compareTo("remove") == 0)
				scopes[menuScope].setElm(null);
			if (ac.compareTo("speed2") == 0)
				scopes[menuScope].speedUp();
			if (ac.compareTo("speed1/2") == 0)
				scopes[menuScope].slowDown();
			if (ac.compareTo("scale") == 0)
				scopes[menuScope].adjustScale(.5);
			if (ac.compareTo("maxscale") == 0)
				scopes[menuScope].adjustScale(1e-50);
			if (ac.compareTo("stack") == 0)
				stackScope(menuScope);
			if (ac.compareTo("unstack") == 0)
				unstackScope(menuScope);
			if (ac.compareTo("selecty") == 0)
				scopes[menuScope].selectY();
			if (ac.compareTo("reset") == 0)
				scopes[menuScope].resetGraph();
			cv.repaint();
		}
		if (ac.indexOf("setup ") == 0) {
			pushUndo();
			readSetupFile(ac.substring(6),
					((MenuItem) e.getSource()).getLabel());
		}
	}

	void stackScope(int s) {
		if (s == 0) {
			if (scopeCount < 2)
				return;
			s = 1;
		}
		if (scopes[s].position == scopes[s - 1].position)
			return;
		scopes[s].position = scopes[s - 1].position;
		for (s++; s < scopeCount; s++)
			scopes[s].position--;
	}

	void unstackScope(int s) {
		if (s == 0) {
			if (scopeCount < 2)
				return;
			s = 1;
		}
		if (scopes[s].position != scopes[s - 1].position)
			return;
		for (; s < scopeCount; s++)
			scopes[s].position++;
	}

	void stackAll() {
		int i;
		for (i = 0; i != scopeCount; i++) {
			scopes[i].position = 0;
			scopes[i].showMax = scopes[i].showMin = false;
		}
	}

	void unstackAll() {
		int i;
		for (i = 0; i != scopeCount; i++) {
			scopes[i].position = i;
			scopes[i].showMax = true;
		}
	}

	void doEdit(Editable eable) {
		clearSelection();
		pushUndo();
		if (editDialog != null) {
			requestFocus();
			editDialog.setVisible(false);
			editDialog = null;
		}
		editDialog = new EditDialog(eable, this);
		editDialog.setVisible(true);
	}

	void doImport() {
		if (impDialog == null)
			impDialog = ImportExportDialogFactory.Create(this,
					ImportExportDialog.Action.IMPORT);
		// impDialog = new ImportExportClipboardDialog(this,
		// ImportExportDialog.Action.IMPORT);
		pushUndo();
		impDialog.execute();
	}

	void doExport(boolean url) {
		String dump = dumpCircuit();
		if (url)
			try {
				dump = baseURL + "#" + URLEncoder.encode(dump, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (expDialog == null) {
			expDialog = ImportExportDialogFactory.Create(this,
					ImportExportDialog.Action.EXPORT);
			// expDialog = new ImportExportClipboardDialog(this,
			// ImportExportDialog.Action.EXPORT);
		}
		expDialog.setDump(dump);
		expDialog.execute();
	}

	String dumpCircuit() {
		int i;
		int f = (dotsCheckItem.getState()) ? 1 : 0;
		f |= (smallGridCheckItem.getState()) ? 2 : 0;
		f |= (voltsCheckItem.getState()) ? 0 : 4;
		f |= (powerCheckItem.getState()) ? 8 : 0;
		f |= (showValuesCheckItem.getState()) ? 0 : 16;
		// 32 = linear scale in afilter
		String dump = "$ " + f + " " + timeStep + " " + getIterCount() + " "
				+ currentBar.getValue() + " " + CircuitElm.voltageRange + " "
				+ powerBar.getValue() + "\n";
		for (i = 0; i != elmList.size(); i++)
			dump += getElm(i).dump() + "\n";
		for (i = 0; i != scopeCount; i++) {
			String d = scopes[i].dump();
			if (d != null)
				dump += d + "\n";
		}
		if (hintType != -1)
			dump += "h " + hintType + " " + hintItem1 + " " + hintItem2 + "\n";
		return dump;
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		System.out.print(((Scrollbar) e.getSource()).getValue() + "\n");
	}

	ByteArrayOutputStream readUrlData(URL url) throws java.io.IOException {
		Object o = url.getContent();
		FilterInputStream fis = (FilterInputStream) o;
		ByteArrayOutputStream ba = new ByteArrayOutputStream(fis.available());
		int blen = 1024;
		byte b[] = new byte[blen];
		while (true) {
			int len = fis.read(b);
			if (len <= 0)
				break;
			ba.write(b, 0, len);
		}
		fis.close();
		return ba;
	}

	URL getCodeBase() {
		try {
			if (applet != null)
				return applet.getCodeBase();
			File f = new File(".");
			return new URL("file:" + f.getCanonicalPath() + "/");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	void getSetupList(Menu menu, boolean retry) {
		Menu stack[] = new Menu[6];
		int stackptr = 0;
		stack[stackptr++] = menu;
		try {
			// hausen: if setuplist.txt does not exist in the same
			// directory, try reading from the jar file
			ByteArrayOutputStream ba = null;
			try {
				URL url = new URL(getCodeBase() + "setuplist.txt");
				ba = readUrlData(url);
			} catch (Exception e) {
				URL url = getClass().getClassLoader().getResource(
						"setuplist.txt");
				ba = readUrlData(url);
			}
			// /hausen

			byte b[] = ba.toByteArray();
			int len = ba.size();
			int p;
			if (len == 0 || b[0] != '#') {
				// got a redirect, try again
				getSetupList(menu, true);
				return;
			}
			for (p = 0; p < len;) {
				int l;
				for (l = 0; l != len - p; l++)
					if (b[l + p] == '\n') {
						l++;
						break;
					}
				String line = new String(b, p, l - 1);
				if (line.charAt(0) == '#')
					;
				else if (line.charAt(0) == '+') {
					Menu n = new Menu(line.substring(1));
					menu.add(n);
					menu = stack[stackptr++] = n;
				} else if (line.charAt(0) == '-') {
					menu = stack[--stackptr - 1];
				} else {
					int i = line.indexOf(' ');
					if (i > 0) {
						String title = line.substring(i + 1);
						boolean first = false;
						if (line.charAt(0) == '>')
							first = true;
						String file = line.substring(first ? 1 : 0, i);
						menu.add(getMenuItem(title, "setup " + file));
						if (first && startCircuit == null) {
							startCircuit = file;
							startLabel = title;
						}
					}
				}
				p += l;
			}
		} catch (Exception e) {
			e.printStackTrace();
			stop("Can't read setuplist.txt!", null);
		}
	}

	void readSetup(String text) {
		readSetup(text, false);
	}

	void readSetup(String text, boolean retain) {
		readSetup(text.getBytes(), text.length(), retain);
		titleLabel.setText("untitled");
	}

	void readSetupFile(String str, String title) {
		t = 0;
		System.out.println(str);
		try {
			URL url = new URL(getCodeBase() + "circuits/" + str);
			ByteArrayOutputStream ba = readUrlData(url);
			readSetup(ba.toByteArray(), ba.size(), false);
		} catch (Exception e1) {
			try {
				URL url = getClass().getClassLoader().getResource(
						"circuits/" + str);
				ByteArrayOutputStream ba = readUrlData(url);
				readSetup(ba.toByteArray(), ba.size(), false);
			} catch (Exception e) {
				e.printStackTrace();
				stop("Unable to read " + str + "!", null);
			}
		}
		titleLabel.setText(title);
	}

	void readSetup(byte b[], int len, boolean retain) {
		int i;
		if (!retain) {
			for (i = 0; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				ce.delete();
			}
			elmList.removeAllElements();
			hintType = -1;
			timeStep = 5e-6;
			dotsCheckItem.setState(false); // hausen: changed from true to false
			smallGridCheckItem.setState(false);
			powerCheckItem.setState(false);
			voltsCheckItem.setState(true);
			showValuesCheckItem.setState(true);
			setGrid();
			speedBar.setValue(117); // 57
			currentBar.setValue(50);
			powerBar.setValue(50);
			CircuitElm.voltageRange = 5;
			scopeCount = 0;
		}
		cv.repaint();
		int p;
		for (p = 0; p < len;) {
			int l;
			int linelen = 0;
			for (l = 0; l != len - p; l++)
				if (b[l + p] == '\n' || b[l + p] == '\r') {
					linelen = l++;
					if (l + p < b.length && b[l + p] == '\n')
						l++;
					break;
				}
			String line = new String(b, p, linelen);
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String type = st.nextToken();
				int tint = type.charAt(0);
				try {
					if (tint == 'o') {
						Scope sc = new Scope(this);
						sc.position = scopeCount;
						sc.undump(st);
						scopes[scopeCount++] = sc;
						break;
					}
					if (tint == 'h') {
						readHint(st);
						break;
					}
					if (tint == '$') {
						readOptions(st);
						break;
					}
					if (tint == '%' || tint == '?' || tint == 'B') {
						// ignore afilter-specific stuff
						break;
					}
					if (tint >= '0' && tint <= '9')
						tint = new Integer(type).intValue();
					int x1 = new Integer(st.nextToken()).intValue();
					int y1 = new Integer(st.nextToken()).intValue();
					int x2 = new Integer(st.nextToken()).intValue();
					int y2 = new Integer(st.nextToken()).intValue();
					int f = new Integer(st.nextToken()).intValue();
					CircuitElm ce = null;
					Class<?> cls = dumpTypes[tint];
					if (cls == null) {
						System.out.println("unrecognized dump type: " + type);
						break;
					}
					// find element class
					Class<?> carr[] = new Class[6];
					// carr[0] = getClass();
					carr[0] = carr[1] = carr[2] = carr[3] = carr[4] = int.class;
					carr[5] = StringTokenizer.class;
					Constructor<?> cstr = null;
					cstr = cls.getConstructor(carr);

					// invoke constructor with starting coordinates
					Object oarr[] = new Object[6];
					// oarr[0] = this;
					oarr[0] = new Integer(x1);
					oarr[1] = new Integer(y1);
					oarr[2] = new Integer(x2);
					oarr[3] = new Integer(y2);
					oarr[4] = new Integer(f);
					oarr[5] = st;
					ce = (CircuitElm) cstr.newInstance(oarr);
					ce.setPoints();
					elmList.addElement(ce);
				} catch (java.lang.reflect.InvocationTargetException ee) {
					ee.getTargetException().printStackTrace();
					break;
				} catch (Exception ee) {
					ee.printStackTrace();
					break;
				}
				break;
			}
			p += l;

		}
		enableItems();
		if (!retain)
			handleResize(); // for scopes
		needAnalyze();
	}

	void readHint(StringTokenizer st) {
		hintType = new Integer(st.nextToken()).intValue();
		hintItem1 = new Integer(st.nextToken()).intValue();
		hintItem2 = new Integer(st.nextToken()).intValue();
	}

	void readOptions(StringTokenizer st) {
		int flags = new Integer(st.nextToken()).intValue();
		dotsCheckItem.setState((flags & 1) != 0);
		smallGridCheckItem.setState((flags & 2) != 0);
		voltsCheckItem.setState((flags & 4) == 0);
		powerCheckItem.setState((flags & 8) == 8);
		showValuesCheckItem.setState((flags & 16) == 0);
		timeStep = new Double(st.nextToken()).doubleValue();
		double sp = new Double(st.nextToken()).doubleValue();
		int sp2 = (int) (Math.log(10 * sp) * 24 + 61.5);
		// int sp2 = (int) (Math.log(sp)*24+1.5);
		speedBar.setValue(sp2);
		currentBar.setValue(new Integer(st.nextToken()).intValue());
		CircuitElm.voltageRange = new Double(st.nextToken()).doubleValue();
		try {
			powerBar.setValue(new Integer(st.nextToken()).intValue());
		} catch (Exception e) {
		}
		setGrid();
	}

	int snapGrid(int x) {
		return (x + gridRound) & gridMask;
	}

	boolean doSwitch(int x, int y) {
		if (mouseElm == null || !(mouseElm instanceof SwitchElm))
			return false;
		SwitchElm se = (SwitchElm) mouseElm;
		se.toggle();
		if (se.momentary)
			heldSwitchElm = se;
		needAnalyze();
		return true;
	}

	int locateElm(CircuitElm elm) {
		int i;
		for (i = 0; i != elmList.size(); i++)
			if (elm == elmList.elementAt(i))
				return i;
		return -1;
	}

	public void mouseDragged(MouseEvent e) {
		// ignore right mouse button with no modifiers (needed on PC)
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			int ex = e.getModifiersEx();
			if ((ex & (MouseEvent.META_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK
					| MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK)) == 0)
				return;
		}
		if (!circuitArea.contains(e.getX(), e.getY()))
			return;
		if (dragElm != null)
			dragElm.drag(e.getX(), e.getY());
		boolean success = true;
		switch (tempMouseMode) {
		case MODE_DRAG_ALL:
			dragAll(snapGrid(e.getX()), snapGrid(e.getY()));
			break;
		case MODE_DRAG_ROW:
			dragRow(snapGrid(e.getX()), snapGrid(e.getY()));
			break;
		case MODE_DRAG_COLUMN:
			dragColumn(snapGrid(e.getX()), snapGrid(e.getY()));
			break;
		case MODE_DRAG_POST:
			if (mouseElm != null)
				dragPost(snapGrid(e.getX()), snapGrid(e.getY()));
			break;
		case MODE_SELECT:
			if (mouseElm == null)
				selectArea(e.getX(), e.getY());
			else {
				tempMouseMode = MODE_DRAG_SELECTED;
				success = dragSelected(e.getX(), e.getY());
			}
			break;
		case MODE_DRAG_SELECTED:
			success = dragSelected(e.getX(), e.getY());
			break;
		}
		dragging = true;
		if (success) {
			if (tempMouseMode == MODE_DRAG_SELECTED
					&& mouseElm instanceof GraphicElm) {
				dragX = e.getX();
				dragY = e.getY();
			} else {
				dragX = snapGrid(e.getX());
				dragY = snapGrid(e.getY());
			}
		}
		cv.repaint(pause);
	}

	void dragAll(int x, int y) {
		int dx = x - dragX;
		int dy = y - dragY;
		if (dx == 0 && dy == 0)
			return;
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.move(dx, dy);
		}
		removeZeroLengthElements();
	}

	void dragRow(int x, int y) {
		int dy = y - dragY;
		if (dy == 0)
			return;
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce.y == dragY)
				ce.movePoint(0, 0, dy);
			if (ce.y2 == dragY)
				ce.movePoint(1, 0, dy);
		}
		removeZeroLengthElements();
	}

	void dragColumn(int x, int y) {
		int dx = x - dragX;
		if (dx == 0)
			return;
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce.x == dragX)
				ce.movePoint(0, dx, 0);
			if (ce.x2 == dragX)
				ce.movePoint(1, dx, 0);
		}
		removeZeroLengthElements();
	}

	boolean dragSelected(int x, int y) {
		boolean me = false;
		if (mouseElm != null && !mouseElm.isSelected())
			mouseElm.setSelected(me = true);

		// snap grid, unless we're only dragging text elements
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce.isSelected() && !(ce instanceof GraphicElm))
				break;
		}
		if (i != elmList.size()) {
			x = snapGrid(x);
			y = snapGrid(y);
		}

		int dx = x - dragX;
		int dy = y - dragY;
		if (dx == 0 && dy == 0) {
			// don't leave mouseElm selected if we selected it above
			if (me)
				mouseElm.setSelected(false);
			return false;
		}
		boolean allowed = true;

		// check if moves are allowed
		for (i = 0; allowed && i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce.isSelected() && !ce.allowMove(dx, dy))
				allowed = false;
		}

		if (allowed) {
			for (i = 0; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				if (ce.isSelected())
					ce.move(dx, dy);
			}
			needAnalyze();
		}

		// don't leave mouseElm selected if we selected it above
		if (me)
			mouseElm.setSelected(false);

		return allowed;
	}

	void dragPost(int x, int y) {
		if (draggingPost == -1) {
			draggingPost = (distanceSq(mouseElm.x, mouseElm.y, x, y) > distanceSq(
					mouseElm.x2, mouseElm.y2, x, y)) ? 1 : 0;
		}
		int dx = x - dragX;
		int dy = y - dragY;
		if (dx == 0 && dy == 0)
			return;
		mouseElm.movePoint(draggingPost, dx, dy);
		needAnalyze();
	}

	void selectArea(int x, int y) {
		int x1 = min(x, initDragX);
		int x2 = max(x, initDragX);
		int y1 = min(y, initDragY);
		int y2 = max(y, initDragY);
		selectedArea = new Rectangle(x1, y1, x2 - x1, y2 - y1);
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.selectRect(selectedArea);
		}
	}

	void setSelectedElm(CircuitElm cs) {
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.setSelected(ce == cs);
		}
		mouseElm = cs;
	}

	void removeZeroLengthElements() {
		int i;
		// boolean changed = false;
		for (i = elmList.size() - 1; i >= 0; i--) {
			CircuitElm ce = getElm(i);
			if (ce.x == ce.x2 && ce.y == ce.y2) {
				elmList.removeElementAt(i);
				ce.delete();
				// changed = true;
			}
		}
		needAnalyze();
	}

	public void mouseMoved(MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
			return;
		int x = e.getX();
		int y = e.getY();
		dragX = snapGrid(x);
		dragY = snapGrid(y);
		draggingPost = -1;
		int i;
		CircuitElm origMouse = mouseElm;
		mouseElm = null;
		mousePost = -1;
		plotXElm = plotYElm = null;
		int bestDist = 100000;
		int bestArea = 100000;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			if (ce.boundingBox.contains(x, y)) {
				int j;
				int area = ce.boundingBox.width * ce.boundingBox.height;
				int jn = ce.getPostCount();
				if (jn > 2)
					jn = 2;
				for (j = 0; j != jn; j++) {
					Point pt = ce.getPost(j);
					int dist = distanceSq(x, y, pt.x, pt.y);

					// if multiple elements have overlapping bounding boxes,
					// we prefer selecting elements that have posts close
					// to the mouse pointer and that have a small bounding
					// box area.
					if (dist <= bestDist && area <= bestArea) {
						bestDist = dist;
						bestArea = area;
						mouseElm = ce;
					}
				}
				if (ce.getPostCount() == 0)
					mouseElm = ce;
			}
		}
		scopeSelected = -1;
		if (mouseElm == null) {
			for (i = 0; i != scopeCount; i++) {
				Scope s = scopes[i];
				if (s.rect.contains(x, y)) {
					s.select();
					scopeSelected = i;
				}
			}
			// the mouse pointer was not in any of the bounding boxes, but we
			// might still be close to a post
			for (i = 0; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				int j;
				int jn = ce.getPostCount();
				for (j = 0; j != jn; j++) {
					Point pt = ce.getPost(j);
					// int dist = distanceSq(x, y, pt.x, pt.y); #unused
					if (distanceSq(pt.x, pt.y, x, y) < 26) {
						mouseElm = ce;
						mousePost = j;
						break;
					}
				}
			}
		} else {
			mousePost = -1;
			// look for post close to the mouse pointer
			for (i = 0; i != mouseElm.getPostCount(); i++) {
				Point pt = mouseElm.getPost(i);
				if (distanceSq(pt.x, pt.y, x, y) < 26)
					mousePost = i;
			}
		}
		if (mouseElm != origMouse)
			cv.repaint();
	}

	int distanceSq(int x1, int y1, int x2, int y2) {
		x2 -= x1;
		y2 -= y1;
		return x2 * x2 + y2 * y2;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && !didSwitch)
			doEditMenu(e);
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
			if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED)
				clearSelection();
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		scopeSelected = -1;
		mouseElm = plotXElm = plotYElm = null;
		cv.repaint();
	}

	public void mousePressed(MouseEvent e) {
		didSwitch = false;

		System.out.println(e.getModifiers());
		int ex = e.getModifiersEx();
		if ((ex & (MouseEvent.META_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK)) == 0
				&& e.isPopupTrigger()) {
			doPopupMenu(e);
			return;
		}
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
			// left mouse
			tempMouseMode = mouseMode;
			if ((ex & MouseEvent.ALT_DOWN_MASK) != 0
					&& (ex & MouseEvent.META_DOWN_MASK) != 0)
				tempMouseMode = MODE_DRAG_COLUMN;
			else if ((ex & MouseEvent.ALT_DOWN_MASK) != 0
					&& (ex & MouseEvent.SHIFT_DOWN_MASK) != 0)
				tempMouseMode = MODE_DRAG_ROW;
			else if ((ex & MouseEvent.SHIFT_DOWN_MASK) != 0)
				tempMouseMode = MODE_SELECT;
			else if ((ex & MouseEvent.ALT_DOWN_MASK) != 0)
				tempMouseMode = MODE_DRAG_ALL;
			else if ((ex & (MouseEvent.CTRL_DOWN_MASK | MouseEvent.META_DOWN_MASK)) != 0)
				tempMouseMode = MODE_DRAG_POST;
		} else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			// right mouse
			if ((ex & MouseEvent.SHIFT_DOWN_MASK) != 0)
				tempMouseMode = MODE_DRAG_ROW;
			else if ((ex & (MouseEvent.CTRL_DOWN_MASK | MouseEvent.META_DOWN_MASK)) != 0)
				tempMouseMode = MODE_DRAG_COLUMN;
			else
				return;
		}

		if (tempMouseMode != MODE_SELECT && tempMouseMode != MODE_DRAG_SELECTED)
			clearSelection();
		if (mouseMode == MODE_SELECT && doSwitch(e.getX(), e.getY())) {
			didSwitch = true;
			return;
		}

		pushUndo();
		initDragX = e.getX();
		initDragY = e.getY();
		dragging = true;
		if (tempMouseMode != MODE_ADD_ELM || addingClass == null)
			return;

		int x0 = snapGrid(e.getX());
		int y0 = snapGrid(e.getY());
		if (!circuitArea.contains(x0, y0))
			return;

		dragElm = constructElement(addingClass, x0, y0);
	}

	CircuitElm constructElement(Class<?> c, int x0, int y0) {
		// find element class
		Class<?> carr[] = new Class[2];
		// carr[0] = getClass();
		carr[0] = carr[1] = int.class;
		Constructor<?> cstr = null;
		try {
			cstr = c.getConstructor(carr);
		} catch (NoSuchMethodException ee) {
			System.out.println("caught NoSuchMethodException " + c);
			return null;
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}

		// invoke constructor with starting coordinates
		Object oarr[] = new Object[2];
		oarr[0] = new Integer(x0);
		oarr[1] = new Integer(y0);
		try {
			return (CircuitElm) cstr.newInstance(oarr);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return null;
	}

	// hausen: add doEditMenu
	void doEditMenu(MouseEvent e) {
		if (mouseElm != null)
			doEdit(mouseElm);
	}

	void doPopupMenu(MouseEvent e) {
		menuElm = mouseElm;
		menuScope = -1;
		if (scopeSelected != -1) {
			PopupMenu m = scopes[scopeSelected].getMenu();
			menuScope = scopeSelected;
			if (m != null)
				m.show(e.getComponent(), e.getX(), e.getY());
		} else if (mouseElm != null) {
			elmEditMenuItem.setEnabled(mouseElm.getEditInfo(0) != null);
			elmScopeMenuItem.setEnabled(mouseElm.canViewInScope());
			elmMenu.show(e.getComponent(), e.getX(), e.getY());
		} else {
			doMainMenuChecks(mainMenu);
			mainMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	void doMainMenuChecks(Menu m) {
		int i;
		if (m == optionsMenu)
			return;
		for (i = 0; i != m.getItemCount(); i++) {
			MenuItem mc = m.getItem(i);
			if (mc instanceof Menu)
				doMainMenuChecks((Menu) mc);
			if (mc instanceof CheckboxMenuItem) {
				CheckboxMenuItem cmi = (CheckboxMenuItem) mc;
				cmi.setState(mouseModeStr.compareTo(cmi.getActionCommand()) == 0);
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		int ex = e.getModifiersEx();
		if ((ex & (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.META_DOWN_MASK)) == 0
				&& e.isPopupTrigger()) {
			doPopupMenu(e);
			return;
		}
		tempMouseMode = mouseMode;
		selectedArea = null;
		dragging = false;
		boolean circuitChanged = false;
		if (heldSwitchElm != null) {
			heldSwitchElm.mouseUp();
			heldSwitchElm = null;
			circuitChanged = true;
		}
		if (dragElm != null) {
			// if the element is zero size then don't create it
			if (dragElm.x == dragElm.x2 && dragElm.y == dragElm.y2)
				dragElm.delete();
			else {
				elmList.addElement(dragElm);
				circuitChanged = true;
			}
			dragElm = null;
		}
		if (circuitChanged)
			needAnalyze();
		if (dragElm != null)
			dragElm.delete();
		dragElm = null;
		cv.repaint();
	}

	void enableItems() {
		if (powerCheckItem.getState()) {
			powerBar.setEnabled(true);
			powerLabel.setEnabled(true);
		} else {
			powerBar.setEnabled(false);
			powerLabel.setEnabled(false);
		}
		enableUndoRedo();
	}

	public void itemStateChanged(ItemEvent e) {
		cv.repaint(pause);
		Object mi = e.getItemSelectable();
		if (mi == stoppedCheck)
			return;
		if (mi == smallGridCheckItem)
			setGrid();
		if (mi == powerCheckItem) {
			if (powerCheckItem.getState())
				voltsCheckItem.setState(false);
			else
				voltsCheckItem.setState(true);
		}
		if (mi == voltsCheckItem && voltsCheckItem.getState())
			powerCheckItem.setState(false);
		enableItems();
		if (menuScope != -1) {
			Scope sc = scopes[menuScope];
			sc.handleMenu(e, mi);
		}
		if (mi instanceof CheckboxMenuItem) {
			MenuItem mmi = (MenuItem) mi;
			int prevMouseMode = mouseMode;
			setMouseMode(MODE_ADD_ELM);
			String s = mmi.getActionCommand();
			if (s.length() > 0)
				mouseModeStr = s;
			if (s.compareTo("DragAll") == 0)
				setMouseMode(MODE_DRAG_ALL);
			else if (s.compareTo("DragRow") == 0)
				setMouseMode(MODE_DRAG_ROW);
			else if (s.compareTo("DragColumn") == 0)
				setMouseMode(MODE_DRAG_COLUMN);
			else if (s.compareTo("DragSelected") == 0)
				setMouseMode(MODE_DRAG_SELECTED);
			else if (s.compareTo("DragPost") == 0)
				setMouseMode(MODE_DRAG_POST);
			else if (s.compareTo("Select") == 0)
				setMouseMode(MODE_SELECT);
			else if (s.length() > 0) {
				try {
					addingClass = Class.forName(s);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			} else
				setMouseMode(prevMouseMode);
			tempMouseMode = mouseMode;
		}
	}

	void setGrid() {
		gridSize = (smallGridCheckItem.getState()) ? 8 : 16;
		gridMask = ~(gridSize - 1);
		gridRound = gridSize / 2 - 1;
	}

	void pushUndo() {
		redoStack.removeAllElements();
		String s = dumpCircuit();
		if (undoStack.size() > 0 && s.compareTo(undoStack.lastElement()) == 0)
			return;
		undoStack.add(s);
		enableUndoRedo();
	}

	void doUndo() {
		if (undoStack.size() == 0)
			return;
		redoStack.add(dumpCircuit());
		String s = undoStack.remove(undoStack.size() - 1);
		readSetup(s);
		enableUndoRedo();
	}

	void doRedo() {
		if (redoStack.size() == 0)
			return;
		undoStack.add(dumpCircuit());
		String s = redoStack.remove(redoStack.size() - 1);
		readSetup(s);
		enableUndoRedo();
	}

	void enableUndoRedo() {
		redoItem.setEnabled(redoStack.size() > 0);
		undoItem.setEnabled(undoStack.size() > 0);
	}

	void setMouseMode(int mode) {
		mouseMode = mode;
		if (mode == MODE_ADD_ELM)
			cv.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		else
			cv.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	void setMenuSelection() {
		if (menuElm != null) {
			if (menuElm.selected)
				return;
			clearSelection();
			menuElm.setSelected(true);
		}
	}

	void doCut() {
		int i;
		pushUndo();
		setMenuSelection();
		clipboard = "";
		for (i = elmList.size() - 1; i >= 0; i--) {
			CircuitElm ce = getElm(i);
			if (ce.isSelected()) {
				clipboard += ce.dump() + "\n";
				ce.delete();
				elmList.removeElementAt(i);
			}
		}
		enablePaste();
		needAnalyze();
	}

	void doDelete() {
		int i;
		pushUndo();
		setMenuSelection();
		boolean hasDeleted = false;

		for (i = elmList.size() - 1; i >= 0; i--) {
			CircuitElm ce = getElm(i);
			if (ce.isSelected()) {
				ce.delete();
				elmList.removeElementAt(i);
				hasDeleted = true;
			}
		}

		if (!hasDeleted) {
			for (i = elmList.size() - 1; i >= 0; i--) {
				CircuitElm ce = getElm(i);
				if (ce == mouseElm) {
					ce.delete();
					elmList.removeElementAt(i);
					hasDeleted = true;
					mouseElm = null;
					break;
				}
			}
		}

		if (hasDeleted)
			needAnalyze();
	}

	void doCopy() {
		int i;
		clipboard = "";
		setMenuSelection();
		for (i = elmList.size() - 1; i >= 0; i--) {
			CircuitElm ce = getElm(i);
			if (ce.isSelected())
				clipboard += ce.dump() + "\n";
		}
		enablePaste();
	}

	void enablePaste() {
		pasteItem.setEnabled(clipboard.length() > 0);
	}

	void doPaste() {
		pushUndo();
		clearSelection();
		int i;
		Rectangle oldbb = null;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			Rectangle bb = ce.getBoundingBox();
			if (oldbb != null)
				oldbb = oldbb.union(bb);
			else
				oldbb = bb;
		}
		int oldsz = elmList.size();
		readSetup(clipboard, true);

		// select new items
		Rectangle newbb = null;
		for (i = oldsz; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.setSelected(true);
			Rectangle bb = ce.getBoundingBox();
			if (newbb != null)
				newbb = newbb.union(bb);
			else
				newbb = bb;
		}
		if (oldbb != null && newbb != null && oldbb.intersects(newbb)) {
			// find a place for new items
			int dx = 0, dy = 0;
			int spacew = circuitArea.width - oldbb.width - newbb.width;
			int spaceh = circuitArea.height - oldbb.height - newbb.height;
			if (spacew > spaceh)
				dx = snapGrid(oldbb.x + oldbb.width - newbb.x + gridSize);
			else
				dy = snapGrid(oldbb.y + oldbb.height - newbb.y + gridSize);
			for (i = oldsz; i != elmList.size(); i++) {
				CircuitElm ce = getElm(i);
				ce.move(dx, dy);
			}
			// center circuit
			handleResize();
		}
		needAnalyze();
	}

	void clearSelection() {
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.setSelected(false);
		}
	}

	void doSelectAll() {
		int i;
		for (i = 0; i != elmList.size(); i++) {
			CircuitElm ce = getElm(i);
			ce.setSelected(true);
		}
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == 127) {
			doDelete();
			return;
		}
		if (e.getKeyChar() > ' ' && e.getKeyChar() < 127) {
			Class<?> c = shortcuts[e.getKeyChar()];
			if (c == null)
				return;
			CircuitElm elm = null;
			elm = constructElement(c, 0, 0);
			if (elm == null)
				return;
			setMouseMode(MODE_ADD_ELM);
			mouseModeStr = c.getName();
			addingClass = c;
		}
		if (e.getKeyChar() == ' ' || e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			setMouseMode(MODE_SELECT);
			mouseModeStr = "Select";
		}
		tempMouseMode = mouseMode;
	}

	// factors a matrix into upper and lower triangular matrices by
	// gaussian elimination. On entry, a[0..n-1][0..n-1] is the
	// matrix to be factored. ipvt[] returns an integer vector of pivot
	// indices, used in the lu_solve() routine.
	boolean lu_factor(double a[][], int n, int ipvt[]) {
		double scaleFactors[];
		int i, j, k;

		scaleFactors = new double[n];

		// divide each row by its largest element, keeping track of the
		// scaling factors
		for (i = 0; i != n; i++) {
			double largest = 0;
			for (j = 0; j != n; j++) {
				double x = Math.abs(a[i][j]);
				if (x > largest)
					largest = x;
			}
			// if all zeros, it's a singular matrix
			if (largest == 0)
				return false;
			scaleFactors[i] = 1.0 / largest;
		}

		// use Crout's method; loop through the columns
		for (j = 0; j != n; j++) {

			// calculate upper triangular elements for this column
			for (i = 0; i != j; i++) {
				double q = a[i][j];
				for (k = 0; k != i; k++)
					q -= a[i][k] * a[k][j];
				a[i][j] = q;
			}

			// calculate lower triangular elements for this column
			double largest = 0;
			int largestRow = -1;
			for (i = j; i != n; i++) {
				double q = a[i][j];
				for (k = 0; k != j; k++)
					q -= a[i][k] * a[k][j];
				a[i][j] = q;
				double x = Math.abs(q);
				if (x >= largest) {
					largest = x;
					largestRow = i;
				}
			}

			// pivoting
			if (j != largestRow) {
				double x;
				for (k = 0; k != n; k++) {
					x = a[largestRow][k];
					a[largestRow][k] = a[j][k];
					a[j][k] = x;
				}
				scaleFactors[largestRow] = scaleFactors[j];
			}

			// keep track of row interchanges
			ipvt[j] = largestRow;

			// avoid zeros
			if (a[j][j] == 0.0) {
				System.out.println("avoided zero");
				a[j][j] = 1e-18;
			}

			if (j != n - 1) {
				double mult = 1.0 / a[j][j];
				for (i = j + 1; i != n; i++)
					a[i][j] *= mult;
			}
		}
		return true;
	}

	// Solves the set of n linear equations using a LU factorization
	// previously performed by lu_factor. On input, b[0..n-1] is the right
	// hand side of the equations, and on output, contains the solution.
	void lu_solve(double a[][], int n, int ipvt[], double b[]) {
		int i;

		// find first nonzero b element
		for (i = 0; i != n; i++) {
			int row = ipvt[i];

			double swap = b[row];
			b[row] = b[i];
			b[i] = swap;
			if (swap != 0)
				break;
		}

		int bi = i++;
		for (; i < n; i++) {
			int row = ipvt[i];
			int j;
			double tot = b[row];

			b[row] = b[i];
			// forward substitution using the lower triangular matrix
			for (j = bi; j < i; j++)
				tot -= a[i][j] * b[j];
			b[i] = tot;
		}
		for (i = n - 1; i >= 0; i--) {
			double tot = b[i];

			// back-substitution using the upper triangular matrix
			int j;
			for (j = i + 1; j != n; j++)
				tot -= a[i][j] * b[j];
			b[i] = tot / a[i][i];
		}
	}

}
