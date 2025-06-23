package com.creditapi.controller;

import com.creditapi.dto.LoanCreateResponseDTO;
import com.creditapi.dto.LoanInstallmentDTO;
import com.creditapi.dto.LoanCreateRequestDTO;
import com.creditapi.dto.LoanResponseDTO;
import com.creditapi.dto.PayInstallmentRequestDTO;
import com.creditapi.dto.PayInstallmentResponseDTO;
import com.creditapi.exception.LoanNotFoundException;
import com.creditapi.mapper.LoanMapper;
import com.creditapi.service.LoanService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(LoanController.class)
class LoanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private LoanMapper loanMapper;

    @Test
    @WithMockUser
    void shouldCreateLoan() throws Exception {
        LoanCreateResponseDTO createResponse = new LoanCreateResponseDTO();
        createResponse.setId(UUID.randomUUID());
        createResponse.setCustomerId(UUID.randomUUID());
        createResponse.setLoanAmount(new BigDecimal("1000"));
        createResponse.setInterestRate(0.2);
        createResponse.setNumberOfInstallments(12);
        createResponse.setCreateDate(LocalDate.now());
        createResponse.setPaymentAmount(new BigDecimal("1200.0"));
        createResponse.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        Mockito.when(loanService.createLoan(any(LoanCreateRequestDTO.class))).thenReturn(createResponse);

        String requestJson = "{" +
                "\"customerId\": \"" + createResponse.getCustomerId() + "\"," +
                "\"loanAmount\": 1000," +
                "\"interestRate\": 0.2," +
                "\"numberOfInstallments\": 12}";

        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanAmount", is(1000)))
                .andExpect(jsonPath("$.interestRate", is(0.2)))
                .andExpect(jsonPath("$.numberOfInstallments", is(12)))
                .andExpect(jsonPath("$.paymentAmount", is(1200.0)))
                .andExpect(jsonPath("$.firstPaymentDate", notNullValue()));
    }

    @Test
    @WithMockUser
    void shouldListLoansByCustomerId() throws Exception {
        LoanResponseDTO response = new LoanResponseDTO();
        response.setId(UUID.randomUUID());
        response.setCustomerId(UUID.randomUUID());
        response.setLoanAmount(new BigDecimal("1000"));
        response.setInterestRate(0.2);
        response.setNumberOfInstallments(12);
        response.setCreateDate(LocalDate.now());
        response.setPaid(false);
        response.setPaymentAmount(new BigDecimal("1200.0"));
        response.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        Mockito.when(loanService.listLoansByCustomerId(response.getCustomerId(), null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/loans?customerId=" + response.getCustomerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanAmount", is(1000)))
                .andExpect(jsonPath("$[0].interestRate", is(0.2)))
                .andExpect(jsonPath("$[0].numberOfInstallments", is(12)))
                .andExpect(jsonPath("$[0].paymentAmount", is(1200.0)))
                .andExpect(jsonPath("$[0].firstPaymentDate", notNullValue()));
    }

    @Test
    @WithMockUser
    void shouldRejectNegativeLoanAmount() throws Exception {
        String requestJson = "{" +
                "\"customerId\": \"" + UUID.randomUUID() + "\"," +
                "\"loanAmount\": -1000," +
                "\"interestRate\": 0.2," +
                "\"numberOfInstallments\": 12}";
        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("loanAmount")));
    }

    @Test
    @WithMockUser
    void shouldRejectOutOfRangeInterestRate() throws Exception {
        String requestJson = "{" +
                "\"customerId\": \"" + UUID.randomUUID() + "\"," +
                "\"loanAmount\": 1000," +
                "\"interestRate\": 0.6," +
                "\"numberOfInstallments\": 12}";
        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("interestRate")));
    }

    @Test
    @WithMockUser
    void shouldRejectInvalidEnumValueForInstallments() throws Exception {
        String requestJson = "{" +
                "\"customerId\": \"" + UUID.randomUUID() + "\"," +
                "\"loanAmount\": 1000," +
                "\"interestRate\": 0.2," +
                "\"numberOfInstallments\": 36}";
        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("numberOfInstallments")));
    }

    @Test
    @WithMockUser
    void shouldListLoansByCustomerIdWithAllFilters() throws Exception {
        LoanResponseDTO response1 = new LoanResponseDTO();
        response1.setId(UUID.randomUUID());
        response1.setCustomerId(UUID.randomUUID());
        response1.setLoanAmount(new BigDecimal("1000"));
        response1.setInterestRate(0.2);
        response1.setNumberOfInstallments(12);
        response1.setCreateDate(LocalDate.now());
        response1.setPaid(false);
        response1.setPaymentAmount(new BigDecimal("1200.0"));
        response1.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        LoanResponseDTO response2 = new LoanResponseDTO();
        response2.setId(UUID.randomUUID());
        response2.setCustomerId(response1.getCustomerId());
        response2.setLoanAmount(new BigDecimal("2000"));
        response2.setInterestRate(0.2);
        response2.setNumberOfInstallments(12);
        response2.setCreateDate(LocalDate.now());
        response2.setPaid(true);
        response2.setPaymentAmount(new BigDecimal("2400.0"));
        response2.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        // Only response2 matches all filters
        Mockito.when(loanService.listLoansByCustomerId(
            response1.getCustomerId(), 12, true)).thenReturn(List.of(response2));

        mockMvc.perform(get("/loans")
                .param("customerId", response1.getCustomerId().toString())
                .param("numberOfInstallments", "12")
                .param("isPaid", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].loanAmount", is(2000)))
                .andExpect(jsonPath("$[0].isPaid", is(true)))
                .andExpect(jsonPath("$[0].numberOfInstallments", is(12)));
    }

    @Test
    @WithMockUser
    void shouldListLoansByCustomerIdWithIsPaidFilter() throws Exception {
        LoanResponseDTO response1 = new LoanResponseDTO();
        response1.setId(UUID.randomUUID());
        response1.setCustomerId(UUID.randomUUID());
        response1.setLoanAmount(new BigDecimal("1000"));
        response1.setInterestRate(0.2);
        response1.setNumberOfInstallments(12);
        response1.setCreateDate(LocalDate.now());
        response1.setPaid(false);
        response1.setPaymentAmount(new BigDecimal("1200.0"));
        response1.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        LoanResponseDTO response2 = new LoanResponseDTO();
        response2.setId(UUID.randomUUID());
        response2.setCustomerId(response1.getCustomerId());
        response2.setLoanAmount(new BigDecimal("2000"));
        response2.setInterestRate(0.2);
        response2.setNumberOfInstallments(12);
        response2.setCreateDate(LocalDate.now());
        response2.setPaid(true);
        response2.setPaymentAmount(new BigDecimal("2400.0"));
        response2.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        Mockito.when(loanService.listLoansByCustomerId(
            response1.getCustomerId(), null, true)).thenReturn(List.of(response2));

        mockMvc.perform(get("/loans")
                .param("customerId", response1.getCustomerId().toString())
                .param("isPaid", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].loanAmount", is(2000)))
                .andExpect(jsonPath("$[0].isPaid", is(true)))
                .andExpect(jsonPath("$[0].numberOfInstallments", is(12)));
    }

    @Test
    @WithMockUser
    void shouldListLoansByCustomerIdWithNumberOfInstallmentsFilter() throws Exception {
        LoanResponseDTO response1 = new LoanResponseDTO();
        response1.setId(UUID.randomUUID());
        response1.setCustomerId(UUID.randomUUID());
        response1.setLoanAmount(new BigDecimal("1000"));
        response1.setInterestRate(0.2);
        response1.setNumberOfInstallments(6);
        response1.setCreateDate(LocalDate.now());
        response1.setPaid(false);
        response1.setPaymentAmount(new BigDecimal("1200.0"));
        response1.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        LoanResponseDTO response2 = new LoanResponseDTO();
        response2.setId(UUID.randomUUID());
        response2.setCustomerId(response1.getCustomerId());
        response2.setLoanAmount(new BigDecimal("2000"));
        response2.setInterestRate(0.2);
        response2.setNumberOfInstallments(12);
        response2.setCreateDate(LocalDate.now());
        response2.setPaid(true);
        response2.setPaymentAmount(new BigDecimal("2400.0"));
        response2.setFirstPaymentDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        Mockito.when(loanService.listLoansByCustomerId(
            response1.getCustomerId(), 6, null)).thenReturn(List.of(response1));

        mockMvc.perform(get("/loans")
                .param("customerId", response1.getCustomerId().toString())
                .param("numberOfInstallments", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].loanAmount", is(1000)))
                .andExpect(jsonPath("$[0].numberOfInstallments", is(6)))
                .andExpect(jsonPath("$[0].isPaid", is(false)));
    }

    @Test
    @WithMockUser
    void shouldListInstallmentsForLoan() throws Exception {
        UUID loanId = UUID.randomUUID();
        LoanInstallmentDTO inst1 = new LoanInstallmentDTO();
        inst1.setId(UUID.randomUUID());
        inst1.setAmount(new BigDecimal("100"));
        inst1.setPaidAmount(BigDecimal.ZERO);
        inst1.setDueDate(LocalDate.of(2025, 7, 1));
        inst1.setPaid(false);
        LoanInstallmentDTO inst2 = new LoanInstallmentDTO();
        inst2.setId(UUID.randomUUID());
        inst2.setAmount(new BigDecimal("100"));
        inst2.setPaidAmount(BigDecimal.ZERO);
        inst2.setDueDate(LocalDate.of(2025, 8, 1));
        inst2.setPaid(false);
        List<LoanInstallmentDTO> expected = List.of(inst1, inst2);
        Mockito.when(loanService.listInstallmentsByLoanId(loanId)).thenReturn(expected);

        mockMvc.perform(get("/loans/" + loanId + "/installments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].amount", is(100)))
                .andExpect(jsonPath("$[0].isPaid", is(false)))
                .andExpect(jsonPath("$[1].dueDate", is("2025-08-01")));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenLoanDoesNotExist() throws Exception {
        UUID loanId = UUID.randomUUID();
        Mockito.when(loanService.listInstallmentsByLoanId(loanId)).thenThrow(new LoanNotFoundException("Loan not found"));

        mockMvc.perform(get("/loans/" + loanId + "/installments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Loan not found")));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidLoanId() throws Exception {
        mockMvc.perform(get("/loans/not-a-uuid/installments"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidCustomerId() throws Exception {
        mockMvc.perform(get("/loans?customerId=not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldPaySingleInstallment() throws Exception {
        UUID loanId = UUID.randomUUID();
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("100"));
        PayInstallmentResponseDTO response = new PayInstallmentResponseDTO();
        response.setNumberOfInstallmentsPaid(1);
        response.setTotalAmountSpent(new BigDecimal("100"));
        response.setLoanFullyPaid(false);
        response.setPaidInstallments(List.of());
        Mockito.when(loanService.payInstallments(Mockito.eq(loanId), any(PayInstallmentRequestDTO.class))).thenReturn(response);

        String requestJson = "{\"amount\":100}";
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfInstallmentsPaid", is(1)))
                .andExpect(jsonPath("$.totalAmountSpent", is(100)))
                .andExpect(jsonPath("$.loanFullyPaid", is(false)));
    }

    @Test
    @WithMockUser
    void shouldPayMultipleInstallments() throws Exception {
        UUID loanId = UUID.randomUUID();
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("300"));
        PayInstallmentResponseDTO response = new PayInstallmentResponseDTO();
        response.setNumberOfInstallmentsPaid(3);
        response.setTotalAmountSpent(new BigDecimal("300"));
        response.setLoanFullyPaid(false);
        response.setPaidInstallments(List.of());
        Mockito.when(loanService.payInstallments(Mockito.eq(loanId), any(PayInstallmentRequestDTO.class))).thenReturn(response);

        String requestJson = "{\"amount\":300}";
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfInstallmentsPaid", is(3)))
                .andExpect(jsonPath("$.totalAmountSpent", is(300)))
                .andExpect(jsonPath("$.loanFullyPaid", is(false)));
    }

    @Test
    @WithMockUser
    void shouldFullyPayLoan() throws Exception {
        UUID loanId = UUID.randomUUID();
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("600"));
        PayInstallmentResponseDTO response = new PayInstallmentResponseDTO();
        response.setNumberOfInstallmentsPaid(6);
        response.setTotalAmountSpent(new BigDecimal("600"));
        response.setLoanFullyPaid(true);
        response.setPaidInstallments(List.of());
        Mockito.when(loanService.payInstallments(Mockito.eq(loanId), any(PayInstallmentRequestDTO.class))).thenReturn(response);

        String requestJson = "{\"amount\":600}";
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfInstallmentsPaid", is(6)))
                .andExpect(jsonPath("$.totalAmountSpent", is(600)))
                .andExpect(jsonPath("$.loanFullyPaid", is(true)));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForNegativeOrZeroAmount() throws Exception {
        UUID loanId = UUID.randomUUID();
        String requestJson = "{\"amount\":0}";
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("must be positive")));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundForInvalidLoanId() throws Exception {
        UUID loanId = UUID.randomUUID();
        String requestJson = "{\"amount\":100}";
        Mockito.when(loanService.payInstallments(Mockito.eq(loanId), any(PayInstallmentRequestDTO.class)))
                .thenThrow(new LoanNotFoundException("Loan not found"));
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Loan not found")));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidLoanIdFormat() throws Exception {
        String requestJson = "{\"amount\":100}";
        mockMvc.perform(post("/loans/not-a-uuid/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnOkIfNoInstallmentsEligibleDueToThreeMonthRule() throws Exception {
        UUID loanId = UUID.randomUUID();
        PayInstallmentRequestDTO request = new PayInstallmentRequestDTO();
        request.setAmount(new BigDecimal("100"));
        PayInstallmentResponseDTO response = new PayInstallmentResponseDTO();
        response.setNumberOfInstallmentsPaid(0);
        response.setTotalAmountSpent(BigDecimal.ZERO);
        response.setLoanFullyPaid(false);
        response.setPaidInstallments(List.of());
        Mockito.when(loanService.payInstallments(Mockito.eq(loanId), any(PayInstallmentRequestDTO.class))). thenReturn(response);

        String requestJson = "{\"amount\":100}";
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfInstallmentsPaid", is(0)))
                .andExpect(jsonPath("$.totalAmountSpent", is(0)))
                .andExpect(jsonPath("$.loanFullyPaid", is(false)));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForMissingRequestBody() throws Exception {
        UUID loanId = UUID.randomUUID();
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForMalformedRequestBody() throws Exception {
        UUID loanId = UUID.randomUUID();
        String malformedJson = "{\"amount\": }";
        mockMvc.perform(post("/loans/" + loanId + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
