package com.lastmile.infrastructure.adapter.out.notification.template;

import com.lastmile.domain.model.Order;
import com.lastmile.domain.model.Stop;
import org.springframework.stereotype.Component;

@Component
public class OrderFailedEmailTemplate {

    public String subject(Order order) {
        return "⚠️ Intento de entrega fallido — " + order.getTrackingCode();
    }

    public String build(Order order, Stop stop) {
        String failureMessage = order.getDeliveryAttempts() < 3
                ? "Realizaremos un nuevo intento de entrega próximamente. No necesitas hacer nada."
                : "Has alcanzado el máximo de intentos de entrega. Por favor contáctanos para coordinar la entrega de tu paquete.";

        String failureColor = order.getDeliveryAttempts() >= 3 ? "#dc2626" : "#d97706";
        String failureReason = translateReason(stop.getFailureReason().toString());

        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Intento de Entrega Fallido</title>
        </head>
        <body style="margin:0;padding:0;background:#f4f4f4;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:40px 0;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                <!-- HEADER -->
                <tr>
                  <td style="background:linear-gradient(135deg,#d97706,#b45309);padding:40px 40px 32px;text-align:center;">
                    <div style="font-size:13px;color:#fde68a;letter-spacing:3px;text-transform:uppercase;margin-bottom:8px;">Last Mile Delivery</div>
                    <div style="font-size:48px;margin-bottom:8px;">⚠️</div>
                    <div style="font-size:26px;font-weight:700;color:#ffffff;letter-spacing:1px;">INTENTO FALLIDO</div>
                    <div style="margin-top:12px;font-size:14px;color:#fde68a;">No pudimos completar la entrega</div>
                  </td>
                </tr>

                <!-- TRACKING CODE -->
                <tr>
                  <td style="padding:32px 40px;text-align:center;background:#fffbeb;border-bottom:1px solid #fef3c7;">
                    <div style="font-size:11px;color:#999;letter-spacing:2px;text-transform:uppercase;margin-bottom:8px;">Código de seguimiento</div>
                    <div style="font-size:26px;font-weight:700;color:#d97706;letter-spacing:3px;font-family:monospace;">%s</div>
                  </td>
                </tr>

                <!-- GREETING -->
                <tr>
                  <td style="padding:32px 40px 16px;">
                    <p style="font-size:16px;color:#333;margin:0;">Hola <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#555;line-height:1.6;margin:12px 0 0;">Lamentamos informarte que no pudimos completar la entrega de tu pedido en esta ocasión.</p>
                  </td>
                </tr>

                <!-- DETAILS -->
                <tr>
                  <td style="padding:16px 40px;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#fffbeb;border-radius:8px;border:1px solid #fde68a;overflow:hidden;">
                      <tr>
                        <td style="padding:16px 20px;border-bottom:1px solid #fef3c7;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">📍 Dirección visitada</span><br>
                          <span style="font-size:14px;color:#333;font-weight:500;margin-top:4px;display:block;">%s</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:16px 20px;border-bottom:1px solid #fef3c7;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">❌ Motivo</span><br>
                          <span style="font-size:14px;color:%s;font-weight:600;margin-top:4px;display:block;">%s</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:16px 20px;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">🔄 Intentos realizados</span><br>
                          <span style="font-size:14px;color:#333;font-weight:500;margin-top:4px;display:block;">%d de 3</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- MESSAGE -->
                <tr>
                  <td style="padding:24px 40px;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#fff7ed;border-radius:8px;border-left:4px solid #d97706;">
                      <tr>
                        <td style="padding:16px 20px;">
                          <p style="font-size:14px;color:#92400e;margin:0;line-height:1.6;">%s</p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="background:#78350f;padding:24px 40px;text-align:center;">
                    <p style="font-size:12px;color:#fcd34d;margin:0;">¿Tienes dudas? Responde este correo y te ayudamos.</p>
                    <p style="font-size:11px;color:#92400e;margin:8px 0 0;">© 2026 Last Mile Delivery — Todos los derechos reservados</p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(
                order.getTrackingCode(),
                order.getRecipientName(),
                order.getAddressText(),
                failureColor,
                failureReason,
                order.getDeliveryAttempts(),
                failureMessage
        );
    }

    private String translateReason(String reason) {
        return switch (reason) {
            case "NOBODY_HOME" -> "Nadie en casa";
            case "INCORRECT_ADDRESS" -> "Dirección incorrecta";
            case "INACCESSIBLE_AREA" -> "Área inaccesible";
            case "CUSTOMER_REJECTED" -> "Cliente rechazó el paquete";
            case "OTHER" -> "Otro motivo";
            default -> reason;
        };
    }
}