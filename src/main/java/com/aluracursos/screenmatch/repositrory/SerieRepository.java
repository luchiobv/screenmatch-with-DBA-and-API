package com.aluracursos.screenmatch.repositrory;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
  Optional<Serie> findByTituloContainingIgnoreCase(String nombreSerie);

  List<Serie>findTop5ByOrderByEvaluacionDesc();
  List<Serie>findByGenero(Categoria categoria);
  //native query to search
  //List<Serie>findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(int totalTemporadas, Double evaluacion);
  //native query
  //@Query(value="SELECT * FROM series WHERE series.total_temporadas<= 6 AND series.evaluacion >=7.5", nativeQuery=true)
  //JPQL search
  @Query("SELECT s FROM Serie s WHERE s.totalTemporadas<= :totalTemporadas AND s.evaluacion >= :evaluacion")
  List <Serie> seriesPorTemporadaYEvaluacion(int totalTemporadas, Double evaluacion);

  @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:nombreEpisodio%")
  List<Episodio> episodiosPorNombre(String nombreEpisodio);
  @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.evaluacion DESC LIMIT 5")
  List<Episodio> top5Episodios(Serie serie);

}
