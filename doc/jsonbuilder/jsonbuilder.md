# Json Builder
The json builder is a easy to use library to build json in java code.
It is also possible to use variables, that can be replaced on runtime. 
The json builder implementation is based on the jackson lib.

Features:
- load json file from classpath
- create a json from a json string
- replace parameters inside json
- replace parameters inside json text nodes

Use cases:
- build elasticsearch queries

## Build
In the unit test are a lot of code snippets available, that show how a json can be build.
- https://github.com/quasiris/qsf-integration/blob/master/src/test/java/com/quasiris/qsf/json/JsonBuilderTest.java

### String
create a json from a string

```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.string("{\"foo\" : {}}");
```

result:
```json
{
  "foo" : {}
}
```
### add string
create a json from a string and add it to an array. If the current node is no array, an array wrapper is created

```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.addString("{\"foo\" : {}}");
```

result:
```json
[
  {
    "foo" : {}
  }
]

```

### Classpath
Load the json from a file in the classpath.
```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.classpath("com/quasiris/qsf/json/test-nested-replace-array.json");
```

### create object

Create a empty object 
```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.object();
```
result:
```json
{}
```


Create a object with a key
```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.object("foo");
```
result:
```json
{
  "foo" : {}
}
```

Create a object with a key and value
```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.object("foo", "bar");
```
result:
```json
{
  "foo" : "bar"
}
```

### Add Value

### Add Json object

### Add Json array

### Add Pojo

### Path force create

Navigate to the specified path, if the path not exists, the path is created.

```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.object("n1").object("n2");
jsonBuilder.root();
jsonBuilder.pathsForceCreate("n1/n2/n3/n4");
jsonBuilder.object("n33");
```

result:
```json
{
    "n1": {
        "n2": {
            "n3": {
                "n4": {
                    "n33": {}
                }
            }
        }
    }
}
```



## Navigate

### Path
With the path method it is possible to navigate to a specific key in json.

In the following example a json with 4 nested keys is created.
Then we navigate back to the root.
With the path method we navigate to key n1/n2
```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.object("n1").object("n2").object("n3").object("n4");
jsonBuilder.root();
jsonBuilder.paths("n1/n2");

```

### Exists
Test that a json key exists.

If the path exists, true is returned.
Otherwise false is returned.
```java
JsonBuilder jsonBuilder = new JsonBuilder();
jsonBuilder.object("n1").object("n2");
jsonBuilder.root();
boolean exists = jsonBuilder.exists("n1/n2");
```

### Current
Return the json based on the current pointer.

### Root
Return the json root.

### Get
Return the json root.

### Stash / Unstash

## Replace


- https://github.com/quasiris/qsf-integration/blob/master/src/test/java/com/quasiris/qsf/json/JsonSubstitutorTest.java

### Replace a json node


```json
{
  "foo" : {
    "$myVar" : {}
  }

}
```

- $myVar must be a json
- example:
```json
{
  "alice" : "bob"
}
```

- the whole node is replaced
- result:

```json
{
  "foo" : {
    "alice" : "bob"
  }
}
```

### Replace a json node value

- value can be a:
  - json
  - string
  - long
  - integer
  - double
  - float
  - boolean

```json
{
  "myNode" : "$myVar"
}
```

- json
```json
{
  "foo" : "bar"
}
```

- result:
```json
{
  "myNode" : {
    "foo" : "bar"
  }
  
}
```

- string: "bar"
- result:
```json
{
  "myNode" : "bar"
  
}
```

- long: 4711
- result:
```json
{
  "myNode" : 4711
  
}
```

- boolean: true
- result:
```json
{
  "myNode" : true
  
}
```

- float: 1.2
- result:
```json
{
  "myNode" : 1.2
  
}
```

### Replace a array value
```json
{
  "myArray" : [
    "value1",
    "value2",
    "$myVar",
    "value5"
  ]
}
```

- the replaced value must be a array or list
```json
[
  "value3",
  "value4"
]
```

- result:
```json
{
  "myArray" : [
    "value1",
    "value2",
    "value3",
    "value4",
    "value5"
  ]
}
```


### Replace a nested array value
```json
{
  "must": [
    {
      "foo" : "$foo",
      "bool": {}
    }
  ]
}
```

- $foo = bar
- result:
```json
{
  "must": [
    {
      "foo" : "bar",
      "bool": {}
    }
  ]
}
```


### Replace a empty key


```json
{
  "alice" : "bob",
  "$replaceMe" : {}
}
```

- $replaceMe: {}

```json
{
  "alice" : "bob"
}
```


Alternatively you can use a EmptyNode() object
```java
jsonBuilder.valueMap("removeMe", new EmptyNode());
```


## Replace a variable in a text node
TODO

## FAQ

### How can i repalce a variable by a long value

