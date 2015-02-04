package teedjay;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * Testing simple Hystrix functions
 *
 * @author thore
 */
public class CommandFirstSteps extends HystrixCommand<String> {

    private final String name;
    private final int timeout;

    // defaults to 3 seconds timeout
    public CommandFirstSteps(String name) {
        this(name, 3000);
    }

    // user specified timeout
    public CommandFirstSteps(String name, int timeout) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationThreadTimeoutInMilliseconds(timeout)));
        System.out.println("Using timout " + timeout);
        this.name = name;
        this.timeout = timeout;
    }

    @Override
    protected String run() {
        // a real example would do work like a network call here, we just wait a while
        long milliseconds = (long) (1000.0 * Math.random()); // max 1-2 seconds workload
        try {
            Thread.sleep(10 + milliseconds);
        } catch (InterruptedException ex) {
            System.out.println("Someone disturbed my sleep : " + ex.getMessage());
        }
        return "Hello " + name + "!";
    }

    @Override
    protected String getFallback() {
        return "TimedOut " + name + ", waiting " + timeout + "ms was probably too short";
    }

}