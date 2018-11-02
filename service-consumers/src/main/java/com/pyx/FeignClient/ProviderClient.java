package com.pyx.FeignClient;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "service-provider", url = "${${env:}.url.member:}")
public interface ProviderClient {

	@RequestMapping(value = "/getConfig", method = RequestMethod.GET)
	public String getInfo(@RequestParam("name") String name);

}
