package triangulo;
import reta.RetaGr;
import java.awt.Color;
import java.awt.Graphics;

public class TrianguloGr {
    private int x1, y1, x2, y2, esp;
    private Color cor;

    // Quando o triangulo vem de um espelhamento em relacao a uma reta qualquer,
    // o resultado pode nao ser mais isosceles com base horizontal, entao guardamos
    // os 3 vertices reais.
    private boolean tresVertices = false;
    private int vx1, vy1, vx2, vy2, vx3, vy3;

    public TrianguloGr(int x1, int y1, int x2, int y2, Color cor, int esp) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.cor = cor; this.esp = esp;
    }

    // Construtor para triangulo ja definido pelos 3 vertices (resultado de um espelhamento)
    public TrianguloGr(int vx1, int vy1, int vx2, int vy2, int vx3, int vy3, Color cor, int esp) {
        this.tresVertices = true;
        this.vx1 = vx1; this.vy1 = vy1;
        this.vx2 = vx2; this.vy2 = vy2;
        this.vx3 = vx3; this.vy3 = vy3;
        this.cor = cor; this.esp = esp;
        this.x1 = Math.min(Math.min(vx1, vx2), vx3);
        this.y1 = Math.min(Math.min(vy1, vy2), vy3);
        this.x2 = Math.max(Math.max(vx1, vx2), vx3);
        this.y2 = Math.max(Math.max(vy1, vy2), vy3);
    }

    public void desenhar(Graphics g) {
        if (tresVertices) {
            new RetaGr(vx1, vy1, vx2, vy2, cor, esp).desenhar(g);
            new RetaGr(vx2, vy2, vx3, vy3, cor, esp).desenhar(g);
            new RetaGr(vx3, vy3, vx1, vy1, cor, esp).desenhar(g);
        } else {
            int meioX = (x1 + x2) / 2;
            new RetaGr(meioX, y1, x1, y2, cor, esp).desenhar(g);
            new RetaGr(meioX, y1, x2, y2, cor, esp).desenhar(g);
            new RetaGr(x1, y2, x2, y2, cor, esp).desenhar(g);
        }
    }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public Color getCor() { return cor; }
    public int getEsp() { return esp; }

    public boolean isTresVertices() { return tresVertices; }
    // Os 3 vertices reais do triangulo (uteis para espelhar de novo um triangulo ja espelhado)
    public int getVX1() { return tresVertices ? vx1 : (x1 + x2) / 2; }
    public int getVY1() { return tresVertices ? vy1 : y1; }
    public int getVX2() { return tresVertices ? vx2 : x1; }
    public int getVY2() { return tresVertices ? vy2 : y2; }
    public int getVX3() { return tresVertices ? vx3 : x2; }
    public int getVY3() { return tresVertices ? vy3 : y2; }
}
