package de.shop.util.web;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Random;

import javax.enterprise.context.SessionScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import de.shop.auth.service.AuthService;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
public class Captcha implements Serializable {
	private static final long serialVersionUID = -2422584806291795656L;
	
	private static final int WIDTH = 110;
	private static final int HEIGHT = 50;
	private static final int X_COORD = 20;
	private static final int Y_COORD = 35;
	private static final Color BACKGROUND = new Color(190, 214, 248);
	private static final Color DRAW_COLOR = new Color(0, 0, 0);
	private static final Font FONT = new Font(Font.SERIF, Font.TRUETYPE_FONT, 30);
	private static final int CAPTCHA_LENGTH = 4;
	
	@Inject
	private AuthService authService;
	
	private String value;

	public String getValue() {
		return value;
	}
	
	public void paint(OutputStream stream, Object unused) throws IOException {
		value = generateString();
		
		final BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D graphics2D = img.createGraphics();
		graphics2D.setBackground(BACKGROUND);
		graphics2D.setColor(DRAW_COLOR);
		graphics2D.clearRect(0, 0, WIDTH, HEIGHT); // x, y, Breite, Hoehe
		graphics2D.setFont(FONT);
		graphics2D.drawString(value, X_COORD, Y_COORD); // String zeichnen an x-/y-Koordinaten
		
		ImageIO.write(img, "png", stream);  // PNG-Bild erzeugen
	}
	
	private String generateString() {
		// Zufallszahl generieren
		final Random generator = new Random(System.currentTimeMillis());
		final long longValue = generator.nextLong();
		
		// Zufallszahl wie ein Passwort verschluesseln und dadurch einen Base64-String produzieren
		final char[] hashValue = authService.verschluesseln(String.valueOf(longValue)).toCharArray();
		
		// die ersten 4 Buchstaben und Ziffern aus dem Base64-String extrahieren
		final StringBuilder sb = new StringBuilder();
		for (char c : hashValue) {
			if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			}
			if (sb.length() == CAPTCHA_LENGTH) {
				break;
			}
		}
		
		if (sb.length() < CAPTCHA_LENGTH) {
			// Der Base64-String enthaelt weniger als 4 Buchstaben und Ziffern, d.h. fast nur + und /
			return generateString();
		}
		
		return sb.toString();
	}
}
