//////////////////////////////////////   README.txt    /////////////////////////////////////////

The package contains the definition of the clients used by the service components to communicate using the rabbitMQ service.
The package is composed by two subpackages:

 - out : the package contains the stateless beans classes used to propagate messages to all the other components of the service.
         There is only one class(UpdateSender) that will be used by only the middleware components(only the middleware will send messages)

 - in : it contains the classes used to receive the propagated updates to the clients:
        - SmarthomeUpdater: the class used by the SmarthomeManager class to update the smarthome at the receival of the messages
                            from the middleware

 All the rabbitMQ clients are derived from the EndPoint class which defines the methods to connect to the rabbitMQ message exchange
 giving back a channel ready to be used to send messages

 MORE: there is a second package related to the RabbitMQ into the WebServer, this is used by the WebSockets to propagate the messages
       to the associated web clients. The reason why it isn't deployed inside the same package into the middleware is due to the usage
       of the users web sessions, this will not be accessible from outer packages


 OTHER USABLE CLASSES:

    - DeviceUpdate: structure of a single update to notify a change into the user smarthome. It contains all the fields
                    expected to manage the update from the front-end components. More details on the parameters that must be
                    present is described in the next section

    - DeviceUpdateMessage: message that can be sent to notify updates to a user. A message can contains several DeviceUpdate
                           instances but all the instances must be referred to the same user

    - UpdateNotifier: class that extends Endpoint to implement a rabbitMQ client to send updates to all the involved elements.
                      The class gives an API to send messages without having to handle the rabbitMQ message exchange. It also
                      manage in a transparent way the smarthome of the user, validating the commands before sending them to the users
                      and updating the information available between all the user session instances

