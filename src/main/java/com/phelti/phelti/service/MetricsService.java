package com.phelti.phelti.service;

import com.phelti.phelti.model.UserMetrics;

public interface MetricsService {

    UserMetrics findUserMetrics(Integer userId);
}
