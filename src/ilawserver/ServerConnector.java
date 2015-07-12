package ilawserver;

import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Prado Bognot
 */
public class ServerConnector {
    public static void main(String[] args){
        DatabaseConnector dc = new DatabaseConnector();
        //new BulbInformationChecker(dc);
        //new WritePowerAnalysis(dc);
        new SendScheduledLightEvents(dc);
    }
}
/*
 * BulbInformationChecker - Checks and corrects the state, mode and level of each lamp. 
 * Runs after 0 milliseconds on server start up and checks every 50000 milliseconds.
 */
class BulbInformationChecker{
    DatabaseConnector dc;
    BulbInformationChecker(DatabaseConnector dc){
        this.dc = dc;
    }    
    private ArrayList<String> getAllIpAddresses(){
        return dc.getAllIpAddresses();
    }   
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    final Runnable bulbinformationchecker = new Runnable(){    
        @Override
        public void run(){
            int tasksMax = 16;
            int ipCounter = 0;
            boolean areInfoChecksProcessing = true;

            ExecutorService executor = Executors.newFixedThreadPool(tasksMax);
            Future[] task;
            task = new Future[tasksMax];            
            
            System.out.println("***BulbInformationChecker(): Start");
            for(String ip : getAllIpAddresses()){
                task[ipCounter++] = executor.submit(new BulbInfoCheckThread(dc, ip));   
                
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }                
            }
            
            while(areInfoChecksProcessing) {
                boolean isDone = true;

                for(int i=0; i<ipCounter; i++) {
                    Object status = 0;

                    try {
                        Thread.sleep(100);                 //1000 milliseconds is one second.
                        status = task[i].get();
                    } catch(ExecutionException ex) {
                        Thread.currentThread().interrupt();
                    } catch (InterruptedException ex) {
                        //todo
                    }

                    // if null the task has finished
                    if (status == null) {
                        System.out.println("***BulbInformationChecker(): Task[" + i + "] completed");
                    }
                    else {
                        // if it doesn't finish, wait
                        isDone = isDone && false;
                    }
                }

                if(isDone) {
                    areInfoChecksProcessing = false;
                }
            }    
            
            executor.shutdown();
            System.out.println("---------------------");
            try {
                // wait until all tasks are finished
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                //Logger.getLogger(BulbInformationChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("***BulbInformationChecker(): All tasks are finished!");
        }
    };
    final ScheduledFuture<?> writer = scheduler.scheduleAtFixedRate(bulbinformationchecker, 1, 30, SECONDS);
}

class BulbInfoCheckThread implements Runnable {
    
    DatabaseConnector dc;
    private String ipAddress;
    
    BulbInfoCheckThread(DatabaseConnector dc, String ipAddress) {
        this.dc = dc;
        this.ipAddress = ipAddress;
    }

    @Override
    public void run() {
        try {
            System.out.println("***BulbInfoCheckThread(): " + ipAddress);
            String result;

            URL url = new URL("http://" + ipAddress + "/lightvalues.txt");
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String[] str = new String[3];

            while ((result = br.readLine()) != null) {
                str = result.split(",");
            }
            Lamp lamp = new Lamp(ipAddress, str[0], str[1], str[2]);
            dc.checkBulbConsistency(lamp);    
        } catch (Exception ex) {
            dc.changeStateToCnbr(ipAddress);
            System.out.println("***BulbInfoCheckThread(): The lamp is currently unreachable. Please check connections for " + ipAddress);
            System.out.println(ex);
        }        
    }
}

/*
 * WritePowerAnalysis - Retrieves and writes the power analysis of each lamp.
 * Runs after 250 seconds on server start up and checks every 5 seconds
 * 
 */
class WritePowerAnalysis{
    DatabaseConnector dc;
    WritePowerAnalysis(DatabaseConnector dc){
        this.dc = dc;
    }
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    final Runnable writepoweranalysis = new Runnable(){    
        @Override
        public void run(){
            int tasksMax = 16;
            int ipCounter = 0;
            boolean areInfoChecksProcessing = true;

            ExecutorService executor = Executors.newFixedThreadPool(tasksMax);
            Future[] task;
            task = new Future[tasksMax];                
            
            System.out.println("***WritePowerAnalysis(): Starting");
            for(String ip : dc.getAllIpAddresses()){
                task[ipCounter++] = executor.submit(new WritePowerAnalysisThread(dc, ip));   
                
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }                
            }
            
            while(areInfoChecksProcessing) {
                boolean isDone = true;

                for(int i=0; i<ipCounter; i++) {
                    Object status = 0;

                    try {
                        Thread.sleep(100);                 //1000 milliseconds is one second.
                        status = task[i].get();
                    } catch(ExecutionException ex) {
                        Thread.currentThread().interrupt();
                    } catch (InterruptedException ex) {
                        //todo
                    }

                    // if null the task has finished
                    if (status == null) {
                        System.out.println("***WritePowerAnalysis(): Task[" + i + "] completed");
                    }
                    else {
                        // if it doesn't finish, wait
                        isDone = isDone && false;
                    }
                }

                if(isDone) {
                    areInfoChecksProcessing = false;
                }
            }    
            
            executor.shutdown();
            System.out.println("---------------------");
            try {
                // wait until all tasks are finished
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                //Logger.getLogger(BulbInformationChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("***WritePowerAnalysis(): All tasks are finished!");
        }
    };
    final ScheduledFuture<?> writer = scheduler.scheduleAtFixedRate(writepoweranalysis, 10, 60, SECONDS);
}

