package com.kibocommerce.bpm.fulfillment.hidden;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.api.task.model.Status.Reserved;

public class AFG_Custom_BOPIS_Process_Test extends JbpmJUnitBaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(AFG_Custom_BOPIS_Process_Test.class);

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
    private TaskService taskService;

    public AFG_Custom_BOPIS_Process_Test() {
        super(true, false);
    }

    @Before
    public void init() {
        runtimeManager = createRuntimeManager("com/kibocommerce/bpm/fulfillment/hidden/AFG_Custom_BOPIS_Process.bpmn");
        runtimeEngine = getRuntimeEngine(null);
        kieSession = runtimeEngine.getKieSession();
        taskService = runtimeEngine.getTaskService();
    }

    @Test
    public void acceptShipment_true() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);

        assertNodeActive(wpi.getId(), kieSession, "Print Pick List");
        assertEquals("ACCEPTED_SHIPMENT", wpi.getVariable("currentState"));
    }

    @Test
    public void acceptShipment_false() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, false);

        assertProcessInstanceActive(wpi.getId(), kieSession);
        assertEquals("REJECTED", wpi.getVariable("currentState"));
    }

    @Test
    public void printPickList() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);

        assertNodeActive(wpi.getId(), kieSession, "Validate Items In Stock");
        assertEquals("PROCESSING_PICK_LIST", wpi.getVariable("currentState"));
    }

    @Test
    public void validateStock_inStock() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "IN_STOCK", null);

        assertNodeActive(wpi.getId(), kieSession, "Wait for Payment Confirmation");
        assertEquals("INVENTORY_AVAILABLE", wpi.getVariable("currentState"));
    }

    @Test
    public void validateStock_partialStock_noTransfer() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "PARTIAL_STOCK", false);

        assertNodeActive(wpi.getId(), kieSession, "Wait for Payment Confirmation");
        assertEquals("PARTIAL_INVENTORY_NOPE", wpi.getVariable("currentState"));
    }

    @Test
    public void validateStock_partialStock_createTransfer() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "PARTIAL_STOCK", true);

        assertNodeActive(wpi.getId(), kieSession, "Wait for Transfer");
        assertEquals("WAITING_FOR_TRANSFER", wpi.getVariable("currentState"));
    }

    @Test
    public void validateStock_noStock_noTransfer() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "NO_STOCK", false);

        assertNodeActive(wpi.getId(), kieSession, "Wait for Payment Confirmation");
        assertEquals("INVENTORY_NOPE", wpi.getVariable("currentState"));
    }

    @Test
    public void validateStock_noStock_createTransfer() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "NO_STOCK", true);

        assertNodeActive(wpi.getId(), kieSession, "Wait for Transfer");
        assertEquals("WAITING_FOR_TRANSFER", wpi.getVariable("currentState"));
    }

    @Test
    public void waitForTransfer() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "NO_STOCK", true);
        waitForTransfer(wpi);

        assertNodeActive(wpi.getId(), kieSession, "Wait for Payment Confirmation");
        assertEquals("ALL_TRANSFERS_RECEIVED", wpi.getVariable("currentState"));
    }

    @Test
    public void confirmPayment() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "IN_STOCK", null);
        confirmPayment(wpi, null);

        assertNodeActive(wpi.getId(), kieSession, "Customer Pickup");
        assertEquals("PAYMENT_CONFIRMED", wpi.getVariable("currentState"));
    }

    @Test
    public void customerPickup() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "IN_STOCK", null);
        confirmPayment(wpi, null);
        customerPickup(wpi, null);

        assertProcessInstanceNotActive(wpi.getId(), kieSession);
        assertEquals("COMPLETED", wpi.getVariable("currentState"));
    }

    @Test
    public void customerPickup_declined() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        printPickList(wpi);
        validateStock(wpi, "IN_STOCK", false);
        confirmPayment(wpi, null);
        customerPickup(wpi, false);

        assertProcessInstanceNotActive(wpi.getId(), kieSession);
        assertEquals("CANCELED", wpi.getVariable("currentState"));
    }

    @Test
    public void pick() {
        WorkflowProcessInstance wpi = createProcess();

        wpi.signalEvent("picked", null);

        assertNodeActive(wpi.getId(), kieSession, "Wait for Payment Confirmation");
        assertEquals("INVENTORY_AVAILABLE", wpi.getVariable("currentState"));
    }

    @Test
    public void fulfill() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        wpi.signalEvent("fulfilled", null);

        assertProcessInstanceNotActive(wpi.getId(), kieSession);
        assertEquals("COMPLETED", wpi.getVariable("currentState"));
    }

    @Test
    public void cancel() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        wpi.signalEvent("canceled", null);

        assertProcessInstanceNotActive(wpi.getId(), kieSession);
        assertEquals("CANCELED", wpi.getVariable("currentState"));
    }

    @Test
    public void customerCare() {
        WorkflowProcessInstance wpi = createProcess();

        acceptShipment(wpi, true);
        wpi.signalEvent("customer_care", null);

        assertProcessInstanceNotActive(wpi.getId(), kieSession);
        assertEquals("CUSTOMER_CARE", wpi.getVariable("currentState"));
    }

    private WorkflowProcessInstance createProcess() {
        Map<String, Object> processParam = new HashMap<>();
        processParam.put("initiator", "john");
        ProcessInstance processInstance = kieSession.startProcess("AFG_Custom_BOPIS_Process", processParam);

        assertTrue(processInstance instanceof WorkflowProcessInstance);
        WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;

        assertNodeExists(wpi,
                "Accept Shipment",
                "Print Pick List",
                "Validate Items In Stock",
                "Wait for Transfer",
                "Wait for Payment Confirmation",
                "Customer Pickup");

        assertNodeTriggered(wpi.getId(), "Pre-Accept Shipment", "Accept Shipment");
        assertEquals("PRE_ACCEPT_SHIPMENT", wpi.getVariable("currentState"));

        logger.info("Created process {}", wpi.getProcessName());

        return wpi;
    }

    private void acceptShipment(WorkflowProcessInstance wpi, Boolean shipmentAccepted) {
        final String expectedTaskName = "Accept Shipment";

        assertProcessInstanceActive(wpi.getId(), kieSession);
        assertNodeActive(wpi.getId(), kieSession, expectedTaskName);

        List<TaskSummary> tasks = taskService.getTasksByStatusByProcessInstanceId(wpi.getId(), Arrays.asList(Reserved), "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(expectedTaskName, task.getName());
        taskService.start(task.getId(), "john");
        Map<String, Object> data = new HashMap<>();
        data.put("shipmentAccepted", shipmentAccepted);
        taskService.complete(task.getId(), "john", data);
    }

    private void printPickList(WorkflowProcessInstance wpi) {
        final String expectedTaskName = "Print Pick List";

        assertProcessInstanceActive(wpi.getId(), kieSession);
        assertNodeActive(wpi.getId(), kieSession, expectedTaskName);

        List<TaskSummary> tasks = taskService.getTasksByStatusByProcessInstanceId(wpi.getId(), Arrays.asList(Reserved), "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(expectedTaskName, task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", new HashMap<>());
    }

    private void validateStock(WorkflowProcessInstance wpi, String stockLevel, Boolean createTransfer) {
        final String expectedTaskName = "Validate Items In Stock";

        assertProcessInstanceActive(wpi.getId(), kieSession);
        assertNodeActive(wpi.getId(), kieSession, expectedTaskName);

        List<TaskSummary> tasks = taskService.getTasksByStatusByProcessInstanceId(wpi.getId(), Arrays.asList(Reserved), "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(expectedTaskName, task.getName());
        taskService.start(task.getId(), "john");
        Map<String, Object> data = new HashMap<>();
        data.put("stockLevel", stockLevel);
        data.put("createTransfer", createTransfer);
        taskService.complete(task.getId(), "john", data);
    }

    private void waitForTransfer(WorkflowProcessInstance wpi) {
        final String expectedTaskName = "Wait for Transfer";

        assertProcessInstanceActive(wpi.getId(), kieSession);
        assertNodeActive(wpi.getId(), kieSession, expectedTaskName);

        List<TaskSummary> tasks = taskService.getTasksByStatusByProcessInstanceId(wpi.getId(), Arrays.asList(Reserved), "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(expectedTaskName, task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", new HashMap<>());
    }

    private void confirmPayment(WorkflowProcessInstance wpi, Boolean back) {
        final String expectedTaskName = "Wait for Payment Confirmation";

        assertProcessInstanceActive(wpi.getId(), kieSession);
        assertNodeActive(wpi.getId(), kieSession, expectedTaskName);

        List<TaskSummary> tasks = taskService.getTasksByStatusByProcessInstanceId(wpi.getId(), Arrays.asList(Reserved), "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(expectedTaskName, task.getName());
        taskService.start(task.getId(), "john");
        Map<String, Object> data = new HashMap<>();
        data.put("back", back);
        taskService.complete(task.getId(), "john", data);
    }

    private void customerPickup(WorkflowProcessInstance wpi, Boolean customerAccepted) {
        final String expectedTaskName = "Customer Pickup";

        assertProcessInstanceActive(wpi.getId(), kieSession);
        assertNodeActive(wpi.getId(), kieSession, expectedTaskName);

        List<TaskSummary> tasks = taskService.getTasksByStatusByProcessInstanceId(wpi.getId(), Arrays.asList(Reserved), "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(expectedTaskName, task.getName());
        taskService.start(task.getId(), "john");
        Map<String, Object> data = new HashMap<>();
        data.put("customerAccepted", customerAccepted);
        taskService.complete(task.getId(), "john", data);
    }

}
