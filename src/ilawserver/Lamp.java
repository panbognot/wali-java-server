/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ilawserver;

/**
 *
 * @author Kana Antonio
 * 
 * Class info: Class for the bulb information accessed from the arduino XML
 */
public class Lamp {
    private String ipaddress;
    private String state;
    private String level;
    private String mode;
    
    public Lamp(){
        
    }
    
    public Lamp(String ipaddress, String state, String level, String mode){
        this.ipaddress = ipaddress;
        this.state = state;
        this.level = level;
        this.mode = mode;
    }

    /**
     * @return the ipaddress
     */
    public String getIpaddress() {
        return ipaddress;
    }
    /**
     * @return the light
     */
    public String getState() {
        return state;
    }
    
    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }
}
