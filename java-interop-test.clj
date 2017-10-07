(ns java-interop-test)

;; To call Java classes , you can go to search.maven.org, note your GroupId, ArtifactId and Version
;; and use [GroupId/ArtifactId "Version"] in your project.clj file
;; Use lein deps to download and install dependencies
;; You can also use lein deps :tree -useful for version management

(String. ) ;; note the absence of space after the class name
(String. "lili")
(new String "lili") ;; note that the new keyword is supported

(.toLowerCase (String. "ANNA KARENINA")) ;; For methods that don't take arguments pattern is (.methodName (ClassName. constructor_vars)), note that you could use def and create a value that holds the class bit in the inner bracket
(->> "ANNA KARENINA"
     String.
     .toLowerCase
) ;; This works as well. Note the thread operator
(.substring (String. "Anna Karenina") 0 5) ;; For functions that take arguments

(-> "Anna Karenina"
    String.
    (.substring 0 5)) ;; This works

(->> "Anna Karenina"
     String.
     (.substring 0 5)) ;; This however does not as the variable position is all wrong
(-> (->> "Anna Karenina" String.)
    (.substring 0 5)) ;; This again works

(class (String. )) ;; Returns name of Java class

(System/getenv "TEST") ;; Used for static methods

;; Uses of doto

;; Notice that this is super-unreadable
(.toString (.append
             (.append
               (.append (StringBuffer.) "Alexei ") "Alexandrovich ") "Romanov"))

(.toString (doto (StringBuffer.)
             (.append "Alexander ")
             (.append "Alexaderovitch ")
             (.append "Romanov")
            )
) ;; Create an instance of class do stuff to it