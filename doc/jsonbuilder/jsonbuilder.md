# Json Builder

## Build
- https://github.com/quasiris/qsf-integration/blob/master/src/test/java/com/quasiris/qsf/json/JsonBuilderTest.java

### String
create a json from a string

### create object

### Add Value

### Add Json object

### Add Json array

### Add Pojo



## Navigate

### Path

### Path force create

### Exists

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

