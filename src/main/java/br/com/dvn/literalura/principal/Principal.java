package br.com.dvn.literalura.principal;

import br.com.dvn.literalura.models.*;
import br.com.dvn.literalura.repositories.AutorRepository;
import br.com.dvn.literalura.repositories.LivroRepository;
import br.com.dvn.literalura.services.ConsumoApi;
import br.com.dvn.literalura.services.ConverterJsonToObject;

import java.util.*;

import static java.util.Comparator.*;

public class Principal {

    private AutorRepository autorRepository;
    private LivroRepository livroRepository;
    // String endereco = "https://gutendex.com/books/";
    private final String ENDERECO = "https://gutendex.com/books/?search=";
    Scanner scanner = new Scanner(System.in);
    ConsumoApi consumoApi = new ConsumoApi();
    ConverterJsonToObject conversor = new ConverterJsonToObject();
    List<Livro> livros = new ArrayList<>();
    List<Autor> autores = new ArrayList<>();

    public Principal(LivroRepository livroRepository, AutorRepository autorRepository){
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }


    public void exibirMenu(){
        boolean mostrar = true;
        int option = -1;

        String mensagem =
                """
                
                ---------------------------------------------------
                Bem-vindo ao LiterAlura - Catálogo de Livros online
                Selecione a opção desejada:
                ---------------------------------------------------
                 1) Buscar livro pelo título
                 2) Listar livros registrados
                 3) Listar autores registrados  
                 4) Listar autores vivos em determinado ano 
                 5) Listar livros em determinado idioma
                 6) Listar Top 5 livros 
                 7) Buscar autor por nome
                 0) Sair
                ---------------------------------------------------
                """;
        try{
            while(mostrar){

                System.out.println(mensagem);
                option = scanner.nextInt();

                switch(option){
                    case 1:
                        getLivroFromApi();
                        break;

                    case 2:
                        getAllLivrosFromDb();
                        break;
                    case 3:
                        getAllAutoresFromDb();
                        break;
                    case 4:
                        getAutoresVivoAno();
                        break;
                    case 5:
                        getLivrosNaLingua();
                        break;
                    case 6:
                        getTop5Livros();
                        break;
                    case 7:
                        getAutorPorNome();
                        break;
                    case 0:
                        System.out.println("Volte Sempre!!");
                        mostrar = false;
                        break;
                    default:
                        System.out.println("Digite uma opção válida!");
                        break;
                }
            }
        }catch (InputMismatchException e){
            System.out.println("Entrada inválida, por favor escolha um número do menu!");
        }
    }

    
    private DataLivraria getLivraria() {
        System.out.println("Digite o nome do livro para buscar:");
        scanner.nextLine();
        var bookName = scanner.nextLine();

        String json = consumoApi.obterJson(ENDERECO + bookName.replace(" ","%20").toLowerCase().trim());

        var livraria = conversor.converterDados(json, DataLivraria.class);
        return livraria;
    }



    private void getLivroFromApi(){


        DataLivraria dLivraria = getLivraria();

        Optional<DataLivro> optDataLivro = dLivraria.livros().stream()
                .sorted(comparing(DataLivro::id))
                .findFirst();

        if(optDataLivro.isPresent()){

            DataLivro dataLivro = optDataLivro.get();
            String titulo = dataLivro.titulo();

            Optional<Livro> optLivro = livroRepository.findByTituloEqualsIgnoreCase(titulo);

            if(optLivro.isPresent()){
                System.out.println("Esse livro já esta registrado no banco de dados.");
            }else{

                imprimirDataLivro(dataLivro);

                System.out.println("Digite 1 se é o livro que buscava ou 2 se não é o livro");
                int achou= scanner.nextInt();
                scanner.nextLine();
                if(achou == 1){

                    Autor autor = new Autor(dataLivro.autores().get(0));
                    Livro livro = new Livro(dataLivro);
                    autor.setLivro(livro);

                    Optional<Autor> optAutor = autorRepository.findByNomeEqualsIgnoreCase(autor.getNome());

                    if(optAutor.isPresent()){
                        Autor autorRegistrado = optAutor.get();
                        livro.setAutor(autorRegistrado);
                        livroRepository.save(livro);

                    }else{
                        autorRepository.save(autor);
                    }
                    System.out.println("Livro salvo com sucesso!");
                }else{
                    System.out.println("Tente agregando mais palavras ao titulo");
                }
            }
        }else{
            System.out.println("Livro não encontrado");
        }
    }

