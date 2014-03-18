# Unmanaged Extension for Activity Aggregation

## Use-Case

A user wants to see all the recent activity on the boards it is following. For instance the last 20 or 50 posts.

A graph model example looks like this:

![](https://dl.dropboxusercontent.com/u/14493611/data_modeling_activity_stream.png)


The use-case can be easily represented as Cypher query like this, but unfortunately this doesn't yet perform well:

````
MATCH (head:ActivityHead)-[:NEXT*]->(activity)-[:IN_FORUM]->
              (forum:Forum)<-[:FOLLOWS]-(user:User {name: {name}}),
              (activity)<-[:WROTE]-(author:User)
RETURN author.name, activity.message, forum.name
LIMIT 20
````

That's why an unmanaged extension can reach out only to those boards and posts that are relevant without touching anything else in the graph. The code in this repository encapsulates the algorithm as a single Java class and exposes it to the outside world as a Neo4j server extension.


## Setup

The ressource has to be configured in `conf/neo4j-server.properties` as 

    org.neo4j.server.thirdparty_jaxrs_classes=org.neo4j.example.activity=/api 
    
It is then available for the front-end at the URL `http://host:7474/api/activities/{username}`.
