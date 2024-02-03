package com.nttdatabc.mscreditos.service;

import com.nttdatabc.mscreditos.model.Credit;
import com.nttdatabc.mscreditos.model.enums.TypeCustomer;
import com.nttdatabc.mscreditos.repository.CreditRepository;
import com.nttdatabc.mscreditos.service.interfaces.CreditService;
import com.nttdatabc.mscreditos.utils.Utilitarios;
import com.nttdatabc.mscreditos.utils.exceptions.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.nttdatabc.mscreditos.utils.Constantes.*;
import static com.nttdatabc.mscreditos.utils.CreditValidator.*;

@Service
public class CreditServiceImpl implements CreditService {

  @Autowired
  private CreditRepository creditRepository;
  @Autowired
  private CustomerApiExtImpl customerApiExtImpl;
  @Override
  public Flux<Credit> getAllCreditsService() {
    return creditRepository.findAll().switchIfEmpty(Flux.empty());
  }

  @Override
  public Mono<Credit> getCreditByIdService(String creditId) {
    return creditRepository.findById(creditId)
        .switchIfEmpty(Mono.error(new ErrorResponseException(EX_NOT_FOUND_RECURSO,
            HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND)));
  }

  @Override
  public Mono<Void> createCreditService(Credit credit) {
    return validateCreditsNoNulls(credit)
        .then(validateCreditsEmpty(credit))
        .then(verifyTypeCredits(credit))
        .then(verifyValues(credit))
        .then(Mono.just(credit))
        .flatMap(creditFlux -> verifyCustomerExists(creditFlux.getCustomerId(), customerApiExtImpl)
            .flatMap(customerFound -> getCreditsByCustomerId(creditFlux.getCustomerId())
                .collectList()
                .flatMap(listCreditsByCustomer -> {
                  if (customerFound.getType().equalsIgnoreCase(TypeCustomer.PERSONA.toString())
                      && listCreditsByCustomer.size() >= MAX_SIZE_ACCOUNT_CUSTOMER_PERSONA) {
                    return Mono.error(new ErrorResponseException(EX_ERROR_CONFLICTO_CUSTOMER_PERSONA,
                        HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT));
                  }
                  return Mono.just(customerFound);
                })))
        .then(Mono.just(credit))
        .map(creditFlujo -> {
          creditFlujo.setId(Utilitarios.generateUuid());
          creditFlujo.setDateOpen(LocalDateTime.now().toString());
          creditFlujo.setInterestRate(BigDecimal.valueOf(INTEREST_RATE));
          return creditFlujo;
        }).flatMap(creditInsert -> creditRepository.save(creditInsert))
        .then();
  }

  @Override
  public Mono<Void> updateCreditService(Credit credit) {
    return validateCreditsNoNulls(credit)
        .then(validateCreditsEmpty(credit))
        .then(verifyTypeCredits(credit))
        .then(getCreditByIdService(credit.getId()))
        .map(creditFlujo -> {
          creditFlujo.setMountLimit(credit.getMountLimit());
          creditFlujo.setTypeCredit(credit.getTypeCredit());
          return creditFlujo;
        }).flatMap(creditProcesado -> creditRepository.save(creditProcesado))
        .then();
  }

  @Override
  public Mono<Void> deleteCreditById(String creditId) {
    return getCreditByIdService(creditId)
        .flatMap(credit -> creditRepository.delete(credit))
        .then();
  }

  @Override
  public Flux<Credit> getCreditsByCustomerId(String customerId) {
    return verifyCustomerExists(customerId, customerApiExtImpl)
        .thenMany(creditRepository.findByCustomerId(customerId));
  }
}
