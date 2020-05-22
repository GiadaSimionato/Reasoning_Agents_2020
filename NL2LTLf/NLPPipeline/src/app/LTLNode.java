package app;

import edu.stanford.nlp.ling.HasWord;
import java.util.List;

/**
 * Class that models of an LTLTree. This structure represents a NL sentence as a
 * tree where branch nodes are LTL operators (eg. AND, OR...) and leaf nodes are
 * "simple" sentences (sentences that do not contains LTL Operators, eg. go to
 * the box).
 *
 * Attributes: type=the type of the node ("PLAIN" for simple sentence
 * "AND","OR"... for branches) x,y= child nodes (Since in our case the operators
 * are at most binary we have only two childs) sentence=list of StanfordNLP
 * HasWord modelling the sentente associated to a leaf node
 *
 * It contains only costructor, getters/setters and toString methods
 *
 * @author Riccardo Ratini <riccardo.ratini>
 */
public class LTLNode {
    private String type;
    private LTLNode x, y;
    private List<HasWord> sentence = null;
    
    public LTLNode(List<HasWord> text){
        this.type = "PLAIN";
        this.sentence = text;
        this.x = null;
        this.y = null;
    }
    public LTLNode(String type, LTLNode x, LTLNode y){
        this.type = type;
        this.sentence = null;
        this.x = x;
        this.y = y;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public LTLNode getX() {
        return x;
    }
    public void setX(LTLNode x) {
        this.x = x;
    }

    public LTLNode getY() {
        return y;
    }
    public void setY(LTLNode y) {
        this.y = y;
    }
    
    public boolean isLeaf(){
        return this.type.equals("PLAIN");
    }
    
    public List<HasWord> getSentence() {
        return sentence;
    }
    public void setSentence(List<HasWord> sentence) {
        this.sentence = sentence;
    }
    
    @Override public String toString(){
        if(isLeaf()){
            return List2String();
        }else{
            return "["+type +"| X: "+ x +" Y: "+y+"]";
        }
    }    
    private String List2String(){
        String text = "";
        for(HasWord word: sentence){
            text += word.toString() + " ";
        }
        return text.trim();
    }
}
