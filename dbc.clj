(defn limited-sqrt [x]
    {:pre  [(pos? x)]
     :post [(>= % 0), (< % 10)]}
    (Math/sqrt x))

(println (limited-sqrt 9))   ;; 3.0
(println (limited-sqrt -9))  ;; AssertionError Assert failed: (pos? x)
(println (limited-sqrt 144)) ;; AssertionError Assert failed: (< % 10)
