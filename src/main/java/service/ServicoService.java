package service;

import java.util.List;

import dao.ServicoDAO;

import model.Servico;

public class ServicoService {

    private final ServicoDAO dao = new ServicoDAO();

    public void adicionar(Servico service) throws Exception {
        dao.inserir(service);
    }

    public List<Servico> listar() throws Exception {
        return dao.listar();
    }

    public Servico buscarPorId(int codigo) throws Exception{
        return dao.buscarPorId(codigo);
    }
}