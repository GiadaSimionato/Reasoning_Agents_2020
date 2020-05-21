package app;

import edu.mit.jverbnet.data.IFrame;
import edu.mit.jverbnet.data.IVerbClass;
import edu.mit.jverbnet.index.IVerbIndex;
import edu.mit.jverbnet.index.VerbIndex;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

/**
 * NL2LTL Translator class. Use transalte method to translate a sentence
 *
 * @author Riccardo Ratini <riccardo.ratini>
 */
public class NL2LTLTranslator {
    /**
     * Resources path
     */
    private final String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words-distsim.tagger";
    private final String grammarPath = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
    private final String[] options = {"-maxLength", "80", "-retainTmpSubcategories"};
    private final String pathToVerbnet = "/app/models/verbnet/";
    
    /**
     * EXTERNAL RESOURCES 
     * tagger                     : Stanford POS Tagger 
     * slem                       : Stanford Lemmatizer
     * dep_parser                 : Stanford Dependency parser
     * lex_parser                 : Stanford Semantic parser
     * verb_index                 : VerbNet index database
     */
    private final MaxentTagger tagger;
    private final StanfordLemmatizer slem;
    private final DependencyParser dep_parser;
    private final LexicalizedParser lex_parser;
    private final IVerbIndex verb_index;
    
    /**
     * Last direct object visited. Used for pronoun disambiguation
     */
    private String last_object = null;
    
    /**
     * Constructor
     * @throws MalformedURLException 
     */
    public NL2LTLTranslator() throws MalformedURLException{
        // Initializatons
        // POS Tagger 
        tagger = new MaxentTagger(taggerPath);
        // Lemmatizer
        slem = new StanfordLemmatizer();
        // Dependency Parser
        dep_parser = DependencyParser.loadFromModelFile(DependencyParser.DEFAULT_MODEL);;
        // Semantic Parser
        lex_parser = LexicalizedParser.loadModel(grammarPath, options);
        // WordNet
        WordNetUtils.configureJWordNet();
        // VerbNet
        URL url = getClass().getResource(pathToVerbnet);
        verb_index = new VerbIndex(url);
    }
    
    /**
     * Translate function.
     * @param text input sentence to translate into LTL
     * @return LTLf Formula
     * @throws IOException
     * @throws JWNLException 
     */
    public String translate(String text) throws IOException, JWNLException{
        last_object = null;
        
        //Lemmatize the input sentence
        List<HasWord> lem_sentence = slem.lemmatize(text);
        
        //Generate the sentence LTLf Operator Tree
        LTLNode root = NL2LTLUtils.generateLTLTree(lem_sentence);
        
        //Translate the LTL Tree into an LTLf formula
        verb_index.open();
        String result = _tranlsate(root, tagger, dep_parser, lex_parser, verb_index, this);
        verb_index.close();
        
        //return
        return result;
    }   
       
