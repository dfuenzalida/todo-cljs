(ns todo-cljs.todos)

;; helpers, constants...
(def ENTER_KEY 13)
(defn by-id [id] (.getElementById js/document id))
(defn hello[] (js/alert "hello!"))
(def todo-list (atom []))
(def stat (atom {}))

(defn change-toggle-all-checkbox-state [] )

(defn save-todos []
  (.setItem js/localStorage "todos-cljs"
            (.stringify js/JSON (clj->js @todo-list))))

(defn compute-stats []
  (let [total     (count @todo-list)
        completed (count (filter #(= true (% "completed")) @todo-list))
        left      (- total completed)]
    (swap! stat conj {:total total :completed completed :left left})))

(defn refresh-data []
  (do
    (save-todos)
    (compute-stats)
    ;; (redraw-todos-ui)
    ;; (redraw-status-ui)
    (change-toggle-all-checkbox-state)))

(defn add-todo [text]
  (let [trimmed (.trim text)]
    (if (> (count trimmed) 0)
      (do
        (swap! todo-list conj {"title" trimmed, "completed" false})
        (refresh-data)))))

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
