package com.phelti.phelti.repository;

import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;

import java.util.List;
import java.util.Optional;

public interface PublicationRepository {

    List<Publication> findAllPublications();

    Optional<Publication> findPublicationById(Integer publicationId);

    List<Publication> searchPublications(PublicationFilter filter);

    Publication createPublication(Publication publication);

    Publication updatePublication(Integer publicationId, Publication publication);

    void deletePublication(Integer publicationId);

    void lockPublication(Integer publicationId);

    void rejectPendingProposals(Integer publicationId);

    boolean hasPendingProposals(Integer publicationId);
}
