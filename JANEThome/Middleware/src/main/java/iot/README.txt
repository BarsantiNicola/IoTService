Package containing a set of classes for defaining the smarthome.
The classes are mainly used by the webServer, to only classes usable by the middleware/google home are

   - SmarthomeDevice: the definition of a device accordingly to the google home device structure
   - SmarthomeWebDevice: extend the definition of the SmarthomeDevice to add the device sensors values(status of a light/fan/ etc...)

TO FEDERICO: i recommend you to use SmarthomeWebDevice as the definition class for the mongoDB storage. In this way you can save in a easy way all the parameters
             related to the service and at the same time it doesn't represent a problem of compatibility with google home(if needed mongoDB allows you to extract only
             the parent class by changing the connection associated bean class(linking SmarthomeDevice you will get only the basic google device, linking SmarthomeWebDevice
             you will take the complete class definition used by the WebServer)