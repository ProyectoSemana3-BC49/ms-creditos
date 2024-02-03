package com.nttdatabc.mscreditos.controller;

import com.nttdatabc.mscreditos.controller.interfaces.ReportControllerApi;
import com.nttdatabc.mscreditos.model.BalanceAccounts;
import com.nttdatabc.mscreditos.service.ReportServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.nttdatabc.mscreditos.utils.Constantes.PREFIX_PATH;

/**
 * Report controller.
 */
@RestController
@RequestMapping(PREFIX_PATH)
@Slf4j
public class ReportController implements ReportControllerApi {
  @Autowired
  private ReportServiceImpl reportServiceImpl;

  @Override
  public ResponseEntity<Mono<BalanceAccounts>> getBalanceCredit(String customerId, ServerWebExchange exchange) {
    return new ResponseEntity<>(reportServiceImpl.getBalanceAverageService(customerId)
        .doOnSubscribe(unused -> log.info("getBalanceCredit:: iniciando"))
        .doOnError(throwable -> log.error("getBalanceCredit:: error " + throwable.getMessage()))
        .doOnSuccess(ignored -> log.info("getBalanceCredit:: finalizado con exito"))
        , HttpStatus.OK);
  }
}
