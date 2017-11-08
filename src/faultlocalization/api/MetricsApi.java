package faultlocalization.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.org.glassfish.external.statistics.Statistic;

import java.util.Map.Entry;

import faultlocalization.data.Ranking;
import jdk.internal.dynalink.beans.StaticClass;

public class MetricsApi {
	
	public static enum STATISTIC {
			
			MEAN {
	
				@Override
				public String getName() {
					return "MEAN";
				}
	
				@Override
				public String description() {
					return "Mathematic mean";
				}
				
				@Override
				public Float calculate(int positive, int negative, int totalPositive, int totalNegative) {
					return (negative/(float)totalNegative)/((negative/(float)totalNegative)+(positive/(float)totalPositive));
				}
				
			},
			
			VARIANCE {
				
				@Override
				public String getName() {
					return "VARIANCE";
				}
	
				@Override
				public String description() {
					return "Mathematic variance";
				}
				
				@Override
				public Float calculate(int positive, int negative, int totalPositive, int totalNegative) {
					return (negative/(float)totalNegative)/((negative/(float)totalNegative)+(positive/(float)totalPositive));
				}
				
			},
			
			STANDARD_DEVIATION {
	
				@Override
				public String getName() {
					return "STANDARD DEVIATION";
				}
	
				@Override
				public String description() {
					return "Mathematic standard deviation";
				}
				
				@Override
				public Float calculate(int positive, int negative, int totalPositive, int totalNegative) {
					return negative / (float) Math.sqrt(totalNegative * (negative + positive));
				}
				
			};
		

		public abstract String getName();
		public abstract String description();
		public abstract Float calculate(int positive, int negative, int totalPositive, int totalNegative);
			
	}

	
	/**
	 * Compute the biggest standard deviation 
	 * @param rankings is a set of rankings.
	 * @return the biggest Standard deviations of dkl measures.
	 * @throws IllegalArgumentException if {@code rankings.size()==0} or {@code rankings == null}
	 */
	public static float biggestStandardDeviationDklMetric(List<Ranking> rankings){
		if(rankings == null || rankings.size() == 0 )
			throw new IllegalArgumentException();
		float biggestSD = -1;	
		for(Ranking ideal : rankings){
			float sd = divergenceStatisticFrom(ideal, rankings, STATISTIC.STANDARD_DEVIATION);
			if(biggestSD < sd)
				biggestSD = sd;
		}
		return biggestSD;
	}
	
	/**
	 * Compute the smallest standard deviation.
	 * @param rankings is a set of rankings.
	 * @return the smallest Standard deviations of dkl measures.
	 * @throws IllegalArgumentException if {@code rankings.size()==0} or {@code rankings == null}
	 */
	public static float smallestStandardDeviationDklMetric(List<Ranking> rankings){
		if(rankings == null || rankings.size() == 0 )
			throw new IllegalArgumentException();
		float smallestSD = 1000;	
		for(Ranking ideal : rankings){
			float sd = divergenceStatisticFrom(ideal, rankings, STATISTIC.STANDARD_DEVIATION);
			if(smallestSD > sd)
				smallestSD = sd;
		}
		return smallestSD;
	}

	/**
	 * Calculate standard deviation between rankings.
	 * @param rankings is a set of rankings.
	 * @return Standard deviations of dkl measures.
	 */
	public static Map<String, Float> dklStandardDeviations(List<Ranking> rankings){
		Map<String,Float> dklDeviations = new TreeMap<String, Float>();		
		for(Ranking ideal : rankings){
			//dklDeviations.put(ideal.getFormula().getName(), divergenceStatisticFrom(ideal,rankings));//TODO fix name to formula
			//dklDeviations.put(ideal.getFormula().getName(), divergenceStatisticFrom(ideal,rankings,STATISTIC.STANDARD_DEVIATION));
			//dklDeviations.put(ideal.getFormula().getName(), divergenceStatisticFrom(ideal,rankings,STATISTIC.MEAN));
			dklDeviations.put(ideal.getFormula().getName(), divergenceStatisticFromE(ideal,rankings,STATISTIC.VARIANCE));
		}
		return dklDeviations;
	}
	
