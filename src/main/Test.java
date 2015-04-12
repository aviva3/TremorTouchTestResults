package main;
import java.util.ArrayList;

public class Test {
	private Circle circle;
	private ArrayList<Touch> touches;
	private String type;
	public Test(Circle circle, ArrayList<Touch> touches,String type) {
		this.circle = circle;
		this.touches = touches;
		this.type = type;
	}

	public Circle getCircle() {
		return circle;
	}
	public void setCircle(Circle circle) {
		this.circle = circle;
	}
	public ArrayList<Touch> getTouches() {
		return touches;
	}
	public void setTouches(ArrayList<Touch> touches) {
		this.touches = touches;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}	
	
	
	
	
}
