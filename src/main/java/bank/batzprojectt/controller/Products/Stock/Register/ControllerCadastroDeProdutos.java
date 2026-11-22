package bank.batzprojectt.controller.Products.Stock.Register;

// Importação para escrita performática em arquivos
import java.io.BufferedWriter;
// Importação para tratamento de exceções de I/O
import java.io.IOException;
// Importação da classe Files para operações com arquivos NIO
import java.nio.file.Files;
// Importação da interface Path para o caminho de arquivos
import java.nio.file.Path;
// Importação da classe Paths para criação de Path
import java.nio.file.Paths;
// Importação de lista dinâmica ArrayList
import java.util.ArrayList;
// Importação de conjunto único HashSet
import java.util.HashSet;
// Importação da interface List
import java.util.List;
// Importação da interface Set
import java.util.Set;

public class ControllerCadastroDeProdutos {

    // Lista estática em memória para armazenar os produtos carregados
    private static final List<Product> listaDeProdutos = new ArrayList<>();

    /* Usamos static final para transformar o
       caminho do arquivo em uma constante 
       imutável e compartilhada na memória, 
       garantindo acesso direto por métodos estáticos 
       e evitando que o valor seja alterado por engano */
    private static final Path CAMINHO_ARQUIVO = Paths.get(System.getProperty("user.dir"), "ProdutosLoja.txt");

    // Bloco estático executado automaticamente ao carregar a classe para ler o arquivo do disco
    static {
        carregarProdutosDoArquivo();
    }

    // Classe interna estática que representa o modelo de dados do Produto
    public static class Product {
        // Atributo para armazenar o código sequencial do produto
        private String codigo;
        // Atributo para armazenar o nome do produto
        private String nome;
        // Atributo para armazenar a categoria do produto
        private String categoria;
        // Atributo para armazenar a unidade de medida do produto
        private String unidadeDeMedida;

        // Construtor, necessário para usar "New"
        public Product(String codigo, String nome, String categoria, String unidadeDeMedida) {
            this.codigo = codigo; // Define o código do objeto
            this.nome = nome; // Define o nome do objeto
            this.categoria = categoria; // Define a categoria do objeto
            this.unidadeDeMedida = unidadeDeMedida; // Define a unidade de medida do objeto
        }

        // Getter
        public String getCodigo() {
            return codigo; // Retorna o código
        }

        // Getter   
        public String getNome() {
            return nome; // Retorna o nome
        }

        // Getter    
        public String getCategoria() {
            return categoria; // Retorna a categoria
        }

        // Getter    
        public String getUnidadeDeMedida() {
            return unidadeDeMedida; // Retorna a unidade de medida
        }

        // Getter
        public String getProduct() {
            // Concatena as informações do produto separadas por ";" e converte tudo para caixa alta
            String gettedProduct = (this.codigo + ";" + this.nome + ";" + this.categoria + ";" + this.unidadeDeMedida).toUpperCase();
            return gettedProduct; // Retorna a String formatada
        }
    }

    // Método responsável pelo cadastro de novos produtos
    public static void cadastrarProduto(String nome, String categoria, String unidadeDeMedida) {
        // Busca o próximo código disponível (preenchendo a menor lacuna)
        String codigoFormatado = getProximoCodigo();
        // Instancia o objeto do novo produto
        Product produto = new Product(codigoFormatado, nome, categoria, unidadeDeMedida);
        // Adiciona o produto na lista em memória
        listaDeProdutos.add(produto);
        // Ordena a lista dinamicamente em ordem crescente
        ordenarLista();
        // Reescreve o arquivo no disco com a lista completa reordenada
        reescreverArquivo();
    }

    // Getter para consulta externa (necessário pois é private)
    public static List<Product> getListaDeProdutos() {
        // Garante que a lista entregue para a View esteja sempre em ordem crescente
        ordenarLista();
        return listaDeProdutos; // Retorna a lista completa de produtos
    }

    // Método que gera o próximo código sequencial preenchendo lacunas vazias
    public static String getProximoCodigo() {
        // Conjunto de números inteiros para registrar os códigos já ocupados
        Set<Integer> codigosUsados = new HashSet<>();
        
        // Itera sobre os produtos cadastrados para extrair os códigos existentes
        for (Product produto : listaDeProdutos) {
            try {
                // Converte o código texto para inteiro e adiciona ao conjunto
                codigosUsados.add(Integer.parseInt(produto.getCodigo()));
            } catch (NumberFormatException ignored) {
                // Caso haja algum código em formato não numérico, ignora
            }
        }

        // Começa a verificação sempre a partir do menor código possível (1)
        int proximoCodigo = 1;
        // Enquanto o número verificado já estiver em uso, avança 1 passo
        while (codigosUsados.contains(proximoCodigo)) {
            proximoCodigo++; // Avança até encontrar a menor lacuna livre
        }

        // Retorna o código formatado com 5 dígitos (ex: 00015)
        return String.format("%05d", proximoCodigo);
    }

