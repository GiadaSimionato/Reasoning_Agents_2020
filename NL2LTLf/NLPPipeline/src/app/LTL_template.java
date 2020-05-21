package app;

import java.util.List;

/**
 * Utility methods used for the generation of the textual form of LTL formulas
 * @author Riccardo Ratini <riccardo.ratini>
 */
public class LTL_template {
    /*
    LTL Symbols constants
    */
    static final String NOT = "!";
    static final String AND = "&";
    static final String OR = "|";
    static final String IMPLICATION = "->";
    static final String EQUIVALENCE = "<->";
    static final String NEXT = "X";
    static final String WEAK = "WX";
    static final String UNTIL = "U";
    static final String RELEASE = "R";
    static final String EVENTUALLY = "F";
    static final String ALWAYS = "G";
    
    /*
    LTL Operators functions
    */   
    /** 
     * Adds spaces around the input text
     * @param text input text     
     * @return input text with spaces
     */
    public static String space(String text){return " " + text + " ";}
    
    /**
     * Adds brackets around the input text
     * @param text input text
     * @return input text with brackets
     */
    public static String parens(String text){ return "("+text+")";}
    
    /**
     * Implements the AND operator.
     * NB according to the reference paper the operator is implemented as
     * "F(props1 & F(props2))" 
     * @param props1 first proposition
     * @param props2 second proposition
     * @return  F(props1 & F(props2))
     */
    public static String and(String props1, String props2){ return eventually(parens(props1 + space(AND) + eventually(props2)));}
    
    /**
     * Implements the OR operator.
     * NB according to the reference paper the operator is implemented as
     * "F(props1) | F(props2)"
     * @param props1 first proposition
     * @param props2 second proposition
     * @return F(props1) | F(props2)
     */
    public static String or(String props1, String props2){ return eventually(parens(props1) + space(OR) + eventually(parens(props2))); }
    
    /**
     * Implements the NOT operator.
     * NB according to the reference paper the operator is implemented as
     * "!text"
     * @param text propositon
     * @return !text
     */
    public static String not(String text){ return NOT + text;} //return NOT + parens(text);}
    
    /**
     * Implements the Until operator.
     * NB according to the reference paper the operator is implemented as
     * "props1 U props2"
     * @param props1 first proposition
     * @param props2 second proposition
     * @return props1 U props2
     */
    public static String until(String props1, String props2){ return props1 + space(UNTIL) + props2;}
    
    /**
     * Implements the Always operator.
     * NB according to the reference paper the operator is implemented as
     * "G text"
     * @param text propositon
     * @return G text
     */
    public static String always(String text){ return ALWAYS + " "+text;} //return ALWAYS + parens(text);}
    
    /**
     * Implements the Eventually operator.
     * NB according to the reference paper the operator is implemented as
     * "F text"
     * @param text propositon
     * @return F text
     */
    public static String eventually(String text){ return EVENTUALLY + " "+text;} //return EVENTUALLY + parens(text);}
    
    /**
     * Implements the Immediately operator.
     * NB according to the reference paper the operator is implemented as
     * "X text"
     * @param text propositon
     * @return X text
     */
    public static String immediately(String text){ return NEXT + " "+text;} //return NEXT + parens(text);}
    
    /**
     * Implements the Alwats Immediately operator.
     * NB according to the reference paper the operator is implemented as
     * "G(F text)"
     * @param text propositon
     * @return G(F text)
     */
    public static String always_eventually(String text){ return ALWAYS +parens(EVENTUALLY + parens(text));}
    
    /**
     * Implements the Next operator.
     * NB according to the reference paper the operator is implemented as
     * "X text"
     * @param text propositon
     * @return X text
     */
    public static String next(String text){ return NEXT+ text;} //return NEXT+ parens(text);}
    
    /**
     * Implements the EQUIVALENCE operator. NB according to the reference paper
     * the operator is implemented as "(props1 <-> props2)"
     *
     * @param props1 first proposition
     * @param props2 second proposition
     * @return (props1 <-> props2)
     */
    public static String equivalence(String props1, String props2){ return parens(props1 + space(EQUIVALENCE) + props2);}
    
    /**
     * Implements the IMPLICATION operator. NB according to the reference paper
     * the operator is implemented as "(props1 -> props2)"
     *
     * @param props1 first proposition
     * @param props2 second proposition
     * @return (props1 -> props2)
     */
    public static String implication(String props1, String props2){ return parens(props1 + space(IMPLICATION) + props2);}
    
    
    /*
        BabyAIActions2LTLformula templates
    */
    /**
     * LTL formula of GO action.
     * @param to destination
     * @return "at_to"
     */
    public static String go_to_temp(String to){
        return "at_"+to.replace(" ", "_");
    }    
    
    /**
     * LTL formula of GO action.
     * @param from initial_location
     * @param to destination
     * @return "at_from -> F at_to"
     */
    public static String go_to_temp(String from, String to){
        return "at_"+from.replace(" ", "_") + " -> F at_"+to.replace(" ", "_");
    }
    
    /**
     * LTL formula of PICK action.
     * @param theme object to pick
     * @return "pick_theme"
     */
    public static String pick_temp(String theme){
        return "pick_"+theme.replace(" ", "_");
    }  
    
    /**
     * LTL formula of PICK action.
     * @param theme object to pick
     * @param where location
     * @return "at_where & X pick_theme"
     */
    public static String pick_temp(String theme, String where){
        return "at_"+where.replace(" ", "_") + " & X pick_"+theme.replace(" ", "_");
    }
    
