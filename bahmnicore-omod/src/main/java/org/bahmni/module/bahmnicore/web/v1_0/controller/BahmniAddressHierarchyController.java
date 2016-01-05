package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniAddressHierarchyService;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1)
public class BahmniAddressHierarchyController {

    private BahmniAddressHierarchyService bahmniAddressHierarchyService;

    @Autowired
    public BahmniAddressHierarchyController(BahmniAddressHierarchyService bahmniAddressHierarchyService) {
        this.bahmniAddressHierarchyService = bahmniAddressHierarchyService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/addressHierarchy/{uuid}")
    @ResponseBody
    public AddressHierarchyEntry get(@PathVariable("uuid") String uuid) {
        if (uuid == null) {
            return null;
        }
        return bahmniAddressHierarchyService.getAddressHierarchyEntryByUuid(uuid);
    }

}
