package com.example.demo.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.example.demo.repository.ServiceRepository;

import test.generated.CreateRequest;
import test.generated.DeleteServiceRequest;
import test.generated.GetAllServicesResponse;
import test.generated.GetServiceRequest;
import test.generated.GetServiceResponse;
import test.generated.Response;
import test.generated.ServiceInfo;
import test.generated.UpdateRequest;

@Endpoint
public class ServiceEndpoint {
    private static final String NAMESPACE_URI = "test";
    private final ServiceRepository repository;
    
    public ServiceEndpoint(ServiceRepository repository) {
        this.repository = repository;
    }
    private void applyTransformations(ServiceInfo req) {
        if ("123456789".equals(req.getCustomerId())) {
            req.setVIPCustomer(true);
        }

        if (req.getServiceDetails() != null) {
            String plan = req.getServiceDetails().getPlanType();
            String limit = req.getServiceDetails().getDataLimit();
            if ("5G".equals(plan) && (limit == null || limit.isEmpty())) {
                req.getServiceDetails().setSpecialOffer("ExtraData");
            }

            if (req.getCustomerDetails().getAddress() != null) {
                String country = req.getCustomerDetails().getAddress().getCountry();
                if (country != null && !"Sweden".equalsIgnoreCase(country)) {
                    req.getServiceDetails().setRoamingEnabled(null);
                }
            }
        }
    }

    private Response errorResponse(String code, String message) {

        Response res = new Response();
        res.setStatus("Error");
        res.setErrorCode(code);
        res.setErrorMessage(message);
        return res;
    }

    private Response validateRequest(ServiceInfo request) {
        if (request.getServiceId() == null || request.getCustomerDetails() == null) {
            return errorResponse("400", "Mandatory field missing");
        }
        String contact = request.getCustomerDetails().getContactNumber();
        if (contact == null || !contact.matches("^\\+\\d{11,15}$")) {
            return errorResponse("400", "Invalid contact number format");
        }
    return null;
    }

private Response successResponse(String message) {
    Response res = new Response();
    res.setStatus("Success");
    res.setMessage(message);
    return res;
}

    // --- CREATE ---
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateRequest")
    @ResponsePayload
    public Response createService(@RequestPayload CreateRequest request) {
        ServiceInfo info = request.getService(); 
        
        if (repository.findById(info.getServiceId()) != null) {
            return errorResponse("409", "Service with ID " + info.getServiceId() + " already exists.");
        }

        Response validationResponse = validateRequest(info);
        if (validationResponse != null) return validationResponse;

        applyTransformations(info);
        repository.save(info);

        return successResponse("Service created successfully");
    }

    // --- GET ---
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetServiceRequest")
    @ResponsePayload
    public GetServiceResponse getService(@RequestPayload GetServiceRequest request) {
        GetServiceResponse response = new GetServiceResponse();
        response.setService(repository.findById(request.getServiceId()));
        return response;
    }

    // --- UPDATE ---
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "UpdateRequest")
    @ResponsePayload
    public Response updateService(@RequestPayload UpdateRequest request) {
        ServiceInfo info = request.getService();

        if (repository.findById(info.getServiceId()) == null) {
            return errorResponse("404", "Service not found. Use Create first.");
        }

        Response validationResponse = validateRequest(info);
        if (validationResponse != null) return validationResponse;

        applyTransformations(info);
        repository.save(info);

        return successResponse("Service updated successfully");
    }

    // --- DELETE ---
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteServiceRequest")
    @ResponsePayload
    public Response deleteService(@RequestPayload DeleteServiceRequest request) {
        Response response = new Response();
        
        if (repository.findById(request.getServiceId()) == null) {
            return errorResponse("404", "Service not found. Nothing to delete.");
        }

        repository.delete(request.getServiceId());
        
        response.setStatus("Success");
        response.setMessage("Service with ID " + request.getServiceId() + " has been deleted.");
        return response;
    }

    // --- GET ALL ---
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAllServicesRequest")
    @ResponsePayload
    public GetAllServicesResponse getAllServices() {
        GetAllServicesResponse response = new GetAllServicesResponse();
        
        response.getServices().addAll(repository.findAll());
        
        return response;
    }
}