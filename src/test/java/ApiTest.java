import java.util.List;

import org.freyja.v2.jdbc.FreyjaJdbcTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:./config/spring/spring.xml",
		"file:./config/spring/spring-db.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
public class ApiTest {

	@Autowired
	private FreyjaJdbcTemplate freyjaJdbcTemplate;

	@Test
	public void a() {
		String sql = "select * from User  where gold > ?";

		// sql = "select * from User    where userId = ? and id = 2";
		List list = freyjaJdbcTemplate.query(sql, 5000);

		System.out.println();
		// String sql2 = "update  User set userId = ? where id = ?";
		// freyjaJdbcTemplate.executeUpdate(sql2, 1);

	}

}
