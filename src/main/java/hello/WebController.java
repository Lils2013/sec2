package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class WebController implements WebMvcConfigurer {

    //    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/results").setViewName("results");
//    }
    JdbcTemplate jdbcTemplate;

    @Autowired
    public WebController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE customers(" +
                "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

        // Split up the array of whole names into an array of first/last names
        List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream()
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        // Uses JdbcTemplate's batchUpdate operation to bulk load data
        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);
    }


    @GetMapping("/injectable")
    public String showFormInjectable(PersonForm personForm) {
        return "form";
    }

    @PostMapping("/injectable")
    public String checkPersonInfoInjectable(PersonForm personForm, Model model) {
        List<Map<String, Object>> result;
        try {
            result = jdbcTemplate.queryForList("SELECT id, first_name, last_name FROM customers WHERE LOWER(first_name) = '" + personForm.getName().toLowerCase() + "'");
            if (result.isEmpty())
                model.addAttribute("name", "Not found");
            else
                model.addAttribute("name", result);
        } catch (Exception e) {
            model.addAttribute("name", "Not found");
        }
        return "form";
    }

    @GetMapping("/secure")
    public String showFormSecure(PersonForm personForm) {
        return "form_secure";
    }

    @PostMapping("/secure")
    public String checkPersonInfoSecure(PersonForm personForm, Model model) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT id, first_name, last_name FROM customers WHERE LOWER(first_name) = (?)", personForm.getName().toLowerCase());
        if (result.isEmpty())
            model.addAttribute("name", "Not found");
        else
            model.addAttribute("name", result);
        return "form_secure";
    }
}
