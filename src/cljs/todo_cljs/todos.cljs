(ns todo-cljs.todos)

;; helpers, constants...
(def ENTER_KEY 13)
(defn by-id [id] (.getElementById js/document id))
(defn hello[] (js/alert "hello!"))

(defn add-todo [text]
  (let [trimmed (.trim text)]
    (js/alert trimmed)))

(defn new-todo-handler [ev]
  (if (= ENTER_KEY (.-keyCode ev))
    (add-todo (.-value (by-id "new-todo")))))

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


(defn add-event-listeners []
  (.addEventListener (by-id "new-todo") "keypress" new-todo-handler false))

(defn window-load-handler []
  (add-event-listeners))

;; window.onload ...
(.addEventListener js/window "load" window-load-handler false)
