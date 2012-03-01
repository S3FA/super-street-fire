package ca.site3.ssf.gesturerecognizer;


import java.io.File;

import libsvm.LibSVM;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.fastdtw.timeseries.PAA;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;
import net.sf.javaml.tools.data.FileHandler;



public class Tests {
    /**
     * Shows the default usage of the KNN algorithm.
     */
    public static void main(String[] args) throws Exception {

        /* Load a data set */
        Dataset data = FileHandler.loadDataset(new File("C:/projects/superstreetfire/ssf-java/javaml-0.1.5/datasets/iris/iris.data"), 4, ",");
        
         // USE THIS TO SHRINK/STRETCH DATA SERIES!!!
        {
	        TimeSeries timeSeries = new PAA(new TimeSeries(data.instance(0)), 4);
	        for (int i = 0; i < timeSeries.numOfPts(); i++) {
	        	System.out.println(timeSeries.getTimeAtNthPoint(i));
	        }
	        System.out.println(timeSeries.toString());
	        System.out.println("-------------------------------------------");
        }
        {
	        TimeSeries timeSeries = new PAA(new TimeSeries(data.instance(0)), 3);
	        for (int i = 0; i < timeSeries.numOfPts(); i++) {
	        	System.out.println(timeSeries.getTimeAtNthPoint(i));
	        }
	        System.out.println(timeSeries.toString());
        }
        
        /*
         * Contruct a KNN classifier that uses 5 neighbors to make a decision.
         */
        Classifier knn = new KNearestNeighbors(5);
        knn.buildClassifier(data);

        /*
         * Load a data set for evaluation, this can be a different one, but we
         * will use the same one.
         */
        Dataset dataForClassification = FileHandler.loadDataset(new File("C:/projects/superstreetfire/ssf-java/javaml-0.1.5/datasets/iris/iris.data"), 4, ",");
        /* Counters for correct and wrong predictions. */
        int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */
        for (Instance inst : dataForClassification) {
            Object predictedClassValue = knn.classify(inst);
            Object realClassValue = inst.classValue();
            if (predictedClassValue.equals(realClassValue))
                correct++;
            else
                wrong++;
        }
        System.out.println("Correct predictions  " + correct);
        System.out.println("Wrong predictions " + wrong);
        
        otherTest();

    }
    
    private static void otherTest() throws Exception {
        /* Load a data set */
        Dataset data = FileHandler.loadDataset(new File("C:/projects/superstreetfire/ssf-java/javaml-0.1.5/datasets/iris/iris.data"), 4, ",");
        /*
         * Contruct a LibSVM classifier with default settings.
         */
        Classifier svm = new LibSVM();
        svm.buildClassifier(data);

        /*
         * Load a data set, this can be a different one, but we will use the
         * same one.
         */
        Dataset dataForClassification = FileHandler.loadDataset(new File("C:/projects/superstreetfire/ssf-java/javaml-0.1.5/datasets/iris/iris.data"), 4, ",");
        /* Counters for correct and wrong predictions. */
        int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */
        for (Instance inst : dataForClassification) {
            Object predictedClassValue = svm.classify(inst);
            Object realClassValue = inst.classValue();
            if (predictedClassValue.equals(realClassValue))
                correct++;
            else
                wrong++;
        }
        System.out.println("Correct predictions  " + correct);
        System.out.println("Wrong predictions " + wrong);
    }

}