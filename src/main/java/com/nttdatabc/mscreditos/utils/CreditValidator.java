package com.nttdatabc.mscreditos.utils;

import static com.nttdatabc.mscreditos.utils.Constantes.EX_ERROR_REQUEST;
import static com.nttdatabc.mscreditos.utils.Constantes.EX_ERROR_TYPE_ACCOUNT;
import static com.nttdatabc.mscreditos.utils.Constantes.EX_ERROR_VALUE_MIN;
import static com.nttdatabc.mscreditos.utils.Constantes.EX_NOT_FOUND_RECURSO;
import static com.nttdatabc.mscreditos.utils.Constantes.EX_VALUE_EMPTY;
import static com.nttdatabc.mscreditos.utils.Constantes.VALUE_MIN_ACCOUNT_BANK;

import com.nttdatabc.mscreditos.model.Credit;
import com.nttdatabc.mscreditos.model.CustomerExt;
import com.nttdatabc.mscreditos.model.enums.TypeCredit;
import com.nttdatabc.mscreditos.service.CustomerApiExtImpl;
import com.nttdatabc.mscreditos.utils.exceptions.errors.ErrorResponseException;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;


/**
 * Clase validator credit.
 */
public class CreditValidator {
  /**
   * Valida que los campos esenciales de un crédito no sean nulos.
   *
   * @param credit El crédito que se va a validar.
   * @throws ErrorResponseException Si algún campo esencial es nulo.
   */
  public static Mono<Void> validateCreditsNoNulls(Credit credit) {
    return Mono.just(credit)
        .filter(c -> c.getCustomerId() != null)
        .filter(c -> c.getMountLimit() != null)
        .filter(c -> c.getTypeCredit() != null)
        .switchIfEmpty(Mono.error(new ErrorResponseException(EX_ERROR_REQUEST,
            HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST)))
        .then();
  }

  /**
   * Valida que los campos de un crédito no estén vacíos.
   *
   * @param credit El crédito que se va a validar.
   * @throws ErrorResponseException Si algún campo esencial está vacío.
   */
  public static Mono<Void> validateCreditsEmpty(Credit credit) {
    return Mono.just(credit)
        .filter(c -> !c.getCustomerId().isEmpty())
        .filter(c -> !c.getMountLimit().toString().isBlank())
        .filter(c -> !c.getTypeCredit().isBlank())
        .switchIfEmpty(Mono.error(new ErrorResponseException(EX_VALUE_EMPTY,
            HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST)))
        .then();
  }

  /**
   * Verifica que el tipo de crédito sea válido.
   *
   * @param credit El crédito que se va a verificar.
   * @throws ErrorResponseException Si el tipo de crédito no es válido.
   */
  public static Mono<Void> verifyTypeCredits(Credit credit) {
    return Mono.just(credit)
        .filter(c -> {
          String typeAccount = c.getTypeCredit();
          return typeAccount.equalsIgnoreCase(TypeCredit.EMPRESA.toString())
              || typeAccount.equalsIgnoreCase(TypeCredit.PERSONAL.toString())
              || typeAccount.equalsIgnoreCase(TypeCredit.TARJETA.toString());
        })
        .switchIfEmpty(Mono.error(new ErrorResponseException(EX_ERROR_TYPE_ACCOUNT,
            HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST)))
        .then();
  }

  /**
   * Verifica que los valores del crédito sean válidos.
   *
   * @param credit El crédito que se va a verificar.
   * @throws ErrorResponseException Si los valores no son válidos.
   */
  public static Mono<Void> verifyValues(Credit credit) {
    return Mono.just(credit)
        .filter(c -> credit.getMountLimit().doubleValue() > VALUE_MIN_ACCOUNT_BANK)
        .switchIfEmpty(Mono.error(new ErrorResponseException(EX_ERROR_VALUE_MIN,
            HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST)))
        .then();
  }

  /**
   * Verifica la existencia de un cliente mediante su ID.
   *
   * @param customerId         El ID del cliente.
   * @param customerApiExtImpl Implementación de la interfaz CustomerApiExt.
   * @return La información del cliente si existe.
   * @throws ErrorResponseException Si el cliente no existe.
   */
  public static Mono<CustomerExt> verifyCustomerExists(String customerId, CustomerApiExtImpl customerApiExtImpl) {
    return customerApiExtImpl.getCustomerById(customerId)
        .switchIfEmpty(Mono.error(new ErrorResponseException(EX_NOT_FOUND_RECURSO,
            HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND)));
  }
}
