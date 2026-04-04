package com.greenleaf.paperplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * Sends a thank-you confirmation email to the customer.
     */
    @Async
    public void sendThankYouEmail(String toEmail, String customerName) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(fromAddress, "GreenLeaf Paper Plates");
            helper.setTo(toEmail);
            helper.setSubject("Thank you for reaching out — GreenLeaf Paper Plates 🍃");
            helper.setText(buildEmailBody(customerName), true); // true = HTML

            mailSender.send(msg);
            log.info("Thank-you email sent to {}", toEmail);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Sends an internal notification to the factory team.
     */
    @Async
    public void sendInternalNotification(String name, String phone, String email,
                                          String location, String message) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(fromAddress, "GreenLeaf Website");
            helper.setTo(fromAddress); // notify the factory address itself
            helper.setSubject("📩 New Enquiry from " + name);
            helper.setText(buildInternalBody(name, phone, email, location, message), true);

            mailSender.send(msg);
            log.info("Internal notification sent for enquiry from {}", email);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send internal notification: {}", e.getMessage());
        }
    }

    // ─── Email Templates ─────────────────────────────────────────

    private String buildEmailBody(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Georgia, serif; background: #F5F0E8; margin: 0; padding: 0; }
                .wrap { max-width: 580px; margin: 40px auto; background: #fff;
                        border-radius: 6px; overflow: hidden;
                        box-shadow: 0 4px 20px rgba(59,42,26,0.1); }
                .header { background: #3B2A1A; padding: 2.5rem; text-align: center; }
                .header h1 { color: #F5F0E8; font-size: 1.8rem; margin: 0; }
                .header p { color: rgba(245,240,232,0.7); font-size: 0.85rem;
                             margin-top: 0.4rem; font-family: monospace; }
                .body { padding: 2.5rem; color: #3B2A1A; }
                .body h2 { font-size: 1.3rem; margin-bottom: 1rem; }
                .body p { line-height: 1.8; font-size: 0.95rem; margin-bottom: 1rem;
                           color: #5a3e26; }
                .highlight { background: #F5F0E8; border-left: 4px solid #C15F2E;
                              padding: 1rem 1.2rem; border-radius: 2px;
                              font-size: 0.9rem; color: #3B2A1A; margin: 1.5rem 0; }
                .cta { text-align: center; margin: 2rem 0; }
                .cta a { background: #C15F2E; color: #fff; padding: 0.85rem 2rem;
                          text-decoration: none; border-radius: 3px; font-size: 0.95rem; }
                .footer { background: #1C1612; padding: 1.2rem 2rem; text-align: center;
                           font-size: 0.75rem; color: rgba(245,240,232,0.4); }
              </style>
            </head>
            <body>
              <div class="wrap">
                <div class="header">
                  <h1>🍃 GreenLeaf Paper Plates</h1>
                  <p>Pure · Eco · Crafted · Est. 2008</p>
                </div>
                <div class="body">
                  <h2>Thank you, %s! 🙏</h2>
                  <p>We've received your enquiry and we're thrilled to hear from you.</p>
                  <div class="highlight">
                    Our team will review your message and get back to you within
                    <strong>24 business hours</strong>. If your query is urgent, feel free
                    to call us directly at <strong>+91 98765 43210</strong>.
                  </div>
                  <p>At GreenLeaf, we take pride in delivering the best quality paper plates,
                  tailored to your requirements — whether you need bulk quantities, custom
                  printing, or eco-premium areca leaf options.</p>
                  <div class="cta">
                    <a href="mailto:nagamahendram098@gmail.com">Reply to this email</a>
                  </div>
                  <p style="font-size:0.85rem; color:#888; text-align:center;">
                    GreenLeaf Paper Plates Pvt. Ltd.<br/>
                    Plot No. 47, APIIC Industrial Estate, Tirupati — 517520, AP
                  </p>
                </div>
                <div class="footer">
                  © 2025 GreenLeaf Paper Plates · You're receiving this because you
                  contacted us through our website.
                </div>
              </div>
            </body>
            </html>
            """.formatted(name);
    }

    private String buildInternalBody(String name, String phone, String email,
                                      String location, String message) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: sans-serif; background:#f0f0f0; padding:20px;">
              <div style="max-width:540px; margin:auto; background:#fff;
                          border-radius:6px; overflow:hidden;">
                <div style="background:#3B2A1A; padding:1.5rem; color:#F5F0E8;">
                  <h2 style="margin:0;">📩 New Website Enquiry</h2>
                </div>
                <div style="padding:1.5rem; color:#333;">
                  <table style="width:100%; border-collapse:collapse;">
                    <tr style="border-bottom:1px solid #eee;">
                      <td style="padding:0.6rem 0; font-weight:600; width:120px;">Name</td>
                      <td style="padding:0.6rem 0;">%s</td>
                    </tr>
                    <tr style="border-bottom:1px solid #eee;">
                      <td style="padding:0.6rem 0; font-weight:600;">Phone</td>
                      <td style="padding:0.6rem 0;">%s</td>
                    </tr>
                    <tr style="border-bottom:1px solid #eee;">
                      <td style="padding:0.6rem 0; font-weight:600;">Email</td>
                      <td style="padding:0.6rem 0;">%s</td>
                    </tr>
                    <tr style="border-bottom:1px solid #eee;">
                      <td style="padding:0.6rem 0; font-weight:600;">Location</td>
                      <td style="padding:0.6rem 0;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:0.6rem 0; font-weight:600; vertical-align:top;">Message</td>
                      <td style="padding:0.6rem 0;">%s</td>
                    </tr>
                  </table>
                </div>
              </div>
            </body>
            </html>
            """.formatted(name, phone, email, location,
                          message == null || message.isBlank() ? "—" : message);
    }
}