    /**
     * Recursion auxiliary function
     * 
     * @param node current LTLTSubtree root
     * @param tagger Stanford POS Tagger 
     * @param depParser Stanford Lemmatizer
     * @param lex_parser Stanford Semantic parser
     * @param verb_index VerbNet index database
     * @param translator NL2LTLTranslator itself
     * @return LTL Formula
     * @throws IOException
     * @throws JWNLException 
     */
    private static String _tranlsate(LTLNode node, MaxentTagger tagger, DependencyParser depParser, LexicalizedParser lex_parser, IVerbIndex verb_index, NL2LTLTranslator translator) throws IOException, JWNLException{
        // Base case
        // If the current node is a leaf (an atomic sentence)
        // Then generate the LTL formula of the sentence and return it;
        if(node.isLeaf()){
            // SENTENCE SEMANTIC TREE GENERATION:
            // Pos Tagging
            List<HasWord> sentence = node.getSentence();
            List<TaggedWord> tagged_sentence = tagger.tagSentence(sentence);            
            // Null-Element Restoraton
            List<TaggedWord> restored_sentence = NL2LTLUtils.null_restoration(tagged_sentence, depParser); 
            // semantic tree generation
            Tree parsed_sentence = lex_parser.parse(restored_sentence); 
            
            // BABYAI ACTION EXTRACTION:
            // Extracts the verb of the sentence
            String verb = NL2LTLUtils.get_tagged_sentence_verb(tagged_sentence);
            if(verb == null){
                System.out.println("Nessuna azione riconosciuta!!");
                return null;
            }
            // Extracts the BabyAI action related to the verb
            babyai_actions ACTION = LTL_template.get_action(verb);
            if(ACTION == babyai_actions.None){
                System.out.println("Azione "+verb+" non supportata!!");
                return null;
            }
            
            //BEST VerbNET FRAME EXTRACTION:
            // List of all the WordNET sensekeys of the verb
            List<String> sensekeys = NL2LTLUtils.get_sense_keys(POS.VERB, verb);
            // Best frame container       
            List<FrameItem> bestFrame = new ArrayList<>();
            // VerbNet Iterator
            Iterator<IVerbClass> iterator = verb_index.iteratorRoots(); 
            // Iterates over all the VerbNet verbclass and extract the best frame
            // that matches the semantic tree
            while(iterator.hasNext()){
                //Current VerbNet Class
                IVerbClass verbclass = iterator.next();
                              
                // verbclass roles
                List<String> roles = NL2LTLUtils.get_class_roles(verbclass);   
                
                // Check if the verb belongs to the current verbclass
                boolean belong = NL2LTLUtils.belong_to_class(sensekeys, verbclass);               
                if(belong) {
                    // current verbclass frames list
                    List<IFrame> frames = verbclass.getFrames();
                    // For each class frame, check if it matches with the tree and if
                    // it is the best frame (the frame with the greatest number of actors extracted
                    for (IFrame frame : frames) {
                        List<FrameItem> frame_template = NL2LTLUtils.build_frame_template(frame, roles, verb);
                        List<FrameItem> extractedFrame = matcher.match(parsed_sentence, frame_template, translator.last_object);
                        if (extractedFrame.size() == frame_template.size() && extractedFrame.size() > bestFrame.size()) {
                            // If it is the best frame update the variable and update the last direct object
                            bestFrame = extractedFrame;
                            translator.last_object = bestFrame.get(bestFrame.size()-1).getValue();
                        }
                    }
                }
            }
            
            //Finally convert the frame into an atomic LTLf Formula
            String formula = LTL_template.get_atomic_action(ACTION, bestFrame);
            if(formula == null){
                return null;
            }           
            return formula;
        }else{
            // Else Case
            // Visits the tree until it reach the leafs, build the atomic formulas
            // Then recursively combine togheder the formulas using the LTL Operators
            // Lower level formulas
            String x, y;
            List<String> list = new ArrayList<>();
            // Get the first lower level formula
            if(node.getX() != null){
                x = _tranlsate(node.getX(), tagger, depParser, lex_parser, verb_index, translator);
                if(x==null)return null;
                list.add(x);
            }
            // Get the second lower level formula (if any)
            if(node.getY() != null){
                y = _tranlsate(node.getY(), tagger, depParser, lex_parser, verb_index, translator);
                if(y==null)return null;
                list.add(y);
            }
            // Combine the formulas togheter and return it
            return NL2LTLUtils.build_formula(node.getType(), list);
        }
    }

    public static void main(String[] args) throws MalformedURLException, JWNLException, IOException {
        NL2LTLTranslator translator = new NL2LTLTranslator();
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()));
        String input_sentence = "go to the grey ball then go to the grey key"; 
        //String input_sentence = "go to the box and when you are at the box, open it";
        System.out.println("INPUT SENTENCE:   "+input_sentence); 
        System.out.println("LTL FORMULA TREE: "+translator.translate(input_sentence)); 
        System.out.println("===================================================");
        
        System.out.println(dtf.format(LocalDateTime.now()));
        input_sentence = "go to a box and immediately open it";       
        System.out.println("INPUT SENTENCE:   "+input_sentence);       
        System.out.println("LTL FORMULA TREE: "+translator.translate(input_sentence)); 
        System.out.println("===================================================");
        
        System.out.println(dtf.format(LocalDateTime.now()));
        input_sentence = "when you are at a box, pick up it";        
        System.out.println("INPUT SENTENCE:   "+input_sentence);       
        System.out.println("LTL FORMULA TREE: "+translator.translate(input_sentence));        
        System.out.println("===================================================");
        
        System.out.println(dtf.format(LocalDateTime.now()));
        input_sentence = "go to room_x and looks the object";
        System.out.println("INPUT SENTENCE:   "+input_sentence);        
        System.out.println("LTL FORMULA TREE: "+translator.translate(input_sentence));      
        System.out.println("==================================================="); 
    }
}