class WritePowerAnalysisThread implements Runnable {
    
    DatabaseConnector dc;
    private String ipAddress;
    
    WritePowerAnalysisThread(DatabaseConnector dc, String ipAddress) {
        this.dc = dc;
        this.ipAddress = ipAddress;
    }

    @Override
    public void run() {
        //thread from power analysis
        try {
            System.out.println("***WritePowerAnalysisThread(): " + ipAddress);

            String urlForReadings = "http://" + ipAddress + "/send.php";
            URL url = new URL(urlForReadings);
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuilder sb = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            String str_url = sb.toString();

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(str_url);
            JSONArray array = (JSONArray)obj;

            for(int i = 0; i < array.size(); i++){
                JSONObject json = (JSONObject)array.get(i);
                String stat = json.get("stat").toString().trim();
                String pf = json.get("pf").toString().trim();
                String watts = json.get("watts").toString().trim();
                String va = json.get("va").toString().trim();
                String var = json.get("var").toString().trim();
                String volt = json.get("volt").toString().trim();
                String ampere = json.get("ampere").toString().trim();
                String timestamp = json.get("timestamp").toString().trim();

                if((stat.equals("InRange"))||(stat.equals("Overflw"))){
                    Readings readings = new Readings(ipAddress, stat, pf, watts, va,
                        var, volt, ampere, timestamp);
                    dc.writePowerAnalysis(readings);
                }
            }

        } catch(Exception e) {
            dc.changeStateToCnbr(ipAddress);
            System.out.println("***WritePowerAnalysis(): The lamp is currently unreachable. Please check connections for " + ipAddress);
            System.out.println(e);
        }        
    }
}

/*
 * SendScheduledLightEvents - Retrieves and sends lamp schedule to the Raspberry Pi 
 * Runs on the first :30 time interval on server start up and checks every 30 minutes
 */
class SendScheduledLightEvents{
    DatabaseConnector dc;
    Calendar serverStartTime;
    SendScheduledLightEvents(DatabaseConnector dc){
        this.dc = dc;
    }
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat sdf_time_2 = new SimpleDateFormat("HHmm");
    
    private ArrayList<Schedule> schedule_on = new ArrayList<>();
    private ArrayList<Schedule> schedule_off = new ArrayList<>();
    
    private int minuteSched = 1;
    
