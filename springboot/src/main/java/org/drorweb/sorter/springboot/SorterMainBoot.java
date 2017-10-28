package org.drorweb.sorter.springboot;

import com.amazonaws.auth.AWSCredentials;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.drorweb.sorter.lambda.FileGenerateRequest;
import org.drorweb.sorter.lambda.FileGenerateResponse;
import org.drorweb.sorter.lambda.FileSortRequest;
import org.drorweb.sorter.lambda.FileSortResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by avinun
 *
 *  Swagger JSON: http://localhost:8080/v2/api-docs
 *  Swagger UI: http://localhost:8080/swagger-ui.html
 */
@EnableAutoConfiguration
@RestController

@Configuration
@EnableSwagger2

@Api(value = "/Drorweb Sorter APIs", description = "Drorweb Sorter APIs")
public class SorterMainBoot {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SorterMainBoot.class, args);
    }

    @ApiOperation(value = "Generate a Random File")
    @RequestMapping(method = RequestMethod.POST, value = "/sorter/file/generate", produces = "application/json")
    public FileGenerateResponse generate(@RequestBody FileGenerateRequest request) {
        FileGenerateResponse response = new FileGenerateResponse("FileGenerate is doing nothing - just for the swagger");
        return response;
    }

    @ApiOperation(value = "Sort the file")
    @RequestMapping(method = RequestMethod.POST, value = "/sorter/file/sort", produces = "application/json")
    public FileSortResponse sort(@RequestBody FileSortRequest request) {

        return new FileSortResponse("FileSort is doing nothing - just for the swagger");
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex(".*sorter.*"))
                .build();
    }

}
