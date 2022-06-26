package ru.skillbox.socnetwork.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skillbox.socnetwork.logging.DebugLogs;
import ru.skillbox.socnetwork.model.entity.City;
import ru.skillbox.socnetwork.model.entity.Country;
import ru.skillbox.socnetwork.repository.CityCountryRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class SearchService {

    private final CityCountryRepository cityCountryRepository;

    public List<String> getCountryList() {
        return cityCountryRepository
                .getCountryList()
                .stream()
                .map(Country::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> getCityList() {
        return cityCountryRepository
                .getCityList()
                .stream()
                .map(City::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
