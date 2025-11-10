package com.csy.springbootauthbe;

import com.csy.springbootauthbe.admin.repository.AdminRepository;
import com.csy.springbootauthbe.booking.repository.BookingRepository;
import com.csy.springbootauthbe.notification.repository.NotificationRepository;
import com.csy.springbootauthbe.notification.service.NotificationService;
import com.csy.springbootauthbe.student.repository.StudentRepository;
import com.csy.springbootauthbe.tutor.repository.TutorRepository;
import com.csy.springbootauthbe.user.repository.UserRepository;
import com.csy.springbootauthbe.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = {
                SpringbootAuthBeApplication.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.lazy-initialization=true",
                "spring.main.web-application-type=none",
                "spring.main.allow-bean-definition-overriding=true",

                "app.mongo.enabled=false",
                "spring.data.mongodb.repositories.enabled=false",
                "spring.data.mongodb.auditing.enabled=false",

                "aws.s3.access-key=dummy",
                "aws.s3.secret-key=dummy",
                "aws.s3.region=us-east-1",
                "aws.s3.bucket=test-bucket",
                "jwt.secret.key=dummy",
                "stripe.secret-key=dummy",

                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.elasticsearch.RestClientAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
        }
)
@ActiveProfiles("test")
class SpringbootAuthBeApplicationTests {

    @MockBean private AdminRepository adminRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private TutorRepository tutorRepository;
    @MockBean private StudentRepository studentRepository;
    @MockBean private BookingRepository bookingRepository;
    @MockBean private NotificationRepository notificationRepository;

    // --- Services that depend on repositories ---
    @MockBean private WalletService walletService;
    @MockBean private NotificationService notificationService;

    // --- Supporting infra that would otherwise trigger Mongo beans ---
    @MockBean private MongoTemplate mongoTemplate;
    @MockBean private GridFsTemplate gridFsTemplate;

    @Test
    void contextLoads() {
        // If the context boots, we pass.
        assertTrue(true);
    }
}
