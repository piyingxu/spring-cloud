package com.pyx.control;

import com.pyx.ServiceInfoUtil;
import com.pyx.config.ServiceConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "控制器", tags = "TEST")
@RestController
public class WebController {

	private Logger logger = LoggerFactory.getLogger(WebController.class);

	@Autowired
	private ServiceConfig config;

    @ApiOperation("1、获取配置文件")
	@RequestMapping(value = "/getConfig", method = RequestMethod.GET)
	public String health(String name) {
	    String configInfo = "hellow " + name + "----" + config.getName() + "---" + config.getAge() + "----" + config.getAddress() + "----" + ServiceInfoUtil.getPort();
		return configInfo;
	}



}
