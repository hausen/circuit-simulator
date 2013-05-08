import java.awt.*;
import java.awt.event.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

class ImportExportClipboardDialog
extends Dialog
implements ImportExportDialog,ActionListener
{
    CirSim cframe;
    Button importButton, closeButton;
    TextArea text;
    Action type;

    Clipboard clipboard = null;

    ImportExportClipboardDialog(CirSim f, Action type) {
	super(f, (type == Action.EXPORT) ? "Export" : "Import", false);
	cframe = f;
	setLayout(new ImportExportDialogLayout());
	add(text = new TextArea("", 10, 60, TextArea.SCROLLBARS_BOTH));
	if (type == Action.EXPORT)
	    importButton = new Button("Copy to clipboard");
	else
	    importButton = new Button("Import");
	this.type = type;
	add(importButton);
	importButton.addActionListener(this);
	add(closeButton = new Button("Close"));
	closeButton.addActionListener(this);
	Point x = cframe.main.getLocationOnScreen();
	resize(400, 300);
	Dimension d = getSize();
	setLocation(x.x + (cframe.winSize.width-d.width)/2,
		    x.y + (cframe.winSize.height-d.height)/2);
    }

    public void setDump(String dump)
    {
	text.setText(dump);
    }

    public void execute()
    {
	if ( type == Action.EXPORT )
	    text.selectAll();
	setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
	int i;
	Object src = e.getSource();
	if (src == importButton) {
	    if (clipboard == null)
		clipboard = getToolkit().getSystemClipboard();
	    if ( type == Action.EXPORT )
	    {
		StringSelection data = new StringSelection(text.getText());
		clipboard.setContents(data, data);
	    }
	    else
	    {
	        cframe.readSetup(text.getText());
	    }
	}
	if (src == closeButton)
	    setVisible(false);
    }

    public boolean handleEvent(Event ev) {
	if (ev.id == Event.WINDOW_DESTROY) {
	    CirSim.main.requestFocus();
	    setVisible(false);
	    cframe.impDialog = null;
	    return true;
	}
	return super.handleEvent(ev);
    }

}
