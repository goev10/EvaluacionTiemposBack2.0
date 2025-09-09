package com.web.back.services;

import com.web.back.clients.ZWSHREvaluacioClient;
import com.web.back.model.responses.evaluacion.Calendario;
import com.web.back.model.responses.evaluacion.EvaluacionApiResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
public class FiltersService {
    private final ZWSHREvaluacioClient zwshrEvaluacioClient;

    public FiltersService(ZWSHREvaluacioClient zwshrEvaluacioClient) {
        this.zwshrEvaluacioClient = zwshrEvaluacioClient;
    }

    public Mono<EvaluacionApiResponse> getFilters(String userName) {
        String currentDate = LocalDate.now().toString();

        try{

            return zwshrEvaluacioClient.getEvaluacion(userName, currentDate, currentDate)
                    .map(filters -> {
                        var calendarioList = Optional.ofNullable(filters.getCalendario())
                                .orElseGet(Collections::emptyList)
                                .stream()
                                .filter(this::isVisible)
                                .toList();
                        filters.setCalendario(calendarioList);
                        return filters;
                    });
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isVisible(Calendario calendar) {
        return calendar != null &&
                calendar.getMostrar() != null &&
                calendar.getMostrar().equalsIgnoreCase("X");
    }
}
