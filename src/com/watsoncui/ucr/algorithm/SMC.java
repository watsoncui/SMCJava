package com.watsoncui.ucr.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SMC {
	
	public static List<String> getPermutations(char[] charList, int depth) {
		List<String> stringList = new ArrayList<String>();
		if (1 == depth) {
			for (int i = 0; i < charList.length; i++) {
				stringList.add(String.valueOf(charList[i]));
			}
		} else {
			List<String> subStringList = getPermutations(charList, depth - 1);
			for (int i = 0; i < charList.length; i++) {
				for (int j = 0; j < subStringList.size(); j++) {
					stringList.add(String.valueOf(charList[i]) + subStringList.get(j));
				}
			}
		}
		return stringList;
	}
	
	public static List<Double> startProbability(List<SingleRead> singleReadList, List<List<Integer>> importantWeightMatrix, int readId, int sampleId) {
		List<Integer> accuCountList = new ArrayList<Integer>();
		List<Integer> transCountList = singleReadList.get(readId).getTransCountwithReverseList();
		for (int j = 0; j < transCountList.size(); j+=4) {
			Integer sum = transCountList.get(j) + transCountList.get(j + 1) + transCountList.get(j + 2) + transCountList.get(j + 3);
			accuCountList.add(sum);
		}
		for (int i = 0; i < readId; i++) {
			if (importantWeightMatrix.get(i).get(sampleId).equals(1)) {
				transCountList = singleReadList.get(i).getTransCountwithReverseList();		
				for (int j = 0; j < transCountList.size(); j+=4) {
					Integer sum = transCountList.get(j) + transCountList.get(j + 1) + transCountList.get(j + 2) + transCountList.get(j + 3);
					accuCountList.set(j/4, sum + accuCountList.get(j/4));
				}
			}
		}
		
		
		int accuSum = 0;
		for (Integer count:accuCountList) {
			accuSum += count;
		}
		
		List<Double> startProbList = new ArrayList<Double>(accuCountList.size());
		
		for (int i = 0; i < accuCountList.size(); i++) {
			startProbList.add(((double) accuCountList.get(i)) / accuSum);
		}
		
		return startProbList;
		
	}
	
	public static List<Double> transProbability(List<SingleRead> singleReadList, List<List<Integer>> importantWeightMatrix, int readId, int sampleId, int groupId) {
		List<Integer> accuCountList = new ArrayList<Integer>();
		List<Integer> transCountList = singleReadList.get(readId).getTransCountwithReverseList();
		for (int j = 0; j < transCountList.size(); j++) {
			accuCountList.add(transCountList.get(j));
		}
		for (int i = 0; i < readId; i++) {
			if (importantWeightMatrix.get(i).get(sampleId).equals(groupId)) {
				transCountList = singleReadList.get(i).getTransCountwithReverseList();		
				for (int j = 0; j < transCountList.size(); j++) {
					accuCountList.set(j, transCountList.get(j) + accuCountList.get(j));
				}
			}
		}
		int accuSum = 0;
		for (Integer count:accuCountList) {
			accuSum += count;
		}
		List<Double> transProbList = new ArrayList<Double>(accuCountList.size());
		
		for (int i = 0; i < accuCountList.size(); i++) {
			transProbList.add(((double) accuCountList.get(i)) / accuSum);
		}
		
		return transProbList;
		
	}
	
	
	public static List<Double> postProbability(List<SingleRead> singleReadList, List<List<Integer>> importantWeightMatrix, List<Double> weightList, int readId, int maxGroupId, int samplesParam, double alpha) {
		
		List<Double> postProbList = new ArrayList<Double>(maxGroupId + 1);
		
		//R code startwhere
		int startWhere = 0;
		char startChar = singleReadList.get(readId).getContent().toUpperCase().charAt(0);
		switch (startChar) {
		case 'C':
			startWhere = 1;
			break;
		case 'G':
			startWhere = 2;
			break;
		case 'T':
			startWhere = 3;
			break;
		default:
			startWhere = 0;
			break;
		}
		
		double[][] probMatrix = new double[maxGroupId + 1][samplesParam];
		for (int i = 0; i < maxGroupId + 1; i++) {
			for (int j = 0; j < samplesParam; j++) {
				probMatrix[i][j] = 0.0;
			}
		}
		for (int sampleId = 0; sampleId < samplesParam; sampleId++) {
			double startP = (startProbability(singleReadList, importantWeightMatrix, readId, sampleId)).get(startWhere);
			
			//R code prior
			int maxSampleGroup = 1;
			for(int i = 1; i < readId; i++) {
				if (importantWeightMatrix.get(i).get(sampleId) > maxSampleGroup) {
					maxSampleGroup = importantWeightMatrix.get(i).get(sampleId);
				}
			}
			
			int[] sampleCount = new int[maxSampleGroup];
			for (int i = 0; i < maxSampleGroup; i++) {
				sampleCount[i] = 0;
			}
			for (int read = 0; read < readId; read++) {
				sampleCount[importantWeightMatrix.get(read).get(sampleId) - 1]++;
			}
			
			List<Double> priorProbList = new ArrayList<Double>(maxSampleGroup + 1);
			for (int i = 0; i < maxSampleGroup; i++) {
				priorProbList.add(sampleCount[i]/(alpha + readId));
			}
			priorProbList.add(alpha/(alpha + readId));
			
			for (int groupId = 1; groupId <= maxSampleGroup + 1; groupId++) {
				List<Double> transProbList = transProbability(singleReadList, importantWeightMatrix, readId, sampleId, groupId);
				probMatrix[groupId - 1][sampleId] = weightList.get(sampleId) * priorProbList.get(groupId - 1) * startP;
				for (int k = 0; k < transProbList.size(); k++) {
					probMatrix[groupId - 1][sampleId] *= Math.pow(transProbList.get(k), singleReadList.get(readId).getTransCountList().get(k));
				}
			}
			
		}
		
		double totalSum = 0.0;
		for (int i = 0; i < maxGroupId + 1; i++) {
			double sum = 0.0;
			for (int j = 0; j < samplesParam; j++) {
				sum += probMatrix[i][j];
			}
			totalSum += sum;
			postProbList.add(sum);
		}
		
		for (int i = 0; i < maxGroupId + 1; i++) {
			postProbList.set(i, postProbList.get(i) / totalSum);
		}
		
		return postProbList;
	}
	
	public static List<Integer> samplingFromProbability(List<Double> posteriorList, int samples) {
		List<Double> cdf = new ArrayList<Double>(posteriorList.size() + 1);
		cdf.add(0.0);
		for (int i = 0; i < posteriorList.size(); i++) {
			cdf.add(cdf.get(i) + posteriorList.get(i));
		}
		
		List<Integer> sampleList = new ArrayList<Integer>(samples);
		
		Random generator = new Random();
		for (int i = 0; i < samples; i++) {
			double rand = generator.nextDouble();
			int j = 1;
			while((rand > cdf.get(j)) && (j < cdf.size() - 1)) {
				j++;
			}
			sampleList.add(j);
		}
		
		return sampleList;
	}
	
	public static List<Double> updateWeightList(List<SingleRead> singleReadList, List<List<Integer>> importantWeightMatrix, List<Double> weightList, int readId, int samplesParam) {
		List<Double> newWeightList = new ArrayList<Double>(weightList.size());
		
		//R code startwhere
		int startWhere = 0;
		char startChar = singleReadList.get(readId).getContent().toUpperCase().charAt(0);
		switch (startChar) {
		case 'C':
			startWhere = 1;
			break;
		case 'G':
			startWhere = 2;
			break;
		case 'T':
			startWhere = 3;
			break;
		default:
			startWhere = 0;
			break;
		}
		
		double totalSum = 0.0;
		for (int sampleId = 0; sampleId < samplesParam; sampleId++) {
			
			double startP = (startProbability(singleReadList, importantWeightMatrix, readId, sampleId)).get(startWhere);
			
			int maxSampleGroup = 1;
			for(int i = 1; i < readId; i++) {
				if (importantWeightMatrix.get(i).get(sampleId) > maxSampleGroup) {
					maxSampleGroup = importantWeightMatrix.get(i).get(sampleId);
				}
			}
			List<Double> transProbList = transProbability(singleReadList, importantWeightMatrix, readId, sampleId, importantWeightMatrix.get(readId).get(sampleId));
			double probability = weightList.get(sampleId) * startP;
			for (int k = 0; k < transProbList.size(); k++) {
				probability *= Math.pow(transProbList.get(k), singleReadList.get(readId).getTransCountList().get(k));
			}
			totalSum += probability;
			newWeightList.add(probability);
		}
		for (int i = 0; i < newWeightList.size(); i++) {
			newWeightList.set(i, newWeightList.get(i) / totalSum);
		}
		return newWeightList;
	}
	
	public static void mainTask(String fileName) throws Exception {
		//parameters
		int transOrderParam = 2;
		double alphaParam = 0.01;
		int samplesParam = 100;
		int readsParam;
		List<String> contentList = new ArrayList<String>();
		
		System.out.println("Reading data file...");
		
		//Read the data file
		BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
		String line;
		while (null != (line = br.readLine())) {
			contentList.add(line.toUpperCase());
		}
		br.close();
		
		System.out.println("Read complete");
		
		System.out.println("Initializing...");
		
		readsParam = contentList.size();
		
		
		//Initialize important weight matrix W(z)
		List<List<Integer>> importantWeightMatrix = new ArrayList<List<Integer>>(readsParam);
		List<Integer> sampleList = new ArrayList<Integer>(samplesParam);
		for (int j = 0; j < samplesParam; j++) {
			sampleList.add(1);
		}
		importantWeightMatrix.add(sampleList);
		for (int i = 1; i < readsParam; i++) {
			List<Integer> samplesList = new ArrayList<Integer>(samplesParam);
			for (int j = 0; j < samplesParam; j++) {
				samplesList.add(0);
			}
			importantWeightMatrix.add(samplesList);
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
			
			List<Double> posteriorList = postProbability(singleReadList, importantWeightMatrix, weightList, readId, maxGroupId, samplesParam, alphaParam);
			
			System.out.println("Sampling");
			
			List<Integer> importantWeightList = samplingFromProbability(posteriorList, samplesParam);
			for (Integer groupId:importantWeightList) {
				if (groupId > maxGroupId) {
					maxGroupId = groupId;
					break;
				}
			}
			importantWeightMatrix.set(readId, importantWeightList);
			
			System.out.println("Updating weight");
			
			weightList = updateWeightList(singleReadList, importantWeightMatrix, weightList, readId, samplesParam);
		}
		
		System.out.println("Calculate complete \n Printing...");
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File("output.txt")));
		for (Double weight:weightList) {
			pw.print(weight);
			pw.print('\t');
		}
		pw.println();
		pw.println();
		
		for (int i = 0; i < importantWeightMatrix.size(); i++) {
			for (int j = 0; j < importantWeightMatrix.get(i).size(); j++) {
				pw.print(importantWeightMatrix.get(i).get(j));
				pw.print('\t');
			}
			pw.println();
		}
		
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		mainTask(args[0]);
	}
	
	public static void permutationTest() {
		char[] charArray = {'A', 'C', 'G', 'T'};
		List<String> strList = getPermutations(charArray, 3);
		for (String str:strList) {
			System.out.println(str);
		}
	}

}
