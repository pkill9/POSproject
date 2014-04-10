package com.google.cloud.backend.qwikserve;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {

	 

	private static final long serialVersionUID = 1L;
	private String name;
	private int numDrinks;
	private ArrayList<String> sides;
	private ArrayList<String> burgers;
	private ArrayList<String> toppings;
	private boolean isCombo;
	// constructor from name on order 
	public Order(String name) {		
		
		this.setName(name);
	}
	
	public Order() {		
		
		this.numDrinks =0;
		this.isCombo = false;
		this.sides = new ArrayList<String>();
		this.burgers = new ArrayList<String>();
		this.toppings = new ArrayList<String>();;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumDrinks() {
		return numDrinks;
	}

	public void addDrink() {
		this.numDrinks++;
	}

	public ArrayList<String> getSides() {
		return this.sides;
	}

	public void addSide(String side) {
		this.sides.add(side);
	}

	public ArrayList<String> getBurgers() {
		return burgers;
	}

	public void addBurger(String type) {
		burgers.add(type);		
	}
	
	public void addTopping(String topping){
		toppings.add(topping);
		
	}
	
	public ArrayList<String> getToppings()
	{
		return this.toppings;
	}

	public boolean isCombo() {
		return isCombo;
	}

	public void setCombo(boolean isCombo) {
		this.isCombo = isCombo;
	}
}// end Order Class
