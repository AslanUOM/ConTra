package com.aslan.contracep.function;

public class VisitedPlaceInfo implements Comparable<VisitedPlaceInfo> {
	
	private String location;
	private String listOfFriends;
	private int countOfFriends;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getListOfFriends() {
		return listOfFriends;
	}
	public void setListOfFriends(String listOfFriends) {
		this.listOfFriends = listOfFriends;
	}
	public int getCountOfFriends() {
		return countOfFriends;
	}
	public void setCountOfFriends(int countOfFriends) {
		this.countOfFriends = countOfFriends;
	}
	@Override
	public int compareTo(VisitedPlaceInfo places) {
		int compareCountOfFriends = places.getCountOfFriends();
		return this.countOfFriends - compareCountOfFriends;
	}
	@Override
	public String toString() {
		
		return location + ":" + listOfFriends + ":" + countOfFriends;
	}
	
	
	
	

}
