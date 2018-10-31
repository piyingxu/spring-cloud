package com.pyx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@Configuration
public class SwaggerConfiguration {

    @Value("${spring.application.name}")
    private String applicaitonName;

    @Value("${spring.application.author}")
    private String author;

    @Value("${spring.application.description}")
    private String description;

    @Bean
    @Primary
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(applicaitonName + " 服务API")
                .description(description)
                .termsOfServiceUrl("http://10.200.110.32:8601/swagger-ui.html#/")
                .contact(new Contact(author, "http://10.200.110.32:8601/swagger-ui.html#/", "516609723@qq.com"))
                .version("9.9")
                .build();
    }


}
