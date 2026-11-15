package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Agendamento;

/**
 * Acesso à tabela de agendamentos no MySQL.
 * Se um dia a tabela for renomeada, basta mudar a constante TABELA.
 */
public class AgendamentoDAO {

    private static final String TABELA = "tabela_agendamentos";

    private static final String COLUNAS =
            "codigo, data, hora, status, codigoAnimal, codigoServico, levaETraz";

    public List<Agendamento> listar() throws SQLException {

        String sql = "SELECT " + COLUNAS + " FROM " + TABELA + " ORDER BY codigo";
        List<Agendamento> agendamentos = new ArrayList<>();

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                agendamentos.add(montarAgendamento(rs));
            }
        }

        return agendamentos;
    }

    public Agendamento buscarPorId(int codigo) throws SQLException {

        String sql = "SELECT " + COLUNAS + " FROM " + TABELA + " WHERE codigo = ?";

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montarAgendamento(rs) : null;
            }
        }
    }

    // Insere e devolve o código gerado pelo AUTO_INCREMENT dentro do próprio objeto.
    public void inserir(Agendamento agendamento) throws SQLException {

        // (Corrigido: faltava o ")" depois de levaETraz.)
        String sql = "INSERT INTO " + TABELA
                + " (data, hora, status, codigoAnimal, codigoServico, levaETraz)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, agendamento.getData());
            ps.setString(2, agendamento.getHora());
            ps.setString(3, agendamento.getStatus());
            ps.setInt(4, agendamento.getCodigoAnimal());
            ps.setInt(5, agendamento.getCodigoServico());
            ps.setBoolean(6, agendamento.isLevaETraz());

            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    agendamento.setCodigo(chaves.getInt(1));
                }
            }
        }
    }

    // Conta quantos agendamentos NÃO cancelados existem naquela data e hora.
    // ignorarCodigo: código de um agendamento a desconsiderar na contagem
    // (usado ao reagendar, para não contar o próprio). Passe 0 para não ignorar nenhum.
    public int contarPorHorario(String data, String hora, int ignorarCodigo) throws SQLException {

        String sql = "SELECT COUNT(*) FROM " + TABELA
                + " WHERE data = ? AND hora = ?"
                + " AND LOWER(status) <> 'cancelado'"
                + " AND codigo <> ?";

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, data);
            ps.setString(2, hora);
            ps.setInt(3, ignorarCodigo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // Devolve false se não existir agendamento com esse código.
    public boolean atualizarDataHora(int codigo, String data, String hora) throws SQLException {

        String sql = "UPDATE " + TABELA + " SET data = ?, hora = ? WHERE codigo = ?";

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, data);
            ps.setString(2, hora);
            ps.setInt(3, codigo);

            return ps.executeUpdate() > 0;
        }
    }

    // Devolve false se não existir agendamento com esse código.
    public boolean atualizarStatus(int codigo, String status) throws SQLException {

        String sql = "UPDATE " + TABELA + " SET status = ? WHERE codigo = ?";

        try (Connection con = ConexaoMySQL.obterConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, codigo);

            return ps.executeUpdate() > 0;
        }
    }

    private Agendamento montarAgendamento(ResultSet rs) throws SQLException {
        return new Agendamento(
                rs.getInt("codigo"),
                rs.getString("data"),
                rs.getString("hora"),
                rs.getString("status"),
                rs.getInt("codigoAnimal"),
                rs.getInt("codigoServico"),
                rs.getBoolean("levaETraz")
        );
    }
}