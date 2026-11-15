package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Servico;

public class ServicoDAO {

    private static String COLUNAS = "codigo, nome, descricao, valorP, valorM, valorG, duracao";

    public List<Servico> listar() throws SQLException {

        String sql = "SELECT " + COLUNAS + " FROM tabela_servico ORDER BY codigo";
        List<Servico> servico = new ArrayList<>();

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                servico.add(montarServico(rs));
            }
        }

        return servico;
    }
    public Servico buscarPorId(int codigo) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM tabela_servico WHERE codigo = ?";

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montarServico(rs) : null;
            }
        }
    }
    public void inserir(Servico servico) throws SQLException {

        String sql = "INSERT INTO tabela_servico (nome, descricao, valorP, valorM, valorG, duracao) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, servico.getNome());
            ps.setString(2, servico.getDescricao());
            ps.setDouble(3, servico.getValorP());
            ps.setDouble(4, servico.getValorM());
            ps.setDouble(5, servico.getValorG());
            ps.setInt(6, servico.getDuracao());

            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    servico.setCodigo(chaves.getInt(1));
                }
            }
        }
    }
    // Converte a linha atual do ResultSet em um objeto Servico.
    private Servico montarServico(ResultSet rs) throws SQLException {
        return new Servico(
                rs.getInt("codigo"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getDouble("valorP"),
                rs.getDouble("valorM"),
                rs.getDouble("valorG"),
                rs.getInt("duracao")
        );
    }
}
