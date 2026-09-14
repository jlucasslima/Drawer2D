import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import ponto.PontoGr;
import reta.RetaGr;
import circulo.CirculoGr;
import retangulo.RetanguloGr;
import triangulo.TrianguloGr;

public class PainelDesenho extends JPanel {
    
    // ESTRUTURA DE DADOS (ED) - Armazena tudo permanentemente nos bastidores
    private List<PontoGr> edPontos = new ArrayList<>();
    private List<RetaGr> edRetas = new ArrayList<>();
    private List<CirculoGr> edCirculos = new ArrayList<>();
    private List<RetanguloGr> edRetangulos = new ArrayList<>();
    private List<TrianguloGr> edTriangulos = new ArrayList<>();

    // TELA (View) - O que está sendo renderizado no momento para o usuário
    private List<PontoGr> telaPontos = new ArrayList<>();
    private List<RetaGr> telaRetas = new ArrayList<>();
    private List<CirculoGr> telaCirculos = new ArrayList<>();
    private List<RetanguloGr> telaRetangulos = new ArrayList<>();
    private List<TrianguloGr> telaTriangulos = new ArrayList<>();
    
    private JLabel msg;
    private TipoPrimitivo tipoAtual;
    private Color corAtual;
    private int esp; 
    private String filtroAtual = "Todos";
    
    private int xInicial, yInicial, xTemp, yTemp;
    private boolean desenhando = false;

