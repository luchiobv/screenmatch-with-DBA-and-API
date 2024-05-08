package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repositrory.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4c22f67e";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<Serie> series;

   private List <DatosSerie> datosSerie =new ArrayList<>();
   private SerieRepository repositorio;
  //global scope for list of serieBuscada.
   private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por titulo
                    5 - Top 5 mejores series
                    6 - Buscar series por categoria o genero
                    7 - Filtrar series
                    8 - Buscar episodios por titulo
                    9- Top 5 episodios por serie 
                                  
                    0 - Salir
                    """;
            out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                        buscarTop5Series();
                        break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodioPorTitulo();
                    break;
                case 9 :
                    buscarTop5Episodios();
                    break;
                case 0:
                    out.println("Cerrando la aplicación...");
                    break;
                default:
                    out.println("Opción inválida");
            }
        }

    }



    private DatosSerie getDatosSerie() {
        out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
       mostrarSeriesBuscadas();
        out.println("Escriba el nombre de la serie que desea ver los episodios");
        var nombreSerie = teclado.nextLine();
        Optional <Serie> serie= series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d-> d.episodios().stream().map(e-> new Episodio(d.numero(),e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }


       //way to search the episode by serie direct to the api
        //DatosSerie datosSerie = getDatosSerie();

    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie= new Serie(datos);
        repositorio.save(serie);
        //Save data in the list
        //datosSerie.add(datos);
        out.println(datos);
    }
    private void mostrarSeriesBuscadas() {
         series = repositorio.findAll();
                //new ArrayList<>();
                //    series=datosSerie.stream()
    //            .map(d -> new Serie(d))
    //            .collect(Collectors.toList());
        //datosSerie.forEach(System.out::println);
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(out::println);

    }
    private void buscarSeriesPorTitulo() {
        System.out.println("Escribe el nombre de la serie que quieres buscar: ");
        var nombreSerie = teclado.nextLine();
        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nombreSerie);

        if (serieBuscada.isPresent()) {
           System.out.println("La serie buscada es: " + serieBuscada.get());
        }else{
            System.out.println("serie no encontrada");
        }
    }
    private void buscarTop5Series(){
        List<Serie>topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s -> out.println("Serie: "+s.getTitulo()+ " Evaluacion: "+s.getEvaluacion()));
    }
    private void buscarSeriesPorCategoria() {
        System.out.println("Escriba el Genero/Categoria de la serie que desa buscar: ");
        var genero= teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List<Serie>seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series por categoria es: "+ genero);
        seriesPorCategoria.forEach(System.out::println);

    }
    public void filtrarSeriesPorTemporadaYEvaluacion(){
        out.println("¿Filtrar séries con cuántas temporadas? ");
        var totalTemporadas= teclado.nextInt();
        teclado.nextLine();
        out.println("A partir de que valor de evaluacion desea buscar: ");
        var evaluacion =teclado.nextDouble();
        teclado.nextLine();
        List<Serie> filtroSeries = repositorio.seriesPorTemporadaYEvaluacion( totalTemporadas,evaluacion);
        //query derivada to create a search
        //List<Serie> filtroSeries = repositorio.findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(totalTemporadas,evaluacion);
        out.println("***series filtradas***");
        filtroSeries.forEach(s -> System.out.println(s.getTitulo()+ "- Evaluacion: "+ s.getEvaluacion()));


    }
    private void buscarEpisodioPorTitulo(){
        out.println("Escriba el nombre del episodio que desea buscar: ");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Serie: %s Temporada %s Evaluacion %s Episodio %s Titulo %s\n",
                e.getSerie().getTitulo(),e.getTemporada(),e.getEvaluacion(),e.getNumeroEpisodio(), e.getTitulo()));
    }

    private  void buscarTop5Episodios(){
        buscarSeriesPorTitulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List <Episodio> topEpisodios =repositorio.top5Episodios(serie);
            topEpisodios.forEach(e -> System.out.printf("Serie: %s - Temporada %s -Evaluacion %s -Episodio %s -Titulo %s\n",
                    e.getSerie().getTitulo(),e.getTemporada(),e.getEvaluacion(),e.getNumeroEpisodio(), e.getTitulo()));
        }
    }

}

