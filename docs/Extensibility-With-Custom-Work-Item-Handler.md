# Custom Work Item Handler Implementation
We can create custom tasks and work item handlers in Business Central to execute custom code within your process flows and extend the operations available for use in jBPM business process. We can use custom tasks to develop operations that jBPM business process Manager does not directly provide and include them in process diagrams.

## Creating custom work item handler project

We can create a work item handler from scratch using a Maven archetype jbpm-workitems-archetype provided by JBPM.

__Steps__

1. Open the command line and create a directory where you will build your work item handler such as workitem-home:
	```
	$ mkdir workitem-home
	```
	
1. In the workitem-home directory, execute the following command:

	```
	$ mvn archetype:generate \
	-DarchetypeGroupId=org.jbpm \
	-DarchetypeArtifactId=jbpm-workitems-archetype \
	-DarchetypeVersion=7.74.0.Final \
	-Dversion=1.0.0-Final \
	-DgroupId=com.kibo \
	-DartifactId=customworkitem \
	-DclassPrefix=CustomWorkItem
	```

1. Check whether customworkitem folder is created in the workitem-home directory

1. Add any Maven dependencies required by the work item handler class to the pom.xml file.

1. To create a deployable JAR for this project, in the parent project folder where the pom.xml file is located, execute the following command:

	```
	$ mvn clean install
	```

1. Several files are created in the target/ directory which include the following two main files:

	Parameter | Description
	--------- | -----------
	`customworkitem-<version>.jar` | `Used for direct deployment to jBPM Process Manager.`
	`customworkitem-<version>.zip` | `Used for deployment using a service repository.`


## Custom Work item handler project customization

1. We can customize the code of a work item handler project. There are two Java methods required by a work item handler, `executeWorkItem` and `abortWorkItem`.

	Java Method | Description
	----------- | -----------
	`executeWorkItem(WorkItem workItem, WorkItemManager manager)` | `Executed by default when the work item handler is run.`
	`abortWorkItem(WorkItem workItem, WorkItemManager manager)` | `Executed when the work item is aborted.`
	
In both methods, the WorkItem parameter contains any of the parameters entered into the custom task through a GUI or API call, and the WorkItemManager parameter is responsible for tracking the state of the custom task.

2. __Example code structure__
```
	public class CustomWorkItemWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
		RequiredParameterValidator.validate(this.getClass(), workItem);

		// sample parameters
		String sampleParam = (String) workItem.getParameter("SampleParam");
		String sampleParamTwo = (String) workItem.getParameter("SampleParamTwo");

		// complete workitem impl...

		// return results
		String sampleResult = "sample result";
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("SampleResult", sampleResult);
		manager.completeWorkItem(workItem.getId(), results);
		} catch(Throwable cause) {
			handleException(cause);
		}
		}

		@Override
		public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// similar
		}
	}
```
	
3. __Parameter Description__

	Parameter | Description
	--------- | -----------
	`RequiredParameterValidator.validate(this.getClass(), workItem);` | Checks that all parameters marked “required” are present.
	`String sampleParam = (String) workItem.getParameter("SampleParam");` | Example of getting a parameter from the `WorkItem` class. The name is always a `string`. 
	`// complete workitem impl…` | Executes the custom Java code when a parameter is received.
	`results.put("SampleResult", sampleResult);` | Passes results to the custom task. The results are placed in the data output areas of the custom task.
	`manager.completeWorkItem(workItem.getId(), results);` | Marks the work item handler as complete. The `WorkItemManager` controls the state of the work item and is responsible for getting the `WorkItem` ID and associate the results with the correct custom task.
	`abortWorkItem()` | Aborts the custom Java code. May be left blank if the work item is not designed to be aborted
	
## Work item definitions

Custom work item handler implementation requires a work item definition (WID) file to identify the data fields to show in Business Central and accept API calls. The WID file is a mapping between user interactions with jBPM Process Manager and the data that is passed to the work item handler. 
The WID file also handles the UI details such as the name of the custom task, the category it is displayed as on the palette in Business Central, the icon used to designate the custom task, and the work item handler the custom task will map to.

Add content to the WorkDefinitions.wid file. 
	Example - 
```
	[
    "name" : "CustomWorkItemDefinitions",
    "displayName" : "CustomWorkItemDefinitions",
    "category" : "",
    "description" : "",
    "defaultHandler" : "mvel: new com.kibo.CustomWorkItemWorkItemHandler()",
    "documentation" : "customworkitem/index.html",
    "parameters" : [
      "SampleParam" : new StringDataType(),
      "SampleParamTwo" : new StringDataType()
    ],
    "results" : [
      "SampleResult" : new StringDataType()
    ],
    "mavenDependencies" : [
      "com.kibo:customworkitem:1.0.0.Final"
    ],
    "icon" : ""
	]
```
	
##  Deploying custom tasks

To use the code in defined custom task, the code must be deployed to the server. Work item handler projects must be Java JAR files that can be placed into a Maven repository.

__Uploading JAR Artifact to Business Central__
Upload the work item handler JAR to the Business Central Maven repository as an artifact by using the legacy and current editors.

__Steps__

1. In Business Central, select the __Admin__ icon in the top-right corner of the screen and and select __Custom Tasks Administration__.

1. To add a custom task, click __Add Custom Task__, browse to the relevant JAR file, and click the __Upload__ icon. The JAR file must contain work item handler implementations annotated with `@Wid`.

1. Enable the newly added custom task which is visible in list of custom tasks. Ex - `CustomWorkItemDefinitions`

## Registering custom tasks

Registering custom tasks by updating the deployment descriptor outside Business Central.

1. Register a custom task work item with the work item handler using the deployment descriptor outside Business Central.

	* The file can be found here: `YOUR_PROJECT_DIRECTORY/src/main/resources/META-INF/kie-deployment-descriptor.xml`
	* Add the following content based on the resolver type under `<work-item-handlers>`:
	
		For MVEL, add the following:
	```
	<work-item-handler>
		<resolver>mvel</resolver>
		<identifier>new com.kibo.CustomWorkItemWorkItemHandler()</identifier>
		<parameters/>
		<name>CustomWorkItem</name>
	</work-item-handler>
	```
	
1. Register a custom task work item with the work item handler using the deployment descriptor inside Business Central.

	* In Business Central, go to __Menu__ → __Design__ → __Projects__ and select the project name.
	* In the project pane, select __Settings__ → __Deployments__ → __Dependencies__.
	* Click on Add from Repository and then select the JAR file for the project.
	* After that, select __Settings__ → __Deployments__ → __Custom Tasks__.
	* Click on install `CustomWorkItemDefinitions`.
	* Click Save to save your changes
	* After that, select __Settings__ → __Deployments__ → __Work Item Handlers__.
	* Check Work Item handler is gets added or not. If not then click __Add Work Item Handler__.
	* In the Name field, enter the display name for the custom task.
	* From the Resolver list, select MVEL. In the `Value` field, enter the value based on the resolver type:
		For MVEL, use the format new <full Java package>.<Java work item handler class name>()
		Example: `new com.kibo.CustomWorkItemWorkItemHandler()`
	* Click Save to save your changes.
	
## Placing custom tasks

When a custom task is registered, it appears in the process designer palette. The custom task is named and categorized according to the entries in its corresponding WID file.

1. Steps to add custom task in workflow diagram - 

	* In Business Central, go to __Menu__ → __Design__ → __Projects__ and click a project.
	* Select the business process that you want to add a custom task to.
	* Select the custom task from the palette and drag it to the BPM diagram.
	* Optional: Change the custom task attributes. For example, change the data output and input from the corresponding WID file.


