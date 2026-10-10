package com.phelti.phelti.service;

import com.phelti.phelti.model.Publication;

import java.util.List;

public interface PublicationService {

    Publication createPublication(Publication publication);

    Publication findPublicationById(Integer publicationId);

    List<Publication> findAllPublications();

    Publication updatePublication(Integer publicationId, Publication publication);

    Publication changePublicationStatus(Integer publicationId, String status);

    void deletePublication(Integer publicationId);
}
