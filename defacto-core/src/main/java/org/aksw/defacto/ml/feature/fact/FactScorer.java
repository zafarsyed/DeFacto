/**
 * 
 */
package org.aksw.defacto.ml.feature.fact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class FactScorer {

    private Classifier classifier       = null;
    private Instances trainingInstances = null;

    /**
     * 
     */
    public FactScorer() {
        
        this.classifier = loadClassifier();
        try {
            
            this.trainingInstances = new Instances(new BufferedReader(new FileReader(loadFileName("/training/arff/fact/defacto_fact_word.arff"))));
        }
        catch (FileNotFoundException e) {

            throw new RuntimeException(e);
        }
        catch (IOException e) {
            
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @param evidence
     */
    public void scoreEvidence(Evidence evidence) {

        for ( ComplexProof proof : evidence.getComplexProofs() ) {
            try {
                
                Instances instancesWithStringVector = new Instances(trainingInstances);
//                System.out.println(withoutStrings);
                
                instancesWithStringVector.setClassIndex(11);
                
                // create new instance and delete debugging features
                Instance newInstance = new Instance(proof.getFeatures());
                newInstance.deleteAttributeAt(10);
                newInstance.deleteAttributeAt(11);
                newInstance.deleteAttributeAt(12);
                newInstance.deleteAttributeAt(12);
                
                // insert all the words which occur
                for ( int i = newInstance.numAttributes() ; i < instancesWithStringVector.numAttributes(); i++) {
                    
                    String name = instancesWithStringVector.attribute(i).name();
                    newInstance.insertAttributeAt(i);
                    newInstance.setValue(instancesWithStringVector.attribute(i), proof.getProofPhrase().contains(name) ? 1D : 0D);
                }
                
                newInstance.setDataset(instancesWithStringVector);
                instancesWithStringVector.add(newInstance);
                
//                System.out.println(newInstance);
                
                proof.setScore(this.classifier.classifyInstance(newInstance));
//                System.out.println(proof.getScore());
            }
            catch (Exception e) {

                e.printStackTrace();
                System.exit(0);
            }
        }
        
        // set for each website the score by multiplying the proofs found on this site
        for ( WebSite website : evidence.getAllWebSites() ) {
            
            double score = 1D;
            
            for ( ComplexProof proof : evidence.getComplexProofs(website)) {

                score *= ( 1D - proof.getScore() );
            }
            website.setScore(1 - score);
        }
    }
    
    /**
     * 
     * @return
     */
    private Classifier loadClassifier() {

        try {
            
            return (Classifier) weka.core.SerializationHelper.read(loadFileName("/classifier/fact/fact.model"));
        }
        catch (Exception e) {

            throw new RuntimeException("Could not load classifier from: " + "classifier/fact/fact.model", e);
        }
    }
    
    public String loadFileName(String name){
    	
    	return new File(FactScorer.class.getResource(name).getFile()).getAbsolutePath(); 
    }
    
    public static void main(String[] args) {
		
    	
	}
}
