package com.nttdatabc.mscreditos.service.interfaces;

import com.nttdatabc.mscreditos.model.CustomerExt;
import reactor.core.publisher.Mono;

/**
 * Interface customer.
 */
public interface CustomerApiExt {
  Mono<CustomerExt> getCustomerById(String id);
}
