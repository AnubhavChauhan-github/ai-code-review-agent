
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ProductionRiskSample {

    public void startWorkers() {

        var executor = Executors.newCachedThreadPool();

        var queue =
                new LinkedBlockingQueue<String>();
    }
}
