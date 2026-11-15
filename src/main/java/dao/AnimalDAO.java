package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Animal;


/**
 * Substitui o ClienteCSV. Em vez de ler/gravar o arquivo inteiro,
 * cada operação conversa direto com a tabela "cliente".
 */
public class AnimalDAO {
    private static final String COLUNAS = "codigo, nome, especie, raça, porte, codigoCliente";

    public List<Animal> listar() throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM tabela_animal ORDER BY codigo";
        List<Animal> animais = new ArrayList<>();

        try (Connection con = ConexaoMySQL.obterConexao();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()){
                animais.add(montarAnimal(rs));
            }
        }
        return animais;
    }

    public Animal buscarPorId(int codigo) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM tabela_animal WHERE codigo = ?";

        try (Connection con = ConexaoMySQL.obterConexao();
            PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1,codigo);

                try (ResultSet rs = ps.executeQuery()){
                    return rs.next() ? montarAnimal(rs) : null;
                }
            }
    }

    public List<Animal> listarPorCliente(int codigoCliente) throws SQLException {
        String sql = "SELECT " + COLUNAS
        + " FROM tabela_animal WHERE codigoCliente = ? ORDER BY codigo";
        List<Animal> animais = new ArrayList<>();

        try (Connection con = ConexaoMySQL.obterConexao();
            PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, codigoCliente);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        animais.add(montarAnimal(rs));
                    }
                }
            }
            return animais;
    }
    public void inserir(Animal animal) throws SQLException {
        String sql = "INSERT INTO tabela_animal " +
                "(nome, especie, raça, porte, codigoCliente) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection con =  ConexaoMySQL.obterConexao();
            PreparedStatement ps = con.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, animal.getNome());
                    ps.setString(2, animal.getEspecie());
                    ps.setString(3, racaOuPadrao(animal.getRaca()));
                    ps.setString(4, animal.getPorte());
                    ps.setInt(5, animal.getCodigoCliente());

                    ps.executeUpdate();

                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    animal.setCodigo(chaves.getInt(1));
                }
            }
        }
    
    }
    // Atualiza nome, espécie, raça e porte. O dono (codigo_cliente) não muda,
    // igual ao comportamento do código antigo.
    // Devolve false se não existir animal com esse código.
    public  boolean atualizar(int codigo, Animal dados) throws SQLException {
        String sql = "UPDATE tabela_animal "
                + "SET nome = ?, especie = ?, raca = ?, porte = ? "
                + "WHERE codigo = ?";
 
        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, dados.getNome());
            ps.setString(2, dados.getEspecie());
            ps.setString(3, racaOuPadrao(dados.getRaca()));
            ps.setString(4, dados.getPorte());
            ps.setInt(5, codigo);
 
            return ps.executeUpdate() > 0;
        }
    }
    // Devolve false se não existir animal com esse código.
    public boolean excluir(int codigo) throws SQLException {
 
        String sql = "DELETE FROM tabela_animal WHERE codigo = ?";
 
        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, codigo);
 
            return ps.executeUpdate() > 0;
        }
    }

    private String racaOuPadrao(String raca) {
        return (raca == null || raca.isBlank()) ? "Não informada" : raca;
    }

    // Converte a linha atual do ResultSet em um objeto Animal.
    private Animal montarAnimal(ResultSet rs) throws SQLException {
        return new Animal(
                rs.getInt("codigo"),
                rs.getString("nome"),
                rs.getString("especie"),
                rs.getString("raca"),
                rs.getString("porte"),
                rs.getInt("codigoCliente")
        );
    }
}

