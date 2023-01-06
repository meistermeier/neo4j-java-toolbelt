# Neo4j Java Driver Tool Belt

This project consists right now of a read-only object mapper and a parameter renderer.
The idea is to collect a bunch of tooling around the official [Neo4j Java Driver](https://neo4j.com/docs/java-manual/current/).
If you're as old as me (or older), you might still remember _Windows 95 Plus!_.
See this as the _Plus! package for the driver_.

## Disclaimer

This project is 100% officially unsupported by Neo4j.
Just my hobby project.

## Documentation

For more usage information and examples, please go to https://meistermeier.github.io/neo4j-java-toolbelt.

## Read-Only Mapper

### Background / Thoughts

From the experience and feedback we made while developing Spring Data Neo4j and Neo4j-OGM, 
it became quite obvious that, as usual with such general purpose solutions,
there is a significant number of users who just wants to get their results mapped.
And I am not talking about generated, general purpose, queries that map to an entity which describes a lot of meta-data via annotations.
No I mean simple POJO/DTO/Value wrapper/you name it-mapping.

For this concern I thought quite some time about how one could achieve this by wrapping the Neo4 Java driver
(again, like we did in Spring Data Neo4j).
After a while the fundamental problem with this approach became clearer and clearer:
You don’t have to wrap the driver but make use of the existing API and incorporate it in the design.
In particular, there is exactly one method that just _screams_ “I want to map”.
This method is called `list(Function<Value, T> mappingFunction)`.
It provides the `Record` (the driver’s type, not the Java one) and the rest is up for us to do.
A `Record` is always a single entry/row, so we won’t need to consider the case of mapping the whole result at once (for now).

### Mapping capabilities

First of all, I created a (partially imaginary, partially written) list of use-cases:

| Use-case                 | Input (Driver value)                       | Output (Java value)                       | Comment                                                   |
|--------------------------|--------------------------------------------|-------------------------------------------|-----------------------------------------------------------|
| Value mapping            |                                            |                                           | 
| Single value mapping     | `xValue(’Test’)`                           | `”Test”`                                  | `xValue` stands for every supported simple Driver type    |
| Collection value mapping | `ListValue[’Test1’,’Test2’]`               | `Collection(”Test1”,”Test2”)`             |                                                           |
| Object mapping           |                                            |                                           | 
| Properties               | `Record(’someProperty’,‘Test’)`            | `Object(someProperty:”Test”)`             | Driver’s `Record/MapAccessor`                             |
| Node                     | `Node(someproperty:’Test’)`                | `Object(someproperty:”Test”)`             |                                                           |
| Collection(properties)   | `ListValue[Record(’someProperty’,‘Test’)]` | `Collection(Object(someProperty:”Test”))` | single record but `RETURN collect(a.propertyX), collect…` |
| Collection(node)         | `ListValue[Node(someproperty:’Test’)]`     | `Collection(Object(someproperty:”Test”))` | single record but `RETURN collect(node)`                  |

As you might have already interpreted the list correctly, the focus of this library is to support you with *read-only* mapping.

### Getting up to speed

Add the dependency to your _pom.xml_ (or _build.gradle_, if you want) and add the following coordinates.

```xml
<dependency>
    <groupId>com.meistermeier.neo4j.toolbelt</groupId>
    <artifactId>neo4j-java-toolbelt-mapper</artifactId>
    <version>0.1.1</version>
</dependency>
```

### How to use this

Mapping of returned data should be as unambiguous as possible.
There are two methods in the `Mapper` that helps you to map data.
The following snippets should give you the right impression:

_Mapping from a node_
```java
// with (:Person{name: 'Gerrit', yearBorn: 1983})
Mapper mapper = Mapper.INSTANCE
try (var session = driver.session()) {
    List<Person> people = session.run("MATCH (p:Person) return p")
    .list(mapper.createConverterFor(Person.class));

    // Person[name:'Gerrit', yearBorn:'1983']
}
```

## Parameter renderer

With a `Renderer`, it is possible to render a class into a map of driver values.
Those parameters can be used within the Driver query.

```java
record ParameterClass(String a) {}
    
Renderer renderer = Renderer.INSTANCE;

var parameterMap = renderer.toParameters(new ParameterClass("myValue"));

String result = session.run("RETURN $a as a").single().get("a").asString();

// result = myValue
```
