package eu.driver.adaptor;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import eu.driver.adapter.core.CISAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan
@EnableSwagger2
@SpringBootApplication
public class MIRestAdaptor {

	private Logger log = Logger.getLogger(this.getClass());

	public MIRestAdaptor() throws Exception {
		log.info("Init. MIRestAdaptor");
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.out.println("MIRestAdaptor started with command-line arguments: " + Arrays.toString(args));
			for (String arg : args) {
				if (arg.indexOf("-config") != -1) {
					StringTokenizer tokenizer = new StringTokenizer(arg, "=");
					String token = tokenizer.nextToken();
					CISAdapter.globalConfigPath = tokenizer.nextToken();
				}
			}
			System.out.println("ConfigPath = " + CISAdapter.globalConfigPath);
		}
		SpringApplication.run(MIRestAdaptor.class, args);
    }
	
	@Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("MIRestAdaptor")
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/MIRestAdaptor.*"))
                .build();
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("MIAdaptor REST Interface API Doc.")
                .description("This is the MIAdaptor REST Interface API Documentation made with Swagger.")
                .version("1.0")
                .build();
    }
}
