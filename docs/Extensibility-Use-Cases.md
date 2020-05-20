# Extensibility Use Cases

* [In a BPM workflow, call another REST service synchronously](#in-a-bpm-workflow-call-another-rest-service-synchronously)
* [In a BPM workflow, call another REST service asynchronously](#in-a-bpm-workflow-call-another-rest-service-asynchronously)
* [In a BPM workflow, signal to advance when stopped at a user task](#in-a-bpm-workflow-signal-to-advance-when-stopped-at-a-user-task)

# In a BPM workflow, call another REST service synchronously
This use case will illustrate how to call an external REST service *synchronously* and use the response data to make flow decisions as well as provide input to subsequent steps in the process.

You will add functionality for calling an external weather service to get a maximum temperature forecast for a given destination ZIP code. The service response data will be used to decide subsequent tasks to be executed based on a given threshold. If the maximum temperature forecast exceeds the threshold, the shipment will be directed to Customer Service; otherwise, the data will simply be provided to the person preparing the shipment, allowing them to decide on suitable packaging or shipping options. Required process instance variables will be initialized with user input received for the __Accept Shipment__ task.

You can use the pre-installed [REST Work Item Handler](https://github.com/kiegroup/jbpm/blob/master/jbpm-workitems/jbpm-workitems-rest/src/main/java/org/jbpm/process/workitem/rest/RESTWorkItemHandler.java) to add external REST API calls to a jBPM business process or workflow. The following steps will guide you through general setup of your custom project and implementation of the above described custom REST call via jBPM Business Central.

The resulting custom fulfillment workflow:

![Example custom fulfillment workflow](/docs/images/example_custom_workflow_calling_weather_api.png)

## Setup your project to use the REST work item task and handler

The following general project setup will allow you to use the __Rest__ work item within any of your custom Kibo Fulfillment Workflows.

1. Log in to jBPM Business Central

1. Open your custom project

1. Navigate to __Settings > Service Tasks__

1. Within __Service Tasks__, click the __Rest__ service task __Install__ button to enable its use within the designer.

1. Move content from the generated `Rest.wid` file to the `WorkDefinitions.wid` file. Replace any existing "Rest" definition within `WorkDefinitions.wid`, save your changes and then delete the `Rest.wid` file/asset

    Example __Rest__ work item definition:
    ```
      [
        "name" : "Rest",
        "displayName" : "Rest",
        "category" : "jbpm-workitems-rest",
        "description" : "",
        "defaultHandler" : "mvel: new org.jbpm.process.workitem.rest.RESTWorkItemHandler()",
        "documentation" : "jbpm-workitems-rest/index.html",
    
        "parameters" : [
          "ConnectTimeout" : new StringDataType()
          ,"ResultClass" : new StringDataType()
          ,"ContentType" : new StringDataType()
          ,"AcceptCharset" : new StringDataType()
          ,"Headers" : new StringDataType()
          ,"AuthUrl" : new StringDataType()
          ,"Method" : new StringDataType()
          ,"ReadTimeout" : new StringDataType()
          ,"Url" : new StringDataType()
          ,"ContentTypeCharset" : new StringDataType()
          ,"HandleResponseErrors" : new StringDataType()
          ,"ContentData" : new StringDataType()
          ,"Username" : new StringDataType()
          ,"Content" : new StringDataType()
          ,"AcceptHeader" : new StringDataType()
          ,"AuthType" : new StringDataType()
          ,"Password" : new StringDataType()
        ],
        "results" : [
          "Result" : new StringDataType()
        ],
        "mavenDependencies" : [
          "org.jbpm:jbpm-workitems-rest:7.26.0.Final"
        ],
        "icon" : "Rest.png"
      ]
    ```

1. Register the __REST Work Item Handler__

    Each defined work item, or service task, is backed by a corresponding work item handler. Registering a work item handler will update the project deployment descriptor with the appropriate initialization code. 

    Navigate to __Settings > Deployments > Work Item Handlers__, click __Add Work Item Handler__ and enter the following:
    
    * Name: `Rest`
    * Value: `new org.jbpm.process.workitem.rest.RESTWorkItemHandler("", "")`
    * Resolver: `MVEL`

1. Save your new __Settings__

1. Confirm that your project deployment descriptor file has been updated appropriately

    The file can be found here: `YOUR_PROJECT_DIRECTORY/src/main/resources/META-INF/kie-deployment-descriptor.xml`

    The content should appear similar to the following example:
    ```xml
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <deployment-descriptor xsi:schemaLocation="http://www.jboss.org/jbpm deployment-descriptor.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <persistence-unit>org.jbpm.domain</persistence-unit>
        <audit-persistence-unit>org.jbpm.domain</audit-persistence-unit>
        <audit-mode>JPA</audit-mode>
        <persistence-mode>JPA</persistence-mode>
        <runtime-strategy>PER_PROCESS_INSTANCE</runtime-strategy>
        <marshalling-strategies/>
        <event-listeners/>
        <task-event-listeners/>
        <globals/>
        <work-item-handlers>
            <work-item-handler>
                <resolver>mvel</resolver>
                <identifier>new org.jbpm.process.workitem.rest.RESTWorkItemHandler("", "")</identifier>
                <parameters/>
                <name>Rest</name>
            </work-item-handler>
        </work-item-handlers>
        <environment-entries/>
        <configurations/>
        <required-roles/>
        <remoteable-classes/>
        <limit-serialization-classes>true</limit-serialization-classes>
    </deployment-descriptor>
    ```

## Create a custom Kibo Fulfillment Workflow

This example customization will illustrate use of the __Rest__ work item within a workflow to enable data-driven decisions and task updates. You will be creating a custom Ship-To-Home fulfillment process which leverages the __Rest__ work item and handler to make external calls to the [OpenWeather](https://openweathermap.org/) [Weather API](https://openweathermap.org/api).

Before proceeding, please [sign up](https://openweathermap.org/home/sign_up) for a free OpenWeather account in order to get a valid API key.

1. Within your custom project, open the __FulfillmentProcess-STH__ workflow

1. Click the __Copy__ button, provide a new name and select your custom package

    Example:
    * New Name: __YOUR_CUSTOM_WORKFLOW_NAME__
    * Package: __YOUR_DEVCENTER_ACCOUNT_KEY__
    
1. Close the design view of the __FulfillmentProcess-STH__ workflow and open the new __YOUR_CUSTOM_WORKFLOW_NAME__ workflow

1. Within the design view of the __YOUR_CUSTOM_WORKFLOW_NAME__ workflow, change the process Name, ID and Package properties

    Set the following in the __Process > Properties__ editor:
    * Name: __YOUR_CUSTOM_WORKFLOW_NAME__
    * ID: __YOUR_DEVCENTER_ACCOUNT_KEY__.__YOUR_CUSTOM_WORKFLOW_NAME__
    * Package: __YOUR_DEVCENTER_ACCOUNT_KEY__

1. Add the following process variables in the __Process Data__ section of the __Process > Properties__ editor:

    __Process Variables__
    
    Name | Data Type 
    ---- | ---------
    `destTempMax` | `Integer`
    `destTempMaxThreshold` | `Integer`
    `destZip` | `String`
    `forecastResult` | `java.util.Map`
    `openweatherAppId` | `String`

1. Add the following data outputs and assignments to the __Accept Shipment__ human task:

    __Accept Shipment Data I/O__
    
    __Data Outputs and Assignments__
    
    Name | Data Type | Target
    ---- | --------- | ------
    `destZip` | `String` | `destZip`
    `destTempMaxThreshold` | `Integer` | `destTempMaxThreshold`
    `openweatherAppId` | `String` | `openweatherAppId`

1. You will be using a Java snippet when you implement the __Rest__ task. Add the following process level data-type imports to avoid using fully qualified class names in script areas.

    Add the following class names in the __Imports > Data Type Imports__ section of the __Process > Properties__ editor:

    __Data Type Imports__
    
    Class Name |
    ---------- |
    `java.util.List` |
    `java.util.Map` |
    `java.util.LinkedHashMap` |
    `java.lang.Math` |
    
    __Tip:__
    > The [Jackson](https://github.com/FasterXML/jackson) JSON parser is a provided dependency. If you wish to use `com.fasterxml.jackson.databind.ObjectMapper` without the fully qualified class name in scripts, add the import to process data type imports.

1. From the __Service Tasks__ section of the toolbar panel, drag the __Rest__ work item or service task onto the process design canvas. It should be located under the __JBPM-WORKITEMS-REST__ category.

1. Configure the REST work item task and add Java code to the __On Exit Action__

    * Select the newly placed __Rest__ work item task
    * For this example, set the __Name__ to `Rest WorkItem Call Weather API`
    * Set the following parameters within the __Data Assignments__ section of the task properties editor:
    
    __Rest Data I/O__
    
    __Data Inputs and Assignments__
    
    Name | Data Type | Source
    ---- | --------- | ------
    `AcceptHeader` | `String` | Constant ... `application/json`
    `Method` | `String` | Constant ... `GET`
    `ResultClass` | `String` | Constant ... `java.util.LinkedHashMap`
    `Url` | `String` | Constant ... `http://api.openweathermap.org/data/2.5/forecast?zip=#{destZip},us&APPID=#{openweatherAppId}&units=imperial`
    
    __Data Outputs and Assignments__
    
    Name | Data Type | Target
    ---- | --------- | ------
    `Result` | Custom ... `java.util.LinkedHashMap` | `forecastResult`
    
    __Tip:__
    > In the example above, notice the value of the `Url` parameter. First of all, instead of using a constant, the entire string value could be set in a process variable and referenced here. Secondly, you may inject process variable values into hard-coded string expressions using syntax like: `#{expression}`. The `zip` and `APPID` query parameters within the `Url` value are using expressions `#{destZip}` and `#{openweatherAppId}`. This can be a very useful tool for process design and is used here for substring replacement. This may be done elsewhere for process and task variables, task descriptions, diverging gateway output flow conditions, etc. 

    * Expand the __Implementation/Execution__ section of the task properties editor
    * Add the following Java snippet to the __On Exit Action__ and ensure `java` is the current pull-down selection
    
        ```
        Map _forecastResult = (Map) kcontext.getVariable("forecastResult");
        List<Map> forecastList = (List<Map>) _forecastResult.get("list");
        Integer _destTempMax = null;
        for (Map f : forecastList) {
            Map main = (Map) f.get("main");
            Object _tempMax = (main != null) ? main.get("temp_max") : null;
            Integer tempMax = (_tempMax != null) ? (int) Math.ceil(Double.parseDouble(_tempMax.toString())) : null;
            if (tempMax != null && (_destTempMax == null || _destTempMax < tempMax)) {
                _destTempMax = tempMax;
            }
        }
        kcontext.setVariable("destTempMax", _destTempMax);
        ```

1. Now that the __Rest__ task has been implemented within the process, you can add new branches to your workflow in order to leverage its functionality

    * Add a new __Exclusive Gateway__ (g1) between the __Converging Exclusive Gateway__ (g0), and the __Prepare for Shipment__ task
    * Change the existing sequence flow from entering the __Prepare for Shipment__ task to instead enter the new __Exclusive Gateway__ (g1)
    * Add a new sequence flow from the new __Exclusive Gateway__ (g1) to the new __Rest__ task and set its __Implementation/Execution > Condition Expression__ to use the following Java snippet:
    
        ```
        return kcontext.getVariable("destZip") != null && kcontext.getVariable("destTempMaxThreshold") != null;
        ```

    * Add two new __Exclusive Gateways__ (g2 & g3) between the new __Rest__ task and the __Prepare for Shipment__ task
    * Add a sequence flow from the __Rest__ task to the second of the new __Exclusive Gateways__ (g2)
    * Add a sequence flow from the second to the third of the new __Exclusive Gateways__ (g2 --> g3)
    * Add a sequence flow from the third new __Exclusive Gateway__ (g3) to the __Prepare for Shipment__ task
    * Add a new __End Signal__ event below the second of the new __Exclusive Gateways__ (g2)
    * Have the new __End Signal__ event send the `customer_care` signal, with a signal scope of `Process Instance`
    * Add a new sequence flow from the second of the new __Exclusive Gateways__ (g2) to the new __End Signal__ event and set its __Implementation/Execution > Condition Expression__ to use the following Java snippet:
    
        ```
        Integer _destTempMax = (Integer) kcontext.getVariable("destTempMax");
        Integer _destTempMaxThreshold = (Integer) kcontext.getVariable("destTempMaxThreshold");
        return _destTempMax == null || (_destTempMax != null && _destTempMax >= _destTempMaxThreshold);
        ```

    * Add a new sequence flow from new __Diverging Exclusive Gateway__ (g1) to new __Converging Exclusive Gateway__ (g3)
    * Change the default route of the new __Diverging Exclusive Gateway__ (g1) to be `Exclusive`
    * Change the default route of the new __Diverging Exclusive Gateway__ (g2) to be `Exclusive`

1. Use service response data as input to another step in the process

    * Select the __Prepare for Shipment__ human task and open the properties editor
    * Open the __Implementation/Execution > Assignments__ section of the task properties editor and add two new data input assignments
        
        __Prepare for Shipment Data I/O__
        
        __Data Inputs and Assignments__
        
        Name | Data Type | Source
        ---- | --------- | ------
        `destTempMaxForecast_In` | `Integer` | `destTempMax`
        `destTempMaxThreshold_In` | `Integer` | `destTempMaxThreshold`

    * These variables may now be used to populate corresponding form fields in a custom Kibo Fulfiller frontend.

1. Save your process changes and close the design view

1. Deploy your custom project and test creating a new instance of process with ID: __YOUR_DEVCENTER_ACCOUNT_KEY__.__YOUR_CUSTOM_WORKFLOW_NAME__

# In a BPM workflow, call another REST service asynchronously
This use case will illustrate how to call an external REST service *asynchronously* and use the response data to make flow decisions as well as provide input to subsequent steps in the process.

You will be building on the custom Kibo Fulfillment process implemented in the section entitled, "[In a BPM workflow, call another REST service synchronously](#in-a-bpm-workflow-call-another-rest-service-synchronously)." If you have not already completed that exercise, please do so before proceeding.

Asynchronous work can be achieved in multiple ways with jBPM. A simple approach could be adding a parallel gateway to your workflow, introducing logically asynchronous branches which start executing near instantaneously. For low-level control, custom work item handlers can be implemented using Java's built-in asynchronous programming features or third-party libraries. You can review the jBPM documentation on [concurrency and asynchronous execution](https://docs.jboss.org/jbpm/release/latest/jbpm-docs/html_single/#_jbpmasyncexecution) for detailed alternatives.

__NOTE:__
> For Kibo Fulfillment workflows, low-level approaches would need to be
> carefully designed and approved for use in a production environment.

With the current version of jBPM, another approach is available which leverages the Java Executor framework and a distributed scheduler. Workflow tasks can be marked as asynchronous using the `Is Async` checkbox within the __Implementation/Execution__ section of the task properties editor. With this service-level support in place, when the flow of execution reaches a `Is Async` task, a corresponding job will be scheduled which ultimately runs work item specific handler operations in a separate thread. This will be the focus of this exercise.

The following assumes you have already [setup your project with the REST work item handler](#setup-your-project-to-use-the-rest-work-item-task-and-handler) and [created a custom Kibo Fulfillment Workflow](#create-a-custom-kibo-fulfillment-workflow) which makes a *synchronous* REST call. 

The resulting custom fulfillment workflow:

![Example custom fulfillment workflow async](/docs/images/example_custom_workflow_calling_weather_api_async.png)

## Create a custom Kibo Fulfillment Workflow using an asynchronous task
__NOTE:__
> A non-interrupting start event does not stop or interrupt the execution of the containing or parent process.

# In a BPM workflow, signal to advance when stopped at a user task
This use case will illustrate how to move a process to the next step when stopped at a human user task using an external signal.