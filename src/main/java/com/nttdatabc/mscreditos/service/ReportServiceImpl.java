package com.nttdatabc.mscreditos.service;

import com.nttdatabc.mscreditos.model.BalanceAccounts;
import com.nttdatabc.mscreditos.model.MovementCredit;
import com.nttdatabc.mscreditos.model.SummaryAccountBalance;
import com.nttdatabc.mscreditos.repository.CreditRepository;
import com.nttdatabc.mscreditos.repository.MovementRepository;
import com.nttdatabc.mscreditos.service.interfaces.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.nttdatabc.mscreditos.utils.CreditValidator.verifyCustomerExists;

@Service
public class ReportServiceImpl implements ReportService {
  @Autowired
  private CustomerApiExtImpl customerApiExtImpl;
  @Autowired
  private CreditRepository creditRepository;
  @Autowired
  private MovementRepository movementRepository;
  @Autowired
  private CreditServiceImpl creditServiceImpl;
  @Override
  public Mono<BalanceAccounts> getBalanceAverageService(String customerId) {
    return verifyCustomerExists(customerId, customerApiExtImpl)
        .flatMap(customerFound -> {
          BalanceAccounts balanceAccounts = new BalanceAccounts();
          balanceAccounts.setCustomerId(customerId);

          LocalDate currentDate = LocalDate.now();
          int daysInMonth = currentDate.lengthOfMonth();
          int year = currentDate.getYear();
          int month = currentDate.getMonthValue();
          String dateFilter = String.format("%d-%s", year, String.valueOf(month).length() == 1 ? "0" + month : month);

          return creditRepository.findByCustomerId(customerId)
              .flatMap(credit -> movementRepository.findByCreditId(credit.getId())
                  .collectList()
                  .map(movements -> {
                    List<MovementCredit> movementsInCurrentMonth = movements.stream()
                        .filter(movement -> movement.getDayCreated().contains(dateFilter))
                        .collect(Collectors.toList());

                    double totalBalance = movementsInCurrentMonth.stream()
                        .mapToDouble(movement -> movement.getAmount().doubleValue())
                        .sum();
                    BigDecimal averageDailyBalance = BigDecimal.valueOf(totalBalance / daysInMonth);

                    SummaryAccountBalance summaryAccountBalance = new SummaryAccountBalance();
                    summaryAccountBalance.setAccountId(credit.getId());
                    summaryAccountBalance.setBalanceAvg(averageDailyBalance);

                    return summaryAccountBalance;
                  }))
              .collectList()
              .doOnNext(summaryAccountBalances -> balanceAccounts.setSummaryAccounts(summaryAccountBalances))
              .thenReturn(balanceAccounts);
        });
  }
}