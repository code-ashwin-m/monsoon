package org.ashwin.test1;

import org.ashwin.monsoon.core.annotations.Singleton;

@Singleton
public class SingletonTest {
    private int count = 0;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
