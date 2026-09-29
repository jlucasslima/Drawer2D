package circulo;
import java.awt.Color;
import java.awt.Graphics;

public class CirculoGr {
    private int x1, y1, x2, y2, esp, raio;
    private Color cor;

    public CirculoGr(int x1, int y1, int x2, int y2, Color cor, int esp) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.cor = cor; this.esp = esp;
        this.raio = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    public void desenhar(Graphics g) {
        g.setColor(cor);
        int x = 0, y = raio, d = 1 - raio;
        desenharOctantes(g, x1, y1, x, y);
        while (y > x) {
            if (d < 0) { d += 2 * x + 3; } else { d += 2 * (x - y) + 5; y--; }
            x++;
            desenharOctantes(g, x1, y1, x, y);
        }
    }
    private void desenharOctantes(Graphics g, int xc, int yc, int x, int y) {
        g.fillRect(xc + x - esp/2, yc + y - esp/2, esp, esp); g.fillRect(xc - x - esp/2, yc + y - esp/2, esp, esp);
        g.fillRect(xc + x - esp/2, yc - y - esp/2, esp, esp); g.fillRect(xc - x - esp/2, yc - y - esp/2, esp, esp);
        g.fillRect(xc + y - esp/2, yc + x - esp/2, esp, esp); g.fillRect(xc - y - esp/2, yc + x - esp/2, esp, esp);
        g.fillRect(xc + y - esp/2, yc - x - esp/2, esp, esp); g.fillRect(xc - y - esp/2, yc - x - esp/2, esp, esp);
    }
    public int getXCentro() { return x1; }
    public int getYCentro() { return y1; }
    public int getRaio() { return raio; }
    public Color getCor() { return cor; }
    public int getEsp() { return esp; }
}