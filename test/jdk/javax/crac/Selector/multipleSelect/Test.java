// Copyright 2013-2019 Azul Systems, Inc.  All Rights Reserved.
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//
// This code is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License version 2 only, as published by
// the Free Software Foundation.
//
// This code is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE.  See the GNU General Public License version 2 for more
// details (a copy is included in the LICENSE file that accompanied this code).
//
// You should have received a copy of the GNU General Public License version 2
// along with this work; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
//
// Please contact Azul Systems, 385 Moffett Park Drive, Suite 115, Sunnyvale,
// CA 94089 USA or visit www.azul.com if you need additional information or
// have any questions.


import java.nio.channels.Selector;
import java.io.IOException;
import java.util.Random;

import java.util.concurrent.atomic.AtomicInteger;


public class Test {

    private final static Random RND = new Random();

    private final static long LONG_TIMEOUT = 3600_000;
    private final static long SHORT_TIMEOUT = 3_000;

    public enum testType {
        NO_TIMEOUTS,    // test only select(), wakeup
        ONLY_TIMEOUTS,  // test only select(timeout), do not call wakeup()
        MIXED};

    private static void test(testType type, boolean skipCR) throws Exception {

        long dt = (type == testType.ONLY_TIMEOUTS) ? SHORT_TIMEOUT : LONG_TIMEOUT;

        int nThreads = (type == testType.ONLY_TIMEOUTS) ? 5 : 20;

        AtomicInteger nSelected = new AtomicInteger(0);

        Selector selector = Selector.open();

        Thread selectThreads[] = new Thread[nThreads];

        boolean setTimeout[] = new boolean[nThreads];
        for (int i = 0; i < nThreads; ++i) {
            boolean t = false; // NO_TIMEOUTS
            if (type == testType.ONLY_TIMEOUTS) { t = true; }
            else if (type == testType.MIXED) { t = RND.nextBoolean(); }
            setTimeout[i] = t;
        }

        Runnable rStart = new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < nThreads; ++i) {

                    boolean timeout = setTimeout[i];

                    selectThreads[i] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int n = nSelected.incrementAndGet();
                                System.out.println(">> select" + (timeout ? " (t)" : "") + " " + n);
                                if (timeout) { selector.select(dt); }
                                else { selector.select(); }
                                nSelected.decrementAndGet();
                                System.out.println(">> done" + (timeout ? " (t)" : "") + " " + n);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    selectThreads[i].start();
                    try { Thread.sleep(200); } catch (InterruptedException ie) {}
                }
            }
        };
        Thread t = new Thread(rStart);
        t.start();
        Thread.sleep(500);

        if (!skipCR) {
            javax.crac.Core.checkpointRestore();
        }

        t.join();
        Thread.sleep(1000);

        if (type == testType.ONLY_TIMEOUTS) { // do not wake threads up, the timeouts must work

            while (nSelected.get() > 0) { Thread.sleep(1000); }

        } else {

            int nWakeups = 0;
            while (true) {

                int nBefore = nSelected.get();
                if (nBefore == 0) { break; }

                System.out.println(">> wakeup() #" + (nWakeups + 1));
                ++nWakeups;

                selector.wakeup();
                while (nSelected.get() == nBefore) { // wait until any select() would be woken up
                    Thread.sleep(500);
                }
            }

            if (nWakeups > nThreads) {
                selector.close();
                throw new RuntimeException("invalid number of wakeups");
            }
        }

        // just in case...
        for (Thread st: selectThreads) { st.join(); }

        // === check that the selector works as expected ===
        if (!selector.isOpen()) { throw new RuntimeException("the selector must be open"); }

        selector.wakeup();
        selector.select();

        selector.selectNow();
        selector.select(200);

        selector.close();
    }



    public static void main(String args[]) throws Exception {

        if (args.length < 1) { throw new RuntimeException("test number is missing"); }

        switch (args[0]) {
            case "1":
                test(testType.ONLY_TIMEOUTS, false);
                break;
            case "2":
                test(testType.NO_TIMEOUTS, false);
                break;
            case "3":
                test(testType.MIXED, false);
                break;
            // 4-6: no C/R
            case "4":
                test(testType.ONLY_TIMEOUTS, true);
                break;
            case "5":
                test(testType.NO_TIMEOUTS, true);
                break;
            case "6":
                test(testType.MIXED, true);
                break;
            default:
                throw new RuntimeException("invalid test number");
        }
    }
}
