package com.creditapi;

import com.creditapi.model.Customer;
import com.creditapi.model.Loan;
import com.creditapi.model.LoanInstallment;
import com.creditapi.model.InstallmentOption;
import com.creditapi.repository.CustomerRepository;
import com.creditapi.repository.LoanRepository;
import com.creditapi.repository.LoanInstallmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * This configuration class seeds the database with initial data for testing purposes.
 * It creates a default customer and several loans with different installment statuses.
 * The loans are created with various conditions such as all installments unpaid, all paid,
 * a mix of paid and unpaid installments, and loans that are fully paid.
 * This setup allows for comprehensive testing of the loan management functionality.  
 */

@Configuration
public class DatabaseSeeder {
    @Bean
    public CommandLineRunner seedDatabase(CustomerRepository customerRepository, LoanRepository loanRepository, LoanInstallmentRepository loanInstallmentRepository) {
        return args -> {
            UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

            Customer customer = customerRepository.findById(id).orElseGet(() -> {
                Customer c = new Customer();
                c.setId(id);
                c.setName("John");
                c.setSurname("Doe");
                c.setCreditLimit(new BigDecimal("100000")); // High limit
                c.setUsedCreditLimit(BigDecimal.ZERO);
                return customerRepository.save(c);
            });

            UUID janeId = UUID.fromString("00000000-0000-0000-0000-000000000002");
            customerRepository.findById(janeId).orElseGet(() -> {
                Customer c = new Customer();
                c.setId(janeId);
                c.setName("Jane");
                c.setSurname("Doe");
                c.setCreditLimit(new BigDecimal("100000"));
                c.setUsedCreditLimit(BigDecimal.ZERO);
                return customerRepository.save(c);
            });

            java.util.function.BiConsumer<java.time.LocalDate, java.util.List<LoanInstallment>> saveLoan = (createDate, installments) -> {
                Loan loan = new Loan();
                loan.setCustomerId(customer.getId());
                loan.setLoanAmount(installments.stream().map(LoanInstallment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                loan.setNumberOfInstallments(InstallmentOption.SIX);
                loan.setInterestRate(0.2);
                loanRepository.save(loan);                
                for (int i = 0; i < installments.size(); i++) {
                    LoanInstallment inst = installments.get(i);
                    inst.setLoan(loan);
                    inst.setDueDate(createDate.plusMonths(i + 1).withDayOfMonth(1));
                    loanInstallmentRepository.save(inst);
                }
            };

            // Loan 1: all unpaid, createDate = today (all due in next 6 months)
            java.time.LocalDate createDate1 = LocalDate.now();
            java.util.List<LoanInstallment> loan1 = new java.util.ArrayList<>();
            for (int i = 0; i < 6; i++) {
                LoanInstallment inst = new LoanInstallment();
                inst.setAmount(new BigDecimal("100"));
                inst.setPaid(false);
                inst.setPaidAmount(BigDecimal.ZERO);
                loan1.add(inst);
            }
            saveLoan.accept(createDate1, loan1);

            // Loan 2: all paid, createDate = 6 months ago
            java.time.LocalDate createDate3 = LocalDate.now().minusMonths(6);
            java.util.List<LoanInstallment> loan2 = new java.util.ArrayList<>();
            for (int i = 0; i < 6; i++) {
                LoanInstallment inst = new LoanInstallment();
                inst.setAmount(new BigDecimal("100"));
                inst.setPaid(true);
                inst.setPaidAmount(new BigDecimal("100"));
                inst.setPaymentDate(createDate3.plusMonths(i + 1).withDayOfMonth(1).minusDays(1));
                loan2.add(inst);
            }
            saveLoan.accept(createDate3, loan2);

            // Loan 3: 3 paid, 3 unpaid, createDate = 3 months ago (unpaid due in next 3 months)
            java.time.LocalDate createDate4 = LocalDate.now().minusMonths(3);
            java.util.List<LoanInstallment> loan4 = new java.util.ArrayList<>();
            for (int i = 0; i < 6; i++) {
                LoanInstallment inst = new LoanInstallment();
                inst.setAmount(new BigDecimal("100"));
                if (i < 3) {
                    inst.setPaid(true);
                    inst.setPaidAmount(new BigDecimal("100"));
                    inst.setPaymentDate(createDate4.plusMonths(i + 1).withDayOfMonth(1).minusDays(1));
                } else {
                    inst.setPaid(false);
                    inst.setPaidAmount(BigDecimal.ZERO);
                    inst.setPaymentDate(null);
                }
                loan4.add(inst);
            }
            saveLoan.accept(createDate4, loan4);

            // Loan 4: all unpaid, createDate = 3 months ago (all due in next 3-8 months)
            java.time.LocalDate createDate5 = LocalDate.now().minusMonths(3);
            java.util.List<LoanInstallment> loan5 = new java.util.ArrayList<>();
            for (int i = 0; i < 6; i++) {
                LoanInstallment inst = new LoanInstallment();
                inst.setAmount(new BigDecimal("100000"));
                inst.setPaid(false);
                inst.setPaidAmount(BigDecimal.ZERO);
                loan5.add(inst);
            }
            saveLoan.accept(createDate5, loan5);

            // Loan 5: all paid, loan.isPaid = true, createDate = 6 months ago
            java.time.LocalDate createDate8 = LocalDate.now().minusMonths(6);
            java.util.List<LoanInstallment> loan8 = new java.util.ArrayList<>();
            for (int i = 0; i < 6; i++) {
                LoanInstallment inst = new LoanInstallment();
                inst.setAmount(new BigDecimal("100"));
                inst.setPaid(true);
                inst.setPaidAmount(new BigDecimal("100"));
                inst.setPaymentDate(createDate8.plusMonths(i + 1).withDayOfMonth(1).minusDays(1));
                loan8.add(inst);
            }

            Loan paidLoan = new Loan();
            paidLoan.setCustomerId(customer.getId());
            paidLoan.setLoanAmount(new BigDecimal("600"));
            paidLoan.setNumberOfInstallments(InstallmentOption.SIX);
            paidLoan.setInterestRate(0.2);
            paidLoan.setPaid(true);
            paidLoan.setCreateDate(createDate8.atStartOfDay());
            loanRepository.save(paidLoan);
            for (int i = 0; i < loan8.size(); i++) {
                LoanInstallment inst = loan8.get(i);
                inst.setLoan(paidLoan);
                inst.setDueDate(createDate8.plusMonths(i + 1).withDayOfMonth(1));
                loanInstallmentRepository.save(inst);
            }
        };
    }
}
