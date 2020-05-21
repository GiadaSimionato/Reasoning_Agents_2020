package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

/**
 * Utility methods for WordNet.
 * @author Riccardo Ratini <riccardo.ratini>
 */
public class WordNetUtils {
    /**
     * Load and initialize the WordNet dictionary
     */
    public static void configureJWordNet() {
        try {
            JWNL.initialize(new FileInputStream("src/app/models/file_properties.xml"));
        } catch (FileNotFoundException | JWNLException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }    
    
    /**
     * Return WordNet POS Family IDs
     * @param pos WordNet POS Family
     * @return POS Family ID
     */
    public static int getPosID(POS pos){
        if(pos == POS.NOUN)return 1;
        else if(pos == POS.VERB)return 2;
        else if(pos == POS.ADJECTIVE)return 3;
        else if(pos == POS.ADVERB)return 4;
        else return 5;
    }
}
