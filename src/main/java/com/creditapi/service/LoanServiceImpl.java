package com.creditapi.service;

import com.creditapi.exception.CustomerNotFoundException;
import com.creditapi.exception.LoanNotFoundException;
import com.creditapi.exception.InsufficientCreditLimitException;
import com.creditapi.model.Customer;
import com.creditapi.model.Loan;
import com.creditapi.model.LoanInstallment;
import com.creditapi.repository.CustomerRepository;
import com.creditapi.repository.LoanRepository;
import com.creditapi.util.LoanUtil;
import com.creditapi.repository.LoanInstallmentRepository;
import com.creditapi.dto.LoanCreateRequestDTO;
import com.creditapi.dto.LoanCreateResponseDTO;
import com.creditapi.dto.LoanResponseDTO;
import com.creditapi.dto.PayInstallmentRequestDTO;
import com.creditapi.dto.PayInstallmentResponseDTO;
import com.creditapi.mapper.LoanMapper;
import com.creditapi.dto.LoanInstallmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final LoanInstallmentRepository loanInstallmentRepository;

    @Autowired
    public LoanServiceImpl(CustomerRepository customerRepository, LoanRepository loanRepository, LoanMapper loanMapper, LoanInstallmentRepository loanInstallmentRepository) {
        this.customerRepository = customerRepository;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    @Override
    @Transactional
    public LoanCreateResponseDTO createLoan(LoanCreateRequestDTO loanCreateRequestDTO) {

        // Check if customer exists
        Optional<Customer> customerOpt = customerRepository.findById(loanCreateRequestDTO.getCustomerId());
        if (customerOpt.isEmpty()) {
            throw new CustomerNotFoundException("Customer not found");
        }

        // Check customer's credit limit 
        Customer customer = customerOpt.get();
        if (customer.getCreditLimit().subtract(customer.getUsedCreditLimit()).compareTo(loanCreateRequestDTO.getLoanAmount()) < 0) {
            throw new InsufficientCreditLimitException("Insufficient credit limit");
        }
        
        Loan loan = loanMapper.toEntity(loanCreateRequestDTO);
        // Save loan first to get an ID
        loanRepository.save(loan);
        int numberOfInstallments = loanCreateRequestDTO.getNumberOfInstallments();

        // Calculate and create installments
        BigDecimal totalToBePaid = LoanUtil.calculateTotalToBePaid(loanCreateRequestDTO.getLoanAmount(), loanCreateRequestDTO.getInterestRate());

        BigDecimal baseAmount = totalToBePaid.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
        BigDecimal totalAssigned = baseAmount.multiply(BigDecimal.valueOf(numberOfInstallments));
        BigDecimal remainder = totalToBePaid.subtract(totalAssigned);
        LocalDate dueDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        for (int i = 0; i < numberOfInstallments; i++) {
            BigDecimal amount = baseAmount;
            if (i == numberOfInstallments - 1) {
                amount = amount.add(remainder);
                amount = amount.setScale(2, RoundingMode.HALF_UP); // Ensure last installment is rounded to 2 decimals
            }
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount(amount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setDueDate(dueDate.plusMonths(i));
            installment.setPaid(false);
            loanInstallmentRepository.save(installment);
        }
        
        // Update customer's used credit limit
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(loanCreateRequestDTO.getLoanAmount()));
        customerRepository.save(customer);

        LoanCreateResponseDTO loanCreateResponseDto = loanMapper.toLoanCreateResponseDTO(loan);
        loanCreateResponseDto.setPaymentAmount(totalToBePaid);
        loanCreateResponseDto.setFirstPaymentDate(LoanUtil.calculateFirstPaymentDate(loan.getCreateDate()));
        return loanCreateResponseDto;
    }

    @Override
    public List<LoanResponseDTO> listLoansByCustomerId(UUID customerId, Integer numberOfInstallments, Boolean isPaid) {
        // Check if customer exists
        Optional<Customer> customer =  customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            throw new CustomerNotFoundException("Customer not found");
        }

        List<Loan> loans = loanRepository.findByCustomerId(customerId);
        return loans.stream()
            .filter(l -> numberOfInstallments == null || l.getNumberOfInstallments().getValue() == numberOfInstallments)
            .filter(l -> isPaid == null || l.isPaid() == isPaid)
            .map(loan -> {
                LoanResponseDTO loanResponseDto = loanMapper.toResponseDto(loan);
                loanResponseDto.setPaymentAmount(LoanUtil.calculateTotalToBePaid(loan.getLoanAmount(), loan.getInterestRate()));
                loanResponseDto.setFirstPaymentDate(LoanUtil.calculateFirstPaymentDate(loan.getCreateDate()));
                return loanResponseDto;
            })
            .toList();
    }

    @Override
    public List<LoanInstallmentDTO> listInstallmentsByLoanId(UUID loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new LoanNotFoundException("Loan not found");
        }

        List<LoanInstallment> installments = loanInstallmentRepository.findByLoanId(loanId);

        // Map entities to DTOs
        return installments.stream()
            .map(loanMapper::toLoanInstallmentDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PayInstallmentResponseDTO payInstallments(UUID loanId, PayInstallmentRequestDTO requestDTO) {

        if (requestDTO.getAmount() == null || requestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new LoanNotFoundException("Loan not found");
        }

        Loan loan = loanOpt.get();

        List<LoanInstallment> installments = loanInstallmentRepository.findByLoanId(loanId);

        // Only unpaid installments, sorted by due date
        LocalDate now = LocalDate.now();
        LocalDate maxPayableDate = now.plusMonths(3).withDayOfMonth(1).minusDays(1).plusMonths(1); // End of 3rd month

        // Only unpaid and due within 3 months
        List<LoanInstallment> eligible = installments.stream()
            .filter(i -> !i.isPaid())
            .filter(i -> !i.getDueDate().isAfter(maxPayableDate))
            .sorted(Comparator.comparing(LoanInstallment::getDueDate))
            .toList();
        
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal remaining = requestDTO.getAmount();
        int paidCount = 0;

        List<PayInstallmentResponseDTO.InstallmentPaymentDetail> details = new java.util.ArrayList<>();

        for (LoanInstallment inst : eligible) {

            if (remaining.compareTo(inst.getAmount()) < 0) {
                break; // Only full payments allowed
            }

            // Reward/Penalty logic
            BigDecimal paidAmount = inst.getAmount();
            boolean isReward = false;
            boolean isPenalty = false;

            long daysDiff = ChronoUnit.DAYS.between(inst.getDueDate(), now);
            if (now.isBefore(inst.getDueDate())) {
                // Early payment: reward (discount)
                long daysEarly = Math.abs(daysDiff);
                BigDecimal discount = inst.getAmount().multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysEarly)).setScale(2, RoundingMode.HALF_UP);
                paidAmount = paidAmount.subtract(discount);
                isReward = true;
            } else if (now.isAfter(inst.getDueDate())) {
                // Late payment: penalty
                long daysLate = daysDiff;
                BigDecimal penalty = inst.getAmount().multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysLate)).setScale(2, RoundingMode.HALF_UP);
                paidAmount = paidAmount.add(penalty);
                isPenalty = true;
            }

            inst.setPaidAmount(paidAmount);
            inst.setPaid(true);
            inst.setPaymentDate(now);
            loanInstallmentRepository.save(inst);
            remaining = remaining.subtract(inst.getAmount()); // always subtract original amount for payment logic
            totalSpent = totalSpent.add(paidAmount);
            paidCount++;

            PayInstallmentResponseDTO.InstallmentPaymentDetail detail = new PayInstallmentResponseDTO.InstallmentPaymentDetail();
            detail.setInstallmentId(inst.getId());
            detail.setPaidAmount(paidAmount);
            detail.setPaymentDate(inst.getPaymentDate().toString());
            detail.setReward(isReward);
            detail.setPenalty(isPenalty);
            details.add(detail);
        }

        // If all installments are paid, mark loan as paid
        boolean loanFullyPaid = installments.stream().allMatch(LoanInstallment::isPaid);
        if (loanFullyPaid && !loan.isPaid()) {
            loan.setPaid(true);
            loanRepository.save(loan);
        }
        
        PayInstallmentResponseDTO resp = new PayInstallmentResponseDTO();
        resp.setNumberOfInstallmentsPaid(paidCount);
        resp.setTotalAmountSpent(totalSpent);
        resp.setLoanFullyPaid(loanFullyPaid);
        resp.setPaidInstallments(details);
        return resp;
    }
}
