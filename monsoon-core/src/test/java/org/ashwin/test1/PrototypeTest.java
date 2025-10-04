package org.ashwin.test1;


import org.ashwin.monsoon.core.annotations.Component;

@Component
public class PrototypeTest {
    private int count = 0;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
