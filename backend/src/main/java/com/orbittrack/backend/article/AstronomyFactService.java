package com.orbittrack.backend.article;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AstronomyFactService {

    private final List<AstronomyFact> facts = List.of(
        new AstronomyFact(
            "Le savais-tu ? La lumière du Soleil",
            "La lumière du Soleil met environ 8 minutes et 20 secondes pour atteindre la Terre.",
            "NASA",
            "https://science.nasa.gov/sun/facts/"
        ),
        new AstronomyFact(
            "Le savais-tu ? Une journée sur Vénus",
            "Vénus met environ 243 jours terrestres pour effectuer une rotation sur elle-même, plus longtemps que ses 225 jours terrestres de révolution autour du Soleil.",
            "NASA",
            "https://science.nasa.gov/venus/venus-facts/"
        ),
        new AstronomyFact(
            "Le savais-tu ? Jupiter",
            "Jupiter est la plus grande planète du système solaire.",
            "NASA",
            "https://science.nasa.gov/jupiter/jupiter-facts/"
        )
    );

    public AstronomyFact getRandomFact() {
        int index = ThreadLocalRandom.current().nextInt(facts.size());
        return facts.get(index);
    }
}
