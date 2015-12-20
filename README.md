# Camel-Spike

TransportRouteTest (CircuitBreakerTest) requires a local broker setup.
To switch to in-memory active-mq change Broker URL in AppConfig to "vm://localhost?broker.persistent=false"

AdviceWith usage example:
http://opensourceconnections.com/blog/2014/04/24/correctly-using-camels-advicewith-in-unit-tests/

Camel jms transactions:
http://tmielke.blogspot.in/2012/03/camel-jms-with-transactions-lessons.html