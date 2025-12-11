package com.kt.common.ses;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ses.model.*;

@Getter
@NoArgsConstructor
public class EmailInfo {
    private String from;
    private String to;
    private String subject;
    private String content;

    @Builder
    public EmailInfo(String from, String to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public SendEmailRequest toSendEmailRequest() {
        Destination destination = Destination.builder()
                .toAddresses(this.to)
                .build();

        Message message = Message.builder()
                .subject(createContent(this.subject))
                .body(Body.builder().html(createContent(this.content)).build())
                .build();

        return SendEmailRequest.builder()
                .source(this.from)
                .destination(destination)
                .message(message)
                .build();
    }

    private Content createContent (String text) {
        return Content.builder()
                .charset("UTF-8")
                .data(text).build();
    }
}
