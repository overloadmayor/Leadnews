package com.heima.wemedia.service.impl;

import com.heima.wemedia.WemediaApplication;
import com.heima.wemedia.service.WmNewsAutoScanService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class WmNewsAutoScanServiceImplTest {
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Test
    public void wmNewsAutoScan() throws InterruptedException {
        wmNewsAutoScanService.autoScanWmNews(6236);
    }
}