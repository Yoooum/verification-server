package com.prprv.verification.token;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * @author Yoooum
 */
@Data
@Entity
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String email;
    private String phone;
    private Date expiredAt;
}
