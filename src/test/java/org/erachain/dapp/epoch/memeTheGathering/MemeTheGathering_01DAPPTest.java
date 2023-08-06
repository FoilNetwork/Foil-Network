package org.erachain.dapp.epoch.memeTheGathering;

import org.junit.Test;

import java.util.Random;

public class MemeTheGathering_01DAPPTest {

    @Test
    public void tt() {

        int count = 10000;
        int ii = count;
        Random rand = new Random();
        int res = 0;
        while (ii-- > 0) {
            int rareVal = rand.nextInt(Short.MAX_VALUE * 2);
            int rareRes = (int)((long)rareVal * 10000L / (long) (Short.MAX_VALUE * 2));
            if (rareRes < 3333)
                res++;
        }
        System.out.println( " res: " + res + " - " + (res * 100f / count) + "%");
    }

    @Test
    public void tt1() {

        int count = 10000;
        int setCount = 4;
        int ii = count;
        Random rand = new Random();
        int[] res = new int[5];
        while (ii-- > 0) {
            int charValue = rand.nextInt(Short.MAX_VALUE * 2);
            charValue = setCount * charValue / (2 * Short.MAX_VALUE);
            res[charValue]++;
        }
        System.out.println( " res: " + res[0] + " " + res[1] + " " + res[2] + " " + res[3] + " " + res[4]);

    }

}