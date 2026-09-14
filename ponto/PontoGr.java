package ponto;
import java.awt.Color;
import java.awt.Graphics;

public class PontoGr {
    private int x, y, esp;
    private Color cor;

    public PontoGr(int x, int y, Color cor, int esp) {
        this.x = x; this.y = y; this.cor = cor; this.esp = esp;
    }
    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(x - esp/2, y - esp/2, esp, esp); 
    }
    public int getX() { return x; }
    public int getY() { return y; }
}