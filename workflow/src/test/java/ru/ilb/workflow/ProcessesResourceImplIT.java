/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.workflow;

import at.together._2006.xpil1.MainWorkflowProcessInstance;
import at.together._2006.xpil1.ManualActivityInstance;
import at.together._2006.xpil1.WorkflowProcessInstance;
import java.util.ArrayList;
import java.util.List;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ru.ilb.workflow.api.ProcessesResource;

/**
 *
 * @author slavb
 */
public class ProcessesResourceImplIT {

    private final static String ENDPOINT_ADDRESS = "http://localhost:54336/workflow/web";
    ProcessesResource resource;

    public ProcessesResourceImplIT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        resource = JAXRSClientFactory.create(ENDPOINT_ADDRESS, ProcessesResource.class); //,"slavb","123",null);
        // Replace 'user' and 'password' by the actual values
        String authorizationHeader = "Basic "
                + org.apache.cxf.common.util.Base64Utility.encode("slavb:123".getBytes());
        WebClient.client(resource).header("Authorization", authorizationHeader);

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of create method, of class ProcessesResourceImpl.
     */
    @Test
    public void testCreate_String_MainWorkflowProcessInstance() {
        System.out.println("create");
        String factoryId = "leave_request##leave_request";
        MainWorkflowProcessInstance mainworkflowprocessinstance = new MainWorkflowProcessInstance();
        mainworkflowprocessinstance.setFactoryId(factoryId);
        String expResult = "leave_request_leave_request";
        String result = resource.createProcessInstance(mainworkflowprocessinstance);
        assertTrue(result.endsWith(expResult));
    }


    /**
     * Test of createXml method, of class ProcessesResourceImpl.
     */
    @Test
    public void testCreateXml() {
        System.out.println("createXml");
        String factoryId = "leave_request##leave_request";
        MainWorkflowProcessInstance mainworkflowprocessinstance = new MainWorkflowProcessInstance();
        mainworkflowprocessinstance.setFactoryId(factoryId);
        String expResult = "leave_request_leave_request";
        List<String> xpilprop=new ArrayList<>();
        xpilprop.add("FILL_RUNNING_ACTIVITIES");
        WorkflowProcessInstance result = resource.createProcessInstanceXml(xpilprop,mainworkflowprocessinstance);
        assertNotNull(result);
        assertNotNull(result.getActivityInstances());
        assertNotNull(result.getActivityInstances().getManualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances());
        assertFalse(result.getActivityInstances().getManualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances().isEmpty());

    }


    /**
     * Test of editActivity method, of class ProcessesResourceImpl.
     */
    @Test
    public void testEditActivity_4args_2() {
        System.out.println("editActivity");
        String factoryId = "leave_request##leave_request";
        MainWorkflowProcessInstance mainworkflowprocessinstance = new MainWorkflowProcessInstance();
        mainworkflowprocessinstance.setFactoryId(factoryId);
        String expResult = "leave_request_leave_request";
        List<String> xpilprop=new ArrayList<>();
        xpilprop.add("FILL_RUNNING_ACTIVITIES");
        WorkflowProcessInstance result = resource.createProcessInstanceXml(xpilprop,mainworkflowprocessinstance);
        ManualActivityInstance act=(ManualActivityInstance)result.getActivityInstances().getManualActivityInstancesAndToolActivityInstancesAndBlockActivityInstances().get(0);
        ManualActivityInstance actnew=new ManualActivityInstance();
        actnew.setState("closed.completed");
        resource.editActivity(result.getId(), act.getId(), actnew);
    }

}
