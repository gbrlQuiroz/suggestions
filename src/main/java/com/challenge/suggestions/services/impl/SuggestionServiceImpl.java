package com.challenge.suggestions.services.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.challenge.suggestions.models.City;
import com.challenge.suggestions.views.CityView;
import com.challenge.suggestions.views.SuggestionView;
import com.challenge.suggestions.persistences.CityRepository;
import com.challenge.suggestions.services.SuggestionService;
import com.challenge.suggestions.converters.CityConverter;

import lombok.extern.slf4j.Slf4j;


@Service
@Transactional(readOnly = true)
@Slf4j
public class SuggestionServiceImpl implements SuggestionService{
    
    private CityRepository cityRepository;
    private CityConverter cityConverter;

    @Autowired
    public void setCityRepository(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Autowired
    public void setCityConverter(CityConverter cityConverter) {
        this.cityConverter = cityConverter;
    }

    @Override
    public SuggestionView getSuggestions(String texto, Double lati, Double longi) {
        try {
            // obtenemos de la DB los registros
            List<City> cityList = cityRepository.findByNameContains(texto);
            // el view donde se guardarán los datos obtenidos
            List<CityView> cityListView = new ArrayList<>();

            //se recorrerá cada registro para procesarlo en el view y aplicarle el score basado en la distancia entre puntos
            cityList.forEach(city -> {
                if (lati != 0 && longi != 0) {
                    // se calculará la distancia entre latitudes y longitudes para asignar el score
                    Double distancia = org.apache.lucene.util.SloppyMath.haversinMeters(city.getLatitude(), city.getLongitude(), lati, longi);
                    cityListView.add(cityConverter.toView(city, distancia));
                } else {
                    // si no nos dan la latitud y longitud es score es siempre 1.0, se evalua en el converter
                    cityListView.add(cityConverter.toView(city, -1.0));
                }
            });

            // se ordena de forma descendente
            cityListView.sort(Comparator.comparing(CityView::getScore).reversed());
            // segun el requerimiento el json debe tener un campo inicial llamado 'suggestions', se hace un wrap 
            SuggestionView suggestion = new SuggestionView();
            suggestion.setSuggestions(cityListView);

            return suggestion;

        } catch (Exception ex) {
            log.error("--->>>CityServiceImpl--->>exception: {}",ex.getMessage());
            return null;
        }

    }
}
