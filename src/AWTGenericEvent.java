import java.awt.*;

public class AWTGenericEvent extends AWTEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -771819092602471815L;

	public AWTGenericEvent() {
		// TODO Auto-generated constructor stub
		super(null);
	}
	
	public AWTGenericEvent(Event ev){
		super(ev);
	}

}
