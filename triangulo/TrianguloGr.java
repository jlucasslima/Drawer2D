package triangulo;
import reta.RetaGr;
import java.awt.Color;
import java.awt.Graphics;

public class TrianguloGr {
    private int x1, y1, x2, y2, esp;
    private Color cor;

    public TrianguloGr(int x1, int y1, int x2, int y2, Color cor, int esp) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.cor = cor; this.esp = esp;
    }
    public void desenhar(Graphics g) {
        int meioX = (x1 + x2) / 2;
        new RetaGr(meioX, y1, x1, y2, cor, esp).desenhar(g);
        new RetaGr(meioX, y1, x2, y2, cor, esp).desenhar(g);
        new RetaGr(x1, y2, x2, y2, cor, esp).desenhar(g);
    }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
}