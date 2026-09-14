package retangulo;
import reta.RetaGr;
import java.awt.Color;
import java.awt.Graphics;

public class RetanguloGr {
    private int x1, y1, x2, y2, esp;
    private Color cor;

    public RetanguloGr(int x1, int y1, int x2, int y2, Color cor, int esp) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.cor = cor; this.esp = esp;
    }
    public void desenhar(Graphics g) {
        new RetaGr(x1, y1, x2, y1, cor, esp).desenhar(g);
        new RetaGr(x1, y2, x2, y2, cor, esp).desenhar(g);
        new RetaGr(x1, y1, x1, y2, cor, esp).desenhar(g);
        new RetaGr(x2, y1, x2, y2, cor, esp).desenhar(g);
    }
    public int getX1() { return Math.min(x1, x2); }
    public int getY1() { return Math.min(y1, y2); }
    public int getX2() { return Math.max(x1, x2); }
    public int getY2() { return Math.max(y1, y2); }
}