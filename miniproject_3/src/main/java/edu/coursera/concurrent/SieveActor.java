package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.Arrays;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 * <p>
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     * <p>
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        if (limit < 2) return 0;
        if (limit == 2) return 1;

        SieveActorActor actor = new SieveActorActor(2);
        finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                actor.send(i);
            }
        });

        SieveActorActor temp = actor;
        int numPrimes = 0;
        while (temp != null) {
            numPrimes += temp.getNumLocalPrimes();
            temp = temp.getNextActor();
        }

        return numPrimes;

        //actor.process(0); // stop the pipeline
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {

        private static final int MAX_LOCAL_PRIMES = 2000;
        private int numLocalPrimes;
        int[] localPrimes;
        private SieveActorActor nextActor;

        public SieveActorActor(int localPrime) {
            localPrimes = new int[MAX_LOCAL_PRIMES];
            numLocalPrimes = 1;
            localPrimes[0] = localPrime;
        }

        public int getNumLocalPrimes() {
            return numLocalPrimes;
        }


        /**
         * Process a single message sent to this actor.
         * <p>
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            int candidate = (Integer) msg;
//            if (num <= 0)
//                if (nextActor != null)
//                    nextActor.send(msg);
//            System.out.println("candidate: " + candidate);

            boolean localPrime = isLocalPrime(candidate);
            if (localPrime) {
                if (numLocalPrimes < MAX_LOCAL_PRIMES) {
                    localPrimes[numLocalPrimes] = candidate;
                    numLocalPrimes += 1;
                } else if (nextActor == null) {
                    nextActor = new SieveActorActor(candidate);
                } else {
                    nextActor.send(candidate);
                }

            }

        }

        private boolean isLocalPrime(int candidate) {
            return checkPrime(candidate, 0, numLocalPrimes);
        }

        private boolean checkPrime(int candidate, int startIndex, int endIndex) {
//            System.out.println(Arrays.toString(localPrimes));
//            System.out.println("startIndex: " + startIndex + ", endIndex: " + endIndex);
            for (int i = startIndex; i < endIndex; i++) {
                if (candidate % localPrimes[i] == 0)
                    return false;
            }
            return true;
        }

        public SieveActorActor getNextActor() {
            return nextActor;
        }
    }
}
