package com.watsoncui.ucr.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMC {
	
	//Get all possible pair
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
	
	//R code: function star_p
	public static List<List<Double>> startProbability(Map<Integer, List<Integer>> accuCountMap, List<SingleRead> singleReadList, List<List<Integer>> zMatrix, int readId, int sampleId, int maxGroupId) {
		List<List<Double>> resultList = new ArrayList<List<Double>>(maxGroupId);
		for (int groupId = 1; groupId <= maxGroupId + 1; groupId++) {
			List<Integer> accuCountList = new ArrayList<Integer>();
			List<Integer> transCountList = singleReadList.get(readId).getTransCountwithReverseList();
			int step = transCountList.size() / 4;
			for (int j = 0; j < transCountList.size(); j+=step) {
				Integer sum = transCountList.get(j);
				for (int k = j + 1; k < j + step; k++) {
					sum += transCountList.get(k);
				}
				accuCountList.add(sum);
			}
			if (accuCountMap.containsKey(groupId)) {
				transCountList = accuCountMap.get(groupId);
				for (int j = 0; j < transCountList.size(); j+=step) {
					Integer sum = transCountList.get(j);
					for (int k = j + 1; k < j + step; k++) {
						sum += transCountList.get(k);
					}
					accuCountList.set(j/step, sum + accuCountList.get(j/step));
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
			
			resultList.add(startProbList);
		}
		
		return resultList;
	}
	
	//NOUSE
	//NOUSE
	public static List<List<Double>> startProbability(List<SingleRead> singleReadList, List<List<Integer>> zMatrix, int readId, int sampleId, int maxGroupId) {
		List<List<Double>> resultList = new ArrayList<List<Double>>(maxGroupId);
		for (int groupId = 1; groupId <= maxGroupId + 1; groupId++) {
			List<Integer> accuCountList = new ArrayList<Integer>();
			List<Integer> transCountList = singleReadList.get(readId).getTransCountwithReverseList();
			int step = transCountList.size() / 4;
			for (int j = 0; j < transCountList.size(); j+=step) {
				Integer sum = transCountList.get(j);
				for (int k = j + 1; k < j + step; k++) {
					sum += transCountList.get(k);
				}
				accuCountList.add(sum);
			}
			for (int i = 0; i <= readId; i++) {
				if (zMatrix.get(i).get(sampleId).equals(groupId)) {
					transCountList = singleReadList.get(i).getTransCountwithReverseList();		
					for (int j = 0; j < transCountList.size(); j+=step) {
						Integer sum = transCountList.get(j);
						for (int k = j + 1; k < j + step; k++) {
							sum += transCountList.get(k);
						}
						accuCountList.set(j/step, sum + accuCountList.get(j/step));
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
			
			resultList.add(startProbList);
		}
		
		return resultList;
	}
	
	
	//R code: trans_p 
	public static List<Double> transProbability(Map<Integer, List<Integer>> accuCountMap, List<SingleRead> singleReadList, List<List<Integer>> zMatrix, int readId, int sampleId, int groupId) {
		List<Integer> accuCountList = new ArrayList<Integer>();
		List<Integer> transCountList = singleReadList.get(readId).getTransCountwithReverseList();
		for (int j = 0; j < transCountList.size(); j++) {
			accuCountList.add(transCountList.get(j));
		}
		
		if (accuCountMap.containsKey(groupId)) {
			transCountList = accuCountMap.get(groupId);
			for (int j = 0; j < transCountList.size(); j++) {
				accuCountList.set(j, transCountList.get(j) + accuCountList.get(j));
			}
		}
		
		List<Double> transProbList = new ArrayList<Double>(accuCountList.size());
		int step = accuCountList.size() / 4;
		for (int i = 0; i < accuCountList.size(); i+=step) {
			int accuSum = accuCountList.get(i);
			for (int j = i + 1; j < i + step; j++) {
				accuSum += accuCountList.get(j);
			}
			for (int j = i; j < i + step; j++) {
				transProbList.add(((double) accuCountList.get(j)) / accuSum);
			}
		}		
		return transProbList;
	}	
	
	//NOUSE
	public static List<Double> transProbability(List<SingleRead> singleReadList, List<List<Integer>> zMatrix, int readId, int sampleId, int groupId) {
		List<Integer> accuCountList = new ArrayList<Integer>();
		List<Integer> transCountList = singleReadList.get(readId).getTransCountwithReverseList();
		for (int j = 0; j < transCountList.size(); j++) {
			accuCountList.add(transCountList.get(j));
		}
		for (int i = 0; i < readId; i++) {
			if (zMatrix.get(i).get(sampleId).equals(groupId)) {
				transCountList = singleReadList.get(i).getTransCountwithReverseList();		
				for (int j = 0; j < transCountList.size(); j++) {
					accuCountList.set(j, transCountList.get(j) + accuCountList.get(j));
				}
			}
		}
		
		List<Double> transProbList = new ArrayList<Double>(accuCountList.size());
		int step = accuCountList.size() / 4;
		for (int i = 0; i < accuCountList.size(); i+=step) {
			int accuSum = accuCountList.get(i);
			for (int j = i + 1; j < i + step; j++) {
				accuSum += accuCountList.get(j);
			}
			for (int j = i; j < i + step; j++) {
				transProbList.add(((double) accuCountList.get(j)) / accuSum);
			}
		}		
		return transProbList;
	}
	
	//R code: post_p
	public static List<Double> postProbability(List<Map<Integer, List<Integer>>> sampleCountSum, List<SingleRead> singleReadList, List<List<Integer>> zMatrix, List<Double> weightList, int readId, int maxGroupId, int samplesParam, double alpha) {
		
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
		
		//R code function post_p_temp 
		for (int sampleId = 0; sampleId < samplesParam; sampleId++) {
			
			//List<List<Double>> startP = startProbability(singleReadList, zMatrix, readId, sampleId, maxGroupId);
			List<List<Double>> startP = startProbability(sampleCountSum.get(sampleId), singleReadList, zMatrix, readId, sampleId, maxGroupId);
			for (int groupId = 1; groupId <= maxGroupId + 1; groupId++) {
				//List<Double> transProbList = transProbability(singleReadList, zMatrix, readId, sampleId, groupId);
				List<Double> transProbList = transProbability(sampleCountSum.get(sampleId), singleReadList, zMatrix, readId, sampleId, groupId);
				probMatrix[groupId - 1][sampleId] = weightList.get(sampleId) * startP.get(groupId - 1).get(startWhere);
				for (int k = 0; k < transProbList.size(); k++) {
					probMatrix[groupId - 1][sampleId] *= Math.pow(transProbList.get(k), singleReadList.get(readId).getTransCountList().get(k));
				}
			}
			
		}
		
		for (int i = 0; i < maxGroupId + 1; i++) {
			double sum = 0.0;
			for (int j = 0; j < samplesParam; j++) {
				sum += probMatrix[i][j];
			}
			postProbList.add(sum / samplesParam);
		}
		
		//R code function post_p
		for (int sampleId = 0; sampleId < samplesParam; sampleId++) {
			//R code prior
			
			int[] sampleCount = new int[maxGroupId];
			for (int i = 0; i < maxGroupId; i++) {
				sampleCount[i] = 0;
			}
			for (int read = 0; read < readId; read++) {
				sampleCount[zMatrix.get(read).get(sampleId) - 1]++;
			}
			
			List<Double> priorProbList = new ArrayList<Double>(maxGroupId + 1);
			int countZero = 1;
			for (int i = 0; i < maxGroupId; i++) {
				if (sampleCount[i] > 0) {
					priorProbList.add(sampleCount[i]/(alpha + readId));
				} else {
					priorProbList.add(0.0);
					countZero++;
				}
			}
			priorProbList.add(alpha/(alpha + readId)/countZero);
			for (int i = 0; i < maxGroupId; i++) {
				if (sampleCount[i] == 0) {
					priorProbList.set(i, alpha/(alpha + readId)/countZero);
				}
			}
					
			for (int groupId = 1; groupId <= maxGroupId + 1; groupId++) {
				probMatrix[groupId - 1][sampleId] = weightList.get(sampleId) * priorProbList.get(groupId - 1) * postProbList.get(groupId - 1);
			}
			
		}
		
		double totalSum = 0.0;
		for (int i = 0; i < maxGroupId + 1; i++) {
			double sum = 0.0;
			for (int j = 0; j < samplesParam; j++) {
				sum += probMatrix[i][j];
			}
			totalSum += sum;
			postProbList.set(i, sum);
		}
		
		for (int i = 0; i < maxGroupId + 1; i++) {
			postProbList.set(i, postProbList.get(i) / totalSum);
		}		
		return postProbList;
	}
	
	//R code: sample.int
	public static List<Integer> samplingFromProbability(List<Double> posteriorList, Random generator, int samples) {
		List<Double> cdf = new ArrayList<Double>(posteriorList.size() + 1);
		cdf.add(0.0);
		for (int i = 0; i < posteriorList.size(); i++) {
			cdf.add(cdf.get(i) + posteriorList.get(i));
		}
		
		List<Integer> sampleList = new ArrayList<Integer>(samples);
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
	
	//R code: function weight
	public static List<Double> updateWeightList(List<Map<Integer, List<Integer>>> sampleCountSum, List<SingleRead> singleReadList, List<List<Integer>> zMatrix, List<Double> weightList, int readId, int samplesParam, int maxGroupId) {
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
			
			//List<List<Double>> startP = startProbability(singleReadList, zMatrix, readId, sampleId, maxGroupId);
			List<List<Double>> startP = startProbability(sampleCountSum.get(sampleId), singleReadList, zMatrix, readId, sampleId, maxGroupId);

			//List<Double> transProbList = transProbability(singleReadList, zMatrix, readId, sampleId, zMatrix.get(readId).get(sampleId));
			List<Double> transProbList = transProbability(sampleCountSum.get(sampleId), singleReadList, zMatrix, readId, sampleId, zMatrix.get(readId).get(sampleId));
			
			double probability = weightList.get(sampleId) * startP.get(zMatrix.get(readId).get(sampleId) - 1).get(startWhere);
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
	
	//R code: SMC main program
	public static void mainTask(String fileName, double alphaParam, int samplesParam, int transOrderParam) throws Exception {
		//parameters

		PrintWriter pw = new PrintWriter(new FileWriter(new File("output.txt")));
		
		pw.print("Read1:\t");
		pw.print(1);
		pw.print("\t");
		pw.println(samplesParam);
		
		System.out.println("alphaParam = " + alphaParam);
		System.out.println("samplesParam = " + samplesParam);
		System.out.println("transOrderParam = " + transOrderParam);
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
		
		
		//Init the intermediate result of every sample
		List<Map<Integer, List<Integer>>> sampleCountSum = new ArrayList<Map<Integer, List<Integer>>>(samplesParam);
		for (int i = 0; i < samplesParam; i++) {
			Map<Integer, List<Integer>> groupVectorMap = new TreeMap<Integer, List<Integer>>();
			groupVectorMap.put(1, singleReadList.get(0).getTransCountwithReverseList());
			sampleCountSum.add(groupVectorMap);
		}
		
		//Core calculation
		for (int readId = 1; readId < readsParam; readId++) {
			
			System.out.println(readId);
			System.out.println("Posterior Probability");
			
			//List<Double> posteriorList = postProbability(singleReadList, zMatrix, weightList, readId, maxGroupId, samplesParam, alphaParam);
			List<Double> posteriorList = postProbability(sampleCountSum, singleReadList, zMatrix, weightList, readId, maxGroupId, samplesParam, alphaParam);
			for (int i = 0; i < posteriorList.size(); i++) {
				System.out.println(posteriorList.get(i));
			}
			
			System.out.println("Sampling");
			Random generator = new Random();
			List<Integer> zList = samplingFromProbability(posteriorList, generator, samplesParam);
			
			int[] zSum = new int[maxGroupId + 1];
			for (int i = 0; i < maxGroupId + 1; i++) {
				zSum[i] = 0;
			}
			for (Integer z:zList) {
				zSum[z - 1]++;
			}
			int maxZ = 0;
			for (int i = 1; i < maxGroupId + 1; i++) {
				if(zSum[i] > zSum[maxZ]) {
					maxZ = i;
				}
			}
			pw.print("Read");
			pw.print(readId + 1);
			pw.print(":\t");
			pw.print(maxZ + 1);
			pw.print("\t");
			pw.println(zSum[maxZ]);
			
			zMatrix.set(readId, zList);

			System.out.println("Updating weight");
			
			//weightList = updateWeightList(singleReadList, zMatrix, weightList, readId, samplesParam, maxGroupId);
			weightList = updateWeightList(sampleCountSum, singleReadList, zMatrix, weightList, readId, samplesParam, maxGroupId);
			
			for (int sampleId = 0; sampleId < samplesParam; sampleId++) {
				Map<Integer, List<Integer>> groupVectorMap = sampleCountSum.get(sampleId);
				Integer key = zMatrix.get(readId).get(sampleId);
				List<Integer> transCountList = singleReadList.get(readId).getTransCountwithReverseList();
				if (groupVectorMap.containsKey(key)) {
					List<Integer> sumList = groupVectorMap.get(key);
					for (int i = 0; i < sumList.size(); i++) {
						sumList.set(i, sumList.get(i) + transCountList.get(i));
					}
				} else {
					groupVectorMap.put(key, transCountList);
				}
				sampleCountSum.set(sampleId, groupVectorMap);
			}
			
			for (Integer groupId:zList) {
				if (groupId > maxGroupId) {
					maxGroupId = groupId;
					break;
				}
			}
		}
		
		pw.close();
	}
	
	
	//The enter point of program
	public static void trueMain(String[] args) throws Exception {
		int transOrderParam = 2;
		double alphaParam = 0.001;
		int samplesParam = 100;
		if (args.length == 1) {
			mainTask(args[0], alphaParam, samplesParam, transOrderParam);
		} else {
			int pos = 0;
			while (pos < args.length - 1) {
				if (args[pos].charAt(0) == '-') {
					switch (args[pos].charAt(1)) {
					case 'a':
					case 'A':
						try {
							alphaParam = Double.parseDouble(args[pos + 1]);
						} catch (NullPointerException npe) {
							System.out.println("Param alpha error! Use default value 0.0001");
							alphaParam = 0.0001;
						} catch (NumberFormatException nfe) {
							System.out.println("Param alpha parse error! Use default value 0.0001");
							alphaParam = 0.0001;
						}
						break;
					case 'n':
					case 'N':
						try {
							samplesParam = Integer.parseInt(args[pos + 1]);
						} catch (NumberFormatException nfe) {
							System.out.println("Param sample_number parse error! Use default value 100");
							samplesParam = 100; 
						}
						break;
					case 'm':
					case 'M':
						try {
							transOrderParam = Integer.parseInt(args[pos + 1]);
						} catch (NumberFormatException nfe) {
							System.out.println("Param transorder_number parse error! Use default value 2");
							transOrderParam = 2; 
						}
						break;
					default:
						break;
					}
				}
				pos += 2;
			}
			mainTask(args[args.length - 1], alphaParam, samplesParam, transOrderParam);
		}
	}
	
	public static void main(String[] args) throws Exception {
		trueMain(args);
		//mainTask("/home/xinping/Desktop/6008/test.txt", 0.5, 100, 2);
		//mainTask("/Users/mac/Desktop/6008/someProject/test.txt", 0.001, 1000, 2);
		//System.out.println(testCountSubstr("AAA","AA"));
	}
	
	public static void permutationTest() {
		char[] charArray = {'A', 'C', 'G', 'T'};
		List<String> strList = getPermutations(charArray, 3);
		for (String str:strList) {
			System.out.println(str);
		}
	}
	public static int testCountSubstr(String str, String substr) {
		int count = 0;
		Pattern p = Pattern.compile(substr);
		Matcher m = p.matcher(str);
		while (m.find()) {
			count++;
		}
		return count;
	}
}
