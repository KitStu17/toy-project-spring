package com.project.toyprojectspring.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.toyprojectspring.dto.MemberDTO;
import com.project.toyprojectspring.dto.ResponseDTO;
import com.project.toyprojectspring.service.MailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("mail")
public class MailController {
    @Autowired
    private final MailService mailService;

    @PostMapping("/send")
    public ResponseEntity<?> mailConfirm(@RequestBody Map<String, String> email) throws Exception {
        String code = mailService.sendSimpleSignUpMessage(email.get("email"));
        log.info("인증코드 : " + code);

        List<String> datas = new ArrayList<String>();
        datas.add(code);

        ResponseDTO<String> response = ResponseDTO.<String>builder().data(datas).build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/check")
    public ResponseEntity<?> compareCode(@RequestBody Map<String, String> code)
            throws ChangeSetPersister.NotFoundException {
        try {
            mailService.verifyCode(code.get("code"));

            List<String> datas = new ArrayList<String>();
            datas.add("인증 성공");

            ResponseDTO<String> response = ResponseDTO.<String>builder().data(datas).build();

            return ResponseEntity.ok().body(response);
        } catch (ChangeSetPersister.NotFoundException e) {
            String error = e.getMessage();
            ResponseDTO<MemberDTO> response = ResponseDTO.<MemberDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            String error = e.getMessage();
            ResponseDTO<MemberDTO> response = ResponseDTO.<MemberDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }

    }
}
