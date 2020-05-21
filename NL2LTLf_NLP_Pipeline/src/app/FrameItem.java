package app;

/**
 * Class that models an element of a VerbNET frame. It just has three attributes
 * (Tag, Frame Role and it's value) and getters/setters methods
 *
 * @author Riccardo Ratini <riccardo.ratini>
 */
public class FrameItem {
    private String tag, role, value;
    
    public FrameItem(String tag, String role, String value){
        this.tag = tag;
        this.role = role;
        this.value = value;
    }

    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString(){
        return "(TAG:"+tag+"|ROLE:"+role+"|VALUE:"+value+")";
    }
}
