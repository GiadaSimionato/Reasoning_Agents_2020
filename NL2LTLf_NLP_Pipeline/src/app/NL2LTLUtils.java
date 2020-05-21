package app;

import edu.mit.jverbnet.data.IFrame;
import edu.mit.jverbnet.data.IMember;
import edu.mit.jverbnet.data.IVerbClass;
import edu.mit.jverbnet.data.IWordnetKey;
import edu.mit.jverbnet.data.syntax.ISyntaxArgDesc;
import edu.mit.jverbnet.data.syntax.ISyntaxDesc;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * Utility methods used in NLP2LTLTranslator.translate
 * @author Riccardo Ratini <riccardo.ratini>
 */
public class NL2LTLUtils {

    /**
     * Given a sentence it returns its LTLTree
     * 
     * This is a recursive method, it create an LTLNode from the first LTL Operator of the sentence
     * then generates its child using the subsenteces related to the operator
     * 
     * @param sentence input sentence in List of HasWord form
     * @return the LTLTree generated from the sentence
     */
    public static LTLNode generateLTLTree(List<HasWord> sentence) {
        int idx_separator;
        for (HasWord word : sentence) {
            switch (word.word()) {
                case "and": {
                    idx_separator = sentence.indexOf(new Word("and"));
                    if (idx_separator != -1) {
                        LTLNode x = generateLTLTree(sentence.subList(0, idx_separator));
                        LTLNode y = generateLTLTree(sentence.subList(idx_separator + 1, sentence.size()));
                        return new LTLNode("AND", x, y);
                    }
                    break;
                }
                case "or": {
                    idx_separator = sentence.indexOf(new Word("or"));
                    if (idx_separator != -1) {
                        LTLNode x = generateLTLTree(sentence.subList(0, idx_separator));
                        LTLNode y = generateLTLTree(sentence.subList(idx_separator + 1, sentence.size()));
                        return new LTLNode("OR", x, y);
                    }
                    break;
                }
                case "not": {
                    idx_separator = sentence.contains(new Word("do")) ? 2 : 1;
                    LTLNode x = generateLTLTree(sentence.subList(idx_separator, sentence.size()));
                    return new LTLNode("NOT", x, null);
                }
                case "until": {
                    idx_separator = sentence.indexOf(new Word("or"));
                    if (idx_separator != -1) {
                        LTLNode x = generateLTLTree(sentence.subList(0, idx_separator));
                        LTLNode y = generateLTLTree(sentence.subList(idx_separator + 1, sentence.size()));
                        return new LTLNode("UNTIL", x, y);
                    }
                    break;
                }
                case "always": {
                    LTLNode x = generateLTLTree(sentence.subList(1, sentence.size()));
                    return new LTLNode("ALWAYS", x, null);
                }
                case "immediately": {
                    LTLNode x = generateLTLTree(sentence.subList(1, sentence.size()));
                    return new LTLNode("IMMEDIATELY", x, null);
                }
                case "when": {
                    idx_separator = sentence.indexOf(new Word("then")) != -1 ? sentence.indexOf(new Word("then")) : sentence.indexOf(new Word(","));
                    if (idx_separator != -1) {
                        LTLNode x = generateLTLTree(sentence.subList(1, idx_separator));
                        LTLNode y = generateLTLTree(sentence.subList(idx_separator + 1, sentence.size()));
                        return new LTLNode("WHEN", x, y);
                    }
                }
                case "then": {
                    idx_separator = sentence.indexOf(new Word("then"));
                    if (idx_separator != -1) {
                        LTLNode x = generateLTLTree(sentence.subList(0, idx_separator));
                        LTLNode y = generateLTLTree(sentence.subList(idx_separator + 1, sentence.size()));
                        return new LTLNode("THEN", x, y);
                    }
                }
                case "if": {
                    idx_separator = sentence.indexOf(new Word("then")) != -1 ? sentence.indexOf(new Word("then")) : sentence.indexOf(new Word(","));
                    if (idx_separator != -1) {
                        LTLNode x = generateLTLTree(sentence.subList(1, idx_separator));
                        LTLNode y = generateLTLTree(sentence.subList(idx_separator + 1, sentence.size()));
                        return new LTLNode("IF", x, y);
                    }
                }
                default:
                    break;
            }
        }
        return new LTLNode(sentence);
    }
    
    
    /**
     * Implementation of the Null-Element-Restoration technique. Given a
     * sentence, it performs a dependency parsing (using Stanford NLP
     * DependencyParser), then if there is no subject in the sentence it add it.
     * (In BabyAI env the subject is always "the agent").
     *
     * @param sentence input sentence
     * @param parser StanfordDepParser
     * @return the input sentence with a subject
     */
    public static List<TaggedWord> null_restoration(List<TaggedWord> sentence, DependencyParser parser) {
        //Dependency parsing.
        GrammaticalStructure gs = parser.predict(sentence);

        //Search for the subject.
        boolean hasSubject = false;
        Collection<TypedDependency> dependencies = gs.allTypedDependencies();
        for (TypedDependency dep : dependencies) {
            if (dep.toString().contains("nsubj")) {
                hasSubject = true;
            }
        }

        //If there is no subject then add it.
        if (!hasSubject) {
            sentence.add(0, new TaggedWord("agent", "NN"));
            sentence.add(0, new TaggedWord("the", "DT"));
        }
        return sentence;
    }
    
    
    /**
     * Verb extraction method. It returns the verb (if any) of a sentence in
     * POSTagged form.
     *
     * @param list input sentence in PosTagged form (each item is in the form
     * word/POS eg. the/DT).
     * @return the verb of the sentene (eg. [the/DT, agent/NN, pick/VB, it/PRP]
     * -> pick).
     */
    public static String get_tagged_sentence_verb(List<TaggedWord> list){
        for(TaggedWord word : list){
                if(word.tag().contains("VB")){
                    return word.value();
                }
        }
        return null;
    }    
    
    
    /**
     * Extracts the WordNet sensekey related to the input word
     *
     * @param pos WordNet Pos family
     * @param word word = input word
     * @return list of all the senses of the word
     * @throws net.didion.jwnl.JWNLException
     */
    public static List<String> get_sense_keys(POS pos, String word) throws JWNLException{
        ArrayList<String> sensekeys = new ArrayList<>(); 
        Dictionary dict = Dictionary.getInstance(); 
        IndexWord verbIndexWord = dict.lookupIndexWord(pos, word);
            Synset[] senses = verbIndexWord.getSenses();
            for(Synset syn : senses){
                sensekeys.add(dict.getSenseKey(syn.getOffset(), word).replace("::", ""));             
            }
        return sensekeys;
    }   
    
    
    /**
     * Extracts all the roles related of a VerbNet class
     *
     * @param verbclass VerbNet class
     * @return list of roles
     */
    public static List<String> get_class_roles(IVerbClass verbclass) {
        List<String> roles = new ArrayList<>();
        verbclass.getThematicRoles().forEach((role) -> {
            roles.add(role.getType().getID());
        });
        return roles;
    }  
    
    
    /**
     * Check if a given verb (in form of its senses) belongs to a VerbNet classù
     *
     * @param sensekeys list of senses of a word
     * @param verbclass VerbNet class
     * @return true if it belongs to the class
     */
    public static boolean belong_to_class(List<String> sensekeys, IVerbClass verbclass) {
        // for each member of the class check if they contains at least one of the senses
        List<IMember> members = verbclass.getMembers();
        for (IMember member : members) {
            Set<IWordnetKey> set = member.getWordnetTypes().keySet();
            for (IWordnetKey key : set) {
                if (sensekeys.contains(key.toString())) {
                    return true;
                }
            }
        }
        return false;
    }  
    
    
    /**
     * Extraction of the template of a frame. This template has the form of a
     * list of FrameItem where each item represent one of the member of the
     * VerbNet frame. The values of the FrameItems are empty.
     *
     * @param frame VerbNet frame
     * @param classRoles list of VerbClass roles
     * @param verb input verb of the sentence
     * @return list of FrameItems representing the FrameTemplate
     */
    public static List<FrameItem> build_frame_template(IFrame frame, List<String> classRoles, String verb) {
        // FrameStructure extractions (eg. NN V NN)
        String frame_id = frame.getPrimaryType().getID();
        String[] frame_vec = frame_id.split(" ");
        ISyntaxDesc syntax = frame.getSyntax();
        List<FrameItem> frameList = new ArrayList<>();

        // For each element of the frame create its FrameItem (Only the verbItem 
        // is filled with its value.)
        int i = 0;
        List<ISyntaxArgDesc> preList = syntax.getPreVerbDescriptors();
        for (ISyntaxArgDesc pre : preList) {
            String role = pre.getValue();
            if (!classRoles.contains(role) || role == null) {
                continue;
            }
            String tag = frame_vec[i++].split("\\.")[0];
            frameList.add(new FrameItem(tag, role, null));
        }
        frameList.add(new FrameItem("V", "verb", verb));
        i++;
        List<ISyntaxArgDesc> postList = syntax.getPostVerbDescriptors();
        for (ISyntaxArgDesc post : postList) {
            String role = post.getValue();
            if (!classRoles.contains(role) || role == null) {
                continue;
            }
            String tag = frame_vec[i++].split("\\.")[0];
            frameList.add(new FrameItem(tag, role, null));
        }
        return frameList;
    }
  
    
    /**
     * Given an LTL operator and a list of atomic formulas it combines them
     * togheter.
     *
     * @param type LTLOperator (eg. AND, OR...)
     * @param ltl_formulas List of LTL formulas
     * @return An ltl formula.
     */
    public static String build_formula(String type, List<String> ltl_formulas){
        String ris = "";
        switch(type){
            case "WHEN":
                ris =  LTL_template.always(LTL_template.implication(ltl_formulas.get(0), LTL_template.immediately(ltl_formulas.get(1))));
                break;
            case "THEN":
            case "IF":
                ris =  LTL_template.always(LTL_template.implication(ltl_formulas.get(0), LTL_template.eventually(ltl_formulas.get(1))));
                break;
            case "AND":
                ris =  LTL_template.and(ltl_formulas.get(0), ltl_formulas.get(1));
                break;
            case "OR":
                ris =  LTL_template.or(ltl_formulas.get(0), ltl_formulas.get(1));
                break;
            case "NOT":
                ris =  LTL_template.not(ltl_formulas.get(0));
                break;
            case "IMMEDIATELY":
                ris =  LTL_template.immediately(ltl_formulas.get(0));
                break;
            case "ALWAYS":
                ris =  LTL_template.always(ltl_formulas.get(0));
                break;
            case "UNTIL":
                ris =  LTL_template.until(ltl_formulas.get(0),ltl_formulas.get(1));
                break;
        }
        return ris;
    }
}

