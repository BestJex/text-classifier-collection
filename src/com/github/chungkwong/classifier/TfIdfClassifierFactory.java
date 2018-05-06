/*
 * Copyright (C) 2018 kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.chungkwong.classifier;
import com.github.chungkwong.classifier.util.*;
import java.util.*;
import java.util.stream.*;
/**
 * 
 * Factory for TF-IDF classifier
 * @author kwong
 */
public class TfIdfClassifierFactory<T> implements ClassifierFactory<Classifier<Stream<T>>,FrequenciesModel<T>,Stream<T>>{
	/**
	 * Standard TF-IDF formula
	 */
	public static final TfIdfFormula STANDARD=((freq,docFreq,docCount)->{
		return freq==0?0:(1+Math.log(freq))*Math.log(1+((double)docCount)/docFreq);
	});
	/**
	 * Use token frequency as TF-IDF
	 */
	public static final TfIdfFormula FREQUENCY=((freq,docFreq,docCount)->{
		return freq;
	});
	/**
	 * Use token occurence as TF-IDF
	 */
	public static final TfIdfFormula THREHOLD=((freq,docFreq,docCount)->{
		return freq==0?0:1;
	});
	private TfIdfFormula tfIdfFormula;
	/**
	 * Create a factory with standard TF-IDF formula
	 */
	public TfIdfClassifierFactory(){
		tfIdfFormula=STANDARD;
	}
	/**
	 * Set TF-IDF formula
	 * @param tfIdfFormula TF-IDF formula
	 * @return
	 */
	public TfIdfClassifierFactory<T> setTfIdfFormula(TfIdfFormula tfIdfFormula){
		this.tfIdfFormula=tfIdfFormula;
		return this;
	}
	/**
	 * @return TF-IDF formula
	 */
	public TfIdfFormula getTfIdfFormula(){
		return tfIdfFormula;
	}
	@Override
	public Classifier<Stream<T>> getClassifier(FrequenciesModel<T> model){
		return new TfIdfClassifier<>(model.getTokenFrequencies(),
				model.getTotalDocumentFrequencies(),model.getSampleCount(),tfIdfFormula);
	}
	@Override
	public FrequenciesModel<T> createModel(){
		return new FrequenciesModel<>();
	}
	/**
	 * TF-IDF formula
	 */
	@FunctionalInterface
	public interface TfIdfFormula{
		/**
		 * Calcuate TF-IDF
		 * @param freq token frequency
		 * @param docFreq document frequency
		 * @param docCount sample document count
		 * @return TF-IDF
		 */
		double calculate(long freq,long docFreq,long docCount);
	}
	private static class TfIdfClassifier<T> implements Classifier<Stream<T>>{
		private final Map<Category,ImmutableFrequencies<T>> profiles;
		private final ImmutableFrequencies<T> documentFrequencies;
		private final long documentCount;
		private final TfIdfClassifierFactory.TfIdfFormula tfIdfFormula;
		public TfIdfClassifier(Map<Category,ImmutableFrequencies<T>> profiles,
				ImmutableFrequencies<T> documentFrequencies,long documentCount,
				TfIdfClassifierFactory.TfIdfFormula tfIdfFormula){
			this.profiles=profiles;
			this.documentFrequencies=documentFrequencies;
			this.documentCount=documentCount;
			this.tfIdfFormula=tfIdfFormula;
		}
		@Override
		public List<ClassificationResult> getCandidates(Stream<T> object,int max){
			ImmutableFrequencies<T> document=new ImmutableFrequencies<>(object);
			return profiles.entrySet().stream().map((e)->new ClassificationResult(cosSquare(document,e.getValue()),e.getKey())).
					collect(Collectors.toList());
		}
		private double cosSquare(ImmutableFrequencies<T> document,ImmutableFrequencies<T> category){
			ImmutableFrequencies<T> shorter, longer;
			if(document.getTokenCount()>category.getTokenCount()){
				shorter=category;
				longer=document;
			}else{
				shorter=document;
				longer=category;
			}
			double product=0, shortModSq=0, longModSq=0;
			Iterator<Map.Entry<T,Long>> iterator=shorter.toMap().entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<T,Long> next=iterator.next();
				T token=next.getKey();
				double shortTfIdf=tfIdfFormula.calculate(next.getValue(),documentFrequencies.getFrequency(token),documentCount);
				double longTfIdf=tfIdfFormula.calculate(longer.getFrequency(token),documentFrequencies.getFrequency(token),documentCount);
				shortModSq+=shortTfIdf*shortTfIdf;
				longModSq+=longTfIdf*longTfIdf;
				product+=shortModSq*longTfIdf;
			}
			return product*product/(shortModSq*longModSq);
		}
	}
	@Override
	public String toString(){
		return "TF-IDF";
	}
}
