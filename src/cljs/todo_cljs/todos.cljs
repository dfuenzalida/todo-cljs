(ns todo-cljs.todos)

;; Simple test
(defn hello[]
  (js/alert "Hello!"))

(defn ^:export init []
  (hello))
