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
import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
/**
 *
 * @author kwong
 */
public class Main{
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException,InterruptedException{
		System.out.println(TextPreprocessors.getPorterStemmer().apply(Stream.of("I","was","eating","balls","happily")).collect(Collectors.toList()));
		System.out.println(TextPreprocessors.getLovinsStemmer().apply(Stream.of("I","was","eating","balls","happily")).collect(Collectors.toList()));
		System.out.println(TextPreprocessors.getStemmer(Locale.ENGLISH).apply(Stream.of("I","was","eating","balls","happily")).collect(Collectors.toList()));
		System.out.println(TextPreprocessors.getIcuTransformer("Simplified-Traditional").apply("万里长城永不倒"));
		System.out.println("hello".replaceFirst("([a-z])","$1$1"));
		FrequenciesModel<String> frequenciesModel=new FrequenciesModel<>();
		frequenciesModel.load(new File("/home/kwong/projects/text-classifier/data/THUCNews/stat"),Function.identity());
		double[] q=new double[101];
		for(int i=0;i<=100;i++){
			q[i]=i*0.01;
		}
		long[] quantile=frequenciesModel.getQuantile(q);
		for(int i=0;i<=100;i++){
			System.out.println(i+":"+quantile[i]);
		}
		
		frequenciesModel.keepFrequencyRange(quantile[98],quantile[100]+1);
		frequenciesModel.save(new File("/home/kwong/projects/text-classifier/data/THUCNews/stat2"),Function.identity());
	}
}