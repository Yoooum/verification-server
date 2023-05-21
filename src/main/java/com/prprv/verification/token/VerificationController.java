package com.prprv.verification.token;

import com.prprv.verification.event.VerificationSendEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Yoooum
 */
@Slf4j
@RestController
@RequestMapping("/verify")
public class VerificationController {
    @Value("${sms.enabled}")
    private boolean SMS_ENABLED;
    private final VerificationCodeRepository repository;
    private final ApplicationEventPublisher publisher;

    public VerificationController(VerificationCodeRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @PostMapping("/email")
    public Object email(String email) {
        publisher.publishEvent(new VerificationSendEvent(email, Type.EMAIL));
        Map<String, Object> map = new HashMap<>();
        map.put("message", "success!  please check your email");
        return map;
    }

    @PostMapping("/phone")
    public Object phone(String phone) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", "success!  please check your sms");
        if (!SMS_ENABLED) {
            map.put("message", "sms service is not enabled");
            return map;
        }
        publisher.publishEvent(new VerificationSendEvent(phone, Type.PHONE));
        map.put("message", "success!  please check your sms");
        return map;
    }

    @GetMapping("/email")
    public Object verifyEmail(@RequestParam String code,@RequestParam String email) {
        Optional<VerificationCode> byCode = repository.findByCode(code);
        Map<String, Object> map = new HashMap<>();
        if (byCode.isPresent() && byCode.get().getEmail().equals(email)) {
            return check(byCode.get(), map);
        }
        map.put("valid", false);
        map.put("message", "verification code is invalid");
        return map;
    }

    @GetMapping("/phone")
    public Object verifyPhone(@RequestParam String code,@RequestParam String phone) {
        Optional<VerificationCode> byCode = repository.findByCode(code);
        Map<String, Object> map = new HashMap<>();
        if (byCode.isPresent() && byCode.get().getPhone().equals(phone)) {
            return check(byCode.get(), map);
        }
        map.put("valid", false);
        map.put("message", "verification code is invalid");
        return map;
    }

    @NotNull
    private Object check(VerificationCode code, Map<String, Object> map) {
        long time = code.getExpiredAt().getTime() - new Date().getTime();
        if (time > 0) {
            map.put("valid", true);
            map.put("message", "verification code is valid");
            return map;
        }
        repository.delete(code);
        map.put("valid", false);
        map.put("message", "verification code is expired");
        return map;
    }
}
