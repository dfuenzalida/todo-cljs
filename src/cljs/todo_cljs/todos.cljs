(ns todo-cljs.todos)

;; Simple test
(defn hello[]
  (js/alert "hello!"))

;; This get-uuid fn is almost equiv to the original
(defn get-uuid []
  (apply
   str
   (map
    (fn [x]
      (if (= x \0)
        (.toString (bit-or (* 16 (.random js/Math)) 0) 16)
        x))
    "00000000-0000-4000-0000-000000000000")))

;; window.onload ...
(.addEventListener js/window "load" hello false)
