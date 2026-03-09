package com.lastmile.infrastructure.adapter.out.notification.template;

import com.lastmile.domain.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderDeliveredEmailTemplate {

    public String subject(Order order) {
        return "🎉 ¡Pedido entregado! — " + order.getTrackingCode();
    }

    public String build(Order order) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Pedido Entregado</title>
        </head>
        <body style="margin:0;padding:0;background:#f4f4f4;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:40px 0;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                <!-- HEADER -->
                <tr>
                  <td style="background:linear-gradient(135deg,#16a34a,#15803d);padding:40px 40px 32px;text-align:center;">
                    <div style="font-size:13px;color:#bbf7d0;letter-spacing:3px;text-transform:uppercase;margin-bottom:8px;">Last Mile Delivery</div>
                    <div style="font-size:56px;margin-bottom:8px;">🎉</div>
                    <div style="font-size:28px;font-weight:700;color:#ffffff;letter-spacing:1px;">¡ENTREGADO!</div>
                    <div style="margin-top:12px;display:inline-block;background:rgba(255,255,255,0.15);border-radius:50px;padding:8px 24px;">
                      <span style="font-size:13px;color:#ffffff;font-weight:600;letter-spacing:1px;">✓ ENTREGA EXITOSA</span>
                    </div>
                  </td>
                </tr>

                <!-- TRACKING CODE -->
                <tr>
                  <td style="padding:32px 40px;text-align:center;background:#f0fdf4;border-bottom:1px solid #dcfce7;">
                    <div style="font-size:11px;color:#999;letter-spacing:2px;text-transform:uppercase;margin-bottom:8px;">Código de seguimiento</div>
                    <div style="font-size:26px;font-weight:700;color:#16a34a;letter-spacing:3px;font-family:monospace;">%s</div>
                  </td>
                </tr>

                <!-- GREETING -->
                <tr>
                  <td style="padding:32px 40px 16px;">
                    <p style="font-size:16px;color:#333;margin:0;">Hola <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#555;line-height:1.6;margin:12px 0 0;">Tu pedido fue entregado exitosamente en la dirección indicada. Esperamos que todo haya llegado en perfecto estado.</p>
                  </td>
                </tr>

                <!-- DETAILS -->
                <tr>
                  <td style="padding:16px 40px;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0fdf4;border-radius:8px;border:1px solid #bbf7d0;overflow:hidden;">
                      <tr>
                        <td style="padding:16px 20px;border-bottom:1px solid #dcfce7;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">📍 Entregado en</span><br>
                          <span style="font-size:14px;color:#333;font-weight:500;margin-top:4px;display:block;">%s</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:16px 20px;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">✅ Estado final</span><br>
                          <span style="font-size:14px;color:#16a34a;font-weight:600;margin-top:4px;display:block;">Entregado exitosamente</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- THANK YOU -->
                <tr>
                  <td style="padding:24px 40px 32px;text-align:center;">
                    <p style="font-size:15px;color:#555;line-height:1.6;margin:0;">Gracias por confiar en <strong>Last Mile Delivery</strong>.<br>Esperamos verte pronto.</p>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="background:#14532d;padding:24px 40px;text-align:center;">
                    <p style="font-size:12px;color:#86efac;margin:0;">¿Tuviste algún problema? Contáctanos y te ayudamos.</p>
                    <p style="font-size:11px;color:#166534;margin:8px 0 0;">© 2026 Last Mile Delivery — Todos los derechos reservados</p>
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
                order.getAddressText()
        );
    }
}