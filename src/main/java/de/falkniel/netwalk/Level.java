package de.falkniel.netwalk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import de.falkniel.netwalk.Field.Status;


public class Level {
	private Field[][] level;//TODO
	private Field server;
	
	private Collection<Field> sceduledOpenFields;
	 
	public Level(int height, int width){
		
				
		level = new Field[height][width];
		
		for (int h = 0; h < level.length; h++) {
	        for (int w = 0; w < level[h].length; w++) {
	        	level[h][w] = new Field(h,w);
	        }
		}
		
		
		//connect fields
		int lasth=height-1;
		int lastw=width-1;
		for (int h = 0; h < level.length; h++) {
	        for (int w = 0; w < level[h].length; w++) {
	        	level[h][w].putNeighbor(Field.Direction.LEFT, level[h][lastw]);
	            level[h][w].putNeighbor(Field.Direction.TOP,level[lasth][w]);
	        	lastw=w;
            }
	        lasth=h;
        }				
	}
	
	public void glow(){
		for (int h = 0; h < level.length; h++) {
	        for (int w = 0; w < level[h].length; w++) {
	        	level[h][w].setOnOff(false);
	        }
		}
		server.glow(true);
	}
	
	public int spin(){
		int retVal = 0;
		for (int h = 0; h < level.length; h++) 
	        for (int w = 0; w < level[h].length; w++) 
	        	retVal +=level[h][w].randomSpin();
		return retVal;
	}
	
	private void addSceduledOpenFields(Field openField, Field parent){
		openField.setParent(parent);
		//if (!openField.isStatusInit()){
			openField.setStatus(Status.SCEDULED);
			sceduledOpenFields.add(openField);
		//}			
	}
	
	private void generateOpenFields(){
		Random random = new Random();
		while (sceduledOpenFields.size()>0){
			int nextInt = random.nextInt(sceduledOpenFields.size());
			Field next = (Field)sceduledOpenFields.toArray()[nextInt];
			generate(next);
			sceduledOpenFields.remove(next);
		}
	}
	
	public void generate(){
		sceduledOpenFields = new ArrayList<Field>();
		Random random = new Random();
		server = level[random.nextInt(level.length)][random.nextInt(level[1].length)];
		server.setServer(true);
		server.setParent(null);
		generate(server);
		
		generateOpenFields();
		//close rest
		
	}
	
	private void generate(Field field){
		//do random
		Random random = new Random();
		
		//System.out.println("gen "+field.getH()+":"+field.getW()+" "+field.getParent().getH()+":"+field.getParent().getW());
		field.setStatus(Field.Status.GENERATING);
		List<Field> openSurroundingFields = field.getOpenSurroundingFiels();
		
		
		
		
		if (openSurroundingFields.size() == 0){
			//field.setTerminator(true);
			field.setStatus(Field.Status.GENERATED);
		}else if (openSurroundingFields.size() == 1){
			Field next = openSurroundingFields.iterator().next();
			addSceduledOpenFields(next, field);
		}else if (openSurroundingFields.size() == 2){
			if (random.nextBoolean()){
				// ein ausgang
				Iterator<Field> iterator = openSurroundingFields.iterator();
				if (random.nextBoolean()){
					//ersten überspringen
					iterator.next();
				}
				Field next = iterator.next();
				addSceduledOpenFields(next, field);		
			}else{
				// zwei ausgänge
				for (Field openField : openSurroundingFields) {
					addSceduledOpenFields(openField, field);	
                }
			}
		}else if (openSurroundingFields.size() == 3){
			if (random.nextInt(100)==1){
				//3 ausgänge (alle)
				for (Field openField : openSurroundingFields) {
					addSceduledOpenFields(openField, field);
				}
			}else{
				//2 1
				Collections.shuffle(openSurroundingFields);
				Iterator<Field> iterator = openSurroundingFields.iterator();
				
				//erstes immer				
				addSceduledOpenFields(iterator.next(), field);
				
				if (random.nextBoolean()){
					// 2
					addSceduledOpenFields(iterator.next(), field);
				}				
			}
		}else if (openSurroundingFields.size() == 4){
			//server
			if (random.nextInt(100)==1){
				//4 ausgänge (alle)
				for (Field openField : openSurroundingFields) {
					addSceduledOpenFields(openField, field);
                }
			}else{
				//server 3 2 1
				Collections.shuffle(openSurroundingFields);
				Iterator<Field> iterator = openSurroundingFields.iterator();
				
				
				int nextInt = random.nextInt(3);
				for (int i = 0; i <= nextInt; i++) {
					addSceduledOpenFields(iterator.next(), field);
				}				
			}
		}		
	}
	
	public String toHtml(){
		String retVal="<html><table border=1 cellpadding=0 cellspacing=0>";
		for (int h = 0; h < level.length; h++) {
			retVal+="<tr>";
	        for (int w = 0; w < level[h].length; w++) {
	            retVal+="<td><img alt=\""+level[h][w].getInfoString()+"\" src=\"img/"+level[h][w].getImg()+".png\"></td>";
            }
	        retVal+="</tr>";
        }	
		
		retVal+="</table></html>";
		return retVal;
	}
	
	public int getTargetClickCount(){
		return server.getTargetClickCount();
	}
	
	public Field[][] getFields(){
		return level;
	}
	
	public boolean isSolved(){
		for (int h = 0; h < level.length; h++) 
	        for (int w = 0; w < level[h].length; w++) 
	        	if (!level[h][w].isOnOff())
	        		return false;	        	
		return true;
	}
	
	public void unLockAll(){
		for (int h = 0; h < level.length; h++) 
	        for (int w = 0; w < level[h].length; w++) 
	        	level[h][w].setOnOff(false);
	}
}