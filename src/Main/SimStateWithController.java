///*
// 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
//*/
//
//package Main;
//
//import sim.display.Controller;
//import sim.display.GUIState;
//import sim.display.SimpleController;
//import sim.engine.SimState;
//
//public class SimStateWithController<T extends GUIState> extends GUIState {
//
//	public SimpleController c;
//	private Class<T> clazz;
//
//	public SimStateWithController(Class<T> clazz) throws InstantiationException, IllegalAccessException {
//		super(clazz.newInstance().state);
//	}
//
//	public SimStateWithController(SimState state)
//	{
//		super(state);
//	}
//
//	public Object getSimulationInspectedObject() { return state; }  // non-volatile
//
//	public Controller createController() {
//		c = new SimpleController(this);
//		return c;
//	}
//
//	public void start()
//	{
//		super.start();
//	}
//
//	public void load(SimState state)
//	{
//		super.load(state);
//	}
//
//	public void init(Controller c)
//	{
//		super.init(c);
//	}
//
//	public void quit()
//	{
//		super.quit();
//	}
//
//	public static void main(String[] args) {
//		new SimStateWithController(new SimState(System.currentTimeMillis())).createController();
//	}
//}