    private void imprimirDataLivro(DataLivro dataLivro){
        System.out.println("-------Livro---------");
        System.out.println("Titulo: " + dataLivro.titulo());
        dataLivro.autores().forEach(this::imprimirDataAutor);
        System.out.println("Idioma: " + String.join(" ", dataLivro.idiomas()));
        System.out.println("Numero de downloads: " + dataLivro.downloads());
        System.out.println("Poster: " + dataLivro.formatos().poster());
        System.out.println("---------------------");
        System.out.println("\n");
    }

    private void imprimirDataAutor(DataAutor dataAutor){
        System.out.println( "Autor: " + dataAutor.nome());
    }

    private void getAllLivrosFromDb(){
        livros = livroRepository.findAll();
        if(livros.isEmpty()){
            System.out.println("========== Não existem livros registrados ==========");
        }
        livros.sort(Comparator.comparing(Livro::getIdioma));
        imprimirLivros(livros);
    }

    private void imprimirLivros(List<Livro> livros) {
        System.out.println("=================== Livros ========================");
        livros.forEach(l-> {
            System.out.println("Titulo: " + l.getTitulo());
            System.out.println("Idioma: " + l.getIdioma());
            System.out.println("Downloads: " + l.getDownloads());
            System.out.println("Poster: " + l.getPoster());
            System.out.println("Autor: " + l.getAutor());
            System.out.println("-----------------------------------------------------");
        });
    }

    private void getAllAutoresFromDb(){
        autores = autorRepository.findAll();

        if(autores.isEmpty()){
            System.out.println("=========== Não existem autores registrados ============");
        }else{
          System.out.println("=================== Autores ========================");

            autores.sort(Comparator.comparing(Autor::getNome));
            imprimirAutores(autores);
         }
        }

    private void imprimirAutores(List<Autor> autores) {
        autores.forEach(a-> {
            System.out.println("Nome: " + a.getNome());
            System.out.println("Nacimento: " + a.getAnoNac());
            System.out.println("Morte: " + a.getAnoMorte());

            List<String> listaTitulos = new ArrayList<>();
            a.getLivros().forEach(l -> listaTitulos.add(l.getTitulo()));

            System.out.println("Livros: " + listaTitulos);
            System.out.println("-----------------------------------------------------");
        });

    }

    private void getAutoresVivoAno(){
        System.out.println("Ingrese o ano:");
        Integer ano = scanner.nextInt();
        scanner.nextLine();

        List<Autor> autoresVivos = autorRepository.BuscaAutoresVivosNumAnoDado(ano);

        if(autoresVivos.isEmpty()){
            System.out.println("Não há autores vivos no ano " + ano);
        }else{
            System.out.println("========= Autores vivos no ano " + ano + " ===============");
            System.out.println('\n');
            imprimirAutores(autoresVivos);
        }
    }

    private void getTop5Livros(){
        List<Livro> topLivros = livroRepository.findTop5ByOrderByDownloadsDesc();

        if(topLivros.isEmpty()){
            System.out.println("Não tem livros registrados");
        }else{
            System.out.println("================ Top 5 Livros ================");
            System.out.println('\n');
            topLivros.forEach(l->
                    System.out.println("Titulo: " + l.getTitulo() + " , Número de downloads: " + l.getDownloads()));
        }
    }
    private void getLivrosNaLingua(){
        scanner.nextLine();
        String menuIdioma =
                """                                              
                Esreva o idioma por favor
                ---------------------------
                 pt - Português 
                 en - Inglês
                 es - Espanhol  
                 fr - Francês        
                ---------------------------
                """;
        System.out.println(menuIdioma);
        String lingua = scanner.nextLine();

        List<Livro> livrosNumaLingua = livroRepository.findByIdioma(lingua);

        if(livrosNumaLingua.isEmpty()){
            System.out.println("Não tem livros registrados no idioma " + lingua);

        }else{
            System.out.println("================ Livros no idioma " + lingua + " ================");
            System.out.println('\n');
            livrosNumaLingua.forEach(l->
                    System.out.println("Titulo: " + l.getTitulo() + " , Autor: " + l.getAutor().getNome()));
        }
    }

    private void getAutorPorNome() {
        scanner.nextLine();
        System.out.println("Inserte o nome do autor que deseja procurar");
        String nome = scanner.nextLine();

        Optional<Autor> optAutor = autorRepository.findFirstByNomeContainingIgnoreCase(nome);
        if(optAutor.isPresent()){

            Autor autorBuscado = optAutor.get();
            List<Autor> listaParaImprimir = new ArrayList<>();
            listaParaImprimir.add(autorBuscado);
            System.out.println();
            System.out.println("================== Dados do Autor ==================");
            imprimirAutores(listaParaImprimir);

        }else{
            System.out.println("========== Autor não registrado no banco de dados ============");
            System.out.println('\n');
        }
    }

}
