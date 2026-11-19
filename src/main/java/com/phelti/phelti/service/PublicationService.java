package com.phelti.phelti.service;

import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;

import java.util.List;

public interface PublicationService {

    Publication createPublication(Publication publication);

    Publication findPublicationById(Integer publicationId);

    List<Publication> findAllPublications();

    List<Publication> searchPublications(PublicationFilter filter);

    Publication updatePublication(Integer publicationId, Publication publication);

    Publication changePublicationStatus(Integer publicationId, String status);

    void deletePublication(Integer publicationId);
}
