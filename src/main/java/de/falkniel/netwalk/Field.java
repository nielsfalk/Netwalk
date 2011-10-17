package de.falkniel.netwalk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Field {
	private boolean onOff=false;
	private Field parent;
	private Direction parentDirection; 
	private int h;
	private int w;
	private Status status;
	private Map<Direction, Field> neighbors;
	private Map<Direction, Field> childs;
	private Spin spin;
	private boolean server;
	private boolean locked=false;
	private Double imgRotation=null;
	
	public enum Spin{
		NO(0,0), RIGHT(1,1), DOUBLE(2,2), LEFT(3,1);
		private final int value;
		private final int clickCount;
		private static Random random = new Random();
		
        Spin(int value, int clickCount) {
        	this.value = value;
        	this.clickCount = clickCount;
        }
		static Spin getRandom(){
			return Spin.values()[random.nextInt(Spin.values().length)];
		}
		
        public int getValue() {
        	return value;
        }
        public Spin leftSpin(){
        	return getKeyFromVal(value+RIGHT.getValue());
        }
        public Spin rightSpin(){
        	return getKeyFromVal(value+LEFT.getValue());
        }
        private Spin getKeyFromVal(int value){
        	if (value > 3){
        		value = value%4;
        	}
        	switch (value) {
	            case 0:return NO;
	            case 1:return RIGHT;
	            case 2:return DOUBLE;
	            case 3:return LEFT;
	            default:return null;
            }
        }
		
        public int getClickCount() {
        	return clickCount;
        } 
	}
	
	public enum Status {
		INIT,SCEDULED,GENERATING,GENERATED;
	}
	public enum Direction{
		LEFT(0),TOP(1),RIGHT(2),BOTTOM(3);
		
		private final int value;
        Direction(int value) {
        	this.value = value;
        }
        public Direction spin (Spin spin){
        	return getKeyFromVal(value+spin.getValue());
        }
        private Direction getKeyFromVal(int value){
        	while (value > 3){
        		value = value%4;
        	}
        	switch (value) {
	            case 0:return LEFT;
	            case 1:return TOP;
	            case 2:return RIGHT;
	            case 3:return BOTTOM;
	            default:return null;
            }
        }
        public Direction getOpposit() {
        	if (this == LEFT) return RIGHT;
        	if (this == RIGHT) return LEFT;
        	if (this == TOP) return BOTTOM;
        	if (this == BOTTOM) return TOP;
        	return null;
        }		
	}
	
    
    public Field(int h, int w){
    	this.h = h;
    	this.w = w;
    	this.neighbors = new HashMap<Direction, Field>();
    	status=Status.INIT;
    	spin=Spin.NO;
    	server = false;
    }
    
    
    
    public void putNeighbor(Direction direction, Field neighbor){
		putNeighbor(direction, neighbor,true);
	}
	private void putNeighbor(Direction direction, Field neighbor, boolean childs){
		if (childs)
			neighbor.putNeighbor(direction.getOpposit(), this, false);
		
		neighbors.put(direction, neighbor);
	}
	
	
	
	public Field getNeighbor(Direction direction){
		return neighbors.get(direction);
	}
	
	
	public void setParent(Field parent) {
		if (parent == null){
			this.parent=null;
			this.parentDirection = null; 
			return;
		}
		if (this.parent != null){
			System.out.println("setParent("+parent.h+":"+parent.w+") geht nicht weil bereits ein Parent existiert");
			System.out.println(getInfoString());
			return;//TODO throw
		}
		
		this.parent = parent;
		if (this.h == parent.h){
			//L R
			parentDirection = ((parent.w-this.w) == 1 || (parent.w-this.w) < -1)?Direction.RIGHT:Direction.LEFT;
		}else if (this.w == parent.w){
			//T B
			parentDirection = ((parent.h-this.h) == 1 || (parent.h-this.h) < -1)?Direction.BOTTOM:Direction.TOP;	
		}else 
			parentDirection = null;  	
    	
    	if (parent.childs == null)
    		parent.childs = new HashMap<Direction, Field>();
    	parent.childs.put(parentDirection.getOpposit(), this);
    }
	
	public boolean isOpen (Direction direction){
		return isOpen(direction, true);
	}
	public boolean isOpen (Direction direction, boolean doSpin){
		if (doSpin && spin != Spin.NO){
			return isOpen(direction.spin(spin), false);
		}
			
		if (!isServer() && parentDirection == direction)
			return true;
		if (childs == null)
			return false;
		return childs.containsKey(direction);
	}
    
    public Collection<Field> getRandomSurroundingFields(){
    	Collection<Field> surroundingFields = new ArrayList<Field>();
    	while (surroundingFields.size() <4){
    		Field neighbor = nextRandomNeighbor();
    		if (!surroundingFields.contains(neighbor)){
    			surroundingFields.add(neighbor);
    		}
    	}
    	return surroundingFields;
    }
    
    public Field nextRandomNeighbor(){
    	int nextInt = new Random().nextInt(4);
    	switch (nextInt) {
	        case 1:
	        	return getNeighbor(Direction.LEFT);
	        case 2:
	        	return getNeighbor(Direction.TOP);
	        case 3:
	        	return getNeighbor(Direction.RIGHT);
	        default:
	        	return getNeighbor(Direction.BOTTOM);
        }
    	
    }
    
    public Collection<Field> getSurroundingFields(){
    	Collection<Field> surroundingFields = new ArrayList<Field>();
    	surroundingFields.addAll(neighbors.values());
    	return surroundingFields;
    }
    
    public List<Field> getOpenSurroundingFiels(){
    	List<Field> surroundingFields = new ArrayList<Field>();
    	Direction[] directions = Direction.values();
    	for (int i = 0; i < directions.length; i++) {
	        Field neighbor = getNeighbor(directions[i]);
	        if (neighbor.isStatusInit())
	        	surroundingFields.add(neighbor);	        	
        }
    	return surroundingFields; 	
    }     
    
    public String getImg(){
    	String retVal = "";
    	if (isServer())
    		retVal+="Server";
    	
    	if(isOnOff())
    		retVal+="On";
    	else
    		retVal+="Off";
    	
    	if(isOpen(Direction.LEFT))
    		retVal+="Left";
    	
    	if(isOpen(Direction.RIGHT))
    		retVal+="Right";
    	if(isOpen(Direction.BOTTOM))
    		retVal+="Bottom";
    	if(isOpen(Direction.TOP))
    		retVal+="Top";
    	
    	
    	return retVal;
    }
    
    public String getInfoString(){
    	String retVal;
    	if (isServer()){
    		retVal="server";
    	}else if(parent == null || parentDirection == null){
    		retVal = "parent = " + parent +" parentDirection = "+parentDirection;
    	}else{
	    	retVal = "parentDirection = "+parentDirection.name();
	    	retVal += "\nparentPos = "+parent.h+":"+parent.w;
    	}
    	
    	if (childs != null){
	    	retVal += "\nchilds: ";
	    	for (Direction direction : childs.keySet()) {
	    		retVal += direction.name() + " ";
	        }
    	}
    	
    	retVal += "\nopen: ";
    	for (Direction direction : Direction.values()) {
    		if (isOpen(direction)) retVal += direction.name() + " ";
        }
    	
    	retVal += "\nStatus = "+status.name();
    	retVal += "\nSpin = "+spin.name();
    	return retVal;
    	
    }
    
    public void glow(boolean onOff){
    	//TODO mit is Open
    	this.onOff=onOff;
    	for (Direction direction : Direction.values()) {
	        if(isOpen(direction)){
	        	Field neighbor = getNeighbor(direction);
	        	if(neighbor.isOpen(direction.getOpposit()))
	        		if (neighbor.onOff != this.onOff)
	        			neighbor.glow(onOff);
	        }
	        		
        }
    	/*
    	if (childs != null){
	    	for (Field child : childs.values()) {
		        if (child.onOff != this.onOff)
		        	child.glow(this.onOff);
	        }
    	}*/
    }
    
    public Field getParent() {
    	return parent;
    }

	
    
    
    public boolean isStatusInit(){
    	return this.status == Status.INIT;
    }
	
    
    public int getH() {
    	return h;
    }

	
    public void setH(int h) {
    	this.h = h;
    }



	
    public int getW() {
    	return w;
    }



	
    public void setW(int w) {
    	this.w = w;
    }



	public boolean isOnOff() {
		if (isServer())
			return true;
		if (parent == null)
			return true;
    	return onOff;
    }


	
    public void setOnOff(boolean onOff) {
    	this.onOff = onOff;
    }

	
	
	
    public Status getStatus() {
    	return status;
    }

	
    public void setStatus(Status status) {
    	this.status = status;
    }
    
    public int randomSpin(){
    	if (childs == null && parent == null)
    		spin = Spin.NO;
    	if (childs != null && childs.size() == (isServer()?4:3))
    		spin = Spin.NO;
    	spin=Spin.getRandom();
    	if (spin == Spin.DOUBLE && isStraight())
    		spin = Spin.NO;
    	return spin.getClickCount();
    }
    public void leftSpin(){
    	spin=spin.leftSpin();
    	if (imgRotation == null)
    		imgRotation = 90.;
    	else 
    		imgRotation += 90;
    }
    public void rightSpin(){
    	spin=spin.rightSpin();
    	if (imgRotation == null)
    		imgRotation = -90.;
    	else 
    		imgRotation -= 90;
    }
    private int getClickCount(){
    	int retVal = spin.getClickCount();
    	if (childs == null)
    		return retVal;
    	for (Field child : childs.values()) {
    		retVal += child.getClickCount();	        
        }
    	return retVal;
    } 
    public int getTargetClickCount(){
    	if (!this.isServer()){
    		System.out.println("not allowd call getTargetClickCount() on the Server");
    	}
    	return getClickCount();
    }
	
	public boolean isStraight(){
		if (childs != null && childs.size()==1){
			if (isOpen(Direction.RIGHT) && isOpen(Direction.LEFT))
				return true;
			if (isOpen(Direction.TOP) && isOpen(Direction.BOTTOM))
				return true;
			
		}
		return false;
	}


	
    public boolean isServer() {
    	return server;
    }

	
    public void setServer(boolean server) {
    	this.server = server;
    }
    
    public void toggleLock (){
    	locked = !locked;
    }



	
    public boolean isLocked() {
    	return locked;
    }
    
    
    public Double getNextImgRotation(long delta){
    	if (imgRotation==null)
    		return null;
    	double increase = delta * 90. /1000.; 
    	double retVal = imgRotation;
    	if (imgRotation > 0){
    		imgRotation = imgRotation -increase;
    		if (imgRotation < 0)
    			imgRotation = null;
    	}else{
    		imgRotation = imgRotation +increase;
    		if (imgRotation > 0)
    			imgRotation = null;
    	}
    	return retVal;
    }
    
    public void stopRotation(){
    	imgRotation = null;
    }

	
}
