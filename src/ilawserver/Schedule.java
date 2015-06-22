package ilawserver;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Kana Antonio
 */
public class Schedule {
    private String ipaddress;
    private String mode;
    private int bulbid;
    private int brightness;
    private String start_date;
    private String end_date;
    private String start_time;
    private String end_time;


    public Schedule(){
        
    }
    public Schedule(String ipaddress, int bulbid, int brightness, String start_date, String end_date, 
            String start_time, String end_time){
        this.ipaddress = ipaddress;
        this.bulbid = bulbid;
        this.brightness = brightness;
        this.start_date = start_date;
        this.end_date = end_date;
        this.start_time = start_time;
        this.end_time = end_time;
    }
    /**
     * @return the start_date
     */
    public String getStart_date() {
        return start_date;
    }

    /**
     * @param start_date the start_date to set
     */
    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    /**
     * @return the end_date
     */
    public String getEnd_date() {
        return end_date;
    }

    /**
     * @param end_date the end_date to set
     */
    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    /**
     * @return the start_time
     */
    public String getStart_time() {
        return start_time;
    }

    /**
     * @param start_time the start_time to set
     */
    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    /**
     * @return the end_time
     */
    public String getEnd_time() {
        return end_time;
    }

    /**
     * @param end_time the end_time to set
     */
    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    /**
     * @return the brightness
     */
    public int getBrightness() {
        return brightness;
    }

    /**
     * @param brightness the brightness to set
     */
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    /**
     * @return the ipaddress
     */
    public String getIpaddress() {
        return ipaddress;
    }

    /**
     * @param ipaddress the ipaddress to set
     */
    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    /**
     * @return the bulbid
     */
    public int getBulbid() {
        return bulbid;
    }

    /**
     * @param bulbid the bulbid to set
     */
    public void setBulbid(int bulbid) {
        this.bulbid = bulbid;
    }

        /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param bulbid the mode to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }
    
}
