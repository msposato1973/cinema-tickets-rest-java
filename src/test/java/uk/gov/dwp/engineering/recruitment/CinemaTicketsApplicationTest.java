package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class CinemaTicketsApplicationTest {

  @Autowired
  ApplicationContext context;

  @Test
  public void whenSpringIsLoadedContextIsNotNull() {
    assertNotNull(context);
  }

  @Test
  void main() {
    CinemaTicketsApplication.main(new String[]{"--spring.main.web-environment=false",});
  }

}
