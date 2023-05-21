package com.prprv.verification;

import org.springframework.stereotype.Component;

/**
 * @author Yoooum
 */
@Component
public class MailContentTemplate {
    public String code(String code) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        p {
                            padding: 0;
                            margin: 0;
                            font-family: "lucida Grande",Verdana,"Microsoft YaHei",sans-serif;
                            font-size: 14px;
                            color: #707070;
                            font-weight: bold;
                        }
                        #code {
                            width: 120px;
                            font-size: 20px;
                            font-weight: bold;
                            padding: 5px;
                            margin: 10px 0;
                            background-color: #f0f0f0;
                            color: #707070;
                            border: 1px solid #707070;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                <div>
                    <p>验证码：</p>
                    <div id="code">%s</div>
                </div>
                </body>
                </html>
                """.formatted(code);
    }
}
