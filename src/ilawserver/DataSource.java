package ilawserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kana Antonio
 */
public class DataSource {    
   
    private Connection connect = null;
    public DataSource(){ }

    protected void connectDB(String url, String name, String user, String password){
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connect = DriverManager.getConnection(url + name, user, password);
            System.out.println("Connection to database established successfully.");
            
        } catch (Exception ex) {
            System.out.println("An error occured: Unable to connect to the database." + ex);
        }
    }
    /*
     * General Bulb Information
     */  
    protected String getBulbId(String ipaddress){
        //System.out.println("Attempting to get bulb id...");
        String bulbid = "";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connect.prepareStatement("SELECT DISTINCT bulbid FROM bulb WHERE ipaddress=?");
            preparedStatement.setString(1, ipaddress);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                bulbid = resultSet.getString("bulbid");
            }
        } catch (SQLException e) {
            System.out.println("An error occured:   " + e);
        } finally {
            close(preparedStatement, resultSet, false);
        }
        return bulbid;
    }
    
    protected String[] getLampInfo(String ip){
        String[] info = new String[3];
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connect.prepareStatement("SELECT DISTINCT state, currbrightness, mode FROM bulb WHERE ipaddress=?");
            preparedStatement.setString(1, ip);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                info[0] = resultSet.getString("state");
                info[1] = resultSet.getString("currbrightness");
                info[2] = resultSet.getString("mode");
            }
            
        } catch (Exception e) {
            System.out.println("An error occured:   " + e);
        } finally {
            close(preparedStatement, resultSet, false);
        }
        return info;      
    }
    protected ArrayList<String> getAllIpAddresses(){
        ArrayList<String> list = new ArrayList<>();
        
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;      
        
        try{
            preparedStatement = connect.prepareStatement("SELECT DISTINCT ipaddress FROM bulb;");
            resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next()){
                list.add(resultSet.getString("ipaddress"));
            }
        } catch (SQLException e){
            System.out.println("An error occured:   " + e);
        } finally {
            close(preparedStatement, resultSet, false);
        }
        return list;       
    }
    
    /*
     * Readings for Power Analysis
     */
    protected void writePowerAnalysis(String ipaddress, String stat, String watts, String va,
            String var, String pf, String volt, String ampere, String timestamp){
        System.out.println("Attempting to write in database...");
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {     
            preparedStatement = connect.prepareStatement("INSERT INTO poweranalyzer "
                    + "VALUES(?,?,?,?,?,?,?,?,STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'))");

            preparedStatement.setString(1, getBulbId(ipaddress));
            preparedStatement.setString(2, stat);
            preparedStatement.setString(3, watts);
            preparedStatement.setString(4, va);
            preparedStatement.setString(5, var);
            preparedStatement.setString(6, pf);
            preparedStatement.setString(7, volt);
            preparedStatement.setString(8, ampere);
            preparedStatement.setString(9, timestamp);
            preparedStatement.executeUpdate();
            
            System.out.println("Writing in database result: success");
        } catch (Exception e) {
            System.out.println("Writing in database result: \n\tAn error occured:   " + e);
        } finally {
            close(preparedStatement, resultSet, false);
        }
    }
    /*
     * Lamp Checking and Update
     */
    protected boolean checkState(String ip){
        System.out.println("Checking bulb state...");
        
        String info = "";
        
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connect.prepareStatement("SELECT DISTINCT state FROM bulb WHERE ipaddress=?");
            preparedStatement.setString(1, ip);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                info = resultSet.getString("state");
            }
            
        } catch (Exception e) {
            System.out.println("An error occured:   " + e);
        } finally {
            close(preparedStatement, resultSet, false);
        }
        
        if(info.equals("off")){
            return false;
        }
        return true;
    }    
    
    protected void setUnreachableBulbInfo(String ipaddress){
        System.out.println("Updating Unreachable Bulb Information: " + ipaddress);
        PreparedStatement preparedStatement = null;
        
        try {    
            preparedStatement = connect.prepareStatement("UPDATE bulb SET state = 'cnbr' WHERE bulbid = ?");
            preparedStatement.setString(1, getBulbId(ipaddress));
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Writing in database result: \n\tAn error occured:   " + e);
        } finally {
            close(preparedStatement, null, false);
        }       
    }
    
    protected void correctBulbState(String ipaddress, String state){
        PreparedStatement preparedStatement = null;
        
        try {  
            preparedStatement = connect.prepareStatement("UPDATE bulb SET state = ? WHERE bulbid = ?");
            preparedStatement.setString(1, state);
            preparedStatement.setString(2, getBulbId(ipaddress));
            preparedStatement.executeUpdate();
            
        } catch (Exception e) {
            System.out.println("Writing in database result: \n\tAn error occured:   " + e);
        } finally {
            close(preparedStatement, null, false);
        }       
    }
    
    protected void correctBulbLevel(String ipaddress, String level){
        PreparedStatement preparedStatement = null;
        
        try {
            preparedStatement = connect.prepareStatement("UPDATE bulb SET currbrightness = ? WHERE bulbid = ?");
            preparedStatement.setString(1, level);
            preparedStatement.setString(2, getBulbId(ipaddress));
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Writing in database result: \n\tAn error occured:   " + e);
        } finally {
            close(preparedStatement, null, false);
        }       
    }
    
    protected void correctBulbMode(String ipaddress, String mode){
        PreparedStatement preparedStatement = null;
        try {  
            preparedStatement = connect.prepareStatement("UPDATE bulb SET mode = ? WHERE bulbid = ?");
            preparedStatement.setString(1, mode);
            preparedStatement.setString(2, getBulbId(ipaddress));
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Writing in database result: \n\tAn error occured:   " + e);
        } finally {
            close(preparedStatement, null, false);
        }              
    }
    
    /*
     * Scheduling 
     */
    protected List<ArrayList<Object>> getLampSchedule_on(
            String start_lower_date, String start_upper_date, 
            String start_lower_time, String start_upper_time){       
        System.out.println("Getting Lamp Schedule ON for date " + start_lower_time);
        List<ArrayList<Object>> schedule = new ArrayList();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try{
            /*
            preparedStatement = connect.prepareStatement("SELECT clusterid, bulbid, brightness, \n" +
                "ipaddress, name, start_date, end_date, start_time, end_time \n" +
                "FROM sched_cluster\n" +
                "JOIN schedule USING (scheduleid)\n" +
                "JOIN cluster_bulb USING (clusterid)\n" +
                "JOIN bulb USING (bulbid) \n" +
                "WHERE start_date = '" + start_lower_date + "' and start_time = '" + start_lower_time + "'");    
            */
            
            preparedStatement = connect.prepareStatement("SELECT DISTINCT clusterid, bulbid, brightness, \n" +
                "ipaddress, name, start_date, end_date, start_time, end_time \n" +
                "FROM sched_cluster\n" +
                "JOIN schedule USING (scheduleid)\n" +
                "JOIN cluster_bulb USING (clusterid)\n" +
                "JOIN bulb USING (bulbid) \n" +
                "WHERE start_date BETWEEN '" + start_lower_date + "' AND '" + start_upper_date + "' \n" +
                "AND start_time BETWEEN '" + start_lower_time + "' AND '" + start_upper_time + "'"); 
            
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {  
                ArrayList<Object> str = new ArrayList();
                
                int _clusterid = resultSet.getInt("clusterid");
                int _bulbid = resultSet.getInt("bulbid");
                int _brightness = resultSet.getInt("brightness");
                String _ipaddress = resultSet.getString("ipaddress");
                String _name = resultSet.getString("name");
                String _start_date = resultSet.getString("start_date");
                String _end_date = resultSet.getString("end_date");
                String _start_time = resultSet.getString("start_time");
                String _end_time = resultSet.getString("end_time");
                
                str.add(_clusterid);
                str.add(_bulbid);
                str.add(_brightness);
                str.add(_ipaddress);
                str.add(_name);
                str.add(_start_date);
                str.add(_end_date);
                str.add(_start_time);
                str.add(_end_time);
                
                schedule.add(str);
            }
        } catch (Exception e){
            
        } finally {
            close(preparedStatement, resultSet, false);
        }
        return schedule;
    }   
    protected List<ArrayList<Object>> getLampSchedule_off(
            String end_lower_date, String end_upper_date, 
            String end_lower_time, String end_upper_time){       
        System.out.println("Getting Lamp Schedule OFF for date " + end_lower_time);
        List<ArrayList<Object>> schedule = new ArrayList();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try{
            /*
            preparedStatement = connect.prepareStatement("SELECT clusterid, bulbid, brightness, \n" +
                "ipaddress, name, start_date, end_date, start_time, end_time \n" +
                "FROM sched_cluster\n" +
                "JOIN schedule USING (scheduleid)\n" +
                "JOIN cluster_bulb USING (clusterid)\n" +
                "JOIN bulb USING (bulbid) \n" +
                "WHERE end_date = '" + end_lower_date + "' and end_time = '" + end_lower_time + "'");    
            */
            
            preparedStatement = connect.prepareStatement("SELECT DISTINCT clusterid, bulbid, brightness, \n" +
                "ipaddress, name, start_date, end_date, start_time, end_time \n" +
                "FROM sched_cluster\n" +
                "JOIN schedule USING (scheduleid)\n" +
                "JOIN cluster_bulb USING (clusterid)\n" +
                "JOIN bulb USING (bulbid) \n" +
                "WHERE end_date BETWEEN '" + end_lower_date + "' AND '" + end_upper_date + "' \n" +
                "AND end_time BETWEEN '" + end_lower_time + "' AND '" + end_upper_time + "'");            
            
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {  
                ArrayList<Object> str = new ArrayList();
                
                int _clusterid = resultSet.getInt("clusterid");
                int _bulbid = resultSet.getInt("bulbid");
                int _brightness = resultSet.getInt("brightness");
                String _ipaddress = resultSet.getString("ipaddress");
                String _name = resultSet.getString("name");
                String _start_date = resultSet.getString("start_date");
                String _end_date = resultSet.getString("end_date");
                String _start_time = resultSet.getString("start_time");
                String _end_time = resultSet.getString("end_time");
                
                str.add(_clusterid);
                str.add(_bulbid);
                str.add(_brightness);
                str.add(_ipaddress);
                str.add(_name);
                str.add(_start_date);
                str.add(_end_date);
                str.add(_start_time);
                str.add(_end_time);
                
                schedule.add(str);
            }
        } catch (Exception e){
            
        } finally {
            close(preparedStatement, resultSet, false);
        }
        return schedule;
    }   

    protected void close(PreparedStatement ps, ResultSet rs, boolean disconnectDatabase){
        try {
            if(ps != null){
                ps.close();
            }
            
            if(rs != null){
                rs.close();
            }
            if(disconnectDatabase){
               System.out.println("Closing database connection...");
               if (connect != null) {
                   connect.close();
               }               
            }

        } catch (Exception e) {
            System.out.println("An error occured while closing the database connection. " + e);
        }
    }
    
    
}
