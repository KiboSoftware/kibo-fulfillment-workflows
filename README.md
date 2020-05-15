# Kibo.FulfillmentWorkflows Project

This project contains the default implementations of Kibo Fulfillment related business assets which are to be deployed within a Business Process Management System. For example, using the [jBPM](https://www.jbpm.org/) Business Process Management Suite.

jBPM is a toolkit for building business applications to help automate business processes and decisions.

## Table of Contents
* [Getting Started](#getting-started)
* [How to extend Kibo Fulfillment workflows](docs/How-to-extend-Kibo-Fulfillment-workflows.md)
* [Extensibility Use Cases](docs/Extensibility-Use-Cases.md)
* [Reference Documentation](docs/Reference-Documentation.md)
    * jBPM
    * Red Hat Process Automation Manager
    

## Getting Started
The following will allow you to author and test Kibo Fulfillment workflows.

__NOTE:__ This documentation instructs use of the __jBPM Business Central__ application for authoring and testing BPMN workflows locally. An IDE such as Eclipse is an alternative for process authoring and is documented at [jbpm.org](https://jbpm.org). Click the __Read Documentation__ link on the jBPM home page and search the referenced document for __Eclipse Developer Tools__.

1. [Setup jBPM Server with Business Central for local development](#setup-jbpm-server-with-business-central-for-local-development)
1. [Clone this repository](#clone-this-repository)
1. [Import your local repository into Business Central](#import-your-forked-business-assets-project-into-business-central)
1. [Export your changes from Business Central to your local repository](#pull-back-your-custom-business-assets-to-the-forked-project-source-code-repository)
1. [Deploy your project to the connected KIE Server within Business Central](#deploy-your-assets-to-the-connected-kie-server)
1. [Interact with your newly deployed business assets](#interact-with-your-newly-deployed-business-assets)
1. [Build BPM project and copy DevCenter assets](#build-bpm-project-and-copy-devcenter-assets)
1. [Execute fulfillment workflows via the Kibo Fulfiller app](#execute-custom-workflows-via-the-kibo-fulfiller-app)
1. [Sync your local copy of this repository with the origin repository](#syncing-your-fork-with-the-upstream-repository)
1. [Sync your jBPM Business Central repository with your local copy of this repository](#syncing-your-jbpm-business-central-repository-with-your-fork-repository)

### Setup jBPM Server with Business Central for local development
The jBPM Server distribution is the easiest way to start with jBPM. The included Business Central application is useful for authoring processes. To get up and running quickly use the jBPM single distribution which can be downloaded at [jbpm.org](http://jbpm.org). Look at the [Getting Started](https://jbpm.org/learn/gettingStarted.html) guide to get yourself familiar with Business Central.

By default, Business Central is available at http://localhost:8080/business-central

### Fork the Kibo Fulfillment Workflows repository
Forking the repository is a simple two-step process.

1. On GitHub, navigate to the [EcommNG/Kibo.FulfillmentWorkflows](https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows) repository.
1. In the top-right corner of the page, click __Fork__.

#### Keep your fork synchronized
It's good practice to regularly sync your fork with the __upstream__ repository. To do this, you'll need to use Git on the command line. You can now set the __upstream__ repository using the [EcommNG/Kibo.FulfillmentWorkflows](https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows) repository you just forked.

##### Step 1: Set up Git
If you haven't yet, you should first set up Git. Don't forget to set up authentication to GitHub from Git as well.

##### Step 2: Create a local clone of your fork
Right now, you have a fork of the Kibo.FulfillmentWorkflows repository on GitHub, but you don't have the files in that repository on your computer. Let's create a clone of your fork locally on your computer.

1. On GitHub, navigate to __your fork__ of the Kibo.FulfillmentWorkflows repository.

1. Under the repository name, click __Clone or download__.

1. To clone the repository using HTTPS, under __Clone with HTTPS__, click the displayed clipboard icon. To clone the repository using an SSH key, including a certificate issued by your organization's SSH certificate authority, click __Use SSH__, then click the __Clone URL__ button.

1. Open Terminal.

1. Type `git clone`, and then paste the URL you copied earlier. It will look like this, with your GitHub username instead of `YOUR_USERNAME`:
    ```
    $ git clone https://github.com/YOUR_USERNAME/YOUR_FORK
    ```

1. Press __Enter__. Your local clone will be created.
    ```
    $ git clone https://github.com/YOUR_USERNAME/YOUR_FORK
    > Cloning into `YOUR_FORK`...
    > remote: Counting objects: 1033, done.
    > remote: Total 1033 (delta 0), reused 0 (delta 0), pack-reused 1033
    > Receiving objects: 100% (1033/1033), 1.22 MiB | 173.00 KiB/s, done.
    > Resolving deltas: 100% (405/405), done.
    ```

Now, you have a local copy of your fork of the Kibo.FulfillmentWorkflows repository.

##### Step 3: Configure Git to sync your fork with the original Kibo.FulfillmentWorkflows repository
When you fork a project you can configure Git to pull changes from the original, or __upstream__, repository into the local clone of your fork.

1. On GitHub, navigate to the original [EcommNG/Kibo.FulfillmentWorkflows](https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows) repository.

1. Under the repository name, click __Clone or download__.

1. To clone the repository using HTTPS, under __Clone with HTTPS__, click the displayed clipboard icon. To clone the repository using an SSH key, including a certificate issued by your organization's SSH certificate authority, click __Use SSH__, then click the __Clone URL__ button.

1. Open Terminal.

1. Change directories to the location of the fork you cloned in [Step 2: Create a local clone of your fork](#step-2-create-a-local-clone-of-your-fork).

    * To go to your home directory, type just `cd` with no other text.
    * To list the files and folders in your current directory, type `ls`.
    * To go into one of your listed directories, type `cd your_listed_directory`.
    * To go up one directory, type `cd ..`

1. Type `git remote -v` and press __Enter__. You'll see the current configured remote repository for your fork.
    ```
    $ git remote -v
    > origin  https://github.com/YOUR_USERNAME/YOUR_FORK.git (fetch)
    > origin  https://github.com/YOUR_USERNAME/YOUR_FORK.git (push)
    ```

1. Type `git remote add upstream`, and then paste the URL you copied in Step 2 and press __Enter__. It will look like this:
    ```
    $ git remote add upstream https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows.git
    ```

1. To verify the new __upstream__ repository you've specified for your fork, type `git remote -v` again. You should see the URL for your fork as __origin__, and the URL for the original Kibo.FulfillmentWorkflows repository as __upstream__.
    ```
    $ git remote -v
    > origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (fetch)
    > origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (push)
    > upstream  https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows (fetch)
    > upstream  https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows (push)
    ```

Now, you can keep your fork synced with the __upstream__ repository with a few Git commands. 

For more information, see "[Syncing your fork with the upstream repository](#syncing-your-fork-with-the-upstream-repository)"

__Next steps__

You can make any changes to a fork, including:

* __Creating branches:__ Branches allow you to build new features or test out ideas without putting your main project at risk.
* __Opening pull requests:__ If you are hoping to propose a change to the original repository, you can send a request to Kibo to pull your fork into their repository by submitting a __pull request__.

__References:__
* [GitHub Getting Started - Fork a Repo](https://help.github.com/en/github/getting-started-with-github/fork-a-repo)

### Import your cloned business assets project into Business Central
The forked business assets project can be easily imported into Business Central since it’s a valid git repository.

1. Create a git branch named __master__ from the default __develop__ branch
    ```
    $ git checkout -b master
    Switched to a new branch 'master'
    --- 
    $ git branch
      develop
    * master
    ```
    __NOTE:__ The above assumes no __master__ branch exists within your forked repository. The name __master__ is used to coincide with the default branch name used by the development jBPM instance.

1. Log in to Business Central and go to __Menu > Design > Projects__

1. Select `Import Project` from the _Add Project_ pull-down and enter the filesystem location of the project git repo within the `Repository URL` field. 
    ```
    file://{filesystem location of forked repository}
    ```
1. Click __Import__, confirm project to be imported and click __Ok__

    __NOTE:__ If attempting upload within a Docker container a volume must be mapped.

1. Work on your business assets

    Once the business assets project is imported into Business Central you can start working on it. Just go to the project and add or modify assets such as business processes, forms, rules, decision tables, etc.

### Pull back your custom business assets to the forked project source code repository

1. Go to Settings of the project within Business Central

1. Copy the __URL__ value from the __General Settings__ view

1. Go to the filesystem location of the forked and imported git repository

1. Type `git remote -v` and press __Enter__. You'll see the current configured remote repositories.

    ```
    $ git remote -v
    > origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (fetch)
    > origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (push)
    ```

1. Type `git remote add jbpm`, and then paste the URL you copied in Step 2. Modify the value to include `wbadmin@` and press __Enter__. It will look like this:

    ```
    $ git remote add jbpm ssh://wbadmin@localhost:8001/MySpace/YOUR_DEVCENTER_ACCOUNT_KEY.YOUR_DEVCENTER_APP_NAME
    ```

1. To verify the new __jbpm__ repository you've specified for your fork, type `git remote -v` again. You should see the URL for the jBPM Business Central project as __jbpm__, the URL for your fork as __origin__, and the URL for the original repository as __upstream__.

    ```
    $ git remote -v
    > jbpm	ssh://wbadmin@localhost:8001/MySpace/YOUR_DEVCENTER_ACCOUNT_KEY.YOUR_DEVCENTER_APP_NAME (fetch)
    > jbpm	ssh://wbadmin@localhost:8001/MySpace/YOUR_DEVCENTER_ACCOUNT_KEY.YOUR_DEVCENTER_APP_NAME (push)
    > origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (fetch)
    > origin    https://github.com/YOUR_USERNAME/YOUR_FORK.git (push)
    > upstream  https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows (fetch)
    > upstream  https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows (push)
    ```
 
1. Pull or fetch your custom business assets from jBPM Business Central to your forked git repository.

    ```
    $ git checkout master
    $ git pull jbpm master - when prompted enter wbadmin as password
    ```
    or
    ```
    $ git checkout master
    $ git fetch jbpm
    $ git rebase jbpm/master
    ```

    __NOTE:__ If you encounter issues connecting to the jBPM generated Git repository over SSH, you can change the protocol to __http__ within the same Business Central __Settings__ view for your project.

1. Synchronize the __develop__ branch of your fork with the __origin__ repository on GitHub.
    ```
    $ git checkout develop
    $ git pull origin develop
    ```
   
1. Rebase your updated local __master__ branch commits on the synchronized __develop__ branch.
    ```
    $ git checkout master
    $ git rebase develop
    ```
   
1. Squash all dedicated jBPM Business Central changes in the __develop__ branch of your fork.
    ```
    $ git checkout develop
    $ git merge --squash master
    ```
    
1. Add & commit the merged changes to the __develop__ branch and then push to this GitHub repository.
    ```
    $ git add -A
    $ git commit -m "some useful comment"
    $ git push origin develop
    ```

1. Reset the jBPM Business Central __master__ branch using the updated __develop__ branch
    ```
    $ git checkout master
    $ git reset --hard develop
    $ git push -f jbpm master
    ```
   
1. With your business asset changes now part of the cloned project source tree, Maven commands can be used to build and publish the KJAR artifact to a Maven repository - without using the standalone jBPM server.
    ```
    $ mvn clean install
    ```

### Deploy your business assets to the connected KIE Server
Deploy the business assets project into the running KIE Server. After adding assets to your project in Business Central you can just deploy it to a running KIE server instance. Click the __Deploy__ button on your project and in few seconds you should see the project deployed.

### Interact with your newly deployed business assets
You can use __Process Definitions__ and __Process Instances__ perspectives of Business Central to interact with your newly deployed business assets such as processes or user tasks.

### Upload and Install custom workflows thru DevCenter
* Change your current working directory to the `devcenter-app` directory
* Validate content of file `mozu.config.json`
* Type `grunt -f` to upload your application assets to Kibo DevCenter
* Log in to Kibo DevCenter and open your application
* Verify existence of your uploaded application assets in the __Packages > Assets__ view
* Click the __Install__ button
* Select the appropriate Sandbox and click __OK__

### Enable default workflows with the Kibo Location Group Configuration API
Change Kibo Location Group configuration settings to use customized processes - reference new containerId(s) and processId(s) by shipmentType.

##### Example cURL Requests
Get all Location Groups for a tenant and site:
```
curl --request GET 'http://t123.mozu.com/api/commerce/admin/locationGroups' \
--header 'x-vol-tenant: 123' \
--header 'x-vol-site: 456' \
--header 'Authorization: Bearer *******'
```
Get configuration for a specific Location Group:
```
curl --request GET 'http://t123.mozu.com/api/commerce/admin/locationGroupConfiguration/2' \
--header 'x-vol-tenant: 123' \
--header 'x-vol-site: 456' \
--header 'Authorization: Bearer *******'
``` 
Set custom BPM configuration for a Location Group:
```
curl --request PUT 'http://t123.mozu.com/api/commerce/admin/locationGroupConfiguration/2' \
--header 'x-vol-tenant: 123' \
--header 'x-vol-site: 456' \
--header 'Authorization: Bearer *******' \
--header 'Content-Type: application/json' \
--data-raw '{
    "tenantId": 123,
    "siteId": 456,
    "locationGroupId": 2,
    "locationGroupCode": "2",
    ...
    "bpmConfigurations": [
        {
            "shipmentType": "BOPIS",
            "workflowContainerId": "DEFAULT_FULFILLMENT_CONTAINER_NAME",
            "workflowProcessId": "fulfillment.FulfillmentProcess-BOPIS"
        },
        {
            "shipmentType": "STH",
            "workflowContainerId": "DEFAULT_FULFILLMENT_CONTAINER_NAME",
            "workflowProcessId": "fulfillment.FulfillmentProcess-STH"
        },
        {
            "shipmentType": "Transfer",
            "workflowContainerId": "DEFAULT_FULFILLMENT_CONTAINER_NAME",
            "workflowProcessId": "fulfillment.FulfillmentProcess-Transfer"
        }
    ],
    ...
}'
```

### Execute custom workflows via the Kibo Fulfiller app
* Log in to the Kibo Unified Commerce Platform Admin application and select the appropriate tenant
* Create a new Order and shipment type matching the custom workflow config
* Navigate to the Kibo Fulfiller App and step through the workflow tasks for the corresponding shipment
* Confirm the functionality of your custom workflow

## Syncing your fork with the upstream repository

1. Open Terminal.

1. Change the current working directory to your local project.

1. Fetch the branches and their respective commits from the upstream repository. Commits to develop will be stored in a local branch, upstream/develop.
    ```
    $ git fetch upstream
    > remote: Counting objects: 8, done.
    > remote: Compressing objects: 100% (8/8), done.
    > remote: Total 8 (delta 3), reused 0 (delta 0), pack-reused 0
    > Unpacking objects: 100% (8/8), done.
    > From https://github.kibocommerce.com/EcommNG/Kibo.FulfillmentWorkflows
    >  * [new branch]      develop                       -> upstream/develop
    ```

1. Check out your fork's local `develop` branch.
    ```
    $ git checkout develop
    > Switched to branch 'develop'
    ```

1. Merge the changes from `upstream/develop` into your local `develop` branch. This brings your fork's `develop` branch into sync with the __upstream__ repository, without losing your local changes.
    ```
    $ git merge upstream/develop
    > Merge made by the 'recursive' strategy.
    ```

    If your local branch didn't have any unique commits, Git will instead perform a "fast-forward":
    ```
    $ git merge upstream/develop
    > Updating 34e91da..16c56ad
    > Fast-forward
    >  README.md                 |    5 +++--
    >  1 file changed, 3 insertions(+), 2 deletions(-)
    ```

__Tip:__ Syncing your fork only updates your local copy of the repository. To update your fork on GitHub, you must push your changes.

For more information, see "[Syncing a fork](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/syncing-a-fork)."

## Syncing your jBPM Business Central repository with your fork repository
    ```
    $ git checkout master
    > Switched to branch 'master'
    
    $ git reset --hard develop
    > HEAD is now at a5fff3d Merge remote-tracking branch 'upstream/develop' into develop
    
    $ git push -f jbpm master
    > Counting objects: 60, done.
    > Delta compression using up to 8 threads.
    > Compressing objects: 100% (52/52), done.
    > Writing objects: 100% (60/60), 155.44 KiB | 8.18 MiB/s, done.
    > Total 60 (delta 26), reused 0 (delta 0)
    > remote: Resolving deltas: 100% (26/26)
    > remote: Updating references: 100% (1/1)
    > To http://localhost:8080/business-central/git/MySpace/YOUR_DEVCENTER_ACCOUNT_KEY.YOUR_DEVCENTER_APP_NAME
    >  + ace62bb...a5fff3d master -> master (forced update)
    ```