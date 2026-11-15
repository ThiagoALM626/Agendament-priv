package service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import dao.AgendamentoDAO;
import model.Agendamento;

/**
 * Versão com MySQL. Não existe mais lista em memória, nem carregar() e salvar():
 * o banco é a única fonte de verdade. As regras de negócio são as mesmas de antes.
 */
public class AgendamentoService {

    // Quantos agendamentos (não cancelados) cabem no mesmo dia e horário.
    private static final int LIMITE_POR_HORARIO = 2;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AgendamentoDAO dao = new AgendamentoDAO();

    // Há vaga no horário? Cancelados não ocupam vaga.
    public boolean horarioDisponivel(String data, String hora) throws Exception {
        return dao.contarPorHorario(data, hora, 0) < LIMITE_POR_HORARIO;
    }

    // O estúdio fecha sábado, domingo e segunda.
    public boolean diaAberto(String data) {

        LocalDate dataAgendamento = LocalDate.parse(data, FORMATO_DATA);

        DayOfWeek diaSemana = dataAgendamento.getDayOfWeek();

        return diaSemana != DayOfWeek.SATURDAY
                && diaSemana != DayOfWeek.SUNDAY
                && diaSemana != DayOfWeek.MONDAY;
    }

    // Funcionamento: das 07:00 até antes das 18:00.
    public boolean horarioFuncionamento(String hora) {

        LocalTime horario = LocalTime.parse(hora);

        LocalTime abertura = LocalTime.of(7, 0);
        LocalTime fechamento = LocalTime.of(18, 0);

        return !horario.isBefore(abertura)
                && horario.isBefore(fechamento);
    }

    // Devolve false se o dia estiver fechado, o horário for inválido ou fora
    // do expediente, ou se o horário estiver lotado. Caso contrário grava no banco.
    public boolean adicionar(Agendamento agendamento) throws Exception {

        try {
            if (!diaAberto(agendamento.getData())) {
                return false;
            }

            if (!horarioFuncionamento(agendamento.getHora())) {
                return false;
            }

        } catch (DateTimeParseException erro) {
            // Data ou hora em formato inválido.
            return false;
        }

        if (!horarioDisponivel(agendamento.getData(), agendamento.getHora())) {
            return false;
        }

        // O código é gerado pelo AUTO_INCREMENT e já volta preenchido no objeto.
        dao.inserir(agendamento);

        return true;
    }

    public List<Agendamento> listar() throws Exception {
        return dao.listar();
    }

    public Agendamento buscarPorId(int codigo) throws Exception {
        return dao.buscarPorId(codigo);
    }

    // Atualiza a data e o horário de um agendamento existente.
    public boolean atualizar(int codigo, String data, String hora) throws Exception {

        Agendamento agendamento = dao.buscarPorId(codigo);

        if (agendamento == null) {
            return false;
        }

        // Só agendamentos com status "Agendado" podem ser remarcados.
        if (!agendamento.getStatus().equalsIgnoreCase("Agendado")) {
            return false;
        }

        // Verifica se a data e o horário são válidos.
        try {
            if (!diaAberto(data) || !horarioFuncionamento(hora)) {
                return false;
            }
        } catch (DateTimeParseException erro) {
            return false;
        }

        // Verifica a disponibilidade sem contar o próprio agendamento.
        if (dao.contarPorHorario(data, hora, codigo) >= LIMITE_POR_HORARIO) {
            return false;
        }

        return dao.atualizarDataHora(codigo, data, hora);
    }

    // Cancela um agendamento, desde que faltem pelo menos 24 horas.
    public boolean cancelar(int codigo) throws Exception {

        Agendamento agendamento = dao.buscarPorId(codigo);

        if (agendamento == null) {
            return false;
        }

        // Impede o cancelamento de agendamentos já cancelados.
        if (agendamento.getStatus().equalsIgnoreCase("Cancelado")) {
            return false;
        }

        // Converte a data e a hora do agendamento.
        LocalDate data = LocalDate.parse(agendamento.getData(), FORMATO_DATA);
        LocalTime hora = LocalTime.parse(agendamento.getHora());

        // Calcula o momento exato do agendamento.
        LocalDateTime dataHoraAgendamento = data.atTime(hora);

        // Calcula o limite mínimo de 24 horas.
        LocalDateTime limite = LocalDateTime.now().plusHours(24);

        // Impede o cancelamento quando faltam menos de 24 horas.
        if (dataHoraAgendamento.isBefore(limite)) {
            return false;
        }

        return dao.atualizarStatus(codigo, "Cancelado");
    }
}