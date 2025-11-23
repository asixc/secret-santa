package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.entity.EmailEncryptConverter;
import dev.jotxee.secretsanta.event.SorteoCreatedEvent;
import dev.jotxee.secretsanta.util.EmailCryptoService;
import dev.jotxee.secretsanta.util.CurrencyFormatter;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String applicationBaseUrl;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String defaultSender;

    @Async
    public void sendParticipantEmail(String sorteoName, Double importeMinimo, Double importeMaximo, 
                                      SorteoCreatedEvent.ParticipantPayload participant) {
        log.info("📧 Iniciando envío de email HTML a {} para sorteo '{}'", new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(participant.email()), sorteoName);
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(participant.email());
            if (StringUtils.hasText(defaultSender)) {
                helper.setFrom(defaultSender);
            }
            helper.setSubject("🎅 Resultado del sorteo: " + sorteoName);
            
            String htmlContent = loadEmailTemplate();
            htmlContent = htmlContent.replace("{{PARTICIPANT_NAME}}", HtmlUtils.htmlEscape(participant.name()));
            htmlContent = htmlContent.replace("{{SORTEO_NAME}}", HtmlUtils.htmlEscape(sorteoName));
            htmlContent = htmlContent.replace("{{REVEAL_URL}}", buildRevealUrl(participant.token()));
            htmlContent = htmlContent.replace("{{GIFT_BUDGET}}", buildGiftBudgetText(importeMinimo, importeMaximo));
            
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("✅ Email HTML enviado exitosamente a {}", 
            new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(participant.email())
            );
        } catch (Exception ex) {
            log.error("❌ Error al enviar email a {}", new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(participant.email()), ex);
        }
    }

    private String loadEmailTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email-template.html");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception _) {
            log.warn("Template no encontrado, usando fallback");
            return buildFallbackTemplate();
        }
    }

    private String buildFallbackTemplate() {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family: Arial; padding: 20px; background: #f4f4f4;">
                    <div style="max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px;">
                        <h1 style="color: #c31432;">🎅 Amigo Invisible</h1>
                        <p>¡Hola <strong>{{PARTICIPANT_NAME}}</strong>!</p>
                        <p>Sorteo: <strong>{{SORTEO_NAME}}</strong></p>
                        <p style="background: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            💰 <strong>Presupuesto del regalo:</strong> {{GIFT_BUDGET}}
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="{{REVEAL_URL}}" style="background: #c31432; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px;">
                                🎁 Ver Mi Amigo Invisible
                            </a>
                        </div>
                        <p>¡Felices fiestas! 🎄</p>
                    </div>
                </body>
                </html>
                """;
    }

    private String buildGiftBudgetText(Double importeMinimo, Double importeMaximo) {
        if (importeMinimo == null && importeMaximo == null) {
            return "";
        }
        
        if (importeMinimo != null && importeMaximo != null) {
            return CurrencyFormatter.formatAmount(importeMinimo) + "€ - " + CurrencyFormatter.formatAmount(importeMaximo) + "€";
        } else if (importeMinimo != null) {
            return "Desde " + CurrencyFormatter.formatAmount(importeMinimo) + "€";
        } else {
            return "Hasta " + CurrencyFormatter.formatAmount(importeMaximo) + "€";
        }
    }

    private String buildRevealUrl(String token) {
        return applicationBaseUrl.endsWith("/")
                ? applicationBaseUrl + "?id=" + token
                : applicationBaseUrl + "/?id=" + token;
    }

    @Async
    public void enviarPassword(String email, String nombre, String password) {
        log.info("📧 Enviando contraseña a {}", new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(email));

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            if (StringUtils.hasText(defaultSender)) {
                helper.setFrom(defaultSender);
            }
            helper.setSubject("🔑 Tu contraseña para Amigo Invisible");

            String htmlContent = loadPasswordEmailTemplate();
            htmlContent = htmlContent.replace("{{PARTICIPANT_NAME}}", HtmlUtils.htmlEscape(nombre));
            htmlContent = htmlContent.replace("{{PASSWORD}}", HtmlUtils.htmlEscape(password));
            htmlContent = htmlContent.replace("{{LOGIN_URL}}", applicationBaseUrl.endsWith("/")
                ? applicationBaseUrl + "login"
                : applicationBaseUrl + "/login");

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("✅ Email de contraseña enviado exitosamente a {}",
                new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(email));
        } catch (Exception ex) {
            log.error("❌ Error al enviar email de contraseña a {}",
                new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(email), ex);
        }
    }

    private String loadPasswordEmailTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email-password.html");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception _) {
            log.warn("Template de contraseña no encontrado, usando fallback");
            return buildPasswordFallbackTemplate();
        }
    }

    private String buildPasswordFallbackTemplate() {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family: Arial; padding: 20px; background: #f4f4f4;">
                    <div style="max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px;">
                        <h1 style="color: #c31432;">🔑 Tu Contraseña</h1>
                        <p>¡Hola <strong>{{PARTICIPANT_NAME}}</strong>!</p>
                        <p>Se ha generado una nueva contraseña para tu cuenta de Amigo Invisible.</p>
                        <div style="background: #f0f0f0; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: center;">
                            <p style="margin: 0; font-size: 14px; color: #666;">Tu contraseña es:</p>
                            <p style="margin: 10px 0 0 0; font-size: 24px; font-weight: bold; color: #c31432; font-family: monospace;">{{PASSWORD}}</p>
                        </div>
                        <p>Usa esta contraseña para acceder a tu perfil y ver tu información de tallas.</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="{{LOGIN_URL}}" style="background: #c31432; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px;">
                                🔐 Iniciar Sesión
                            </a>
                        </div>
                        <p style="color: #666; font-size: 12px;">Por seguridad, te recomendamos cambiar esta contraseña después de iniciar sesión.</p>
                    </div>
                </body>
                </html>
                """;
    }
}
