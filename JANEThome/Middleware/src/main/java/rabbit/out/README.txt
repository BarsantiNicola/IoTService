//////////////////////////////////////   README.txt    /////////////////////////////////////////

The package maitains all the components needed for sending messages between the system nodes using
a rabbitMQ message broker. The usable classes are:

   - DeviceUpdate: structure of a single update to notify a change into the user smarthome. It contains all the fields
                   expected to manage the update from the front-end components. More details on the parameters that must be
                   present is described in the next section

   - DeviceUpdateMessage: message that can be sent to notify updates to a user. A message can contains several DeviceUpdate
                          instances but all the instances must be referred to the same user

   - UpdateNotifier: class that extends Endpoint to implement a rabbitMQ client to send updates to all the involved elements.
                     The class gives an API to send messages without having to handle the rabbitMQ message exchange. It also
                     manage in a transparent way the smarthome of the user, validating the commands before sending them to the users
                     and updating the information available between all the user session instances

   DEVICE UPDATE MESSAGES:

        ADD LOCATION:

        {
                "type": ADD_LOCATION,
                "data": {
                    "location": String,
                    "address": String,
                    "port": String
        }

        RENAME LOCATION:

        {
                "type": RENAME_LOCATION,
                "data": {
                    "old_name": String,
                    "new_name": String
        }

        REMOVE LOCATION:

        {
                "type": "REMOVE_LOCATION",
                "data": {
                    "location": String
                }
        }

        ----------------------------------------------------------------------------------------------------------------

        ADD SUBLOCATION:

        {
                "type": ADD_SUB_LOCATION,
                "data": {
                    "location": String,
                    "sublocation": String
        }

        RENAME SUBLOCATION:

        {
                "type": RENAME_SUB_LOCATION,
                "data": {
                    "location": String,
                    "old_name": String,
                    "new_name": String
        }

        {
                "type": "REMOVE_SUB_LOCATION",
                "data": {
                    "location": String,
                    "sublocation": String
                }
        }

        ----------------------------------------------------------------------------------------------------------------

        ADD DEVICE:

        {
                "type": "ADD_DEVICE",
                "data": {
                    "location": String,
                    "sublocation": String,
                    "dID": String,(not present into requests, only on middleware -> web/google)
                    "name": String,
                    "type": String

        }

        RENAME DEVICE:

        {
                "type": "RENAME_DEVICE",
                "data": {
                    "old_name": String,
                    "new_name": String,
                     "dID": String
        }

        REMOVE DEVICE:

        {
                "type": "REMOVE_DEVICE",
                "data": {
                    "name": String,
                     "dID": String
                }
        }

        CHANGE DEVICE SUBLOCATION:

        {
               "type": "CHANGE_DEVICE_SUB_LOCATION,
               "data": {
                   "location": String,
                   "sublocation": String,
                   "name": String,
                   "dID": String
        }

        EXECUTE COMMAND:

        {
                "type": "UPDATE",
                "data": {
                    "dID": String,
                    "device_name": String,
                    "action": String,  (google home trait)
                    "value": String,
        }

        STATISTIC:


