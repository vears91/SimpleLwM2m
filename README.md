# SimpleLwM2m

This project implements a library for a mock LWM2M client. It is able to register, derregister, hold basic objects, instances and resources and interact with a LWM2M server.

It is build on top of [Californium](https://eclipse.org/californium/), and aims to be a very simple and lightweight option for testing or tinkering. It is being used to load test LWM2M implementations with thousands of simultaneous mock clients.

If you need a full-featured LWM2M implemenation, consider using [Leshan](https://eclipse.org/leshan/).

# Installation

Clone this project with `git clone https://github.com/vears91/simplelwm2m.git`

Build it with maven using `mvn install`

Maven Central artifact coming soon!

# Usage

Check out this [example](https://github.com/vears91/simplelwm2m/blob/master/src/main/java/com/simplelwm2m/simplelwm2m/MockLwM2mClientDemo.java)