	/**
	 * Calculate statistic of dkl measures respect to an ideal ranking.
	 * Efficient computation of values.  
	 * @param idealRanking is the supposed right ranking. 
	 * @param rankings is a set of other rankings.
	 * @return standard deviation of dkl measures.
	 */
	public static float divergenceStatisticFromE(Ranking idealRanking, List<Ranking> rankings, STATISTIC statistic ){
		float dklDivergence; float dklSum = 0; float dklMetricAverage; float variance = 0; float standardDeviation;
		List<Float> dklMetrics = new LinkedList<>();
		String idealCoefficientName = idealRanking.getFormula().getName();

		for(Ranking r : rankings){
			if(r.getFormula().getName().equals(idealCoefficientName)) continue;
			dklDivergence = dkl(idealRanking.getProbabilitiesRanking(), r.getProbabilitiesRanking());
			dklSum += dklDivergence;
			dklMetrics.add(dklDivergence);
			//assert(dklDivergence >= 0);
		}
		
		// compute mean
		dklMetricAverage = dklSum/dklMetrics.size();
		if(statistic == statistic.MEAN){
			System.out.println("DklMetrics mean:"+ dklMetricAverage);
			return dklMetricAverage;
		}

		//compute variance
		for(Float dklMetric : dklMetrics){
			variance += (dklMetricAverage - dklMetric) * (dklMetricAverage - dklMetric); 
		}
		variance = variance / dklMetrics.size();
		if(statistic == statistic.VARIANCE){
			System.out.println("DklMetrics variance:"+ variance);
			return variance;
		}
		
		// compute standard deviation
		standardDeviation = (float) Math.sqrt(variance);
		System.out.println("DklMetrics standard deviation:"+ standardDeviation );
		return standardDeviation;
	}
	
	/**
	 * Calculate statistic of dkl measures respect to an ideal ranking
	 * @param idealRanking is the supposed right ranking. 
	 * @param rankings is a set of rankings.
	 * @return statistic over dkl divergences
	 */
	public static float divergenceStatisticFrom(Ranking idealRanking, List<Ranking> rankings, STATISTIC statistic){
		float output = -1;
		
		List<Float>	dklDivergences = dklDivergencesFrom(idealRanking, rankings);
		
		switch (statistic) {
			case MEAN : output = mean(dklDivergences);
					System.out.println("DklMetrics average:"+ output);
					break;
			case VARIANCE : output = variance(dklDivergences);
					System.out.println("DklMetrics variance:"+ output);
					break;
			case STANDARD_DEVIATION : output = standardDeviation(dklDivergences);
					System.out.println("DklMetrics standard deviation:"+ output );
					break;
			}
		return output;
	}
	
	/**
	 * Compute dkl divergences from ideal ranking
	 * @param idealRanking is supposed as right ranking. 
	 * @param ranking are a set of rankings.
	 * @return all dkl divergences to idealRankings.
	 */
	public static List<Float> dklDivergencesFrom(Ranking idealRanking, List<Ranking> rankings ){
		float dklDivergence;
		List<Float> dklDivergences = new LinkedList<Float>();
		String idealCoefficientName = idealRanking.getFormula().getName();
		for(Ranking r : rankings){
			if(r.getFormula().getName().equals(idealCoefficientName)) continue;
			dklDivergence = dkl(idealRanking.getProbabilitiesRanking(), r.getProbabilitiesRanking());
			dklDivergences.add(dklDivergence);
			System.out.println("Dkl Divergence: "+dklDivergence+" between "+idealCoefficientName+" as ideal and "+r.getFormula().getName());
		}
		return dklDivergences;
	}
	
	/**
	 * Implements Kullback-Leiber divergence between two probability distributions
	 * as in LIL (Locality Information Loss) Metric.
	 * @param idealDistribution is supposed as if was right distribution.
	 * @param otherDistribution is distribution to measure information loss respect to ideal.
	 * @return
	 */
	public static float dkl(Map<Integer, Float> idealDistribution, Map<Integer, Float> otherDistribution){
		float dklDivergence = 0; double ln = 0.0; float pProbSum = 0; float qProbSum = 0; 
		for (Entry<Integer, Float> rank : idealDistribution.entrySet()) {
			Float pProb = rank.getValue(); 
			Float qProb = otherDistribution.get(rank.getKey());
			//assert(pProb > 0); //assert(pProb != 0); //assert(qProb >= 0);
			ln = (double) (Math.log(pProb/qProb) *  pProb);			
			dklDivergence += ln;
			//pProbSum += pProb;
			//qProbSum += qProb;
			//System.out.println("s "+rank.getKey()+" ln (Pi/Qi) * Pi = "+ln);
			//System.out.println("s "+rank.getKey()+" pProb :"+pProb+" -  qProb :"+qProb);
		}
		//System.out.println("pProb sum: "+pProbSum+" qProb sum:"+qProbSum);
		return dklDivergence;
	}
	
	/**
	 * Calculate standard deviation over the values in a list.
	 * @param l a list of numbers.
	 * @return standard deviation of values 
	 */
	public static <T extends Number> float standardDeviation(List<T> l){
		return (float) Math.sqrt(variance(l));
	}
	
	/**
	 * Calculate variance over the values in a list. 
	 * @return variance statistic of values in list l
	 */
	public static <T extends Number> float variance(List<T> l){
		float variance = 0;
		float average = mean(l);
		for(T f : l)
			variance += Math.pow((average - f.floatValue()), 2);
		variance = variance / l.size();
		return variance;
	}
	
	/**
	 * Calculate mean over the values in a list.
	 * @param <T>
	 * @param l a list of numbers.
	 * @return average of values in list l.
	 */
	public static <T extends Number> float mean(List<T> l){
		float sum = 0;
		for(T f : l)
			sum += f.floatValue();
		return sum/l.size();
	}
	

}
