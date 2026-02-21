package org.nurfet.paymentstrackingsystem.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.MeterReading;
import org.nurfet.paymentstrackingsystem.domain.ServiceType;
import org.nurfet.paymentstrackingsystem.dto.request.MeterReadingCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.MeterReadingResponse;
import org.nurfet.paymentstrackingsystem.exception.BusinessException;
import org.nurfet.paymentstrackingsystem.exception.ResourceNotFoundException;
import org.nurfet.paymentstrackingsystem.mapper.MeterReadingMapper;
import org.nurfet.paymentstrackingsystem.repository.MeterReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final AccountService accountService;
    private final MeterReadingMapper meterReadingMapper;

    @Transactional
    public MeterReadingResponse create(Long accountId, MeterReadingCreateRequest request) {
        Account account = accountService.findEntityById(accountId);
        ServiceType serviceType = account.getServiceType();

        validateMeterSupport(serviceType);
        validateReadingLength(serviceType, request.value());

        MeterReading reading = meterReadingMapper.toEntity(account, request);
        MeterReading saved = meterReadingRepository.save(reading);
        return meterReadingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getByAccountId(Long accountId) {
        // Проверяем существование аккаунта
        accountService.findEntityById(accountId);

        return meterReadingRepository.findByAccountIdOrderByReadingDateDesc(accountId)
                .stream()
                .map(meterReadingMapper::toResponse)
                .toList();
    }

    @Transactional
    public MeterReadingResponse update(Long accountId, Long readingId, MeterReadingCreateRequest request) {
        Account account = accountService.findEntityById(accountId);
        ServiceType serviceType = account.getServiceType();

        validateMeterSupport(serviceType);
        validateReadingLength(serviceType, request.value());

        MeterReading reading = meterReadingRepository.findById(readingId)
                .orElseThrow(() -> new ResourceNotFoundException("Показание с ID %d не найдено".formatted(readingId)));

        if (!reading.getAccount().getId().equals(accountId)) {
            throw new BusinessException("Показание #%d не принадлежит лицевому счёту #%d".formatted(readingId, accountId));
        }

        reading.setValue(request.value());
        reading.setReadingDate(request.readingDate());
        MeterReading saved = meterReadingRepository.save(reading);
        return meterReadingMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long accountId, Long readingId) {
        accountService.findEntityById(accountId);

        MeterReading reading = meterReadingRepository.findById(readingId)
                .orElseThrow(() -> new ResourceNotFoundException("Показание с ID %d не найдено".formatted(readingId)));

        if (!reading.getAccount().getId().equals(accountId)) {
            throw new BusinessException("Показание #%d не принадлежит лицевому счёту #%d".formatted(readingId, accountId));
        }

        meterReadingRepository.delete(reading);
    }

    private void validateMeterSupport(ServiceType serviceType) {
        if (!serviceType.isHasMeter()) {
            throw new BusinessException(
                    "Услуга '%s' не поддерживает показания счётчика"
                            .formatted(serviceType.getDisplayName())
            );
        }
    }

    private void validateReadingLength(ServiceType serviceType, String value) {
        int expectedLength = serviceType.getMeterDigits();
        if (value.length() != expectedLength) {
            throw new BusinessException(
                    "Показание для услуги '%s' должно содержать ровно %d цифр, получено: %d"
                            .formatted(serviceType.getDisplayName(), expectedLength, value.length())
            );
        }
    }
}
