package com.lastmile.infrastructure.adapter.out.notification.template;

import com.lastmile.domain.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEmailTemplate {

    public String subject(Order order) {
        return "✅ Pedido confirmado — " + order.getTrackingCode();
    }

    public String build(Order order) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Pedido Confirmado</title>
        </head>
        <body style="margin:0;padding:0;background:#f4f4f4;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:40px 0;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                
                <!-- HEADER -->
                <tr>
                  <td style="background:#1a1a2e;padding:32px 40px;text-align:center;">
                    <div style="font-size:13px;color:#a0a0b0;letter-spacing:3px;text-transform:uppercase;margin-bottom:8px;">Last Mile Delivery</div>
                    <div style="font-size:28px;font-weight:700;color:#ffffff;letter-spacing:1px;">PEDIDO CONFIRMADO</div>
                    <div style="margin-top:16px;display:inline-block;background:#22c55e;border-radius:50px;padding:8px 24px;">
                      <span style="font-size:13px;color:#ffffff;font-weight:600;letter-spacing:1px;">✓ REGISTRADO EXITOSAMENTE</span>
                    </div>
                  </td>
                </tr>

                <!-- TRACKING CODE -->
                <tr>
                  <td style="padding:32px 40px;text-align:center;background:#fafafa;border-bottom:1px solid #eeeeee;">
                    <div style="font-size:11px;color:#999;letter-spacing:2px;text-transform:uppercase;margin-bottom:8px;">Tu código de seguimiento</div>
                    <div style="font-size:28px;font-weight:700;color:#1a1a2e;letter-spacing:3px;font-family:monospace;">%s</div>
                  </td>
                </tr>

                <!-- GREETING -->
                <tr>
                  <td style="padding:32px 40px 16px;">
                    <p style="font-size:16px;color:#333;margin:0;">Hola <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#555;line-height:1.6;margin:12px 0 0;">Tu pedido ha sido registrado en nuestro sistema y será procesado a la brevedad.</p>
                  </td>
                </tr>

                <!-- DETAILS -->
                <tr>
                  <td style="padding:16px 40px 32px;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8f9ff;border-radius:8px;border:1px solid #e8eaf6;overflow:hidden;">
                      <tr>
                        <td style="padding:16px 20px;border-bottom:1px solid #e8eaf6;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">📍 Dirección de entrega</span><br>
                          <span style="font-size:14px;color:#333;font-weight:500;margin-top:4px;display:block;">%s</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:16px 20px;border-bottom:1px solid #e8eaf6;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">📅 Fecha límite de entrega</span><br>
                          <span style="font-size:14px;color:#333;font-weight:500;margin-top:4px;display:block;">%s</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:16px 20px;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">⚡ Prioridad</span><br>
                          <span style="font-size:14px;font-weight:600;margin-top:4px;display:block;color:%s;">%s</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="background:#1a1a2e;padding:24px 40px;text-align:center;">
                    <p style="font-size:12px;color:#a0a0b0;margin:0;">Te notificaremos cuando tu pedido esté en camino.</p>
                    <p style="font-size:11px;color:#666;margin:8px 0 0;">© 2026 Last Mile Delivery — Todos los derechos reservados</p>
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
                order.getDeliveryDeadline().toString(),
                order.getPriority() == com.lastmile.domain.model.OrderPriority.EXPRESS ? "#f59e0b" : "#22c55e",
                order.getPriority() == com.lastmile.domain.model.OrderPriority.EXPRESS ? "EXPRESS 🚀" : "STANDARD"
        );
    }
}