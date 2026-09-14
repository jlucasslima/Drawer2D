import java.util.ArrayList;
import java.util.List;

import ponto.PontoGr;
import reta.RetaGr;
import circulo.CirculoGr;
import retangulo.RetanguloGr;
import triangulo.TrianguloGr;

/**
 * Estrutura de Dados (ED) para armazenamento dos primitivos.
 * 
 * Integrantes do Grupo:
 * - João Lucas de Lima Souza - RA00360044
 * - Pedro Chagas Neves de Farias Nascimento - RA00359511
 * - Guilherme Pereira de Rivoredo - RA00359303
 * - Eduardo Achkar avancini - RA00359889
 */
public class EstruturaDados {
    public List<PontoGr> pontos = new ArrayList<>();
    public List<RetaGr> retas = new ArrayList<>();
    public List<CirculoGr> circulos = new ArrayList<>();
    public List<RetanguloGr> retangulos = new ArrayList<>();
    public List<TrianguloGr> triangulos = new ArrayList<>();
}