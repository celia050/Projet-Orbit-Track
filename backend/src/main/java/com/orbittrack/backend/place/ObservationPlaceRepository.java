
package com.orbittrack.backend.place;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObservationPlaceRepository
        extends JpaRepository<ObservationPlace, Long> {

    List<ObservationPlace> findByActiveTrue();
}
