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

    // ===== Estado da ferramenta de Espelhamento (reflexao em relacao a uma reta) =====
    private TipoPrimitivo tipoSelecionado = null; // tipo do primitivo selecionado p/ espelhar
    private Object objSelecionado = null;         // referencia ao primitivo selecionado
    private boolean desenhandoEixo = false;        // true enquanto o usuario arrasta o eixo (p1->p2)
    private int exInicial, eyInicial, exTemp, eyTemp; // pontos do eixo de espelhamento

    public PainelDesenho(JLabel msg, TipoPrimitivo tipoAtual, Color corAtual, int esp) {
        this.msg = msg; this.tipoAtual = tipoAtual; this.corAtual = corAtual; this.esp = esp;
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (PainelDesenho.this.tipoAtual == TipoPrimitivo.SELECAO) {
                    apagarPrimitivoSelecionado(e.getX(), e.getY());
                } else if (PainelDesenho.this.tipoAtual == TipoPrimitivo.ESPELHO) {
                    tratarCliqueEspelho(e.getX(), e.getY());
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
                            PontoGr p = new PontoGr(xTemp, yTemp, PainelDesenho.this.corAtual, PainelDesenho.this.esp);
                            edPontos.add(p); telaPontos.add(p); 
                            break;
                        case RETA: 
                            RetaGr r = new RetaGr(xInicial, yInicial, xTemp, yTemp, PainelDesenho.this.corAtual, PainelDesenho.this.esp);
                            edRetas.add(r); telaRetas.add(r); 
                            break;
                        case CIRCULO: 
                            CirculoGr c = new CirculoGr(xInicial, yInicial, xTemp, yTemp, PainelDesenho.this.corAtual, PainelDesenho.this.esp);
                            edCirculos.add(c); telaCirculos.add(c); 
                            break;
                        case RETANGULO: 
                            RetanguloGr ret = new RetanguloGr(xInicial, yInicial, xTemp, yTemp, PainelDesenho.this.corAtual, PainelDesenho.this.esp);
                            edRetangulos.add(ret); telaRetangulos.add(ret); 
                            break;
                        case TRIANGULO: 
                            TrianguloGr t = new TrianguloGr(xInicial, yInicial, xTemp, yTemp, PainelDesenho.this.corAtual, PainelDesenho.this.esp);
                            edTriangulos.add(t); telaTriangulos.add(t); 
                            break;
                        default: break;
                    }
                    repaint(); 
                } else if (desenhandoEixo) {
                    // Finaliza o eixo de espelhamento (p2) e espelha o primitivo selecionado
                    desenhandoEixo = false;
                    exTemp = e.getX(); eyTemp = e.getY();
                    espelharSelecionado(exInicial, eyInicial, exTemp, eyTemp);
                    tipoSelecionado = null; objSelecionado = null;
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
                } else if (desenhandoEixo) {
                    exTemp = e.getX(); eyTemp = e.getY();
                    msg.setText(String.format("Msg: eixo de espelhamento (%d,%d) -> (%d,%d)", exInicial, eyInicial, exTemp, eyTemp));
                    repaint();
                }
            }
        });
    }

    // ================== ESPELHAMENTO (reflexao em relacao a uma reta qualquer) ==================

    // Primeiro clique com a ferramenta "Espelhar": seleciona o primitivo mais proximo.
    // Segundo clique (arrastando): define o eixo (p1, p2) e dispara o espelhamento.
    private void tratarCliqueEspelho(int mx, int my) {
        if (objSelecionado == null) {
            Object[] achado = localizarPrimitivo(mx, my);
            if (achado != null) {
                tipoSelecionado = (TipoPrimitivo) achado[0];
                objSelecionado = achado[1];
                msg.setText("Msg: primitivo selecionado! Clique e arraste para desenhar o eixo de espelhamento.");
                repaint();
            } else {
                msg.setText("Msg: clique sobre um primitivo para seleciona-lo.");
            }
        } else {
            exInicial = mx; eyInicial = my;
            exTemp = mx; eyTemp = my;
            desenhandoEixo = true;
        }
    }

    // Localiza (sem remover) o primitivo mais proximo do clique. Mesma logica/tolerancia
    // usada pela borracha (apagarPrimitivoSelecionado), mas devolve a referencia em vez de apagar.
    private Object[] localizarPrimitivo(int mx, int my) {
        int tol = 10;

        for (int i = telaPontos.size() - 1; i >= 0; i--) {
            PontoGr p = telaPontos.get(i);
            if (Math.abs(p.getX() - mx) <= tol && Math.abs(p.getY() - my) <= tol) {
                return new Object[]{ TipoPrimitivo.PONTO, p };
            }
        }
        for (int i = telaCirculos.size() - 1; i >= 0; i--) {
            CirculoGr c = telaCirculos.get(i);
            double dist = Math.sqrt(Math.pow(mx - c.getXCentro(), 2) + Math.pow(my - c.getYCentro(), 2));
            if (Math.abs(dist - c.getRaio()) <= tol) {
                return new Object[]{ TipoPrimitivo.CIRCULO, c };
            }
        }
        for (int i = telaRetangulos.size() - 1; i >= 0; i--) {
            RetanguloGr r = telaRetangulos.get(i);
            boolean tocouX = (mx >= r.getX1() - tol && mx <= r.getX2() + tol) && (Math.abs(my - r.getY1()) <= tol || Math.abs(my - r.getY2()) <= tol);
            boolean tocouY = (my >= r.getY1() - tol && my <= r.getY2() + tol) && (Math.abs(mx - r.getX1()) <= tol || Math.abs(mx - r.getX2()) <= tol);
            if (tocouX || tocouY) {
                return new Object[]{ TipoPrimitivo.RETANGULO, r };
            }
        }
        for (int i = telaRetas.size() - 1; i >= 0; i--) {
            RetaGr r = telaRetas.get(i);
            if (distanciaPontoReta(mx, my, r.getX1(), r.getY1(), r.getX2(), r.getY2()) <= tol) {
                return new Object[]{ TipoPrimitivo.RETA, r };
            }
        }
        for (int i = telaTriangulos.size() - 1; i >= 0; i--) {
            TrianguloGr t = telaTriangulos.get(i);
            if (distanciaPontoReta(mx, my, t.getVX1(), t.getVY1(), t.getVX2(), t.getVY2()) <= tol ||
                distanciaPontoReta(mx, my, t.getVX2(), t.getVY2(), t.getVX3(), t.getVY3()) <= tol ||
                distanciaPontoReta(mx, my, t.getVX3(), t.getVY3(), t.getVX1(), t.getVY1()) <= tol) {
                return new Object[]{ TipoPrimitivo.TRIANGULO, t };
            }
        }
        return null;
    }

    // Reflete o ponto (px,py) em relacao a reta definida por (x1,y1)-(x2,y2)
    private int[] refletirPonto(int px, int py, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double comp2 = dx * dx + dy * dy;
        if (comp2 == 0) return new int[]{ px, py }; // eixo degenerado (p1 == p2): nao ha o que espelhar

        double a = (dx * dx - dy * dy) / comp2;
        double b = (2 * dx * dy) / comp2;
        double relX = px - x1, relY = py - y1;

        double rx = a * relX + b * relY;
        double ry = b * relX - a * relY;

        return new int[]{ (int) Math.round(rx + x1), (int) Math.round(ry + y1) };
    }

    // Cria o primitivo espelhado (mesma cor/espessura do original) e o inclui na ED + Tela
    private void espelharSelecionado(int ax1, int ay1, int ax2, int ay2) {
        if (objSelecionado == null) return;
        if (ax1 == ax2 && ay1 == ay2) {
            msg.setText("Msg: eixo invalido - clique e arraste para definir uma reta.");
            return;
        }

        switch (tipoSelecionado) {
            case PONTO: {
                PontoGr p = (PontoGr) objSelecionado;
                int[] r = refletirPonto(p.getX(), p.getY(), ax1, ay1, ax2, ay2);
                PontoGr novo = new PontoGr(r[0], r[1], p.getCor(), p.getEsp());
                edPontos.add(novo); telaPontos.add(novo);
                break;
            }
            case RETA: {
                RetaGr rt = (RetaGr) objSelecionado;
                int[] r1 = refletirPonto(rt.getX1(), rt.getY1(), ax1, ay1, ax2, ay2);
                int[] r2 = refletirPonto(rt.getX2(), rt.getY2(), ax1, ay1, ax2, ay2);
                RetaGr novo = new RetaGr(r1[0], r1[1], r2[0], r2[1], rt.getCor(), rt.getEsp());
                edRetas.add(novo); telaRetas.add(novo);
                break;
            }
            case CIRCULO: {
                CirculoGr c = (CirculoGr) objSelecionado;
                int[] rc = refletirPonto(c.getXCentro(), c.getYCentro(), ax1, ay1, ax2, ay2);
                // O raio e' preservado (reflexao mantem distancias); sintetiza um 2o ponto
                // a "raio" pixels do novo centro para reaproveitar o construtor existente.
                CirculoGr novo = new CirculoGr(rc[0], rc[1], rc[0] + c.getRaio(), rc[1], c.getCor(), c.getEsp());
                edCirculos.add(novo); telaCirculos.add(novo);
                break;
            }
            case RETANGULO: {
                RetanguloGr rg = (RetanguloGr) objSelecionado;
                int[] v1 = refletirPonto(rg.getVX1(), rg.getVY1(), ax1, ay1, ax2, ay2);
                int[] v2 = refletirPonto(rg.getVX2(), rg.getVY2(), ax1, ay1, ax2, ay2);
                int[] v3 = refletirPonto(rg.getVX3(), rg.getVY3(), ax1, ay1, ax2, ay2);
                int[] v4 = refletirPonto(rg.getVX4(), rg.getVY4(), ax1, ay1, ax2, ay2);
                RetanguloGr novo = new RetanguloGr(
                        v1[0], v1[1], v2[0], v2[1], v3[0], v3[1], v4[0], v4[1],
                        rg.getCor(), rg.getEsp());
                edRetangulos.add(novo); telaRetangulos.add(novo);
                break;
            }
            case TRIANGULO: {
                TrianguloGr t = (TrianguloGr) objSelecionado;
                int[] v1 = refletirPonto(t.getVX1(), t.getVY1(), ax1, ay1, ax2, ay2);
                int[] v2 = refletirPonto(t.getVX2(), t.getVY2(), ax1, ay1, ax2, ay2);
                int[] v3 = refletirPonto(t.getVX3(), t.getVY3(), ax1, ay1, ax2, ay2);
                TrianguloGr novo = new TrianguloGr(
                        v1[0], v1[1], v2[0], v2[1], v3[0], v3[1],
                        t.getCor(), t.getEsp());
                edTriangulos.add(novo); telaTriangulos.add(novo);
                break;
            }
            default: break;
        }
        msg.setText("Msg: primitivo espelhado e incluido na ED!");
    }

    // Destaca visualmente o primitivo selecionado com a ferramenta de espelhamento
    private void desenharDestaqueSelecao(Graphics g) {
        if (objSelecionado == null || !(g instanceof Graphics2D)) return;
        Graphics2D g2 = (Graphics2D) g;
        java.awt.Stroke strokeOriginal = g2.getStroke();
        Color corOriginal = g2.getColor();
        g2.setColor(Color.ORANGE);
        g2.setStroke(new java.awt.BasicStroke(2, java.awt.BasicStroke.CAP_BUTT,
                java.awt.BasicStroke.JOIN_MITER, 10, new float[]{4, 4}, 0));

        int m = 8; // margem do destaque
        switch (tipoSelecionado) {
            case PONTO: {
                PontoGr p = (PontoGr) objSelecionado;
                g2.drawOval(p.getX() - m, p.getY() - m, m * 2, m * 2);
                break;
            }
            case RETA: {
                RetaGr r = (RetaGr) objSelecionado;
                int minX = Math.min(r.getX1(), r.getX2()) - m, minY = Math.min(r.getY1(), r.getY2()) - m;
                g2.drawRect(minX, minY, Math.abs(r.getX2() - r.getX1()) + 2 * m, Math.abs(r.getY2() - r.getY1()) + 2 * m);
                break;
            }
            case CIRCULO: {
                CirculoGr c = (CirculoGr) objSelecionado;
                int raio = c.getRaio() + m;
                g2.drawOval(c.getXCentro() - raio, c.getYCentro() - raio, raio * 2, raio * 2);
                break;
            }
            case RETANGULO: {
                RetanguloGr rg = (RetanguloGr) objSelecionado;
                g2.drawRect(rg.getX1() - m, rg.getY1() - m, (rg.getX2() - rg.getX1()) + 2 * m, (rg.getY2() - rg.getY1()) + 2 * m);
                break;
            }
            case TRIANGULO: {
                TrianguloGr t = (TrianguloGr) objSelecionado;
                int minX = Math.min(Math.min(t.getVX1(), t.getVX2()), t.getVX3()) - m;
                int minY = Math.min(Math.min(t.getVY1(), t.getVY2()), t.getVY3()) - m;
                int maxX = Math.max(Math.max(t.getVX1(), t.getVX2()), t.getVX3()) + m;
                int maxY = Math.max(Math.max(t.getVY1(), t.getVY2()), t.getVY3()) + m;
                g2.drawRect(minX, minY, maxX - minX, maxY - minY);
                break;
            }
            default: break;
        }
        g2.setStroke(strokeOriginal);
        g2.setColor(corOriginal);
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
            if (distanciaPontoReta(mx, my, t.getVX1(), t.getVY1(), t.getVX2(), t.getVY2()) <= tol ||
                distanciaPontoReta(mx, my, t.getVX2(), t.getVY2(), t.getVX3(), t.getVY3()) <= tol ||
                distanciaPontoReta(mx, my, t.getVX3(), t.getVY3(), t.getVX1(), t.getVY1()) <= tol) {
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

        // Destaca o primitivo selecionado e mostra a previa do eixo de espelhamento
        if (tipoAtual == TipoPrimitivo.ESPELHO) {
            desenharDestaqueSelecao(g);
            if (desenhandoEixo && g instanceof Graphics2D) {
                Graphics2D g2 = (Graphics2D) g;
                java.awt.Stroke strokeOriginal = g2.getStroke();
                Color corOriginal = g2.getColor();
                g2.setColor(Color.GRAY);
                g2.setStroke(new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT,
                        java.awt.BasicStroke.JOIN_MITER, 10, new float[]{6, 6}, 0));
                g2.drawLine(exInicial, eyInicial, exTemp, eyTemp);
                g2.setStroke(strokeOriginal);
                g2.setColor(corOriginal);
            }
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

    public void setTipo(TipoPrimitivo tipo) {
        this.tipoAtual = tipo;
        // Trocou de ferramenta: cancela qualquer selecao/eixo pendente do espelhamento
        if (tipo != TipoPrimitivo.ESPELHO) {
            objSelecionado = null; tipoSelecionado = null; desenhandoEixo = false;
        }
        repaint();
    }
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