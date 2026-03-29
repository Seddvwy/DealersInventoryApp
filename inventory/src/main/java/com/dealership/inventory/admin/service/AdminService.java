package com.dealership.inventory.admin.service;

import com.dealership.inventory.dealer.model.SubscriptionType;
import com.dealership.inventory.dealer.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/**
 * Service for GLOBAL_ADMIN operations.
 *
 * <p>Admin operations intentionally bypass tenant scoping – they operate on
 * the full dataset across all tenants.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final DealerRepository dealerRepository;

    /**
     * Returns the count of dealers grouped by {@link SubscriptionType} across
     * <strong>all tenants</strong>.
     */
    public Map<SubscriptionType, Long> countDealersBySubscription() {
        // Seed all tiers with 0 so the response is always complete
        Map<SubscriptionType, Long> result = new EnumMap<>(SubscriptionType.class);
        for (SubscriptionType type : SubscriptionType.values()) {
            result.put(type, 0L);
        }

        dealerRepository.countBySubscriptionTypeGlobal()
                .forEach(proj -> result.put(proj.getSubscriptionType(), proj.getCount()));

        return result;
    }
}