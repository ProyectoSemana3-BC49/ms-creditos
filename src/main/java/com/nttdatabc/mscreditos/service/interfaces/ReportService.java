package com.nttdatabc.mscreditos.service.interfaces;

import com.nttdatabc.mscreditos.model.BalanceAccounts;
import reactor.core.publisher.Mono;

/**
 * Report service interface.
 */
public interface ReportService {
  Mono<BalanceAccounts> getBalanceAverageService(String customerId);
}