    /**
     * LTL formula of DROP action.
     * @param theme object to drop
     * @return "drop_theme"
     */
    public static String drop_temp(String theme){
        return "drop_"+theme.replace(" ", "_");
    }    
    
    /**
     * LTL formula of DROP action.
     * @param theme object to drop
     * @param where location
     * @return "at_where & X drop_theme"
     */
    public static String drop_temp(String theme, String where){
        return "at_"+where.replace(" ", "_") + " & X drop_"+theme.replace(" ", "_");
    }
    
    /**
     * LTL formula of OPEN action.
     * @param theme object to open
     * @return "open_theme"
     */
    public static String open_temp(String theme){
        return "open_"+theme.replace(" ", "_");
    }   
    
    /**
     * LTL formula of OPEN action.
     * @param theme object to open
     * @param where location
     * @return "at_where & X open_theme"
     */
    public static String open_temp(String theme, String where){
        return "at_"+where.replace(" ", "_") + " & X open_"+theme.replace(" ", "_");
    }
    
    /**
     * LTL formula of CLOSE action.
     * @param theme object to close
     * @return "close_theme"
     */
    public static String close_temp(String theme){
        return "close_"+theme.replace(" ", "_");
    }   
    
    /**
     * LTL formula of CLOSE action.
     * @param theme object to close
     * @param where location
     * @return "at_where & X close_theme"
     */
    public static String close_temp(String theme, String where){
        return "at_"+where.replace(" ", "_") + " & X close_"+theme.replace(" ", "_");
    }
    
    /**
     * LTL formula of SEE action.
     * @param attribute object to see
     * @return "see_attribute"
     */
    public static String see_temp(String attribute){
        return "see_"+attribute.replace(" ", "_");  
    }
    
    /**
     * Return the BabyAI action (if any) related to the input verb
     * @param verb input verb
     * @return BabyAI action
     */
    public static babyai_actions get_action(String verb){
        switch (verb.toLowerCase()){
            case "go": case "move": case "be": 
                return babyai_actions.Go;
            case "open":
                return babyai_actions.Open;
            case "close":
                return babyai_actions.Close;
            case "pick":
                return babyai_actions.Pick;
            case "drop":
                return babyai_actions.Drop; 
            case "see": case "look": case "search":
                return babyai_actions.See;
            default:
                break;
        }
        return babyai_actions.None;
    } 
    
    /**
     * Translate a VerbNet Frame into an LTLFormula
     * @param ACTION BabyAI Action
     * @param frame VerbNetFrame
     * @return LTLFormula
     */
    public static String get_atomic_action(babyai_actions ACTION, List<FrameItem> frame){
        //According to the action and the frame size use the correct LTLAction template
        switch(ACTION){
                case Go:
                    switch (frame.size()) {
                        case 3:
                            {
                                String to = frame.get(2).getValue();
                                return LTL_template.go_to_temp(to);
                            }
                        case 4:
                            {
                                String from = frame.get(2).getValue();
                                String to = frame.get(3).getValue();
                                return LTL_template.go_to_temp(from, to);
                            }
                        default:
                            System.out.println("Frame "+frame+" non supportato");
                            return null;
                    }                  
                case Pick:
                    switch (frame.size()) {
                        case 3:
                            {
                                String theme = frame.get(2).getValue();
                                return LTL_template.pick_temp(theme);
                            }
                        case 4:
                            {
                                String theme = frame.get(2).getValue();
                                String to = frame.get(3).getValue();
                                return LTL_template.pick_temp(theme, to);
                            }
                        default:
                            System.out.println("Frame "+frame+" non supportato");
                            return null;
                    }       
                case Drop:
                    switch (frame.size()) {
                        case 3:
                            {
                                String theme = frame.get(2).getValue();
                                return LTL_template.drop_temp(theme);
                            }
                        case 4:
                            {
                                String theme = frame.get(2).getValue();
                                String to = frame.get(3).getValue();
                                return LTL_template.drop_temp(theme, to);
                            }
                        default:
                            System.out.println("Frame "+frame+" non supportato");
                            return null;
                    }           
                case Open:
                    switch (frame.size()) {
                        case 3:
                            {
                                String theme = frame.get(2).getValue();
                                return LTL_template.open_temp(theme);
                            }
                        case 4:
                            {
                                String theme = frame.get(2).getValue();
                                String to = frame.get(3).getValue();
                                return LTL_template.open_temp(theme, to);
                            }
                        default:
                            System.out.println("Frame "+frame+" non supportato");
                            return null;
                    }        
                case Close:
                    switch (frame.size()) {
                        case 3:
                            {
                                String theme = frame.get(2).getValue();
                                return LTL_template.close_temp(theme);
                            }
                        case 4:
                            {
                                String theme = frame.get(2).getValue();
                                String to = frame.get(3).getValue();
                                return LTL_template.close_temp(theme, to);
                            }
                        default:
                            System.out.println("Frame "+frame+" non supportato");
                            return null;
                    }
                case See:
                    switch (frame.size()) {
                        case 3:
                            {
                                String attribute = frame.get(2).getValue();
                                return LTL_template.see_temp(attribute);
                            }
                        default:
                            System.out.println("Frame "+frame+" non supportato");
                            return null;
                    }
            }
        return null;
    }   
}
