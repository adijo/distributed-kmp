
# Distributed-KMP
A toy project implementing a distributed string searching algorithm using akka and the Knuth-Morris-Pratt algorithm.
The string is split into several substrings and these are subsequently searched in parallel. The algorithm **will not** find
a match when the pattern overlaps between two split strings.

# Note
This implementation was done for educational purposes only. The algorithm will **not find all** occurrences. Some occurrences will
be skipped due to the way the functionality is implemented. 

# Installation
* Download [Scala IDE for Eclipse](http://scala-ide.org/).
* Make sure you have Scala's build tool [sbt](http://www.scala-sbt.org/release/tutorial/Installing-sbt-on-Linux.html) installed. 
* Clone the repository.

```
git clone https://github.com/adijo/distributed-kmp.git
```

* Enter the directory.

```
cd distributed-kmp
```

* Type `sbt`. When you see a `>` console at the end of it, type `eclipse` to set up the eclipse project.
* Start the Scala IDE for Eclipse. 
 ```
 File -> Import -> Existing Projects from Workspace
 ```` 
  Navigate to the folder that was just downloaded.
  
* Click Finish and you're done.
