package ilawserver;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Kana Antonio
 * 
 * Class info: Class for the poweranalyzer accessed from the arduino XML
 */

public class Readings {
    private String ip;
    private String stat;
    private String watts;
    private String va;
    private String var;
    private String pf;
    private String volt;
    private String ampere;
    private String timestamp;
    public Readings(){
        
    }
    
    public Readings(String ip, String stat,String pf, String watts, String va, String var, String volt, String ampere, String timestamp){
        this.ip = ip;  
        this.stat = stat;
        this.watts = watts;
        this.va = va;
        this.var = var;
        this.pf = pf;
        this.volt = volt;
        this.ampere = ampere;  
        this.timestamp = timestamp;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return the stat
     */
    public String getStat() {
        return stat;
    }

    /**
     * @return the watts
     */
    public String getWatts() {
        return watts;
    }

    /**
     * @return the va
     */
    public String getVa() {
        return va;
    }

    /**
     * @return the var
     */
    public String getVar() {
        return var;
    }

    /**
     * @return the pf
     */
    public String getPf() {
        return pf;
    }

    /**
     * @return the volt
     */
    public String getVolt() {
        return volt;
    }

    /**
     * @return the ampere
     */
    public String getAmpere() {
        return ampere;
    }
    
    /**
     * @return the ampere
     */
    public String getTimestamp() {
        return timestamp;
    }
}
