package reta;
import java.awt.Color;
import java.awt.Graphics;

public class RetaGr {
    private int x1, y1, x2, y2, esp;
    private Color cor;

    public RetaGr(int x1, int y1, int x2, int y2, Color cor, int esp) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.cor = cor; this.esp = esp;
    }
    public void desenhar(Graphics g) {
        g.setColor(cor);
        int x = x1, y = y1;
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1, sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            g.fillRect(x - esp/2, y - esp/2, esp, esp);
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public Color getCor() { return cor; }
    public int getEsp() { return esp; }
}