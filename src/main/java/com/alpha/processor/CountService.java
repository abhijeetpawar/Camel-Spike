package com.alpha.processor;

import org.springframework.stereotype.Component;

@Component
public class CountService {

    public CountService() {
    }

    public int getCount() {
        return 1;
    }
}
