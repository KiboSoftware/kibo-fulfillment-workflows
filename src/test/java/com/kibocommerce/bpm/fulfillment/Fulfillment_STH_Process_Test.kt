package com.kibocommerce.bpm.fulfillment

import org.jbpm.test.JbpmJUnitBaseTestCase
import org.junit.Before
import org.junit.Test
import org.kie.api.runtime.KieSession
import org.kie.api.runtime.process.WorkflowProcessInstance
import org.kie.api.task.TaskService
import org.kie.api.task.model.Status
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Fulfillment_STH_Process_Test : JbpmJUnitBaseTestCase(true, false) {
    private var kieSession: KieSession? = null
    private var taskService: TaskService? = null

    @Before
    fun init() {
        createRuntimeManager("com/kibocommerce/bpm/fulfillment/FulfillmentProcess-STH.bpmn")
        val runtimeEngine = getRuntimeEngine(null)
        kieSession = runtimeEngine.kieSession
        taskService = runtimeEngine.taskService
    }

    @Test
    fun acceptShipment_true() {
        val wpi = createProcess()
        
        acceptShipment(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Validate Items In Stock")
        assertCurrentState(wpi, "ACCEPTED_SHIPMENT")
    }

    @Test
    fun acceptShipment_false() {
        val wpi = createProcess()
        
        acceptShipment(wpi, false)

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "REASSIGN_SHIPMENT")
    }

    @Test
    fun validateStock_inStock() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        validateStock(wpi, "IN_STOCK")

        assertNodeActive(wpi.id, kieSession, "Print Packing Slip")
        assertCurrentState(wpi, "INVENTORY_AVAILABLE")
    }

    @Test
    fun validateStock_partialStock() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        validateStock(wpi, "PARTIAL_STOCK")

        assertNodeActive(wpi.id, kieSession, "Print Packing Slip")
        assertCurrentState(wpi, "PARTIAL_INVENTORY_NOPE")
    }

    @Test
    fun validateStock_noStock() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        validateStock(wpi, "NO_STOCK")

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "REASSIGN_SHIPMENT")
    }

    @Test
    fun printPackingSlip() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        validateStock(wpi, "IN_STOCK")
        printPackingSlip(wpi, null)

        assertNodeActive(wpi.id, kieSession, "Prepare for Shipment")
        assertCurrentState(wpi, "PRINTED_PACKING_SLIP")
    }

    @Test
    fun printPackingSlip_goBack() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        validateStock(wpi, "IN_STOCK")
        printPackingSlip(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Validate Items In Stock")
        assertCurrentState(wpi, "ACCEPTED_SHIPMENT")
    }

    @Test
    fun prepareForShipment() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        validateStock(wpi, "IN_STOCK")
        printPackingSlip(wpi, null)
        prepareForShipment(wpi, null)

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "COMPLETED")
    }

    @Test
    fun prepareForShipment_goBack() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        validateStock(wpi, "IN_STOCK")
        printPackingSlip(wpi, null)
        prepareForShipment(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Print Packing Slip")
        assertCurrentState(wpi, "INVENTORY_AVAILABLE")
    }

    @Test
    fun pick() {
        val wpi = createProcess()
        wpi.signalEvent("picked", null)
        assertNodeActive(wpi.id, kieSession, "Prepare for Shipment")
        assertCurrentState(wpi, "PRINTED_PACKING_SLIP")
    }

    @Test
    fun fulfill() {
        val wpi = createProcess()
        
        acceptShipment(wpi, true)
        wpi.signalEvent("fulfilled", null)

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "COMPLETED")
    }

    @Test
    fun cancel() {
        val wpi = createProcess()
        
        acceptShipment(wpi, true)
        wpi.signalEvent("canceled", null)

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "CANCELED")
    }

    @Test
    fun customerCare() {
        val wpi = createProcess()
        
        acceptShipment(wpi, true)
        wpi.signalEvent("customer_care", null)

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "CUSTOMER_CARE")
    }

    @Test
    fun rejected() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        wpi.signalEvent("rejected", null)

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "REASSIGN_SHIPMENT")
    }

    private fun createProcess(): WorkflowProcessInstance {
        val processParam = mapOf("initiator" to "john")
        val processInstance = kieSession!!.startProcess("fulfillment.FulfillmentProcess-STH", processParam)

        assertTrue(processInstance is WorkflowProcessInstance)
        assertNodeExists(
            processInstance,
            "Accept Shipment",
            "Validate Items In Stock",
            "Print Packing Slip",
            "Prepare for Shipment"
        )
        assertNodeTriggered(processInstance.id, "Pre-Accept Shipment", "Accept Shipment")
        assertEquals("PRE_ACCEPT_SHIPMENT", processInstance.getVariable("currentState"))

        logger.info("Created process {}", processInstance.processName)
        return processInstance
    }

    private fun acceptShipment(wpi: WorkflowProcessInstance, shipmentAccepted: Boolean) {
        val expectedTaskName = "Accept Shipment"

        assertProcessInstanceActive(wpi.id, kieSession)
        assertNodeActive(wpi.id, kieSession, expectedTaskName)
        
        val tasks = taskService!!.getTasksByStatusByProcessInstanceId(wpi.id, listOf(Status.Reserved), "en-UK")
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertEquals(expectedTaskName, task.name)

        taskService!!.start(task.id, "john")
        val data = mapOf("shipmentAccepted" to shipmentAccepted)
        taskService!!.complete(task.id, "john", data)
    }

    private fun validateStock(wpi: WorkflowProcessInstance, stockLevel: String) {
        val expectedTaskName = "Validate Items In Stock"

        assertProcessInstanceActive(wpi.id, kieSession)
        assertNodeActive(wpi.id, kieSession, expectedTaskName)

        val tasks = taskService!!.getTasksByStatusByProcessInstanceId(wpi.id, listOf(Status.Reserved), "en-UK")
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertEquals(expectedTaskName, task.name)

        taskService!!.start(task.id, "john")
        val data = mapOf(
            "stockLevel" to stockLevel
        )
        taskService!!.complete(task.id, "john", data)
    }

    private fun printPackingSlip(wpi: WorkflowProcessInstance, back: Boolean?) {
        val expectedTaskName = "Print Packing Slip"

        assertProcessInstanceActive(wpi.id, kieSession)
        assertNodeActive(wpi.id, kieSession, expectedTaskName)

        val tasks = taskService!!.getTasksByStatusByProcessInstanceId(wpi.id, listOf(Status.Reserved), "en-UK")
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertEquals(expectedTaskName, task.name)

        taskService!!.start(task.id, "john")
        val data = mapOf("back" to back)
        taskService!!.complete(task.id, "john", data)
    }

    private fun prepareForShipment(wpi: WorkflowProcessInstance, back: Boolean?) {
        val expectedTaskName = "Prepare for Shipment"

        assertProcessInstanceActive(wpi.id, kieSession)
        assertNodeActive(wpi.id, kieSession, expectedTaskName)

        val tasks = taskService!!.getTasksByStatusByProcessInstanceId(wpi.id, listOf(Status.Reserved), "en-UK")
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertEquals(expectedTaskName, task.name)

        taskService!!.start(task.id, "john")
        val data = mapOf("back" to back)
        taskService!!.complete(task.id, "john", data)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Fulfillment_STH_Process_Test::class.java)
    }
}