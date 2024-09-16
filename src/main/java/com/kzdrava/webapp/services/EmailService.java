package com.kzdrava.webapp.services;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmailId;

    private final static String EMAIL_BODY_TEMPLATE = """
    Hello %s,

    The user with email %s wants to share the below resource named %s with the %s %s.
    Please make the appropriate actions in the dashboard.

    Thank you.
    """;



    private final static String EMAIL_SUBJECT = "RESOURCE ACCEPTANCE";


//    @Async
//    public void sendFileEmail(String to, String filename,
//                              byte[] byteArray, String userAskedFor,
//                              String targetedUser, String fileId) throws MessagingException {
//
//        MimeMessage message = javaMailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        String emailBody = formatEmailBody(to, userAskedFor, filename, targetedUser, fileId);
//
//        helper.setTo(to);
//        helper.setSubject(EMAIL_SUBJECT);
//        helper.setText(emailBody);
//        helper.setFrom(fromEmailId);
//
//
//        ByteArrayResource byteArrayInputStream = ResourceUtils.convert(byteArray, filename);
//
//        helper.addAttachment(filename, byteArrayInputStream);
//
//        javaMailSender.send(message);
//
//    }

    @Async
    public void sendEmail(String to, String resourceName,
                          String requester,
                          Set<String> receivers) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String emailBody = formatEmailBody(to, requester, resourceName, receivers);

        helper.setTo(to);
        helper.setSubject(EMAIL_SUBJECT);
        helper.setText(emailBody);
        helper.setFrom(fromEmailId);

        javaMailSender.send(message);

    }

    private static String formatEmailBody(String recipient, String requester, String resource,
                                          Set<String> receivers) {
        String receiverText = receivers.size() == 1 ? "user with email" : "users with emails";
        String receiversString = String.join(", ", receivers);

        return String.format(EMAIL_BODY_TEMPLATE, recipient, requester, resource, receiverText, receiversString);
    }

}
