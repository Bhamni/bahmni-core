package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class BahmniProgramWorkflowServiceImplTest {
    private BahmniProgramWorkflowService workFlowService;

    @Before
    public void setup() {
        workFlowService = new BahmniProgramWorkflowServiceImpl();
    }
}
