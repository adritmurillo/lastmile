package com.lastmile.infrastructure.adapter.out.notification;

import com.lastmile.domain.model.Order;
import com.lastmile.domain.model.Stop;
import com.lastmile.domain.port.out.NotificationPort;
import com.lastmile.infrastructure.adapter.out.notification.template.OrderCreatedEmailTemplate;
import com.lastmile.infrastructure.adapter.out.notification.template.OrderDeliveredEmailTemplate;
import com.lastmile.infrastructure.adapter.out.notification.template.OrderFailedEmailTemplate;
import com.lastmile.infrastructure.adapter.out.notification.template.OrderInTransitEmailTemplate;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;
    private final OrderCreatedEmailTemplate orderCreatedTemplate;
    private final OrderInTransitEmailTemplate orderInTransitTemplate;
    private final OrderDeliveredEmailTemplate orderDeliveredTemplate;
    private final OrderFailedEmailTemplate orderFailedTemplate;
    private final DeliveryVoucherPdfGenerator voucherPdfGenerator;

    @Async
    @Override
    public void notifyOrderCreated(Order order) {
        sendHtml(order, orderCreatedTemplate.subject(order), orderCreatedTemplate.build(order), null, null);
    }

    @Async
    @Override
    public void notifyOrderInTransit(Order order) {
        sendHtml(order, orderInTransitTemplate.subject(order), orderInTransitTemplate.build(order), null, null);
    }

    @Async
    @Override
    public void notifyOrderDelivered(Order order) {
        byte[] pdf = voucherPdfGenerator.generate(order);
        String filename = "comprobante-" + order.getTrackingCode() + ".pdf";
        sendHtml(order, orderDeliveredTemplate.subject(order), orderDeliveredTemplate.build(order), pdf, filename);
    }

    @Async
    @Override
    public void notifyOrderFailed(Order order, Stop stop) {
        sendHtml(order, orderFailedTemplate.subject(order), orderFailedTemplate.build(order, stop), null, null);
    }

    private void sendHtml(Order order, String subject, String html, byte[] attachment, String attachmentName) {
        try {
            if (order.getRecipientEmail() == null || order.getRecipientEmail().isBlank()) {
                log.warn("No email for order {}, skipping notification", order.getTrackingCode());
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(order.getRecipientEmail());
            helper.setSubject(subject);
            helper.setText(html, true);

            if (attachment != null && attachment.length > 0) {
                helper.addAttachment(attachmentName, new ByteArrayDataSource(attachment, "application/pdf"));
            }

            mailSender.send(message);
            log.info("Email sent to {} for order {}", order.getRecipientEmail(), order.getTrackingCode());
        } catch (Exception e) {
            log.error("Failed to send email for order {}: {}", order.getTrackingCode(), e.getMessage());
        }
    }
}