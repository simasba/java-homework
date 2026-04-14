package com.example.demo.repository;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import test.generated.ServiceInfo;

@Component
public class ServiceRepository {
    private final Map<String, ServiceInfo> storage = new HashMap<>();

    public void save(ServiceInfo service) {
        storage.put(service.getServiceId(), service);
    }

    public ServiceInfo findById(String serviceId) {
        return storage.get(serviceId);
    }

    public Collection<ServiceInfo> findAll() {
        return storage.values();
    }

    public void delete(String serviceId) {
        storage.remove(serviceId);
    }
}