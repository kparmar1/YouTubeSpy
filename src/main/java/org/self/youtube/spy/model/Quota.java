package org.self.youtube.spy.model;

// TODO: monitor the quota limit
public class Quota {
    long total;

    public void usedCredit(long credit) {
        total += credit;
    }
}
