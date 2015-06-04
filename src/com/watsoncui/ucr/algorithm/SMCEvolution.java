package com.watsoncui.ucr.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class SMCEvolution {
	public final static boolean USEGCORDER = false;
	public final static int TRANSORDERPARAM = 1;
	public final static int SAMPLESPARAM = 100;
	public final static double ALPHAPARAM = 0.0000001;
	public final static double THRESHOLDPARAM = 0.9;

	public static List<String> getPermutations(char[] charList, int depth) {
		return SMC.getPermutations(charList, depth);
	}

	public static List<Integer> samplingFromProbability(
			List<Double> posteriorList, Random generator, int samples) {
		return SMC.samplingFromProbability(posteriorList, generator, samples);
	}
	
	//R code: function star_p
		public static List<List<Double>> startProbability(Map<Integer, List<Integer>> accuCountMap, List<DoubleRead> doubleReadList, List<List<Integer>> zMatrix, int readId, int sampleId, int maxGroupId) {
			List<List<Double>> resultList = new ArrayList<List<Double>>(maxGroupId);
			for (int groupId = 1; groupId <= maxGroupId + 1; groupId++) {
				List<Integer> accuCountList = new ArrayList<Integer>();
				List<Integer> transCountList = doubleReadList.get(readId).getCountList();
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

	// R code: trans_p
	public static List<Double> transProbability(
			Map<Integer, List<Integer>> accuCountMap,
			List<DoubleRead> doubleReadList, List<List<Integer>> zMatrix,
			int readId, int sampleId, int groupId) {
		List<Integer> accuCountList = new ArrayList<Integer>();
		List<Integer> transCountList = doubleReadList.get(readId)
				.getCountList();
		for (int j = 0; j < transCountList.size(); j++) {
			accuCountList.add(transCountList.get(j));
		}

		if (accuCountMap.containsKey(groupId)) {
			transCountList = accuCountMap.get(groupId);
			for (int j = 0; j < transCountList.size(); j++) {
				accuCountList.set(j,
						transCountList.get(j) + accuCountList.get(j));
			}
		}

		List<Double> transProbList = new ArrayList<Double>(accuCountList.size());
		int step = accuCountList.size() / 4;
		for (int i = 0; i < accuCountList.size(); i += step) {
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

	public static int getStartPoint(char c, boolean origin) {
		int startWhere = 0;
		if (origin) {
			switch (c) {
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
		} else {
			switch (c) {
			case 'A':
				startWhere = 3;
				break;
			case 'C':
				startWhere = 2;
				break;
			case 'G':
				startWhere = 1;
				break;
			default:
				startWhere = 0;
				break;
			}
		}
		return startWhere;
	}

	public static List<Double> updateWeightList(
			List<Map<Integer, List<Integer>>> sampleCountSum,
			List<DoubleRead> doubleReadList, List<List<Integer>> zMatrix,
			List<Double> weightList, int readId, int samplesParam,
			int maxGroupId) {
		List<Double> newWeightList = new ArrayList<Double>(weightList.size());
		DoubleRead dr = doubleReadList.get(readId);
		int[] startPos = {
				getStartPoint(dr.getReadA().charAt(0), true),
				getStartPoint(dr.getReadB().charAt(0), true),
				getStartPoint(dr.getReadA().charAt(dr.getReadA().length() - 1),
						false),
				getStartPoint(dr.getReadB().charAt(dr.getReadB().length() - 1),
						false) };
		double totalSum = 0;
		for (int sampleId = 0; sampleId < samplesParam; sampleId++) {
			List<List<Double>> startP = startProbability(
					sampleCountSum.get(sampleId), doubleReadList, zMatrix,
					readId, sampleId, maxGroupId);
			List<Double> transProbList = transProbability(
					sampleCountSum.get(sampleId), doubleReadList, zMatrix,
					readId, sampleId, zMatrix.get(readId).get(sampleId));
			double probability = weightList.get(sampleId);
			for (int k = 0; k < startPos.length; k++) {
				probability *= startP
						.get(zMatrix.get(readId).get(sampleId) - 1).get(
								startPos[k]);
			}
			for (int k = 0; k < transProbList.size(); k++) {
				probability *= Math.pow(transProbList.get(k), doubleReadList
						.get(readId).getCountList().get(k));
			}
			totalSum += probability;
			newWeightList.add(probability);
		}
		for (int i = 0; i < newWeightList.size(); i++) {
			newWeightList.set(i, newWeightList.get(i) / totalSum);
		}
		return newWeightList;
	}

	public static List<Double> postProbability(
			List<Map<Integer, List<Integer>>> sampleCountSum,
			List<DoubleRead> doubleReadList, List<List<Integer>> zMatrix,
			List<Double> weightList, int readId, int maxGroupId,
			int samplesParam, double alpha) {

		List<Double> postProbList = new ArrayList<Double>(maxGroupId + 1);
		DoubleRead dr = doubleReadList.get(readId);
		int[] startPos = {
				getStartPoint(dr.getReadA().charAt(0), true),
				getStartPoint(dr.getReadB().charAt(0), true),
				getStartPoint(dr.getReadA().charAt(dr.getReadA().length() - 1),
						false),
				getStartPoint(dr.getReadB().charAt(dr.getReadB().length() - 1),
						false) };

		double[][] probMatrix = new double[maxGroupId + 1][samplesParam];
		for (int i = 0; i < maxGroupId + 1; i++) {
			for (int j = 0; j < samplesParam; j++) {
				probMatrix[i][j] = 0.0;
			}
		}

		// R code function post_p_temp
		for (int sampleId = 0; sampleId < samplesParam; sampleId++) {

			List<List<Double>> startP = startProbability(
					sampleCountSum.get(sampleId), doubleReadList, zMatrix,
					readId, sampleId, maxGroupId);
			for (int groupId = 1; groupId <= maxGroupId + 1; groupId++) {
				List<Double> transProbList = transProbability(
						sampleCountSum.get(sampleId), doubleReadList, zMatrix,
						readId, sampleId, groupId);
				probMatrix[groupId - 1][sampleId] = weightList.get(sampleId);
				for (int k = 0; k < startPos.length; k++) {
					probMatrix[groupId - 1][sampleId] *= startP
							.get(groupId - 1).get(startPos[k]);
				}
				for (int k = 0; k < transProbList.size(); k++) {
					probMatrix[groupId - 1][sampleId] *= Math.pow(
							transProbList.get(k), doubleReadList.get(readId)
									.getCountList().get(k));
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

		// R code function post_p
		for (int sampleId = 0; sampleId < samplesParam; sampleId++) {
			// R code prior

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
					priorProbList.add(sampleCount[i] / (alpha + readId));
				} else {
					priorProbList.add(0.0);
					countZero++;
				}
			}
			priorProbList.add(alpha / (alpha + readId) / countZero);
			for (int i = 0; i < maxGroupId; i++) {
				if (sampleCount[i] == 0) {
					priorProbList.set(i, alpha / (alpha + readId) / countZero);
				}
			}

			for (int groupId = 1; groupId <= maxGroupId + 1; groupId++) {
				probMatrix[groupId - 1][sampleId] = weightList.get(sampleId)
						* priorProbList.get(groupId - 1)
						* postProbList.get(groupId - 1);
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

	public static void mainTask(String fileName, double alphaParam,
			int samplesParam, int transOrderParam, double thresholdParam,
			boolean useGCOrder) throws Exception {
		// parameters

		System.out.println("alphaParam = " + alphaParam);
		System.out.println("samplesParam = " + samplesParam);
		System.out.println("transOrderParam = " + transOrderParam);
		System.out.println("thresholdParam = " + thresholdParam);
		if (useGCOrder) {
			System.out.println("Use GC Order");
		}
		int readsParam;
		List<String> contentList = new ArrayList<String>();

		System.out.println("Reading data file...");

		// Read data file
		BufferedReader br = new BufferedReader(new FileReader(
				new File(fileName)));
		String line;
		while (null != (line = br.readLine())) {
			contentList.add(line.toUpperCase());
		}
		br.close();

		System.out.println("Read complete");

		System.out.println("Initializing...");

		readsParam = contentList.size() / 2;

		// Initialize important weight matrix W(z)
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

		// Initialize weight list w
		List<Double> weightList = new ArrayList<Double>(samplesParam);
		for (int j = 0; j < samplesParam; j++) {
			weightList.add(1.0 / samplesParam);
		}

		// Initialize permutation count matrix
		char[] charArray = { 'A', 'C', 'G', 'T' };
		List<String> permutationList = getPermutations(charArray,
				transOrderParam + 1);
		List<DoubleRead> doubleReadList = new ArrayList<DoubleRead>(readsParam);
		for (int readId = 0; readId < readsParam; readId++) {
			doubleReadList.add(new DoubleRead(readId + 1, contentList
					.get(readId * 2), contentList.get(readId * 2 + 1),
					transOrderParam + 1, permutationList));
		}

		if (useGCOrder) {
			System.out.println("Sort according to GCOrder...");
			Collections.sort(doubleReadList, new DoubleReadCompare());
		}

		System.out.println("Complete \n Calculating...");

		int maxGroupId = 1;

		// Init the intermediate result of every sample
		List<Map<Integer, List<Integer>>> sampleCountSum = new ArrayList<Map<Integer, List<Integer>>>(
				samplesParam);
		for (int i = 0; i < samplesParam; i++) {
			Map<Integer, List<Integer>> groupVectorMap = new TreeMap<Integer, List<Integer>>();
			groupVectorMap.put(1, doubleReadList.get(0).getCountList());
			sampleCountSum.add(groupVectorMap);
		}

		// Core calculation
		for (int readId = 1; readId < readsParam; readId++) {

			System.out.println(readId);
			System.out.println("Posterior Probability");

			List<Double> posteriorList = postProbability(sampleCountSum,
					doubleReadList, zMatrix, weightList, readId, maxGroupId,
					samplesParam, alphaParam);

			for (int i = 0; i < posteriorList.size(); i++) {
				System.out.println(posteriorList.get(i));
			}

			System.out.println("Sampling");
			Random generator = new Random();
			List<Integer> zList = samplingFromProbability(posteriorList,
					generator, samplesParam);
			for (Integer groupId : zList) {
				if (groupId > maxGroupId) {
					maxGroupId = groupId;
					break;
				}
			}
			zMatrix.set(readId, zList);

			int count_1 = 0;
			for (int i = 0; i < zList.size(); i++) {
				if (zList.get(i) == 1) {
					count_1++;
				}
			}
			System.out.println(count_1);
			System.out.println("Updating weight");

			weightList = updateWeightList(sampleCountSum, doubleReadList,
					zMatrix, weightList, readId, samplesParam, maxGroupId);
			for (int i = 0; i < weightList.size(); i++) {
				System.out.print(weightList.get(i));
				System.out.print('\t');
			}
			System.out.println();
		}

		System.out.println("Calculate complete \n Printing...");

		PrintWriter pw = new PrintWriter(new FileWriter(new File("output.txt")));
		for (Double weight : weightList) {
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
		int transOrderParam = TRANSORDERPARAM;
		double alphaParam = ALPHAPARAM;
		double thresholdParam = THRESHOLDPARAM;
		int samplesParam = SAMPLESPARAM;
		boolean useGCOrder = USEGCORDER;
		int pos = 0;
		while (pos < args.length - 1) {
			if (args[pos].charAt(0) == '-') {
				switch (args[pos].charAt(1)) {
				case 'a':
				case 'A':
					try {
						alphaParam = Double.parseDouble(args[pos + 1]);
					} catch (NullPointerException npe) {
						System.out
								.println("Param alpha error! Use default value "
										+ ALPHAPARAM);
						alphaParam = ALPHAPARAM;
					} catch (NumberFormatException nfe) {
						System.out
								.println("Param alpha parse error! Use default value "
										+ ALPHAPARAM);
						alphaParam = ALPHAPARAM;
					}
					pos += 2;
					break;
				case 'n':
				case 'N':
					try {
						samplesParam = Integer.parseInt(args[pos + 1]);
					} catch (NullPointerException npe) {
						System.out
								.println("Param sample_number error! Use default value "
										+ SAMPLESPARAM);
						samplesParam = SAMPLESPARAM;
					} catch (NumberFormatException nfe) {
						System.out
								.println("Param sample_number parse error! Use default value "
										+ SAMPLESPARAM);
						samplesParam = SAMPLESPARAM;
					}
					pos += 2;
					break;
				case 'm':
				case 'M':
					try {
						transOrderParam = Integer.parseInt(args[pos + 1]);
					} catch (NullPointerException npe) {
						System.out
								.println("Param transorder_number error! Use default value "
										+ TRANSORDERPARAM);
						transOrderParam = TRANSORDERPARAM;
					} catch (NumberFormatException nfe) {
						System.out
								.println("Param transorder_number parse error! Use default value "
										+ TRANSORDERPARAM);
						transOrderParam = TRANSORDERPARAM;
					}
					pos += 2;
					break;
				case 't':
				case 'T':
					try {
						thresholdParam = Double.parseDouble(args[pos + 1]);
					} catch (NullPointerException npe) {
						System.out
								.println("Param threshold error! Use default value "
										+ THRESHOLDPARAM);
						thresholdParam = THRESHOLDPARAM;
					} catch (NumberFormatException nfe) {
						System.out
								.println("Param threshold parse error! Use default value "
										+ THRESHOLDPARAM);
						thresholdParam = THRESHOLDPARAM;
					}
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
		mainTask(args[args.length - 1], alphaParam, samplesParam,
				transOrderParam, thresholdParam, useGCOrder);
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		frontEnd(args);
	}

}