    public PainelDesenho(JLabel msg, TipoPrimitivo tipoAtual, Color corAtual, int esp) {
        this.msg = msg; this.tipoAtual = tipoAtual; this.corAtual = corAtual; this.esp = esp;
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (PainelDesenho.this.tipoAtual == TipoPrimitivo.SELECAO) {
                    apagarPrimitivoSelecionado(e.getX(), e.getY());
                } else {
                    xInicial = e.getX(); yInicial = e.getY();
                    xTemp = xInicial; yTemp = yInicial;
                    desenhando = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (desenhando) {
                    desenhando = false; xTemp = e.getX(); yTemp = e.getY();
                    
                    // Adiciona a forma tanto na ED (Back-end) quanto na Tela (Front-end)
                    switch (PainelDesenho.this.tipoAtual) {
                        case PONTO: 
                            PontoGr p = new PontoGr(xTemp, yTemp, corAtual, PainelDesenho.this.esp);
                            edPontos.add(p); telaPontos.add(p); 
                            break;
                        case RETA: 
                            RetaGr r = new RetaGr(xInicial, yInicial, xTemp, yTemp, corAtual, PainelDesenho.this.esp);
                            edRetas.add(r); telaRetas.add(r); 
                            break;
                        case CIRCULO: 
                            CirculoGr c = new CirculoGr(xInicial, yInicial, xTemp, yTemp, corAtual, PainelDesenho.this.esp);
                            edCirculos.add(c); telaCirculos.add(c); 
                            break;
                        case RETANGULO: 
                            RetanguloGr ret = new RetanguloGr(xInicial, yInicial, xTemp, yTemp, corAtual, PainelDesenho.this.esp);
                            edRetangulos.add(ret); telaRetangulos.add(ret); 
                            break;
                        case TRIANGULO: 
                            TrianguloGr t = new TrianguloGr(xInicial, yInicial, xTemp, yTemp, corAtual, PainelDesenho.this.esp);
                            edTriangulos.add(t); telaTriangulos.add(t); 
                            break;
                        default: break;
                    }
                    repaint(); 
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (desenhando && tipoAtual != TipoPrimitivo.PONTO) {
                    xTemp = e.getX(); yTemp = e.getY();
                    msg.setText(String.format("Msg: X=%d, Y=%d", xTemp, yTemp));
                    repaint(); 
                }
            }
        });
    }

    private void apagarPrimitivoSelecionado(int mx, int my) {
        int tol = 10;
        
        // Remove da Tela e da ED simultaneamente
        for (int i = telaPontos.size() - 1; i >= 0; i--) {
            PontoGr p = telaPontos.get(i);
            if (Math.abs(p.getX() - mx) <= tol && Math.abs(p.getY() - my) <= tol) { 
                edPontos.remove(p); telaPontos.remove(i); repaint(); return; 
            }
        }
        for (int i = telaCirculos.size() - 1; i >= 0; i--) {
            CirculoGr c = telaCirculos.get(i);
            double dist = Math.sqrt(Math.pow(mx - c.getXCentro(), 2) + Math.pow(my - c.getYCentro(), 2));
            if (Math.abs(dist - c.getRaio()) <= tol) { 
                edCirculos.remove(c); telaCirculos.remove(i); repaint(); return; 
            }
        }
        for (int i = telaRetangulos.size() - 1; i >= 0; i--) {
            RetanguloGr r = telaRetangulos.get(i);
            boolean tocouX = (mx >= r.getX1() - tol && mx <= r.getX2() + tol) && (Math.abs(my - r.getY1()) <= tol || Math.abs(my - r.getY2()) <= tol);
            boolean tocouY = (my >= r.getY1() - tol && my <= r.getY2() + tol) && (Math.abs(mx - r.getX1()) <= tol || Math.abs(mx - r.getX2()) <= tol);
            if (tocouX || tocouY) { 
                edRetangulos.remove(r); telaRetangulos.remove(i); repaint(); return; 
            }
        }
        for (int i = telaRetas.size() - 1; i >= 0; i--) {
            RetaGr r = telaRetas.get(i);
            if (distanciaPontoReta(mx, my, r.getX1(), r.getY1(), r.getX2(), r.getY2()) <= tol) { 
                edRetas.remove(r); telaRetas.remove(i); repaint(); return; 
            }
        }
        for (int i = telaTriangulos.size() - 1; i >= 0; i--) {
            TrianguloGr t = telaTriangulos.get(i);
            int mX = (t.getX1() + t.getX2()) / 2;
            if (distanciaPontoReta(mx, my, mX, t.getY1(), t.getX1(), t.getY2()) <= tol ||
                distanciaPontoReta(mx, my, mX, t.getY1(), t.getX2(), t.getY2()) <= tol ||
                distanciaPontoReta(mx, my, t.getX1(), t.getY2(), t.getX2(), t.getY2()) <= tol) {
                edTriangulos.remove(t); telaTriangulos.remove(i); repaint(); return;
            }
        }
    }

    private double distanciaPontoReta(int px, int py, int x1, int y1, int x2, int y2) {
        int minX = Math.min(x1, x2) - 10; int maxX = Math.max(x1, x2) + 10;
        int minY = Math.min(y1, y2) - 10; int maxY = Math.max(y1, y2) + 10;
        if (px < minX || px > maxX || py < minY || py > maxY) return 9999;
        
        double num = Math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1);
        double den = Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
        if (den == 0) return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));
        return num / den;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Renderiza EXCLUSIVAMENTE o que está na Tela no momento
        for (PontoGr p : telaPontos) p.desenhar(g);
        for (RetaGr r : telaRetas) r.desenhar(g);
        for (CirculoGr c : telaCirculos) c.desenhar(g);
        for (RetanguloGr ret : telaRetangulos) ret.desenhar(g);
        for (TrianguloGr t : telaTriangulos) t.desenhar(g);
        
        if (desenhando) {
            if (tipoAtual == TipoPrimitivo.RETA) { new RetaGr(xInicial, yInicial, xTemp, yTemp, corAtual, esp).desenhar(g); } 
            else if (tipoAtual == TipoPrimitivo.CIRCULO) { new CirculoGr(xInicial, yInicial, xTemp, yTemp, corAtual, esp).desenhar(g); } 
            else if (tipoAtual == TipoPrimitivo.RETANGULO) { new RetanguloGr(xInicial, yInicial, xTemp, yTemp, corAtual, esp).desenhar(g); } 
            else if (tipoAtual == TipoPrimitivo.TRIANGULO) { new TrianguloGr(xInicial, yInicial, xTemp, yTemp, corAtual, esp).desenhar(g); }
        }
    }
    
    public void limparTelaVisualmente() { 
        // Limpa APENAS as listas visuais, mantendo a ED intacta
        telaPontos.clear();
        telaRetas.clear();
        telaCirculos.clear();
        telaRetangulos.clear();
        telaTriangulos.clear();
        repaint(); 
    }

    public void exportarJPEG(String caminho) {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        this.paint(g2); g2.dispose();
        try { ImageIO.write(img, "jpeg", new File(caminho + ".jpeg")); } catch (IOException e) { e.printStackTrace(); }
    }

    public void exportarJSON(String caminho) {
        StringBuilder json = new StringBuilder("{\n  \"figuras\": {\n");

        json.append("    \"pontos\": [\n");
        for (int i = 0; i < edPontos.size(); i++) {
            PontoGr p = edPontos.get(i);
            json.append(String.format("      {\"x\": %d, \"y\": %d}", p.getX(), p.getY()));
            if (i < edPontos.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("    ],\n");

        json.append("    \"retas\": [\n");
        for (int i = 0; i < edRetas.size(); i++) {
            RetaGr r = edRetas.get(i);
            json.append(String.format("      {\"x1\": %d, \"y1\": %d, \"x2\": %d, \"y2\": %d}", r.getX1(), r.getY1(), r.getX2(), r.getY2()));
            if (i < edRetas.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("    ],\n");

        json.append("    \"circulos\": [\n");
        for (int i = 0; i < edCirculos.size(); i++) {
            CirculoGr c = edCirculos.get(i);
            json.append(String.format("      {\"xCentro\": %d, \"yCentro\": %d, \"raio\": %d}", c.getXCentro(), c.getYCentro(), c.getRaio()));
            if (i < edCirculos.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("    ],\n");

        json.append("    \"retangulos\": [\n");
        for (int i = 0; i < edRetangulos.size(); i++) {
            RetanguloGr r = edRetangulos.get(i);
            json.append(String.format("      {\"x1\": %d, \"y1\": %d, \"x2\": %d, \"y2\": %d}", r.getX1(), r.getY1(), r.getX2(), r.getY2()));
            if (i < edRetangulos.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("    ],\n");

        json.append("    \"triangulos\": [\n");
        for (int i = 0; i < edTriangulos.size(); i++) {
            TrianguloGr t = edTriangulos.get(i);
            json.append(String.format("      {\"x1\": %d, \"y1\": %d, \"x2\": %d, \"y2\": %d}", t.getX1(), t.getY1(), t.getX2(), t.getY2()));
            if (i < edTriangulos.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("    ]\n");

        json.append("  }\n}");

        try (FileWriter file = new FileWriter(caminho + ".json")) { file.write(json.toString()); } catch (IOException e) { e.printStackTrace(); }
    }

    public void setTipo(TipoPrimitivo tipo) { this.tipoAtual = tipo; }
    public void setCorAtual(Color cor) { this.corAtual = cor; }
    public void setEsp(int esp) { this.esp = esp; }
    
    public void setFiltroAtual(String filtro) { 
        this.filtroAtual = filtro; 
        
        // Ao acionar um filtro na JComboBox, sincronizamos a Tela com o banco da ED
        limparTelaVisualmente();
        boolean todos = filtro.equals("Todos");
        
        if (todos || filtro.equals("Pontos")) telaPontos.addAll(edPontos);
        if (todos || filtro.equals("Retas")) telaRetas.addAll(edRetas);
        if (todos || filtro.equals("Circulos")) telaCirculos.addAll(edCirculos);
        if (todos || filtro.equals("Retangulos")) telaRetangulos.addAll(edRetangulos);
        if (todos || filtro.equals("Triangulos")) telaTriangulos.addAll(edTriangulos);
        
        repaint(); 
    }
}