    // Método para deletar um produto pelo código e atualizar o arquivo
    public static boolean deletarProdutoPorCodigo(String codigo) {
        // Remove da lista em memória o produto que tiver o código correspondente
        boolean removido = listaDeProdutos.removeIf(p -> p.getCodigo().equalsIgnoreCase(codigo));

        // Se encontrou e removeu o produto da lista
        if (removido) {
            // Reordena a lista em memória
            ordenarLista();
            // Reescreve o arquivo no disco sem o produto deletado e mantendo a ordem crescente
            reescreverArquivo();
        }
        return removido; // Retorna se a remoção teve sucesso
    }

    // Ordena a lista em memória em ordem crescente com base no valor numérico do código
    private static void ordenarLista() {
        // Aplica o método de ordenação comparando os valores inteiros dos códigos
        listaDeProdutos.sort((p1, p2) -> {
            try {
                // Compara numericamente os dois códigos
                return Integer.compare(Integer.parseInt(p1.getCodigo()), Integer.parseInt(p2.getCodigo()));
            } catch (NumberFormatException e) {
                // Fallback para comparação alfabética caso exista código não numérico
                return p1.getCodigo().compareTo(p2.getCodigo());
            }
        });
    }

    // Sobrescreve o arquivo completo com os dados atuais da lista em memória
    private static void reescreverArquivo() {
        // Abre o arquivo para escrita limpa (sobrescrevendo todo o conteúdo)
        try (BufferedWriter writer = Files.newBufferedWriter(CAMINHO_ARQUIVO)) {
            // Percorre cada produto na lista ordenada
            for (Product p : listaDeProdutos) {
                // Escreve a linha do produto no arquivo
                writer.write(p.getProduct());
                // Adiciona a quebra de linha nativa
                writer.newLine();
            }
        } catch (IOException e) {
            // Lança exceção se houver falha na escrita
            throw new RuntimeException(e);
        }
    }

    // Verifica se um código específico já está cadastrado na memória
    private static boolean codigoExiste(String codigo) {
        // Percorre a lista existente
        for (Product produto : listaDeProdutos) {
            // Compara os códigos ignorando maiúsculas/minúsculas
            if (produto.getCodigo().equalsIgnoreCase(codigo)) {
                return true; // Retorna verdadeiro se o código já estiver na lista
            }
        }
        return false; // Retorna falso se não encontrar
    }

    // Lê os dados do arquivo de texto e carrega na lista em memória na inicialização
    private static void carregarProdutosDoArquivo() {
        // Validar se o arquivo existe;
        if (!Files.exists(CAMINHO_ARQUIVO)) {
            return; // Se não existir, interrompe o carregamento
        }
        try {
            // Lê todas as linhas do arquivo de uma vez
            List<String> linhas = Files.readAllLines(CAMINHO_ARQUIVO);
            // Itera linha por linha do arquivo
            for (String linha : linhas) {
                // Remove os espaços em branco e ignora linhas vazias
                if (linha.trim().isEmpty()) {
                    continue; // Pula para a próxima linha
                }

                // Divide a linha em partes separadas por ";"
                String[] partes = linha.split(";");
                // Divide o produto em 4 partes obrigatórias
                if (partes.length >= 4) {
                    String codigoStr = partes[0]; // Extrai o código
                    String nome = partes[1]; // Extrai o nome
                    String categoria = partes[2]; // Extrai a categoria
                    String unidade = partes[3]; // Extrai a unidade de medida
                    
                    // Valida se o produto já não está na lista antes de adicionar
                    if (!codigoExiste(codigoStr)) {
                        // Instancia o objeto Product com os dados do arquivo
                        Product produto = new Product(codigoStr, nome, categoria, unidade);
                        // Adiciona à lista dinâmica
                        listaDeProdutos.add(produto);
                    }
                }
            }
            // Ordena a lista em ordem crescente após carregar todos do arquivo
            ordenarLista();
        } catch (IOException e) {
            // Lança exceção se houver falha de leitura
            throw new RuntimeException(e);
        }
    }
}
