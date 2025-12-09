package com.kt.service.auth;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.dto.auth.MailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private static final String senderEmail = "whtndus0809@gmail.com";
    private static int number;

    public MailResponse sendMail (String mail) {
        try {
            MimeMessage message = CreateMail(mail);
            mailSender.send(message);

            return new MailResponse(
                    true,
                    "인증번호가 전송되었습니다.",
                    number
            );
        } catch (MailException e) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAIL);
        }
    }

    public static void createNumber() {
        number = (int)(Math.random() * 90000) + 100000;
    }
    public boolean checkVerificationCode(int userInput) {
        return userInput == number;
    }

    public MimeMessage CreateMail (String mail) {
        createNumber();
        MimeMessage message = mailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("JNSJ-SHOPPING 인증번호 발송");
            String body = "";
            body += "<h3>" + "요청하신 인증번호 입니다." + "</h3>";
            body += "<h1>" + number + "</h1>";
            body += "<h3>" + "감사합니다 ☺️" + "</h3>";
            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            new CustomException(ErrorCode.MAIL_CONTENT_BUILD_FAIL);
        }
        return message;


    }

}
