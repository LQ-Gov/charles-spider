package com.bh.spider.consistent.raft.log;

import com.bh.spider.consistent.raft.Actuator;
import com.bh.spider.consistent.raft.HardState;
import com.bh.spider.consistent.raft.Raft;
import com.bh.spider.consistent.raft.wal.WAL;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liuqi19
 * @version : Log, 2019-04-08 16:05 liuqi19
 */
public class Log {

    private final static Logger logger = LoggerFactory.getLogger(Log.class);


    private Unstable unstable;

    // applied is the highest log position that the application has
    // been instructed to apply to its state machine.
    // Invariant: applied <= committed
    private long applied;


    // committed is the highest log position that is known to be in
    // stable storage on a quorum of nodes.
    private long committed;


    private List<Entry> entries = new LinkedList<>();


    private final Persistent persistent;


    public Log(Raft raft, Snapshotter snapshotter, WAL wal, Actuator actuator) {

        this.committed = -1;

        this.unstable = new Unstable(0);

        this.persistent = new Persistent(raft, wal, snapshotter, actuator);

        this.persistent.start();
    }


    public void recover(Snapshot snapshot, List<Entry> entries) {
        long offset = 0, committed = -1;
        if (CollectionUtils.isNotEmpty(entries)) {
            offset = entries.get(entries.size() - 1).index();
            committed = entries.get(0).index() - 1;
            this.entries = new LinkedList<>(entries);
        }

        this.unstable = new Unstable(offset);

        this.committed = committed;

    }


    public long committedIndex() {
        return committed;
    }

    public synchronized boolean append(Entry[] entries) {
        unstable.append(entries);

        synchronized (this.persistent) {
            this.persistent.notify();
        }
        return true;
    }


    public void commitTo(long index) {
        // never decrease commit
        if (this.committed < index) {
            if (this.lastIndex() < index) {
                logger.error("index {} is out of range [lastIndex{}]. Was the raft log corrupted, truncated, or lost?", index, this.lastIndex());
            } else
                this.committed = index;
        }

        synchronized (this.persistent) {
            this.persistent.notify();
        }
    }


    public boolean commit(long term, long index) {

        if (index > this.committed) {
            this.commitTo(index);
            return true;
        }

        return false;

    }


    public synchronized Entry[] entries(long startIndex, int size) {
        return this.slice(startIndex, this.lastIndex() + 1, size).toArray(new Entry[0]);
    }


    public Entry entry(long index) {
        Entry[] ents = entries(index, 1);
        return ents != null && ents.length > 0 ? ents[0] : null;
    }


    /**
     * 未持久化的entries
     *
     * @return
     */
    public List<Entry> unstableEntries() {
        return new UnmodifiableList<>(unstable.entries());
    }


    /**
     * nextEntries returns all the available entries for execution.
     * If applied is smaller than the index of snapshot, it returns all committed
     * entries after the index of snapshot.
     */

    public List<Entry> nextEntries() {
        long off = Math.max(this.applied, this.unstable.firstIndex());

        if (committed > off) {
            return this.slice(off, committed + 1, Integer.MAX_VALUE);
        }
        return null;
    }

    /**
     * 返回index所在的term
     *
     * @param index
     * @return
     */
    public long term(long index) {

        if (index < firstIndex() || index > lastIndex())
            return -1;

        long t = unstable.term(index);

        if (t == -1) {
            if (this.entries.isEmpty()) return -1;

            long offset = this.entries.get(0).index();


            if (index < offset) return -1;

            if (index - offset >= this.entries.size()) return -1;

            return this.entries.get((int) (index - offset)).term();
        }

        return t;
    }


    private long firstIndex() {
        long fi = this.unstable.firstIndex();
        if (fi == -1) {
            fi = CollectionUtils.isEmpty(this.entries) ? -1 : this.entries.get(0).index();
        }

        return fi;
    }


    public long lastIndex() {
        long li = this.unstable.lastIndex();
        if (li < 0) {
            li = CollectionUtils.isEmpty(this.entries) ? -1 : this.entries.get(this.entries.size() - 1).index();
        }
        return li;
    }


    private List<Entry> slice(long lo, long hi, int size) {

        List<Entry> entries = new LinkedList<>();


        if (lo < unstable.offset() && CollectionUtils.isNotEmpty(this.entries)) {
            long firstIndex = this.entries.get(0).index();
            List<Entry> sub = this.entries.subList((int) (lo - firstIndex), (int) ((Math.min(hi, unstable.offset()) - firstIndex)));


            if (sub.size() < Math.min(hi, unstable.offset()) - lo)
                return entries;

            entries.addAll(sub);
        }

        if (hi > unstable.offset()) {
            List<Entry> sub = unstable.slice(Math.max(unstable.offset(), lo), hi);
            entries.addAll(sub);
        }


        if (entries.size() > size) entries = entries.subList(0, size);

        return Collections.unmodifiableList(entries);
    }


    public void stableTo(long term, long index) {
        List<Entry> stabled = this.unstable.stableTo(term, index);
        if (stabled != null) {
            this.entries.addAll(stabled);
        }
    }

    public long appliedIndex() {
        return applied;
    }


    public void applyTo(long index) {

        if (index > committed || applied > index) {
            logger.error("applied({}) is out of range [prevApplied({}), committed({})]", index, applied, committed);
            return;
        }
        this.applied = index;
    }


    private class Persistent extends Thread {


        private Raft raft;

        private WAL wal;


        private Snapshotter snapshotter;

        private Actuator actuator;

        public Persistent(Raft raft, WAL wal, Snapshotter snapshotter, Actuator actuator) {

            this.raft = raft;

            this.wal = wal;

            this.snapshotter = snapshotter;

            this.actuator = actuator;

            this.setDaemon(true);
        }


        @Override
        public void run() {

            while (true) {

                synchronized (this) {

                    try {


                        long appliedIndex = -1;
                        List<Entry> entries = Log.this.unstableEntries();

                        List<Entry> committedEntries = Log.this.nextEntries();

                        if (CollectionUtils.isEmpty(entries) && CollectionUtils.isEmpty(committedEntries)) {
                            this.wait(1000 * 60 * 10);
                            continue;
                        }


                        HardState state = raft.hardState();

                        if (CollectionUtils.isNotEmpty(entries)) {

                            this.wal.save(state, entries);

                            Entry entry = entries.get(entries.size() - 1);
                            Log.this.stableTo(entry.term(), entry.index());

                        }

                        //应用到状态机
                        if (CollectionUtils.isNotEmpty(committedEntries)) {

                            for (Entry entry : committedEntries) {
                                if (entry.data() == null || entry.data().length == 0)
                                    continue;

                                this.actuator.apply(entry.data());

                                appliedIndex = entry.index();
                            }
                        }

                        //生成快照
                        if (appliedIndex - snapshotter.lastIndex() >= Snapshotter.SNAP_COUNT_THRESHOLD) {


                            Entry entry = Log.this.entry(appliedIndex);

                            byte[] snap = this.actuator.snapshot();

                            Snapshot snapshot = new Snapshot(new Snapshot.Metadata(entry.term(), entry.index()), snap);


                            snapshotter.save(snapshot);


                            this.wal.save(snapshot.metadata());
                        }

                        if (appliedIndex > 0) {

                            Log.this.applyTo(appliedIndex);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}