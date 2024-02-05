package com.nttdatabc.mscreditos.service;


import static com.nttdatabc.mscreditos.utils.Constantes.EX_NOT_FOUND_RECURSO;
import static com.nttdatabc.mscreditos.utils.Constantes.URL_CUSTOMER_ID;

import com.nttdatabc.mscreditos.model.CustomerExt;
import com.nttdatabc.mscreditos.service.interfaces.CustomerApiExt;
import com.nttdatabc.mscreditos.utils.exceptions.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class CustomerApiExtImpl implements CustomerApiExt {
  @Autowired
  private WebClient webClient;
  @Override
  public Mono<CustomerExt> getCustomerById(String id) {
    String apiUrl = URL_CUSTOMER_ID.concat(id);
    return webClient.get()
        .uri(apiUrl)
        .retrieve()
        .onStatus(HttpStatus::isError, response -> Mono.error(new ErrorResponseException(EX_NOT_FOUND_RECURSO, response.statusCode().value(), response.statusCode())))
        .bodyToMono(CustomerExt.class);
  }
}
