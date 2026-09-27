package com.phelti.phelti.repository;

import com.phelti.phelti.model.Publication;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryPublicationRepositoryTEMPORAL implements PublicationRepository {

    private final Map<Integer, Publication> storage = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public List<Publication> findAllPublications() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Publication> findPublicationById(Integer publicationId) {
        return Optional.ofNullable(storage.get(publicationId));
    }

    @Override
    public Publication createPublication(Publication publication) {
        int id = idGenerator.getAndIncrement();
        publication.setPublicationId(id);
        storage.put(id, publication);
        return publication;
    }

    @Override
    public Publication updatePublication(Integer publicationId, Publication publication) {
        publication.setPublicationId(publicationId);
        storage.put(publicationId, publication);
        return publication;
    }

    @Override
    public void deletePublication(Integer publicationId) {
        storage.remove(publicationId);
    }
}
