package com.project.toyprojectspring.service;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.entity.PostEntity;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    @Autowired
    private final RedisService redisService;

    // 이메일 인증 코드
    private final String certificateNumber = createKey();

    @Value("${spring.mail.username}")
    private String id;

    // 이메일 인증 코드 메세지 생성
    public MimeMessage createSignUpMessage(String to) throws MessagingException, UnsupportedEncodingException {
        log.info("보내는 대상 : " + to);
        log.info("인증 번호 : " + certificateNumber);
        MimeMessage message = javaMailSender.createMimeMessage();

        // 메세지 전송 대상 설정
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
        // 메세지 제목 설정
        message.setSubject("Toy Project 회원가입 인증 코드 : ");

        // 메일 내용의 subtype을 html로 지정하여 html 문법 사용
        String msg = "";
        msg += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">이메일 주소 확인</h1>";
        msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래 확인 코드를 회원가입 화면에서 입력해주세요.</p>";
        msg += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 32px 0 40px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">";
        msg += certificateNumber;
        msg += "</td></tr></tbody></table></div>";

        // 메세지 내용 삽입
        message.setText(msg, "utf-8", "html");
        // 메세지 송신자 이메일 설정
        message.setFrom(new InternetAddress(id));

        return message;
    }

    // 프로젝트 참여 요청 메세지 생성
    public MimeMessage createApplyMessage(String to, PostEntity post, MemberEntity member)
            throws MessagingException, UnsupportedEncodingException {
        log.info("보내는 대상 : " + to);
        MimeMessage message = javaMailSender.createMimeMessage();

        // 메세지 전송 대상 설정
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
        // 메세지 제목 설정
        message.setSubject("Toy Project : " + post.getTitle() + " 참여 요청");

        String applicantName = member.getName();
        String applicantEmail = member.getEmail();

        // 메일 내용의 subtype을 html로 지정하여 html 문법 사용
        String msg = "";
        msg += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">" + "참여 요청 전송" + "</h1>";
        msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">" + post.getTitle()
                + " 의 참여 요청</p>";
        msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">" + applicantName + "("
                + applicantEmail + ")" + " 로 부터의 참여 요청이 전송되었습니다.</p>";

        // 메세지 내용 삽입
        message.setText(msg, "utf-8", "html");
        // 메세지 송신자 이메일 설정
        message.setFrom(new InternetAddress(id));

        return message;
    }

    // 인증 코드 생성
    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 6; i++) {
            // 인증 코드 6자리 생성
            key.append((rnd.nextInt(10)));
        }
        return key.toString();
    }

    // 인증 코드 메일 발송
    // MimeMessage = 전송할 내용
    // sendSimpleMessage = {to : 인증번호를 받을 메일 주소}
    public String sendSimpleSignUpMessage(String to) throws Exception {
        MimeMessage message = createSignUpMessage(to);
        try {
            // 코드 유효 시간 설정(3분)
            redisService.setDateExpire(certificateNumber, to, 60 * 3L);
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return certificateNumber;
    }

    // 참여 요청 메일 발송
    // MimeMessage = 전송할 내용
    // sendSimpleMessage = {to : 인증번호를 받을 메일 주소}
    public void sendSimpleApplyMessage(String to, PostEntity post, MemberEntity member) throws Exception {
        MimeMessage message = createApplyMessage(to, post, member);
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public String verifyCode(String code) throws NotFoundException {
        String memberEmail = redisService.getData(code);
        if (memberEmail == null) {
            throw new NotFoundException();
        }
        redisService.deleteData(code);

        return certificateNumber;
    }
}
