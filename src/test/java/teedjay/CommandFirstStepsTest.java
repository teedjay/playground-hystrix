package teedjay;

import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.*;

/**
 * Testing some Hystrix functionality.
 */
public class CommandFirstStepsTest {

    @Test(priority = 1)
    public void testSynchronous() {
        assertEquals(new CommandFirstSteps("World").execute(), "Hello World!");
        assertEquals(new CommandFirstSteps("Bob").execute(), "Hello Bob!");
    }

    @Test(priority = 2, enabled = false)
    public void testSynchronousAndExpectAllToFail() {
        assertEquals(new CommandFirstSteps("World", 50).execute(), "TimedOut World, waiting 1ms was probably too short");
        assertEquals(new CommandFirstSteps("Bob", 50).execute(), "TimedOut Bob, waiting 1ms was probably too short");
    }

    @Test(priority = 3)
    public void testAsynchronous() throws ExecutionException, InterruptedException, TimeoutException {
        Future<String> f1 = new CommandFirstSteps("World").queue();
        Future<String> f2 = new CommandFirstSteps("Bob").queue();
        assertEquals(f1.get(5, TimeUnit.SECONDS), "Hello World!");
        assertEquals(f2.get(5, TimeUnit.SECONDS), "Hello Bob!");
    }

    @Test(priority = 4)
    public void testAsynchronousAndExpectTimeouts() throws ExecutionException, InterruptedException, TimeoutException {
        // this is expected to fail sometimes ... we have withExecutionIsolationThreadTimeoutInMilliseconds=3000
        List<Future<String>> waitingForTheFuture = new ArrayList<>();
        for (int t=0; t<100; t++) {
            waitingForTheFuture.add(new CommandFirstSteps("Test" + t, 1500).queue());
        }
        long howManyTimedOut = waitingForTheFuture.stream().filter(f -> timedOut(f)).count();
        assertTrue(howManyTimedOut > 50);
        assertTrue(howManyTimedOut < 100);
        System.out.println("Actual number timed out " + howManyTimedOut);
    }

    private static boolean timedOut(Future<String> future) {
        try {
            return future.get().contains("TimedOut"); // wait for completion / fallback value
        } catch (Exception ex) {
            return false;
        }
    }

}