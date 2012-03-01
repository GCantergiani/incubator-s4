package org.apache.s4.wordcount;

import org.apache.s4.ft.KeyValue;
import org.apache.s4.ft.S4TestCase;
import org.apache.s4.ft.StatefulTestPE;
import org.apache.s4.ft.TestUtils;
import org.apache.s4.processor.AbstractPE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.Assert;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class WordClassifier extends AbstractPE implements Watcher {

    TreeMap<String, Integer> counts = new TreeMap();
    private int counter;
    transient private ZooKeeper zk;
    private String id;
    public final static String ROUTING_KEY = "classifier";
	public static final String WORD_COUNT_ZNODE = "/wordcount";

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public void processEvent(WordCount wordCount) throws IOException,
            Exception, InterruptedException {
        if (zk == null) {
            try {
                zk = new ZooKeeper("localhost:21810", 4000, this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("seen: " + wordCount.getWord() + "/"
                + wordCount.getCount());

        if (!counts.containsKey(wordCount.getWord())
                || (counts.containsKey(wordCount.getWord()) && counts.get(
                        wordCount.getWord()).compareTo(wordCount.getCount()) < 0)) {
            // this is because wordCount events arrive unordered
            counts.put(wordCount.getWord(), wordCount.getCount());
        }
        ++counter;
        if (counter == WordCountTest.TOTAL_WORDS) {
            try {
    			zk.delete(WordClassifier.WORD_COUNT_ZNODE, -1);
    		} catch (Exception ignored) {
    		}
            Set<Entry<String, Integer>> entrySet = counts.entrySet();
            StringBuilder sb = new StringBuilder();
            for (Entry<String, Integer> entry : entrySet) {
                sb.append(entry.getKey() + "=" + entry.getValue() + ";");
            }
            zk.create(WordClassifier.WORD_COUNT_ZNODE, sb.toString().getBytes(), Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            zk.create("/textProcessed", new byte[0], Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        } else {
            // NOTE: this will fail if we did not recover the latest counter,
            // because there is already a counter with this number in zookeeper
            zk.create("/classifierIteration_" + counter, new byte[counter],
                    Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            // check if we are allowed to continue
            if (null == zk.exists("/continue_" + counter, null)) {
                CountDownLatch latch = new CountDownLatch(1);
                TestUtils.watchAndSignalCreation("/continue_" + counter, latch,
                        zk);
                latch.await();
            } else {
                zk.delete("/continue_" + counter, -1);
                System.out.println("");
            }

        }
    }

    @Override
    public void output() {
        // TODO Auto-generated method stub

    }

    @Override
    public void process(WatchedEvent event) {
        // TODO Auto-generated method stub

    }
}
