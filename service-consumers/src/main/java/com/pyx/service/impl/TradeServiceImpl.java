package com.pyx.service.impl;

import com.pyx.FeignClient.ProviderClient;
import com.pyx.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/1 14:08
 */
@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private ProviderClient client;

    public String getConfigInfo (String name) {
        return client.getInfo(name);
    }
}
