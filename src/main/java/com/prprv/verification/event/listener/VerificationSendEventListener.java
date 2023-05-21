package com.prprv.verification.event.listener;

import com.prprv.verification.MailContentTemplate;
import com.prprv.verification.event.VerificationSendEvent;
import com.prprv.verification.token.Type;
import com.prprv.verification.token.VerificationCode;
import com.prprv.verification.token.VerificationCodeRepository;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

/**
 * @author Yoooum
 */
@Slf4j
@Component
public class VerificationSendEventListener implements ApplicationListener<VerificationSendEvent> {
    @Value("${spring.mail.username}")
    private String FROM;
    @Value("${sms.secret-id}")
    private String SECRET_ID;
    @Value("${sms.secret-key}")
    private String SECRET_KEY;
    @Value("${sms.sign-name}")
    private String SIGN_NAME;
    @Value("${sms.template-id}")
    private String TEMPLATE_ID;
    @Value("${sms.sdk-app-id}")
    private String SDK_APP_ID;
    private final VerificationCodeRepository codeRepository;
    private final MailContentTemplate template;
    private final JavaMailSender mailSender;

    public VerificationSendEventListener(VerificationCodeRepository codeRepository, MailContentTemplate template, JavaMailSender mailSender) {
        this.codeRepository = codeRepository;
        this.template = template;
        this.mailSender = mailSender;
    }

    @Override
    public void onApplicationEvent(@NonNull VerificationSendEvent event) {
        String source = event.getSource().toString();
        log.info("Received mail send event");
        Type type = event.getType();
        VerificationCode code = new VerificationCode();
        code.setExpiredAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        switch (type) {
            case EMAIL -> {
                code.setType(Type.EMAIL);
                code.setEmail(source);
                code.setCode(generateCode());
                codeRepository.save(code);
                sendMail(source, "验证码", template.code(code.getCode()));
            }
            case PHONE -> {
                code.setType(Type.PHONE);
                code.setPhone(source);
                code.setCode(generateNumber());
                codeRepository.save(code);
                sendSms(source, code.getCode());
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            // num为0-35的随机数，0-9为数字，10-35为大写字母
            int num = (int) (Math.random() * 36);
            if (num < 10) code.append(num);
            else if (num < 36) {
                // 65-90为'A'-'Z'的ASCII码，例如num=10，num+55=65，(char)65='A'
                code.append((char) (num + 55));
            }
        }
        codeRepository.findByCode(code.toString()).ifPresent(verificationCode -> generateCode());
        return code.toString();
    }

    private String generateNumber() {
        String chars = "0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            code.append(chars.charAt(index));
        }
        codeRepository.findByCode(code.toString()).ifPresent(token -> generateNumber());
        return code.toString();
    }

    public void sendMail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom(FROM, "Prprv Team");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendSms(String phone, String code) {
        try {
            SmsClient client = new SmsClient(new Credential(SECRET_ID, SECRET_KEY), "ap-guangzhou");
            SendSmsRequest req = new SendSmsRequest();
            req.setPhoneNumberSet(new String[]{phone});
            req.setTemplateId(TEMPLATE_ID);
            req.setSmsSdkAppId(SDK_APP_ID);
            req.setSignName(SIGN_NAME);
            req.setTemplateParamSet(new String[]{code});
            SendSmsResponse response = client.SendSms(req);
            log.info(SendSmsResponse.toJsonString(response));
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
    }
}
