package service;

import java.util.List;
import dao.AnimalDAO;
import model.Animal;

/**
 * Versão com MySQL. Não existe mais lista em memória, nem carregar() e salvar():
 * o banco é a única fonte de verdade.
 */
public class AnimalService {
    private final AnimalDAO dao = new AnimalDAO();

    public void adicionar(Animal animal) throws Exception {
        dao.inserir(animal);
    }
    public List<Animal> listar() throws Exception {
        return dao.listar();
    }
    public boolean atualizar(int codigo, Animal dadosAtualizados) throws Exception {
        return dao.atualizar(codigo, dadosAtualizados);
    }
    public boolean excluir(int codigo) throws Exception {
        return dao.excluir(codigo);
    }
    public Animal buscarPorId(int codigo) throws Exception{
        return dao.buscarPorId(codigo);
    }
    public List<Animal> listarPorCliente(int codigoCliente) throws Exception {
        return dao.listarPorCliente(codigoCliente);
    }
    public boolean validarAnimal(Animal animal) {
 
        if (animal == null) {
            return false;
        }
 
        if (animal.getNome() == null || animal.getNome().isBlank()) {
            return false;
        }
 
        boolean especieValida = "Cachorro".equals(animal.getEspecie())
                || "Gato".equals(animal.getEspecie());
 
        if (!especieValida) {
            return false;
        }
 
        String porte = animal.getPorte();
 
        boolean porteValido = "P".equals(porte)
                || "M".equals(porte)
                || "G".equals(porte);
 
        if (!porteValido) {
            return false;
        }
 
        return true;
    }
}