//////////////////////////////////////   README.txt    /////////////////////////////////////////

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


