package com.kibocommerce.bpm.fulfillment.hidden

import com.kibocommerce.bpm.fulfillment.assertCurrentState
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

class AFG_Custom_STH_Process_Test : JbpmJUnitBaseTestCase(true, false) {
    private var kieSession: KieSession? = null
    private var taskService: TaskService? = null

    @Before
    fun init() {
        createRuntimeManager("com/kibocommerce/bpm/fulfillment/hidden/AFG_Custom_STH_Process.bpmn")
        val runtimeEngine = getRuntimeEngine(null)
        kieSession = runtimeEngine.kieSession
        taskService = runtimeEngine.taskService
    }

    @Test
    fun acceptShipment_true() {
        val wpi = createProcess()
        
        acceptShipment(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Print Pick List")
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
    fun printPickList() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)

        assertNodeActive(wpi.id, kieSession, "Validate Items In Stock")
        assertCurrentState(wpi, "PROCESSING_PICK_LIST")
    }

    @Test
    fun validateStock_inStock() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)

        assertNodeActive(wpi.id, kieSession, "Wait for Payment Confirmation")
        assertCurrentState(wpi, "INVENTORY_AVAILABLE")
    }

    @Test
    fun validateStock_partialStock_noTransfer() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "PARTIAL_STOCK", null)

        assertNodeActive(wpi.id, kieSession, "Wait for Payment Confirmation")
        assertCurrentState(wpi, "PARTIAL_INVENTORY_NOPE")
    }

    @Test
    fun validateStock_partialStock_createTransfer() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "PARTIAL_STOCK", true)

        assertNodeActive(wpi.id, kieSession, "Wait for Transfer")
        assertCurrentState(wpi, "WAITING_FOR_TRANSFER")
    }

    @Test
    fun validateStock_noStock_noTransfer() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "NO_STOCK", null)

        assertNodeActive(wpi.id, kieSession, "Wait for Payment Confirmation")
        assertCurrentState(wpi, "INVENTORY_NOPE")
    }

    @Test
    fun validateStock_noStock_createTransfer() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "NO_STOCK", true)

        assertNodeActive(wpi.id, kieSession, "Wait for Transfer")
        assertCurrentState(wpi, "WAITING_FOR_TRANSFER")
    }

    @Test
    fun waitForTransfer() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "NO_STOCK", true)
        waitForTransfer(wpi)

        assertNodeActive(wpi.id, kieSession, "Wait for Payment Confirmation")
        assertCurrentState(wpi, "ALL_TRANSFERS_RECEIVED")
    }

    @Test
    fun confirmPayment() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, null)

        assertNodeActive(wpi.id, kieSession, "Print Packing Slip")
        assertCurrentState(wpi, "PAYMENT_CONFIRMED")
    }

    @Test
    fun confirmPayment_goBack() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Validate Items In Stock")
        assertCurrentState(wpi, "ACCEPTED_SHIPMENT")
    }

    @Test
    fun printPackingSlip() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, null)
        printPackingSlip(wpi, null)

        assertNodeActive(wpi.id, kieSession, "Prepare for Shipment")
        assertCurrentState(wpi, "PRINTED_PACKING_SLIP")
    }

    @Test
    fun printPackingSlip_goBack() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, null)
        printPackingSlip(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Wait for Payment Confirmation")
        assertCurrentState(wpi, "INVENTORY_AVAILABLE")
    }

    @Test
    fun prepareForShipment() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, null)
        printPackingSlip(wpi, null)
        prepareForShipment(wpi, null)

        assertNodeActive(wpi.id, kieSession, "Out for Delivery")
        assertCurrentState(wpi, "SHIPPED")
    }

    @Test
    fun prepareForShipment_goBack() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, null)
        printPackingSlip(wpi, null)
        prepareForShipment(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Print Packing Slip")
        assertCurrentState(wpi, "PAYMENT_CONFIRMED")
    }

    @Test
    fun deliver() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, null)
        printPackingSlip(wpi, null)
        prepareForShipment(wpi, null)
        deliver(wpi, null)

        assertProcessInstanceNotActive(wpi.id, kieSession)
        assertCurrentState(wpi, "COMPLETED")
    }

    @Test
    fun deliver_goBack() {
        val wpi = createProcess()

        acceptShipment(wpi, true)
        printPickList(wpi)
        validateStock(wpi, "IN_STOCK", null)
        confirmPayment(wpi, null)
        printPackingSlip(wpi, null)
        prepareForShipment(wpi, null)
        deliver(wpi, true)

        assertNodeActive(wpi.id, kieSession, "Prepare for Shipment")
        assertCurrentState(wpi, "PRINTED_PACKING_SLIP")
    }

    @Test
    fun pick() {
        val wpi = createProcess()
        wpi.signalEvent("picked", null)
        assertNodeActive(wpi.id, kieSession, "Wait for Payment Confirmation")
        assertCurrentState(wpi, "INVENTORY_AVAILABLE")
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

    private fun createProcess(): WorkflowProcessInstance {
        val processParam = mapOf("initiator" to "john")
        val processInstance = kieSession!!.startProcess("AFG_Custom_STH_Process", processParam)

        assertTrue(processInstance is WorkflowProcessInstance)
        assertNodeExists(
            processInstance,
            "Accept Shipment",
            "Validate Items In Stock",
            "Wait for Payment Confirmation",
            "Print Packing Slip",
            "Prepare for Shipment",
            "Out for Delivery"
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

    private fun validateStock(wpi: WorkflowProcessInstance, stockLevel: String, createTransfer: Boolean?) {
        val expectedTaskName = "Validate Items In Stock"

        assertProcessInstanceActive(wpi.id, kieSession)
        assertNodeActive(wpi.id, kieSession, expectedTaskName)

        val tasks = taskService!!.getTasksByStatusByProcessInstanceId(wpi.id, listOf(Status.Reserved), "en-UK")
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertEquals(expectedTaskName, task.name)

        taskService!!.start(task.id, "john")
        val data = mapOf(
            "stockLevel" to stockLevel,
            "createTransfer" to createTransfer
        )
        taskService!!.complete(task.id, "john", data)
    }

    private fun waitForTransfer(wpi: WorkflowProcessInstance) {
        val expectedTaskName = "Wait for Transfer"

        assertProcessInstanceActive(wpi.id, kieSession)
        assertNodeActive(wpi.id, kieSession, expectedTaskName)

        val tasks = taskService!!.getTasksByStatusByProcessInstanceId(wpi.id, listOf(Status.Reserved), "en-UK")
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertEquals(expectedTaskName, task.name)

        taskService!!.start(task.id, "john")
        taskService!!.complete(task.id, "john", HashMap())
    }

    private fun confirmPayment(wpi: WorkflowProcessInstance, back: Boolean?) {
        val expectedTaskName = "Wait for Payment Confirmation"

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

    private fun deliver(wpi: WorkflowProcessInstance, back: Boolean?) {
        val expectedTaskName = "Out for Delivery"

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

    private fun printPickList(wpi: WorkflowProcessInstance) {
        val expectedTaskName = "Print Pick List"

        assertProcessInstanceActive(wpi.id, kieSession)
        assertNodeActive(wpi.id, kieSession, expectedTaskName)

        val tasks = taskService!!.getTasksByStatusByProcessInstanceId(wpi.id, listOf(Status.Reserved), "en-UK")
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertEquals(expectedTaskName, task.name)

        taskService!!.start(task.id, "john")
        taskService!!.complete(task.id, "john", HashMap())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AFG_Custom_STH_Process_Test::class.java)
    }
}