    final Runnable sendscheduledlightevents = new Runnable(){    
        @Override
        public void run(){
            System.out.println("***SendScheduledLightEvents()"); 
            List<ArrayList<Object>> on;
            List<ArrayList<Object>> off;
            List<ArrayList<Object>> changeBrightness;
            ArrayList<Integer> bulbids = new ArrayList<>();
            
            //Set the range of schedules for the lamp ON
            Calendar date_lower = Calendar.getInstance();
            date_lower.set(Calendar.SECOND, 0);
            date_lower.add(Calendar.SECOND, -(minuteSched * 60) / 2);
            
            Calendar date_upper = Calendar.getInstance();
            date_upper.set(Calendar.SECOND, 0);
            date_upper.add(Calendar.SECOND, (minuteSched * 60) / 2);            

            //Set the range of schedules for the lamp OFF
            Calendar end_lower = Calendar.getInstance();
            end_lower.set(Calendar.SECOND, 0);
            end_lower.add(Calendar.SECOND, -(minuteSched * 60) / 2);

            Calendar end_upper = Calendar.getInstance();
            end_upper.set(Calendar.SECOND, 0);
            end_upper.add(Calendar.SECOND, (minuteSched * 60) / 2);            
            
            changeBrightness = dc.getLampSchedule(
                    sdf_time.format(date_lower.getTime()), sdf_time.format(date_upper.getTime()), 0);

            int tasksMax = 16;
            int ipCounter = 0;
            boolean areInfoChecksProcessing = true;

            ExecutorService executor = Executors.newFixedThreadPool(tasksMax);
            Future[] task;
            task = new Future[tasksMax];                
            
            System.out.println("***SendScheduledLightEvents(): Looking for lights to Change Brightness"); 
            for(ArrayList<Object> ar: changeBrightness){ 
                int clusterid = (int) ar.get(0);
                int bulbid = (int) ar.get(1);
                int brightness = (int) ar.get(2);
                
                String ipaddress = ar.get(3).toString();
                String name = ar.get(4).toString();
                String activate_time = ar.get(5).toString();

                int day_of_week = (int) ar.get(6);
                
                bulbids.add(bulbid);
                
                task[ipCounter++] = executor.submit(new SendScheduledLightEventsThread(dc, serverStartTime, ipaddress, bulbid, brightness, activate_time, day_of_week));   
                
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }                  
            }       
            while(areInfoChecksProcessing) {
                boolean isDone = true;

                for(int i=0; i<ipCounter; i++) {
                    Object status = 0;

                    try {
                        Thread.sleep(100);                 //1000 milliseconds is one second.
                        status = task[i].get();
                    } catch(ExecutionException ex) {
                        Thread.currentThread().interrupt();
                    } catch (InterruptedException ex) {
                        //todo
                    }

                    // if null the task has finished
                    if (status == null) {
                        System.out.println("***WritePowerAnalysis(): Task[" + i + "] completed");
                    }
                    else {
                        // if it doesn't finish, wait
                        isDone = isDone && false;
                    }
                }

                if(isDone) {
                    areInfoChecksProcessing = false;
                }
            }    
            
            executor.shutdown();
            System.out.println("---------------------");
            try {
                // wait until all tasks are finished
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                //Logger.getLogger(BulbInformationChecker.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("***SendScheduledLightEvents(): End of function"); 
            
            setTime();
        }
    };
    
    private int setTime(){
        serverStartTime = Calendar.getInstance();
        System.out.println("Initial time for lamp scheduling: " + serverStartTime.getTime());
        
        int serverTime = serverStartTime.get(Calendar.MINUTE);
        //int minuteSched = 3;
        
        serverTime = serverTime % minuteSched;
        int minInterval = ((minuteSched - serverTime) * 60) - serverStartTime.get(Calendar.SECOND);
        System.out.println("seconds - " + minInterval);
        
        //serverStartTime.set(Calendar.MINUTE, 30);
        serverStartTime.add(Calendar.SECOND, minInterval);  
        
        serverStartTime.set(Calendar.SECOND, 0);
        System.out.println("setTime(): Next scheduled setting of lamps will run at " + serverStartTime.getTime());
        //return thirtyMinuteInterval;
        return minInterval;
    }
    
    final ScheduledFuture<?> writer = scheduler.scheduleAtFixedRate(sendscheduledlightevents, setTime(), minuteSched * 60, SECONDS);    

    private void sendLightBrightnessSchedule(ScheduleAlarm schedule){
        System.out.println("sendLightBrightnessSchedule()");
        try {
            Calendar currentTime = Calendar.getInstance();
            currentTime.set(Calendar.SECOND, 0);

            String ip = schedule.getIpaddress();
            int level = schedule.getBrightness();
            String state = "off";
            
            if((level >= 10) && (level <= 100)){
                state = "on";
                System.out.println("Turning on lights... level=" + level);
            }
            else if ((level >= 0) && (level < 10)) {
                level = 0;
                state = "off";
                System.out.println("Turning off lights...");
            }
            
            String mode = "control";
            //String url = "http://localhost:81/test3.php?id=" + level;
            String url = "http://" + ip + "/ilawcontrol.php?state=" + state + "&level=" + level + "&mode=" + mode;

            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            StringBuilder response;

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                }
            }
            System.out.println("sendLightBrightnessSchedule(): Next scheduled setting of lamps will run at " + serverStartTime.getTime());

        } catch (Exception ex) {
            dc.changeStateToCnbr(schedule.getIpaddress());
            System.out.println("Failed to send schedule for lamp " + schedule.getIpaddress() + ". Please check connections.");
        }
    }    
}

class SendScheduledLightEventsThread implements Runnable {
    
    DatabaseConnector dc;
    private Calendar startTime;
    
    private String ipAddress;
    private int bulbid;
    private int brightness;
    private String activate_time;
    private int day_of_week;
    
    SendScheduledLightEventsThread(DatabaseConnector dc, Calendar serverStartTime, String ipAddress,
            int bulbid, int brightness, String activate_time, int day_of_week) {
        this.dc = dc;
        this.startTime = serverStartTime;
        
        this.ipAddress = ipAddress;
        this.bulbid = bulbid;
        this.brightness = brightness;
        this.activate_time = activate_time;
        this.day_of_week = day_of_week;
    }

