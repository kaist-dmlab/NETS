package utils;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is from Luan's work (http://infolab.usc.edu/Luan/Outlier/)
 * @author Luan
 */
public class MeasureMemoryThread extends Thread {
    public long maxMemory = 0;
    public void computeMemory() {
        Runtime.getRuntime().gc();
        long used = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        if(maxMemory < used)
            maxMemory = used;
    }

    @Override
    public void run() {
        while (true) {
            computeMemory();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(MeasureMemoryThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
