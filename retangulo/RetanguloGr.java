package retangulo;
import reta.RetaGr;
import java.awt.Color;
import java.awt.Graphics;

public class RetanguloGr {
    private int x1, y1, x2, y2, esp;
    private Color cor;

    // Quando o retangulo vem de um espelhamento em relacao a uma reta qualquer,
    // o resultado pode nao ser mais eixo-alinhado, entao guardamos os 4 vertices reais.
    private boolean quatroVertices = false;
    private int vx1, vy1, vx2, vy2, vx3, vy3, vx4, vy4;

    public RetanguloGr(int x1, int y1, int x2, int y2, Color cor, int esp) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.cor = cor; this.esp = esp;
    }

    // Construtor para retangulo ja definido pelos 4 vertices (resultado de um espelhamento)
    public RetanguloGr(int vx1, int vy1, int vx2, int vy2, int vx3, int vy3, int vx4, int vy4, Color cor, int esp) {
        this.quatroVertices = true;
        this.vx1 = vx1; this.vy1 = vy1;
        this.vx2 = vx2; this.vy2 = vy2;
        this.vx3 = vx3; this.vy3 = vy3;
        this.vx4 = vx4; this.vy4 = vy4;
        this.cor = cor; this.esp = esp;
        // x1,y1,x2,y2 guardam o retangulo envolvente (bounding box), usado pela
        // ferramenta de selecao/borracha.
        this.x1 = Math.min(Math.min(vx1, vx2), Math.min(vx3, vx4));
        this.y1 = Math.min(Math.min(vy1, vy2), Math.min(vy3, vy4));
        this.x2 = Math.max(Math.max(vx1, vx2), Math.max(vx3, vx4));
        this.y2 = Math.max(Math.max(vy1, vy2), Math.max(vy3, vy4));
    }

    public void desenhar(Graphics g) {
        if (quatroVertices) {
            new RetaGr(vx1, vy1, vx2, vy2, cor, esp).desenhar(g);
            new RetaGr(vx2, vy2, vx3, vy3, cor, esp).desenhar(g);
            new RetaGr(vx3, vy3, vx4, vy4, cor, esp).desenhar(g);
            new RetaGr(vx4, vy4, vx1, vy1, cor, esp).desenhar(g);
        } else {
            new RetaGr(x1, y1, x2, y1, cor, esp).desenhar(g);
            new RetaGr(x1, y2, x2, y2, cor, esp).desenhar(g);
            new RetaGr(x1, y1, x1, y2, cor, esp).desenhar(g);
            new RetaGr(x2, y1, x2, y2, cor, esp).desenhar(g);
        }
    }
    public int getX1() { return Math.min(x1, x2); }
    public int getY1() { return Math.min(y1, y2); }
    public int getX2() { return Math.max(x1, x2); }
    public int getY2() { return Math.max(y1, y2); }
    public Color getCor() { return cor; }
    public int getEsp() { return esp; }

    public boolean isQuatroVertices() { return quatroVertices; }
    // Os 4 vertices reais do retangulo (uteis para espelhar de novo um retangulo ja espelhado)
    public int getVX1() { return quatroVertices ? vx1 : x1; }
    public int getVY1() { return quatroVertices ? vy1 : y1; }
    public int getVX2() { return quatroVertices ? vx2 : x2; }
    public int getVY2() { return quatroVertices ? vy2 : y1; }
    public int getVX3() { return quatroVertices ? vx3 : x2; }
    public int getVY3() { return quatroVertices ? vy3 : y2; }
    public int getVX4() { return quatroVertices ? vx4 : x1; }
    public int getVY4() { return quatroVertices ? vy4 : y2; }
}
