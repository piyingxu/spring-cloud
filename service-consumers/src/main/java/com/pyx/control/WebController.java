package com.pyx.control;

import com.pyx.service.TradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "控制器", tags = "TEST")
@RestController
public class WebController {

	private Logger logger = LoggerFactory.getLogger(WebController.class);

	@Autowired
	private TradeService tradeService;

    @ApiOperation("1、获取配置文件")
	@RequestMapping(value = "/getConfig", method = RequestMethod.POST)
	public String getConfig(@RequestParam("name") String name) {
		return tradeService.getConfigInfo(name);
	}



}
