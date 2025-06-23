package com.creditapi.service;

import com.creditapi.dto.LoanCreateRequestDTO;
import com.creditapi.dto.LoanCreateResponseDTO;
import com.creditapi.dto.LoanResponseDTO;
import com.creditapi.dto.PayInstallmentRequestDTO;
import com.creditapi.dto.PayInstallmentResponseDTO;
import com.creditapi.exception.CustomerNotFoundException;
import com.creditapi.exception.InsufficientCreditLimitException;
import com.creditapi.exception.LoanNotFoundException;
import com.creditapi.dto.LoanInstallmentDTO;
import com.creditapi.mapper.LoanMapper;
import com.creditapi.model.Customer;
import com.creditapi.model.InstallmentOption;
import com.creditapi.model.Loan;
import com.creditapi.model.LoanInstallment;
import com.creditapi.repository.CustomerRepository;
import com.creditapi.repository.LoanInstallmentRepository;
import com.creditapi.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanMapper loanMapper;
    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;
    @InjectMocks
    private LoanServiceImpl loanService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setCreditLimit(new BigDecimal("10000"));
        customer.setUsedCreditLimit(new BigDecimal("0"));
        reset(loanInstallmentRepository); // Ensure mock is reset before each test

        when(loanMapper.toLoanCreateResponseDTO(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            LoanCreateResponseDTO dto = new LoanCreateResponseDTO();
            dto.setId(UUID.randomUUID());
            dto.setCustomerId(loan.getCustomerId());
            dto.setLoanAmount(loan.getLoanAmount());
            dto.setNumberOfInstallments(loan.getNumberOfInstallments() != null ? loan.getNumberOfInstallments().getValue() : 12);
            dto.setInterestRate(loan.getInterestRate());
            dto.setCreateDate(loan.getCreateDate() != null ? loan.getCreateDate().toLocalDate() : LocalDate.now());
            return dto;
        });
    }

    @Test
    void shouldCreateLoanWhenCustomerHasEnoughLimit() {
        // Arrange
        LoanCreateRequestDTO LoanCreateRequestDTO = new LoanCreateRequestDTO();
        LoanCreateRequestDTO.setCustomerId(customer.getId());
        LoanCreateRequestDTO.setLoanAmount(new BigDecimal("5000"));
        LoanCreateRequestDTO.setInterestRate(0.2);
        LoanCreateRequestDTO.setNumberOfInstallments(InstallmentOption.TWELVE.getValue()); // Use enum value
        
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanMapper.toEntity(any(LoanCreateRequestDTO.class))).thenAnswer(invocation -> {
            LoanCreateRequestDTO dto = invocation.getArgument(0);
            Loan loan = new Loan();
            loan.setCustomerId(dto.getCustomerId());
            loan.setLoanAmount(dto.getLoanAmount());
            loan.setInterestRate(dto.getInterestRate());
            loan.setNumberOfInstallments(InstallmentOption.TWELVE); // Always set entity with InstallmentOption
            return loan;
        });

        // Act
        LoanCreateResponseDTO response = loanService.createLoan(LoanCreateRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(LoanCreateRequestDTO.getLoanAmount(), response.getLoanAmount());
        assertEquals(LoanCreateRequestDTO.getNumberOfInstallments(), response.getNumberOfInstallments());
        assertEquals(LoanCreateRequestDTO.getInterestRate(), response.getInterestRate());
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void shouldNotCreateLoanWhenCustomerHasInsufficientLimit() {
        // Arrange
        customer.setCreditLimit(new BigDecimal("1000"));
        LoanCreateRequestDTO LoanCreateRequestDTO = new LoanCreateRequestDTO();
        LoanCreateRequestDTO.setCustomerId(customer.getId());
        LoanCreateRequestDTO.setLoanAmount(new BigDecimal("5000"));
        LoanCreateRequestDTO.setInterestRate(0.2);
        LoanCreateRequestDTO.setNumberOfInstallments(12); // Use integer
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        // Act & Assert
        Exception exception = assertThrows(InsufficientCreditLimitException.class, () ->
            loanService.createLoan(LoanCreateRequestDTO)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("insufficient credit limit"));
        verify(loanRepository, never()).save(any(Loan.class));
    }



    @Test
    void shouldListLoansByCustomerId() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Loan loan1 = new Loan();
        loan1.setCustomerId(customerId);
        loan1.setLoanAmount(new BigDecimal("1000"));
        loan1.setInterestRate(0.2);
        loan1.setNumberOfInstallments(InstallmentOption.TWELVE);
        Loan loan2 = new Loan();
        loan2.setCustomerId(customerId);
        loan2.setLoanAmount(new BigDecimal("2000"));
        loan2.setInterestRate(0.1);
        loan2.setNumberOfInstallments(InstallmentOption.NINE);
        List<Loan> expectedLoans = Arrays.asList(loan1, loan2);
        when(loanRepository.findByCustomerId(customerId)).thenReturn(expectedLoans);
        when(loanMapper.toResponseDto(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            LoanResponseDTO dto = new LoanResponseDTO();
            dto.setCustomerId(loan.getCustomerId());
            dto.setLoanAmount(loan.getLoanAmount());
            dto.setInterestRate(loan.getInterestRate());
            dto.setNumberOfInstallments(loan.getNumberOfInstallments().getValue()); // Use integer for DTO
            return dto;
        });

        // Act
        List<LoanResponseDTO> loans = loanService.listLoansByCustomerId(customerId, null, null);

        // Assert
        assertNotNull(loans);
        assertEquals(2, loans.size());
        assertTrue(loans.stream().allMatch(l -> l.getCustomerId().equals(customerId)));
        verify(loanRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void shouldCreateInstallmentsWhenLoanIsCreated() {
        // Arrange
        LoanCreateRequestDTO LoanCreateRequestDTO = new LoanCreateRequestDTO();
        LoanCreateRequestDTO.setCustomerId(customer.getId());
        LoanCreateRequestDTO.setLoanAmount(new BigDecimal("1200"));
        LoanCreateRequestDTO.setInterestRate(0.2);
        LoanCreateRequestDTO.setNumberOfInstallments(InstallmentOption.TWELVE.getValue()); // Use enum value
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Loan loanEntity = new Loan();
        loanEntity.setCustomerId(customer.getId());
        loanEntity.setLoanAmount(LoanCreateRequestDTO.getLoanAmount());
        loanEntity.setInterestRate(LoanCreateRequestDTO.getInterestRate());
        loanEntity.setNumberOfInstallments(InstallmentOption.TWELVE);
        when(loanMapper.toEntity(any(LoanCreateRequestDTO.class))).thenReturn(loanEntity);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loanService.createLoan(LoanCreateRequestDTO);

        // Assert
        verify(loanInstallmentRepository, times(12)).save(any(LoanInstallment.class));
    }

    @Test
    void shouldCreateInstallmentsWithCorrectAmounts_NoRemainder() {
        // Arrange
        LoanCreateRequestDTO LoanCreateRequestDTO = new LoanCreateRequestDTO();
        LoanCreateRequestDTO.setCustomerId(customer.getId());
        LoanCreateRequestDTO.setLoanAmount(new BigDecimal("1200")); // 1200 * 1.0 = 1200, divides evenly by 12
        LoanCreateRequestDTO.setInterestRate(0.1);
        LoanCreateRequestDTO.setNumberOfInstallments(InstallmentOption.TWELVE.getValue()); // Use enum value
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Loan loanEntity = new Loan();
        loanEntity.setCustomerId(customer.getId());
        loanEntity.setLoanAmount(LoanCreateRequestDTO.getLoanAmount());
        loanEntity.setInterestRate(LoanCreateRequestDTO.getInterestRate());
        loanEntity.setNumberOfInstallments(InstallmentOption.TWELVE);
        when(loanMapper.toEntity(any(LoanCreateRequestDTO.class))).thenReturn(loanEntity);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loanService.createLoan(LoanCreateRequestDTO);

        // Assert
        BigDecimal totalToBePaid = new BigDecimal("1200").multiply(BigDecimal.valueOf(1.1));
        BigDecimal baseAmount = totalToBePaid.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.DOWN);
        ArgumentCaptor<LoanInstallment> captor = ArgumentCaptor.forClass(LoanInstallment.class);
        verify(loanInstallmentRepository, times(12)).save(captor.capture());
        List<LoanInstallment> savedInstallments = captor.getAllValues();
        long baseCount = savedInstallments.stream()
            .filter(inst -> inst.getAmount().compareTo(baseAmount) == 0)
            .count();
        assertEquals(12, baseCount);
    }

    @Test
    void shouldCreateInstallmentsWithCorrectAmounts_WithRemainder() {
        // Arrange
        LoanCreateRequestDTO LoanCreateRequestDTO = new LoanCreateRequestDTO();
        LoanCreateRequestDTO.setCustomerId(customer.getId());
        LoanCreateRequestDTO.setLoanAmount(new BigDecimal("1000")); // 1000 * 1.2 = 1200, but let's use 1000 * 1.1 = 1100 for 6 installments
        LoanCreateRequestDTO.setInterestRate(0.1);
        LoanCreateRequestDTO.setNumberOfInstallments(InstallmentOption.SIX.getValue()); // Use enum value
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Loan loanEntity = new Loan();
        loanEntity.setCustomerId(customer.getId());
        loanEntity.setLoanAmount(LoanCreateRequestDTO.getLoanAmount());
        loanEntity.setInterestRate(LoanCreateRequestDTO.getInterestRate());
        loanEntity.setNumberOfInstallments(InstallmentOption.SIX);
        when(loanMapper.toEntity(any(LoanCreateRequestDTO.class))).thenReturn(loanEntity);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        loanService.createLoan(LoanCreateRequestDTO);

        // Assert
        BigDecimal totalToBePaid = new BigDecimal("1000").multiply(BigDecimal.valueOf(1.1)); // 1100
        BigDecimal baseAmount = totalToBePaid.divide(BigDecimal.valueOf(6), 2, java.math.RoundingMode.DOWN); // 183.33
        BigDecimal totalAssigned = baseAmount.multiply(BigDecimal.valueOf(6));
        BigDecimal remainder = totalToBePaid.subtract(totalAssigned); // 0.02
        BigDecimal lastAmount = baseAmount.add(remainder); // 183.35

        ArgumentCaptor<LoanInstallment> captor = ArgumentCaptor.forClass(LoanInstallment.class);
        verify(loanInstallmentRepository, times(6)).save(captor.capture());
        List<LoanInstallment> savedInstallments = captor.getAllValues();
        long baseCount = savedInstallments.stream()
            .filter(inst -> inst.getAmount().compareTo(baseAmount) == 0)
            .count();
        long lastCount = savedInstallments.stream()
            .filter(inst -> inst.getAmount().compareTo(lastAmount) == 0)
            .count();
        assertEquals(5, baseCount);
        assertEquals(1, lastCount);
    }

    @Test
    void shouldSumInstallmentsToTotalToBePaid() {
        // Arrange
        LoanCreateRequestDTO LoanCreateRequestDTO = new LoanCreateRequestDTO();
        LoanCreateRequestDTO.setCustomerId(customer.getId());
        LoanCreateRequestDTO.setLoanAmount(new BigDecimal("1000"));
        LoanCreateRequestDTO.setInterestRate(0.2);
        LoanCreateRequestDTO.setNumberOfInstallments(InstallmentOption.TWELVE.getValue()); // Use enum value
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Loan loanEntity = new Loan();
        loanEntity.setCustomerId(customer.getId());
        loanEntity.setLoanAmount(LoanCreateRequestDTO.getLoanAmount());
        loanEntity.setInterestRate(LoanCreateRequestDTO.getInterestRate());
        loanEntity.setNumberOfInstallments(InstallmentOption.TWELVE);
        when(loanMapper.toEntity(any(LoanCreateRequestDTO.class))).thenReturn(loanEntity);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<LoanInstallment> createdInstallments = new java.util.ArrayList<>();
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> {
            LoanInstallment inst = invocation.getArgument(0);
            createdInstallments.add(inst);
            return inst;
        });

        // Act
        loanService.createLoan(LoanCreateRequestDTO);

        // Assert
        BigDecimal totalToBePaid = new BigDecimal("1000").multiply(BigDecimal.valueOf(1.2));
        BigDecimal sum = createdInstallments.stream()
            .map(LoanInstallment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalToBePaid.compareTo(sum));
    }

    @Test
    void shouldReturnFirstPaymentDateAndTotalAmountInListLoansByCustomerId() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Loan loan = new Loan();
        loan.setCustomerId(customerId);
        loan.setLoanAmount(new BigDecimal("1000"));
        loan.setInterestRate(0.2);
        loan.setNumberOfInstallments(InstallmentOption.TWELVE);
        // Set createDate for correct firstPaymentDate calculation
        loan.setCreateDate(java.time.LocalDate.of(2025, 6, 22).atStartOfDay());
        // Create installments with known due dates
        LoanInstallment inst1 = new LoanInstallment();
        inst1.setDueDate(java.time.LocalDate.of(2025, 7, 1));
        LoanInstallment inst2 = new LoanInstallment();
        inst2.setDueDate(java.time.LocalDate.of(2025, 8, 1));
        loan.setInstallments(Arrays.asList(inst1, inst2));
        when(loanRepository.findByCustomerId(customerId)).thenReturn(List.of(loan));
        when(loanMapper.toResponseDto(any(Loan.class))).thenAnswer(invocation -> {
            Loan l = invocation.getArgument(0);
            LoanResponseDTO dto = new LoanResponseDTO();
            dto.setCustomerId(l.getCustomerId());
            dto.setLoanAmount(l.getLoanAmount());
            dto.setInterestRate(l.getInterestRate());
            dto.setNumberOfInstallments(l.getNumberOfInstallments().getValue()); // Use integer for DTO
            return dto;
        });

        // Act
        List<LoanResponseDTO> result = loanService.listLoansByCustomerId(customerId, null, null);

        // Assert
        assertEquals(1, result.size());
        LoanResponseDTO dto = result.get(0);
        assertTrue(new BigDecimal("1200.0").compareTo(dto.getPaymentAmount()) == 0); // 1000 * 1.2
        assertEquals(java.time.LocalDate.of(2025, 7, 1), dto.getFirstPaymentDate());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        LoanCreateRequestDTO LoanCreateRequestDTO = new LoanCreateRequestDTO();
        LoanCreateRequestDTO.setCustomerId(UUID.randomUUID());
        LoanCreateRequestDTO.setLoanAmount(new BigDecimal("1000"));
        LoanCreateRequestDTO.setInterestRate(0.2);
        LoanCreateRequestDTO.setNumberOfInstallments(InstallmentOption.TWELVE.getValue());
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(CustomerNotFoundException.class, () ->
            loanService.createLoan(LoanCreateRequestDTO)
        );
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void shouldListLoansByCustomerIdWithAllFilters() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Loan loan1 = new Loan();
        loan1.setCustomerId(customerId);
        loan1.setLoanAmount(new BigDecimal("1000"));
        loan1.setInterestRate(0.2);
        loan1.setNumberOfInstallments(InstallmentOption.TWELVE);
        loan1.setPaid(false);
        Loan loan2 = new Loan();
        loan2.setCustomerId(customerId);
        loan2.setLoanAmount(new BigDecimal("2000"));
        loan2.setInterestRate(0.2);
        loan2.setNumberOfInstallments(InstallmentOption.TWELVE);
        loan2.setPaid(true);
        List<Loan> allLoans = Arrays.asList(loan1, loan2);
        when(loanRepository.findByCustomerId(customerId)).thenReturn(allLoans);
        when(loanMapper.toResponseDto(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            LoanResponseDTO dto = new LoanResponseDTO();
            dto.setCustomerId(loan.getCustomerId());
            dto.setLoanAmount(loan.getLoanAmount());
            dto.setInterestRate(loan.getInterestRate());
            dto.setNumberOfInstallments(loan.getNumberOfInstallments().getValue());
            dto.setPaid(loan.isPaid());
            return dto;
        });

        // Act
        List<LoanResponseDTO> filtered = loanService.listLoansByCustomerId(customerId, 12, true);

        // Assert
        assertEquals(1, filtered.size());
        LoanResponseDTO dto = filtered.get(0);
        assertEquals(new BigDecimal("2000"), dto.getLoanAmount());
        assertTrue(dto.isPaid());
        assertEquals(12, dto.getNumberOfInstallments());
    }

    @Test
    void shouldListInstallmentsForGivenLoan() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        LoanInstallment inst1 = new LoanInstallment();
        inst1.setId(UUID.randomUUID());
        inst1.setLoan(loan);
        inst1.setAmount(new java.math.BigDecimal("100"));
        inst1.setPaidAmount(java.math.BigDecimal.ZERO);
        inst1.setDueDate(java.time.LocalDate.of(2025, 7, 1));
        inst1.setPaid(false);
        LoanInstallment inst2 = new LoanInstallment();
        inst2.setId(UUID.randomUUID());
        inst2.setLoan(loan);
        inst2.setAmount(new java.math.BigDecimal("100"));
        inst2.setPaidAmount(java.math.BigDecimal.ZERO);
        inst2.setDueDate(java.time.LocalDate.of(2025, 8, 1));
        inst2.setPaid(false);
        java.util.List<LoanInstallment> entityList = java.util.Arrays.asList(inst1, inst2);
        java.util.List<LoanInstallmentDTO> expected = new java.util.ArrayList<>();
        for (LoanInstallment entity : entityList) {
            LoanInstallmentDTO dto = new LoanInstallmentDTO();
            dto.setId(entity.getId());
            dto.setLoanId(loanId);
            dto.setAmount(entity.getAmount());
            dto.setPaidAmount(entity.getPaidAmount());
            dto.setDueDate(entity.getDueDate());
            dto.setPaid(entity.isPaid());
            expected.add(dto);
            when(loanMapper.toLoanInstallmentDTO(entity)).thenReturn(dto);
        }
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(entityList);
        when(loanRepository.existsById(loanId)).thenReturn(true);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        // Act
        java.util.List<LoanInstallmentDTO> result = loanService.listInstallmentsByLoanId(loanId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expected.get(0).getId(), result.get(0).getId());
        assertEquals(expected.get(1).getId(), result.get(1).getId());
        verify(loanInstallmentRepository, times(1)).findByLoanId(loanId);
    }

    @Test
    void shouldPayFirstSingleInstallment() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        // Create 6 installments, mark none as paid
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            inst.setPaidAmount(BigDecimal.ZERO);
            inst.setDueDate(LocalDate.now().plusMonths(i));
            inst.setPaid(false);
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("100"));
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        assertEquals(1, response.getNumberOfInstallmentsPaid());
        assertEquals(new BigDecimal("100"), response.getTotalAmountSpent());
        assertFalse(response.isLoanFullyPaid());
        assertTrue(installments.get(0).isPaid());
        assertNotNull(installments.get(0).getPaymentDate());
        assertEquals(new BigDecimal("100"), installments.get(0).getPaidAmount());
        assertFalse(loan.isPaid());

        // Verify that loanInstallmentRepository.save is called for the paid installment
        verify(loanInstallmentRepository, times(1)).save(installments.get(0));
        // Verify that loanRepository.save is not called (since loan is not fully paid)
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void shouldPayMultipleInstallmentsIfAmountCoversMoreThanOne() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        // 6 unpaid installments, each 100
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            inst.setPaidAmount(BigDecimal.ZERO);
            inst.setDueDate(LocalDate.now().plusMonths(i));
            inst.setPaid(false);
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("300")); // enough for 3 installments
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        assertEquals(3, response.getNumberOfInstallmentsPaid());
        // Calculate expected paid amounts for each installment
        BigDecimal expectedTotal = BigDecimal.ZERO;
        for (int i = 0; i < 3; i++) {
            int daysEarly = (int) ChronoUnit.DAYS.between(LocalDate.now(), installments.get(i).getDueDate());
            BigDecimal expectedPaid = new BigDecimal("100").subtract(new BigDecimal("100").multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysEarly))).setScale(2, RoundingMode.HALF_UP);
            assertTrue(installments.get(i).isPaid());
            assertEquals(0, expectedPaid.compareTo(installments.get(i).getPaidAmount().setScale(2, RoundingMode.HALF_UP)));
            expectedTotal = expectedTotal.add(expectedPaid);
        }
        expectedTotal = expectedTotal.setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, expectedTotal.compareTo(response.getTotalAmountSpent().setScale(2, RoundingMode.HALF_UP)));
        assertFalse(installments.get(3).isPaid());
        assertFalse(response.isLoanFullyPaid());
        assertFalse(loan.isPaid());
    }

    @Test
    void shouldFullyPayLoanWithMultipleInstallmentsInOnePayment() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        // 6 installments, first 3 already paid, last 3 unpaid and due within 3 months
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            if (i < 3) {
                inst.setPaidAmount(new BigDecimal("100"));
                inst.setPaid(true);
                inst.setPaymentDate(LocalDate.now().minusMonths(3 - i));
            } else {
                inst.setPaidAmount(BigDecimal.ZERO);
                inst.setPaid(false);
                inst.setPaymentDate(null);
            }
            inst.setDueDate(LocalDate.now().plusWeeks(i)); // All due within 3 months
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("300")); // enough for last 3 installments
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        assertEquals(3, response.getNumberOfInstallmentsPaid());
        // Calculate expected paid amounts for each installment
        BigDecimal expectedTotal = BigDecimal.ZERO;
        for (int i = 3; i < 6; i++) {
            int daysEarly = (int) ChronoUnit.DAYS.between(LocalDate.now(), installments.get(i).getDueDate());
            BigDecimal expectedPaid = new BigDecimal("100").subtract(new BigDecimal("100").multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysEarly))).setScale(2, RoundingMode.HALF_UP);
            assertTrue(installments.get(i).isPaid());
            assertEquals(0, expectedPaid.compareTo(installments.get(i).getPaidAmount().setScale(2, RoundingMode.HALF_UP)));
            assertNotNull(installments.get(i).getPaymentDate());
            expectedTotal = expectedTotal.add(expectedPaid);
        }
        expectedTotal = expectedTotal.setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, expectedTotal.compareTo(response.getTotalAmountSpent().setScale(2, RoundingMode.HALF_UP)));
        assertTrue(response.isLoanFullyPaid());
        assertTrue(loan.isPaid());
        verify(loanInstallmentRepository, times(3)).save(any(LoanInstallment.class));
        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    void shouldNotPayIfAllInstallmentsArePaid() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(true);
        // All 6 installments already paid
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            inst.setPaidAmount(new BigDecimal("100"));
            inst.setDueDate(LocalDate.now().plusMonths(i));
            inst.setPaid(true);
            inst.setPaymentDate(LocalDate.now().minusDays(1));
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("100"));
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        assertEquals(0, response.getNumberOfInstallmentsPaid());
        assertEquals(BigDecimal.ZERO, response.getTotalAmountSpent());
        assertTrue(response.isLoanFullyPaid());
        assertTrue(loan.isPaid());

        // Should return a response with zero payments if loan is already fully paid
        PayInstallmentResponseDTO response2 = loanService.payInstallments(loanId, request);
        assertEquals(0, response2.getNumberOfInstallmentsPaid());
        assertEquals(BigDecimal.ZERO, response2.getTotalAmountSpent());
        assertTrue(response2.isLoanFullyPaid());
        assertTrue(loan.isPaid());
    }

    @Test
    void shouldNotPayIfAmountIsLessThanInstallment() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        // 6 unpaid installments
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            inst.setPaidAmount(BigDecimal.ZERO);
            inst.setDueDate(LocalDate.now().plusMonths(i));
            inst.setPaid(false);
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("50"));
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        assertEquals(0, response.getNumberOfInstallmentsPaid());
        assertEquals(BigDecimal.ZERO, response.getTotalAmountSpent());
        assertFalse(response.isLoanFullyPaid());
        assertFalse(loan.isPaid());
    }

    @Test
    void shouldThrowExceptionForNegativeOrZeroAmount() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(BigDecimal.ZERO);
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> loanService.payInstallments(loanId, request));
        request.setAmount(new BigDecimal("-10"));
        assertThrows(IllegalArgumentException.class, () -> loanService.payInstallments(loanId, request));
    }

    @Test
    void shouldThrowExceptionForInvalidLoanId() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("100"));
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(LoanNotFoundException.class, () -> loanService.payInstallments(loanId, request));
    }

    @Test
    void shouldOnlyPayFullInstallmentsAndIgnoreRemainder() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        // 6 unpaid installments, each 15
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("15"));
            inst.setPaidAmount(BigDecimal.ZERO);
            inst.setDueDate(LocalDate.now().plusMonths(i));
            inst.setPaid(false);
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("20"));
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        assertEquals(1, response.getNumberOfInstallmentsPaid());
        assertEquals(new BigDecimal("15"), response.getTotalAmountSpent());
        assertTrue(installments.get(0).isPaid());
        assertFalse(installments.get(1).isPaid());
        assertFalse(response.isLoanFullyPaid());
        assertFalse(loan.isPaid());
    }

    @Test
    void shouldNotPayIfNoInstallmentsEligibleDueToThreeMonthRule() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        // 6 unpaid installments, all due more than 3 months from now
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            inst.setPaidAmount(BigDecimal.ZERO);
            inst.setDueDate(LocalDate.now().plusMonths(4 + i)); // all due after 4+ months
            inst.setPaid(false);
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("100"));
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);
        assertEquals(0, response.getNumberOfInstallmentsPaid());
        assertEquals(BigDecimal.ZERO, response.getTotalAmountSpent());
        assertFalse(response.isLoanFullyPaid());
        assertFalse(loan.isPaid());
    }

    @Test
    void shouldApplyDiscountForEarlyPayment() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            inst.setPaidAmount(BigDecimal.ZERO);
            inst.setPaid(false);
            // Due date is in the future (early payment)
            inst.setDueDate(LocalDate.now().plusDays(10 + i));
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("200"));

        // Act
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);

        // Assert
        assertEquals(2, response.getNumberOfInstallmentsPaid());
        int daysEarly1 = (int) ChronoUnit.DAYS.between(LocalDate.now(), installments.get(0).getDueDate());
        int daysEarly2 = (int) ChronoUnit.DAYS.between(LocalDate.now(), installments.get(1).getDueDate());
        BigDecimal expected1 = new BigDecimal("100").subtract(new BigDecimal("100").multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysEarly1))).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected2 = new BigDecimal("100").subtract(new BigDecimal("100").multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysEarly2))).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = expected1.add(expected2).setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, expectedTotal.compareTo(response.getTotalAmountSpent().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, expected1.compareTo(response.getPaidInstallments().get(0).getPaidAmount().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, expected2.compareTo(response.getPaidInstallments().get(1).getPaidAmount().setScale(2, RoundingMode.HALF_UP)));
        assertTrue(response.isLoanFullyPaid());
        assertTrue(loan.isPaid());
    }

    @Test
    void shouldApplyPenaltyForLatePayment() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setPaid(false);
        List<LoanInstallment> installments = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
            LoanInstallment inst = new LoanInstallment();
            inst.setId(UUID.randomUUID());
            inst.setLoan(loan);
            inst.setAmount(new BigDecimal("100"));
            inst.setPaidAmount(BigDecimal.ZERO);
            inst.setPaid(false);
            // Due date is in the past (late payment)
            inst.setDueDate(LocalDate.now().minusDays(10 + i));
            installments.add(inst);
        }
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(installments);
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("200"));

        // Act
        PayInstallmentResponseDTO response = loanService.payInstallments(loanId, request);

        // Assert
        assertEquals(2, response.getNumberOfInstallmentsPaid());
        int daysLate1 = (int) ChronoUnit.DAYS.between(installments.get(0).getDueDate(), LocalDate.now());
        int daysLate2 = (int) ChronoUnit.DAYS.between(installments.get(1).getDueDate(), LocalDate.now());
        BigDecimal expected1 = new BigDecimal("100").add(new BigDecimal("100").multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysLate1))).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected2 = new BigDecimal("100").add(new BigDecimal("100").multiply(new BigDecimal("0.001")).multiply(BigDecimal.valueOf(daysLate2))).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = expected1.add(expected2).setScale(2, RoundingMode.HALF_UP);
        System.out.println("Expected1: " + expected1 + ", Actual1: " + response.getPaidInstallments().get(0).getPaidAmount());
        System.out.println("Expected2: " + expected2 + ", Actual2: " + response.getPaidInstallments().get(1).getPaidAmount());
        System.out.println("ExpectedTotal: " + expectedTotal + ", ActualTotal: " + response.getTotalAmountSpent());
        List<BigDecimal> expectedPaidAmounts = Arrays.asList(expected1, expected2).stream().sorted().toList();
        List<BigDecimal> actualPaidAmounts = response.getPaidInstallments().stream()
            .map(detail -> detail.getPaidAmount().setScale(2, RoundingMode.HALF_UP))
            .sorted()
            .toList();
        assertEquals(expectedPaidAmounts, actualPaidAmounts);
        assertTrue(response.isLoanFullyPaid());
        assertTrue(loan.isPaid());
    }
}
