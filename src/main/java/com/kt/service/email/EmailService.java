package com.kt.service.email;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.ses.EmailInfo;
import com.kt.dto.email.EmailResponse;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final UserRepository userRepository;
    @Value("${aws.ses.send-mail-from}")
    private String sender;
    private final SesClient sesClient;
    private final TemplateEngine templateEngine;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String EMAIL_AUTH_PREFIX = "emailAuth:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    public EmailResponse.AuthenticationResponse sendEmail(String email) {

        String code = createCode();

        String key = EMAIL_AUTH_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_TTL);

        Context context = new Context();
        context.setVariable("code", code);

        String content = templateEngine.process("authentication-email", context);

        EmailInfo emailInfo = EmailInfo.builder()
                .from(sender)
                .to(email)
                .subject("JNSJ 인증번호 발송")
                .content(content)
                .build();
        try {
            sesClient.sendEmail(emailInfo.toSendEmailRequest());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAIL);
        }

        return new EmailResponse.AuthenticationResponse(
                true,
                "인증번호가 전송되었습니다.",
                code
        );
    }

    public boolean verifyCode(String email, String code) {
        String key = EMAIL_AUTH_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        Preconditions.nullValidate(savedCode, ErrorCode.EMAIL_AUTH_CODE_INVALID_REQUEST);
        Preconditions.validate(savedCode.equals(code), ErrorCode.EMAIL_AUTH_CODE_INVALID);

        redisTemplate.delete(key);
        return true;
    }

    public void sendLoginIdEmail(String email, String name, String loginId) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("loginId", loginId);

        String content = templateEngine.process("find-id-email", context);

        EmailInfo emailInfo = EmailInfo.builder()
                .from(sender)
                .to(email)
                .subject("JNSJ 아이디 안내")
                .content(content)
                .build();

        try {
            sesClient.sendEmail(emailInfo.toSendEmailRequest());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAIL);
        }

    }

    private String createCode() {
        int number = (int) (Math.random() * 900000) + 100000; // 100000 ~ 999999
        return String.valueOf(number);
    }
}
