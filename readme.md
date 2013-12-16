Equivalent Cypher query, does not perform.

````
MATCH (head:ActivityHead)-[:NEXT*]->(activity)-[:IN_FORUM]->
              (forum:Forum)<-[:FOLLOWS]-(user:User {name: {name}}),
              (activity)<-[:WROTE]-(author:User)
RETURN author.name, activity.message, forum.name
LIMIT 20
````