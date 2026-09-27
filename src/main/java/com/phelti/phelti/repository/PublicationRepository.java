package com.phelti.phelti.repository;

import com.phelti.phelti.model.Publication;

import java.util.List;
import java.util.Optional;

public interface PublicationRepository {

    List<Publication> findAllPublications();

    Optional<Publication> findPublicationById(Integer publicationId);

    Publication createPublication(Publication publication);

    Publication updatePublication(Integer publicationId, Publication publication);

    void deletePublication(Integer publicationId);
}
