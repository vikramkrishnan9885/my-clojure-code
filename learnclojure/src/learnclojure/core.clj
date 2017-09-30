(ns learnclojure.core) ;; Look at this name space

;; (require 'clojure.string) ;; This is similar to import in Python or Java. Note that the module name needs to be quoted so that it is not evaluated
;; (require '[clojure.string :as s]) ;; So that you don't have to keep typing clojure.string. Also, note the vector
(require '[clojure.string :refer [split]]) ;; Ensure that when you do this, you don't import same function from different namespaces
;; (require '[clojure.string :refer :all]) ;; This is dangerous. Don't do this or (use 'clojure.string)
;; (import 'java.util.Date) ;; Importing java functions
(import '[java.util Date Calendar]) ;; If you want to import multiple classes from same Java module

(defn foo
  "I don't do a whole lot."
  [x]
  (str x "Hello, World!"))

;; (clojure.string/split "a,b,c" #",")
;; (s/split "a,b,c" #",")
(split "a,b,c" #",")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; JAVA INTEROP
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(Date.) ;; dot after Date denotes constructor - Java InterOp
(new java.util.Date) ;; This also works and is equivalent to the dot notation used above

(System/currentTimeMillis) ;; java.lang.System does not need import
(Date. (System/currentTimeMillis)) ;; Date Constructor initialized with inputs

;; To call a Java instance method, use this syntax:
;; (. object_name method_name args) → is Java's object_name.method_name(args)
;; It is also equivalent to:
;; (.method_name object_name args)
(. (Date.) getTime)
(.getTime (Date.)) ;; Equivalent to above


;; How to call static methods
(. String (valueOf 1))
(. String valueOf 1) ;; This is a more consistent syntax (. class_name method_name args)
;; (.valueOf String 1)  ;; This used to be usable, but now is not valid - you cannot use (. method_name class_name value)
(String/valueOf 1) ;; This works as well (class_name/method_name value)
;; call Java static method named floor in class Math
(Math/floor 1.7)

;;Fortunately, Clojure has a handy built-in macro for just this kind of situation: doto.  The doto macro takes a body of expressions.  It evaluates the first expression and saves it in a temporary variable, and then inserts that variable as the first argument in each of the following expressions.  Finally, doto returns the value of the temporary variable.  An example will make more sense:
;;
;; This code:
;; (doto (make-thing)
;;  (foo 1 2)
;;  (bar 3 4))
;;
;; Expands to this:
;; (let [x (make-thing)]
;; (foo x 1 2)
;; (bar x 3 4)
;;  x)
;; The object created on the first line gets threaded through each of the following expressions and returned at the end.
;; So to recap, doto exists because you wish to apply a series of methods to an instance
;; Java and Clojure differ in that Java you create an object and then use a bunch of setter methods
;; While in Clojure objects are immutable
(doto (Calendar/getInstance)
  (.set Calendar/YEAR 1985)
  (.set Calendar/MONTH 8)
  (.set Calendar/DATE 25)
) ;;Note that the series of method invocations the instance has been left out

;; Array operations
(int-array 100)
(object-array 100) ;; see the object array
(into-array String ["this" "is" "an" "array"])
(def myarray (into-array String ["this" "is" "an" "array"]))
;; you can now use aget and aset macros
(aget myarray 2)
(aset myarray 1 "was")
(aget myarray 1) ;; Note value has changed
;; amap macro
(amap myarray idx ret (aset ret idx (apply str (reverse (aget myarray idx)))))
;; you can also create areduce

;; Another important point in Java Interop is type-hinting wherein you can hint the type of the variable
;; Clojure supports the use of type hints to assist the compiler in avoiding reflection in performance-critical areas of code. Normally, one should avoid the use of type hints until there is a known performance bottleneck. Type hints are metadata tags placed on symbols or expressions that are consumed by the compiler
(defn len [x]
  (.length x))
(defn len2 [^String x]
  (.length x))

(time (reduce + (map len (repeat 1000000 "asdf"))))
(time (reduce + (map len2 (repeat 1000000 "asdf"))))
;; 1895 vs 95 seconds without and with type hinting

;; proxy and reify macros
;; Clojure supports the dynamic creation of objects that implement one or more interfaces and/or extend a class with the proxy macro. The resulting objects are of an anonymous class.
;; ( proxy [class-and-interfaces] [args] fs+)
;; class-and-interfaces - a vector of class names
;; args - a (possibly empty) vector of arguments to the superclass constructor.
;; f ⇒ (name [params*] body) or (name ([params*] body) ([params+] body) …)
(def mythread (proxy [Thread] [] (run [] (println "Running in a thread"))))
(import 'java.util.concurrent.Executors)
(def mypool (Executors/newFixedThreadPool 4))
(.submit  mypool mythread) ;; submit to thread pool

(def myrunnable (proxy [Runnable] [] (run [] (println "Running in a runnable"))))
(.submit mypool myrunnable)

;; Articles on Reification
;; http://www.lispcast.com/reification
(.listFiles
 (java.io.File. ".")
 (reify
   java.io.FileFilter
   (accept [this f]
     (.isDirectory f))))

;; Generating Java Classes
(gen-class
  :name learnclojure.MyClass ;; this has to be the first thing namespace.classname
  :prefix "my-" ;; this is the prefix used to identify methods that belong to the class
  :methods [[getName [] String]] ;; Described as a vector of vectors where each vector is a method without the prefix of the form [name [args] return_type]
  :constructors {[String] []} ;; this enables the class to store internal state. This is a map that maps lists of types to super constructor types
  :state state ;; name of the public final field present on our class that we can access from within any of our methods
  :init init ;;Usually state is called state and init is called init
)

(defn my-init [name] ;; constructor that takes string argument called name
  [[] {:name name}] ;; Vector of length 2, super constructor value is empty because super class is Object which takes no arguments. Second value is the state that we want it to take at initialization
)

;; (defn my-getName [this] ;; refers to the instance that calls this method
;;  "MyClass's method"
;; )

(defn my-getName [this]
  (get (.state this) :name)
)

;; Ideally note that this gen-class malarkey would not be needed. Create folders called java/ and clj/ in your src/ directory. Add :source-paths and :java-source-pths to project.clj and import the class using classname in your clojure code


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; MAIN AND TESTING
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; To create an executable we need a -main function similar to Java main
(defn -main []
  (foo "Main")
)

(comment
(require '[clojure.test :refer [is testing]])
(testing "Make sure foo works"
  (is (= (foo "") "Hello, World!"))
  (is (= (foo "Test ") "Test Hello, World!"))
)
) ;; This ensures that the code is not created into an executable

