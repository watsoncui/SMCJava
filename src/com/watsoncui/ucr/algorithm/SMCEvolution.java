package com.watsoncui.ucr.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SMCEvolution {

	public static void mainTask(String fileName, double alphaParam, int samplesParam, int transOrderParam, boolean useGCOrder) throws Exception {
		//parameters

		System.out.println("alphaParam = " + alphaParam);
		System.out.println("samplesParam = " + samplesParam);
		System.out.println("transOrderParam = " + transOrderParam);
		int readsParam;
		List<String> contentList = new ArrayList<String>();
		
		System.out.println("Reading data file...");
		
		//Read data file
		BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
		String line;
		while (null != (line = br.readLine())) {
			contentList.add(line.toUpperCase());
		}
		br.close();
		
		System.out.println("Read complete");
		
		System.out.println("Initializing...");
		
		readsParam = contentList.size() / 2;
		
		//Initialize important weight matrix W(z)
		List<List<Integer>> zMatrix = new ArrayList<List<Integer>>(readsParam);
		List<Integer> sampleList = new ArrayList<Integer>(samplesParam);
		for (int j = 0; j < samplesParam; j++) {
			sampleList.add(1);
		}
		zMatrix.add(sampleList);
		for (int i = 1; i < readsParam; i++) {
			List<Integer> samplesList = new ArrayList<Integer>(samplesParam);
			for (int j = 0; j < samplesParam; j++) {
				samplesList.add(0);
			}
			zMatrix.add(samplesList);
		}
		
		//Initialize weight list w
		List<Double> weightList = new ArrayList<Double>(samplesParam);
		for (int j = 0; j < samplesParam; j++) {
			weightList.add(1.0/samplesParam);
		}
		
		//Initialize permutation count matrix
		char[] charArray = {'A', 'C', 'G', 'T'};
		List<String> permutationList =  getPermutations(charArray, transOrderParam);
		List<SingleRead> singleReadList = new ArrayList<SingleRead>(readsParam);
		for (int readId = 0; readId < readsParam; readId++) {
			singleReadList.add(new SingleRead(contentList.get(readId), samplesParam, transOrderParam, permutationList));
		}
		
		System.out.println("Complete \n Calculating...");
		
		int maxGroupId = 1;
		
		//Core calculation
		for (int readId = 1; readId < readsParam; readId++) {
			
			System.out.println(readId);
			System.out.println("Posterior Probability");
			
			List<Double> posteriorList = postProbability(singleReadList, zMatrix, weightList, readId, maxGroupId, samplesParam, alphaParam);
			
			for (int i = 0; i < posteriorList.size(); i++) {
				System.out.println(posteriorList.get(i));
			}
			
			System.out.println("Sampling");
			Random generator = new Random();
			List<Integer> zList = samplingFromProbability(posteriorList, generator, samplesParam);
			for (Integer groupId:zList) {
				if (groupId > maxGroupId) {
					maxGroupId = groupId;
					break;
				}
			}
			zMatrix.set(readId, zList);
			
			int count_1 = 0;
			for (int i = 0; i < zList.size(); i++) {
				if(zList.get(i) == 1) {
					count_1++;
				}
			}
			System.out.println(count_1);
			System.out.println("Updating weight");
			
			weightList = updateWeightList(singleReadList, zMatrix, weightList, readId, samplesParam);
			for (int i = 0; i < weightList.size(); i++) {
				System.out.print(weightList.get(i));
				System.out.print('\t');
			}
			System.out.println();
		}
		
		System.out.println("Calculate complete \n Printing...");
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File("output.txt")));
		for (Double weight:weightList) {
			pw.print(weight);
			pw.print('\t');
		}
		pw.println();
		pw.println();
		
		for (int i = 0; i < zMatrix.size(); i++) {
			for (int j = 0; j < zMatrix.get(i).size(); j++) {
				pw.print(zMatrix.get(i).get(j));
				pw.print('\t');
			}
			pw.println();
		}
		
		pw.close();
	}
	
	public static void frontEnd(String[] args) throws Exception {
		int transOrderParam = 1;
		double alphaParam = 0.0000001;
		double thresholdParam = 0.9;
		int samplesParam = 100;
		boolean useGCOrder = false;
		int pos = 0;
		while (pos < args.length - 1) {
			if (args[pos].charAt(0) == '-') {
				switch (args[pos].charAt(1)) {
				case 'a':
				case 'A':
					try {
						alphaParam = Double.parseDouble(args[pos + 1]);
					} catch (NullPointerException npe) {
						System.out.println("Param alpha error! Use default value 0.01");
						alphaParam = 0.01;
					} catch (NumberFormatException nfe) {
						System.out.println("Param alpha parse error! Use default value 0.01");
						alphaParam = 0.01;
					}
					pos += 2;
					break;
				case 'n':
				case 'N':
					try {
						samplesParam = Integer.parseInt(args[pos + 1]);
					} catch (NumberFormatException nfe) {
						System.out.println("Param sample number parse error! Use default value 100");
						samplesParam = 100;
					}
					pos += 2;
					break;
				case 'g':
				case 'G':
					useGCOrder = true;
					pos++;
					break;
				default:
					pos++;
					break;
				}
			} else {
				pos++;	
			}
		}
		mainTask(args[args.length - 1], alphaParam, samplesParam, transOrderParam, useGCOrder);
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		frontEnd(args);
	}

}
