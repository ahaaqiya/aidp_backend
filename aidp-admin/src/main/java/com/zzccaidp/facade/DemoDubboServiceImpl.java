package com.zzccaidp.facade;

import com.zzccaidp.DemoDubboService;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 14:18
 * @Version: 1.0
 */
@Service(value = "demoDubboService")
public class DemoDubboServiceImpl implements DemoDubboService {
    @Override
    public String sayHello(String hello) {
        return "";
    }
}
