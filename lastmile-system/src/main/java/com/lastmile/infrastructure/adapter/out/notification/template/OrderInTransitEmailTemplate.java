package com.lastmile.infrastructure.adapter.out.notification.template;

import com.lastmile.domain.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderInTransitEmailTemplate {

    public String subject(Order order) {
        return "🚴 Tu pedido está en camino — " + order.getTrackingCode();
    }

    public String build(Order order) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Pedido en Camino</title>
        </head>
        <body style="margin:0;padding:0;background:#f4f4f4;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:40px 0;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                <!-- HEADER -->
                <tr>
                  <td style="background:linear-gradient(135deg,#0ea5e9,#0284c7);padding:32px 40px;text-align:center;">
                    <div style="font-size:13px;color:#bae6fd;letter-spacing:3px;text-transform:uppercase;margin-bottom:8px;">Last Mile Delivery</div>
                    <div style="font-size:48px;margin-bottom:8px;">🚴</div>
                    <div style="font-size:26px;font-weight:700;color:#ffffff;letter-spacing:1px;">EN CAMINO</div>
                    <div style="margin-top:12px;font-size:14px;color:#bae6fd;">Tu pedido está de camino a ti</div>
                  </td>
                </tr>

                <!-- TRACKING CODE -->
                <tr>
                  <td style="padding:32px 40px;text-align:center;background:#f0f9ff;border-bottom:1px solid #e0f2fe;">
                    <div style="font-size:11px;color:#999;letter-spacing:2px;text-transform:uppercase;margin-bottom:8px;">Código de seguimiento</div>
                    <div style="font-size:26px;font-weight:700;color:#0284c7;letter-spacing:3px;font-family:monospace;">%s</div>
                  </td>
                </tr>

                <!-- GREETING -->
                <tr>
                  <td style="padding:32px 40px 16px;">
                    <p style="font-size:16px;color:#333;margin:0;">Hola <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#555;line-height:1.6;margin:12px 0 0;">¡Buenas noticias! Un courier ya está en camino hacia tu dirección con tu pedido.</p>
                  </td>
                </tr>

                <!-- DETAILS -->
                <tr>
                  <td style="padding:16px 40px 32px;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f9ff;border-radius:8px;border:1px solid #bae6fd;overflow:hidden;">
                      <tr>
                        <td style="padding:16px 20px;border-bottom:1px solid #e0f2fe;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">📍 Dirección de entrega</span><br>
                          <span style="font-size:14px;color:#333;font-weight:500;margin-top:4px;display:block;">%s</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:16px 20px;">
                          <span style="font-size:12px;color:#888;text-transform:uppercase;letter-spacing:1px;">⚠️ Recomendación</span><br>
                          <span style="font-size:14px;color:#0284c7;font-weight:500;margin-top:4px;display:block;">Asegúrate de estar disponible para recibir tu pedido.</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="background:#0c4a6e;padding:24px 40px;text-align:center;">
                    <p style="font-size:12px;color:#7dd3fc;margin:0;">Te notificaremos cuando tu pedido sea entregado.</p>
                    <p style="font-size:11px;color:#075985;margin:8px 0 0;">© 2026 Last Mile Delivery — Todos los derechos reservados</p>
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