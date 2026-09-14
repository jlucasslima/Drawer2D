import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JOptionPane; 

/**
 * Cria a interface com o usuario (GUI).
 * 
 * Integrantes do Grupo:
 * - João Lucas de Lima Souza - RA00360044
 * - Pedro Chagas Neves de Farias Nascimento - RA00359511
 * - Guilherme Pereira de Rivoredo - RA00359303
 * - Eduardo Achkar avancini - RA00359889
 */
@SuppressWarnings("serial")
class Gui extends JFrame {
    private TipoPrimitivo tipoAtual = TipoPrimitivo.NENHUM;
    private Color corAtual = Color.BLACK;
    private int espAtual = 1;

    private JToolBar barraComandos = new JToolBar();
    private JLabel msg = new JLabel("Msg: ");
    private PainelDesenho areaDesenho = new PainelDesenho(msg, tipoAtual, corAtual, 10);

    // Botoes Primitivos
    private JButton jbPonto = new JButton("Ponto");
    private JButton jbReta = new JButton("Reta");
    private JButton jbCirculo = new JButton("Circulo");
    private JButton jbRetangulo = new JButton("Retângulo");
    private JButton jbTriangulo = new JButton("Triângulo");
    
    private JButton jbSelecao = new JButton("Borracha/Seleção");
    private JButton jbExportarJPEG = new JButton("Exportar JPEG");
    private JButton jbExportarJSON = new JButton("Exportar JSON");

    // Botões de Ações e Utilidades
    private JButton jbLimpar = new JButton("Limpar Tela");
    private JButton jbCor = new JButton("Cor");
    private JButton jbSair = new JButton("Sair");

    private JLabel jlEsp = new JLabel("   Esp: " + String.format("%-5s", 1));
    private JSlider jsEsp = new JSlider(1, 50, 1);
    
    // Combo Box para filtro (Teste de Redesenho da ED)
    private JLabel jlFiltro = new JLabel("  Ver: ");
    private JComboBox<String> jcbFiltro = new JComboBox<>(new String[]{"Todos", "Pontos", "Retas", "Circulos", "Retangulos", "Triangulos"});

    public Gui(int larg, int alt) {
        super("Testa Primitivos - Projeto Em Grupo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(larg, alt);
        
        setResizable(true); 
        
        setVisible(true);

        // Adicionando os componentes na barra
        barraComandos.add(jbPonto);
        barraComandos.add(jbReta);
        barraComandos.add(jbCirculo);
        barraComandos.add(jbRetangulo);
        barraComandos.add(jbTriangulo);
        
        // Adicionando os novos botões na barra
        barraComandos.addSeparator();
        barraComandos.add(jbSelecao);
        barraComandos.add(jbExportarJPEG);
        barraComandos.add(jbExportarJSON);
        
        barraComandos.addSeparator();
        barraComandos.add(jbCor);
        barraComandos.add(jlEsp);
        barraComandos.add(jsEsp);
        
        barraComandos.addSeparator();
        barraComandos.add(jlFiltro);
        barraComandos.add(jcbFiltro);
        
        barraComandos.addSeparator();
        barraComandos.add(jbLimpar);
        barraComandos.add(jbSair); 

        areaDesenho.setEsp(espAtual);

        add(barraComandos, BorderLayout.NORTH);
        add(areaDesenho, BorderLayout.CENTER);
        add(msg, BorderLayout.SOUTH);

        // Tratamento de Eventos (Listeners)
        jbPonto.addActionListener(e -> { tipoAtual = TipoPrimitivo.PONTO; areaDesenho.setTipo(tipoAtual); });
        jbReta.addActionListener(e -> { tipoAtual = TipoPrimitivo.RETA; areaDesenho.setTipo(tipoAtual); });
        jbCirculo.addActionListener(e -> { tipoAtual = TipoPrimitivo.CIRCULO; areaDesenho.setTipo(tipoAtual); });
        jbRetangulo.addActionListener(e -> { tipoAtual = TipoPrimitivo.RETANGULO; areaDesenho.setTipo(tipoAtual); });
        jbTriangulo.addActionListener(e -> { tipoAtual = TipoPrimitivo.TRIANGULO; areaDesenho.setTipo(tipoAtual); });

        jbSelecao.addActionListener(e -> { 
            tipoAtual = TipoPrimitivo.SELECAO; 
            areaDesenho.setTipo(tipoAtual); 
        });

        jbExportarJPEG.addActionListener(e -> {
            areaDesenho.exportarJPEG("desenho");
            JOptionPane.showMessageDialog(null, "Imagem exportada com sucesso como desenho.jpeg!");
        });

        jbExportarJSON.addActionListener(e -> {
            areaDesenho.exportarJSON("dados");
            JOptionPane.showMessageDialog(null, "Dados exportados com sucesso como dados.json!");
        });

        // Chama a rotina que limpa APENAS visualmente (preservando a ED)
        jbLimpar.addActionListener(e -> {
            areaDesenho.limparTelaVisualmente(); 
        });

        jbCor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(null, "Escolha uma cor", msg.getForeground());
            if (c != null){ corAtual = c; }
            areaDesenho.setCorAtual(corAtual);
        });

        jsEsp.addChangeListener(e -> {
            espAtual = jsEsp.getValue();
            jlEsp.setText("   Esp: " + String.format("%-5s", espAtual));
            areaDesenho.setEsp(espAtual);
        });
        
        // Listener do Combo Box - Repassa o filtro para a classe Desenho e força Redesenho
        jcbFiltro.addActionListener(e -> {
            String filtro = (String) jcbFiltro.getSelectedItem();
            areaDesenho.setFiltroAtual(filtro);
        });

        jbSair.addActionListener(e -> System.exit(0));
    }
}