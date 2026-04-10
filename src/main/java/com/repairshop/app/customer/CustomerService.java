package com.repairshop.app.customer;

import com.repairshop.app.repair.RepairItemRepository;
import com.repairshop.app.shop.ShopRepository;
import com.repairshop.app.web.form.CustomerForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RepairItemRepository repairItemRepository;
    private final ShopRepository shopRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            RepairItemRepository repairItemRepository,
            ShopRepository shopRepository
    ) {
        this.customerRepository = customerRepository;
        this.repairItemRepository = repairItemRepository;
        this.shopRepository = shopRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerListItemView> listForShop(Long shopId) {
        return customerRepository.findAllByShopIdOrderByFullNameAsc(shopId).stream()
                .map(customer -> new CustomerListItemView(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getPrimaryPhone(),
                        customer.getSecondaryPhone(),
                        customer.getNotes()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerDetailView getDetail(Long shopId, Long customerId) {
        Customer customer = getCustomerEntity(shopId, customerId);
        List<CustomerRepairSummaryView> repairs = repairItemRepository.findAllByCustomerIdAndShopIdWithCustomer(customerId, shopId)
                .stream()
                .map(item -> new CustomerRepairSummaryView(
                        item.getId(),
                        item.getTitle(),
                        item.getStatus(),
                        item.getExpectedDeliveryDate(),
                        item.getRemainingBalance()
                ))
                .toList();

        return new CustomerDetailView(
                customer.getId(),
                customer.getFullName(),
                customer.getPrimaryPhone(),
                customer.getSecondaryPhone(),
                customer.getNotes(),
                repairs
        );
    }

    @Transactional(readOnly = true)
    public CustomerForm getForm(Long shopId, Long customerId) {
        Customer customer = getCustomerEntity(shopId, customerId);
        CustomerForm form = new CustomerForm();
        form.setFullName(customer.getFullName());
        form.setPrimaryPhone(customer.getPrimaryPhone());
        form.setSecondaryPhone(customer.getSecondaryPhone());
        form.setNotes(customer.getNotes());
        return form;
    }

    @Transactional
    public Long create(Long shopId, CustomerForm form) {
        String primaryPhone = normalizeNullable(form.getPrimaryPhone());
        validatePrimaryPhone(shopId, primaryPhone, null);

        Customer customer = new Customer();
        customer.setShop(shopRepository.getReferenceById(shopId));
        applyForm(customer, form, primaryPhone);
        return persistCustomer(customer, primaryPhone).getId();
    }

    @Transactional
    public void update(Long shopId, Long customerId, CustomerForm form) {
        Customer customer = getCustomerEntity(shopId, customerId);
        String primaryPhone = normalizeNullable(form.getPrimaryPhone());
        validatePrimaryPhone(shopId, primaryPhone, customerId);
        applyForm(customer, form, primaryPhone);
        persistCustomer(customer, primaryPhone);
    }

    private Customer getCustomerEntity(Long shopId, Long customerId) {
        return customerRepository.findByIdAndShopId(customerId, shopId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private void validatePrimaryPhone(Long shopId, String primaryPhone, Long customerId) {
        if (primaryPhone == null) {
            return;
        }

        boolean exists = customerId == null
                ? customerRepository.existsByShopIdAndPrimaryPhone(shopId, primaryPhone)
                : customerRepository.existsByShopIdAndPrimaryPhoneAndIdNot(shopId, primaryPhone, customerId);

        if (exists) {
            throw new DuplicateCustomerPhoneException(primaryPhone);
        }
    }

    private void applyForm(Customer customer, CustomerForm form, String primaryPhone) {
        customer.setFullName(normalizeRequired(form.getFullName()));
        customer.setPrimaryPhone(primaryPhone);
        customer.setSecondaryPhone(normalizeNullable(form.getSecondaryPhone()));
        customer.setNotes(normalizeNullable(form.getNotes()));
    }

    private Customer persistCustomer(Customer customer, String primaryPhone) {
        try {
            return customerRepository.saveAndFlush(customer);
        } catch (DataIntegrityViolationException ex) {
            if (primaryPhone != null) {
                throw new DuplicateCustomerPhoneException(primaryPhone);
            }
            throw ex;
        }
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
