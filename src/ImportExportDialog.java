import java.awt.*;
import java.awt.event.*;

public interface ImportExportDialog {
    public enum Action { IMPORT, EXPORT };

    public void setDump(String dump);

    public void execute();
}

