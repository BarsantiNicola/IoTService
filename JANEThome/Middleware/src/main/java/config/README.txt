//////////////////////////////////////   README.txt    /////////////////////////////////////////

The package contains the components to spread the configurations to all the elements of the service
The config can be obtained by calling the Singleton EJB component Configuration which maintains all
the information. The information are very etherogeneous(the config class can maintain config file
for many different services(so some parameter can be duplicated for different meanings),
for these reason the information are organized as a set of config file from which some parameters are available.
Into a generic information request you need to provide both the config file from which the parameter has to be extracted and the parameter name.

THE CONFIGURATIONS MUST BE DEPLOYED ON THE resources/META-INF FOLDER WITH EXTENSION .CONF
THEY WILL AUTOMATICALLY DETECTED AND LOADED FROM THE CLASS AND WILL BE AVAILABLE USING THEIR CONFIGURATION FILENAME WITHOUT THE .CONF EXTENSION
