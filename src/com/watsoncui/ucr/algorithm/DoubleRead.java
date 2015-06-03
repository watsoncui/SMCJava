package com.watsoncui.ucr.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DoubleRead {

	private int id;
	private String readA;
	private String readB;
	private int transOrder;
	private List<Integer> countList;
	private double GC;
	
	public DoubleRead(String readA, String readB, int transOrder, List<String> permutationList) {
		this.readA = readA;
		this.readB = readB;
		this.transOrder = transOrder;
		fillCountList(permutationList);
		calculateGC();
	}
	
	public DoubleRead(int id, String readA, String readB, int transOrder, List<String> permutationList) {
		this.id = id;
		this.readA = readA;
		this.readB = readB;
		this.transOrder = transOrder;
		fillCountList(permutationList);
		calculateGC();
	}
	
	private void calculateGC() {
		int gcCount = 0;
		char[] charList = readA.toUpperCase().toCharArray();
		for (char c : charList) {
			if (('C' == c) || ('G' == c)) {
				gcCount++;
			}
		}
		charList = readB.toUpperCase().toCharArray();
		for (char c : charList) {
			if (('C' == c) || ('G' == c)) {
				gcCount++;
			}
		}
		GC = (gcCount + 0.0) / (readA.length() + readB.length());
	}
	
	private String getTransReverse(String origin) {
		char[] charList = origin.toUpperCase().toCharArray();
		for (int i = 0; i < charList.length; i++) {
			switch (charList[i]) {
			case 'A':
				charList[i] = 'T';
				break;
			case 'C':
				charList[i] = 'G';
				break;
			case 'G':
				charList[i] = 'C';
				break;
			case 'T':
				charList[i] = 'A';
				break;
			default:
				break;	
			}
		}
		return (new StringBuilder(String.valueOf(charList))).reverse().toString();
	}

	private void countMerForElement(String data, Map<String, Integer> countMap, List<String> permutationList) {
		for (int i = 0; i < data.length() - transOrder + 1; i++) {
			String key = data.substring(i, i + transOrder);
			countMap.put(key, countMap.get(key) + 1);
		}
	}
	
	private List<Integer> countMerForList(String[] dataList, List<String> permutationList) {
		List<Integer> integerList = new ArrayList<Integer>(permutationList.size());
		Map<String, Integer> countMap = new TreeMap<String, Integer>();
			for (String permutation:permutationList) {
				countMap.put(permutation, 0);
			}
			for (int i = 0; i < dataList.length; i++) {
				countMerForElement(dataList[i], countMap, permutationList);
			}
			for (int i = 0; i < permutationList.size(); i++) {
				integerList.add(countMap.get(permutationList.get(i)));
			}
		return integerList;
	}
	
	private void fillCountList(List<String> permutationList) {
		String[] dataList = {readA, readB, getTransReverse(readA), getTransReverse(readB)};
		countList = countMerForList(dataList, permutationList);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReadA() {
		return readA;
	}

	public void setReadA(String readA) {
		this.readA = readA;
	}

	public String getReadB() {
		return readB;
	}

	public void setReadB(String readB) {
		this.readB = readB;
	}

	public int getTransOrder() {
		return transOrder;
	}

	public void setTransOrder(int transOrder) {
		this.transOrder = transOrder;
	}

	public List<Integer> getCountList() {
		return countList;
	}

	public void setCountList(List<Integer> countList) {
		this.countList = countList;
	}

	public double getGC() {
		return GC;
	}

	public void setGC(double gC) {
		GC = gC;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((readA == null) ? 0 : readA.hashCode());
		result = prime * result + ((readB == null) ? 0 : readB.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DoubleRead other = (DoubleRead) obj;
		if (readA == null) {
			if (other.readA != null)
				return false;
		} else if (!readA.equals(other.readA))
			return false;
		if (readB == null) {
			if (other.readB != null)
				return false;
		} else if (!readB.equals(other.readB))
			return false;
		return true;
	}

	
}
