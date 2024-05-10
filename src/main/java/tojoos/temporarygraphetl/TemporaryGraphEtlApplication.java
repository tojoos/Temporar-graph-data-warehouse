package tojoos.temporarygraphetl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class TemporaryGraphEtlApplication {

  private final static Logger log = LoggerFactory.getLogger(TemporaryGraphEtlApplication.class);


  public static void main(String[] args) {
    SpringApplication.run(TemporaryGraphEtlApplication.class, args);
  }
}
