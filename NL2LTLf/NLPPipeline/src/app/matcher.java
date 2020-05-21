package app;

import edu.stanford.nlp.trees.Tree;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Semantic Tree Matcher. Class used to visit and extracts frame roles from a
 * semantic tree.
 *
 * @author Riccardo Ratini <riccardo.ratini>
 */
public class matcher {
    /**
     * Visits the tree and return a Frame with all the rules of the input frame
     * that are matched in the tree.
     *
     * @param root root node of the current subtree
     * @param rules Verbnet frame to fill
     * @param last_object Last direct object
     * @return List of filled FrameItems.
     */
    public static List<FrameItem> match(Tree root, List<FrameItem> rules, String last_object){
        List<Tree> children = root.getChildrenAsList();
        List<FrameItem> newRules = new ArrayList<>(), _rules  = rules.stream().collect(Collectors.toList());
        for(int i=0; i < children.size(); i++){
            String child_value = children.get(i).value();
            if(child_value.contains("V") && !child_value.equals("VP")){
                child_value = "V";
            }
            if(_rules.size() > 0 && child_value.equals(_rules.get(0).getTag())){
                FrameItem rule = _rules.get(0);
                _rules.remove(rule);
                if(child_value.equals("V")){ newRules.add(rule);}
                else if (child_value.equals("PP")){
                    String val = find_value_PP(children.get(i), rule.getRole(), last_object);
                    rule.setValue(val);
                    newRules.add(rule);
                }
                else {
                    String val = find_value(children.get(i), last_object);
                    rule.setValue(val);
                    newRules.add(rule);
                }
            }else{
                List<FrameItem> ris = match(children.get(i), _rules, last_object);
                if(ris != null){
                    newRules.addAll(ris);
                }               
            }
        }
        for(FrameItem rule : newRules){
            if(rule.getValue()== null){
                return null;
            }
        }
        return newRules;
    }    
    
    /**
     * Visits the subtree rooted into a possessive pronoun (PP) and returns its 
     * part of sentence.
     * eg.
     * 
     * subtree: 
     * PP --> TO --> to
     *    |-> NP --> DT --> the
     *           |-> NN --> hallway
     * 
     * returns: "to the hallway"
     * 
     * @param root
     * @param role
     * @param last_object
     * @return 
     */
    public static String find_value_PP(Tree root, String role, String last_object){
        List<Tree> children = root.getChildrenAsList();
        String IN = "", value = "";

        for (Tree child : children) {
            if(child.value().equals("IN"))
                IN = find_value(child, last_object);
            else{
                value = find_value(child, last_object);
            }
        }
       
        if(role.toLowerCase().equals("initial_location") && !IN.toLowerCase().equals("from") ){
            return null;
        }
        return value;
    }   
    
    /**
     * Visits the subtree rooted into a non possessive pronoun and returns its 
     * part of sentence.
     * eg.
     * 
     * subtree: 
     * PP --> TO --> to
     *    |-> NP --> DT --> the
     *           |-> NN --> hallway
     * 
     * returns: "to the hallway"
     * 
     * @param root
     * @param last_object
     * @return 
     */
    public static String find_value(Tree root, String last_object){
        List<Tree> children = root.getChildrenAsList();
        String ris = "";
        if(root.isLeaf()) {
            return root.value();
        } else{
            for(Tree child: children){
                if(child.value().equals("DT")){
                    continue;
                }
                
                if(root.value().contains("PRP") && last_object != null){
                    ris += last_object+ " ";
                }else{
                    ris += find_value(child, last_object) + " ";
                }
            }
            return ris.trim();
        }
    }    
}