    @Override
    public void run() {
        ScheduleAlarm sched = new ScheduleAlarm(ipAddress, bulbid, brightness, activate_time, day_of_week);
        sendLightBrightnessScheduleThread(sched, startTime);    
    }
    
    private void sendLightBrightnessScheduleThread(ScheduleAlarm schedule, Calendar serverStartTime){
        System.out.println("sendLightBrightnessSchedule()");
        try {
            Calendar currentTime = Calendar.getInstance();
            currentTime.set(Calendar.SECOND, 0);

            String ip = schedule.getIpaddress();
            int level = schedule.getBrightness();
            String state = "off";
            
            if((level >= 10) && (level <= 100)){
                state = "on";
                System.out.println("Turning on lights... level=" + level);
            }
            else if ((level >= 0) && (level < 10)) {
                level = 0;
                state = "off";
                System.out.println("Turning off lights...");
            }
            
            String mode = "control";
            //String url = "http://localhost:81/test3.php?id=" + level;
            String url = "http://" + ip + "/ilawcontrol.php?state=" + state + "&level=" + level + "&mode=" + mode;

            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            StringBuilder response;

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                }
            }
            System.out.println("sendLightBrightnessSchedule(): Next scheduled setting of lamps will run at " + serverStartTime.getTime());

        } catch (Exception ex) {
            dc.changeStateToCnbr(schedule.getIpaddress());
            System.out.println("Failed to send schedule for lamp " + schedule.getIpaddress() + ". Please check connections.");
        }
    }        
}

/*
 * DatabaseConnector - Directly connects to the database
 */
class DatabaseConnector {
    DataSource ds;
    DatabaseConnector(){
        ds = new DataSource();
        this.connectToDatabase();
    }
    protected void connectToDatabase(){
        String url = "jdbc:mysql://localhost:3306/";
        String name = "ilaw";
        String user = "pi";//"root";
        String password = "raspberry";//"";
        ds.connectDB(url, name, user, password);   
    }
    protected ArrayList<String> getAllIpAddresses(){
        return ds.getAllIpAddresses();
    }
    protected void checkBulbConsistency(Lamp lamp){
        String ip = lamp.getIpaddress();
        String state = lamp.getState();
        String level = lamp.getLevel();
        String mode = lamp.getMode();

        System.out.println("Checking possible inconsistencies...");  
        String[] info = ds.getLampInfo(ip);
     
        if(!info[0].equalsIgnoreCase(state)){
            System.out.println("WARNING: INCONSISTENT BULB STATE!");
            System.out.println("\tInfo: Bulb is set to " + state + " while database is set to " + info[0]);
            System.out.println("Correcting inconsistencies...");
            ds.correctBulbState(ip, state);
        } 
        System.out.println("Consistent bulb state...");
        
        if(!info[1].equalsIgnoreCase(level)){
            System.out.println("WARNING: INCONSISTENT BULB BRIGHTNESS LEVEL!");
            System.out.println("\tInfo: Bulb level is set to " + level + " while database is set to " + info[1]);
            System.out.println("Correcting inconsistencies...");
            ds.correctBulbLevel(ip, level);
        } 
        System.out.println("Consistent bulb level...");
        
        if(!info[2].equalsIgnoreCase(mode)){
            System.out.println("WARNING: INCONSISTENT BULB MODE!");
            System.out.println("\tInfo: Bulb mode is set to " + mode + " while database is set to " + info[2]);
            System.out.println("Correcting inconsistencies...");
            ds.correctBulbMode(ip, mode);
        } 
        System.out.println("Consistent bulb mode...");
    }  
    protected void writePowerAnalysis(Readings readings) throws Exception{    
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        ds.writePowerAnalysis(readings.getIp(), readings.getStat(), readings.getWatts(), readings.getVa(),
                readings.getVar(), readings.getPf(), readings.getPf(), readings.getAmpere(), sdf.format(sdf.parse(readings.getTimestamp())));
    }

     protected List<ArrayList<Object>> getLampSchedule(
            String activate_time_bottom_range, String activate_time_upper_range, int day_of_week){    
        
        System.out.println("Activation time between - " + activate_time_bottom_range + " and " + activate_time_upper_range); 
        System.out.println("Day of Week - " + day_of_week);
     
        return ds.getLampScheduleAlarm(
        activate_time_bottom_range, activate_time_upper_range, day_of_week);
    }
    
    protected void changeStateToCnbr(String ipaddress){
        if(!ipaddress.isEmpty()){
            ds.setUnreachableBulbInfo(ipaddress);
        }
    }
    
    protected void disconnectDatabase(){
        ds.close(null, null, true);
    }
}
