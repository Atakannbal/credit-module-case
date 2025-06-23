package com.creditapi.mapper;

import com.creditapi.dto.LoanCreateRequestDTO;
import com.creditapi.dto.LoanResponseDTO;
import com.creditapi.model.Loan;
import com.creditapi.model.InstallmentOption;
import com.creditapi.dto.LoanCreateResponseDTO;
import com.creditapi.dto.LoanInstallmentDTO;
import com.creditapi.model.LoanInstallment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "installments", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "numberOfInstallments", source = "numberOfInstallments", qualifiedByName = "intToInstallmentOption")
    Loan toEntity(LoanCreateRequestDTO dto);

    @Mapping(target = "numberOfInstallments", source = "numberOfInstallments", qualifiedByName = "installmentOptionToInt")
    @Mapping(target = "createDate", expression = "java(entity.getCreateDate().toLocalDate())")
    @Mapping(target = "paymentAmount", ignore = true)
    @Mapping(target = "firstPaymentDate", ignore = true)
    LoanCreateResponseDTO toLoanCreateResponseDTO(Loan entity);


    @Mapping(target = "numberOfInstallments", source = "numberOfInstallments", qualifiedByName = "installmentOptionToInt")
    @Mapping(target = "paymentAmount", ignore = true)
    @Mapping(target = "firstPaymentDate", ignore = true)
    LoanResponseDTO toResponseDto(Loan entity);

    LoanCreateResponseDTO toLoanCreateResponseDTO(LoanResponseDTO response);

    @Named("intToInstallmentOption")
    static InstallmentOption intToInstallmentOption(Integer value) {
        if (value == null) return null;
        for (InstallmentOption option : InstallmentOption.values()) {
            if (option.getValue() == value) return option;
        }
        throw new IllegalArgumentException("Invalid installment option value: " + value);
    }

    @Named("installmentOptionToInt")
    static Integer installmentOptionToInt(InstallmentOption option) {
        return option != null ? option.getValue() : null;
    }

    @Mapping(target = "loanId", source = "loan.id")
    LoanInstallmentDTO toLoanInstallmentDTO(LoanInstallment entity);
}
