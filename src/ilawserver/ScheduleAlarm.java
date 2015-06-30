/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ilawserver;

/**
 *
 * @author PradoArturo
 */
public class ScheduleAlarm {
    private String ipaddress;
    private String mode;
    private int bulbid;
    private int brightness;
    private String activate_time;
    private int day_of_week;


    public ScheduleAlarm(){
        
    }
    public ScheduleAlarm(String ipaddress, int bulbid, int brightness, 
            String activate_time, int day_of_week){
        this.ipaddress = ipaddress;
        this.bulbid = bulbid;
        this.brightness = brightness;
        this.activate_time = activate_time;
        this.day_of_week = day_of_week;
    }

    /**
     * @return the activate_time
     */
    public String getActivate_time() {
        return activate_time;
    }

    /**
     * @param activate_time the start_time to set
     */
    public void setActivate_time(String activate_time) {
        this.activate_time = activate_time;
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
     * @return the brightness
     */
    public int getDayOfWeek() {
        return day_of_week;
    }

    /**
     * @param brightness the brightness to set
     */
    public void setDayOfWeek(int brightness) {
        this.day_of_week = day_of_week;
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
