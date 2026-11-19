package com.phelti.phelti.service;

import com.phelti.phelti.model.PublicationMatch;

import java.util.List;

public interface MatchingService {

    List<PublicationMatch> findMatchesForUser(Integer userId);
}
