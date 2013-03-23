(ns todo-cljs.todos
  (:require [clojure.browser.repl :as repl]
            [clojure.browser.dom  :as dom]
            [clojure.browser.event :as ev]))

;; Constants, Helpers and State

(def ENTER_KEY 13)
(def STORAGE_NAME "todos-cljs")
(defn by-id [id] (dom/get-element id))
(def todo-list (atom [])) ;; all the application state in this vector

;; State management

(defn save-todos []
  (.setItem js/localStorage STORAGE_NAME
            (.stringify js/JSON (clj->js @todo-list))))

(defn load-todos []
  (if (not (seq (.getItem js/localStorage STORAGE_NAME)))
    (do
      (reset! todo-list [])
      (save-todos)))
  (reset! todo-list
         (js->clj (.parse js/JSON (.getItem js/localStorage STORAGE_NAME)))))

;; now this fn is called whenever info about the status is needed
(defn stats []
  (let [total     (count @todo-list)
        completed (count (filter #(= true (% "completed")) @todo-list))
        left      (- total completed)]
    {:total total :completed completed :left left}))

;; HELPER: updates a todo by its id, changes puts a new val for the attr
(defn update-attr [id attr val]
  (let [updated
        (vec (map #(if (= (% "id") id) (conj % {attr val}) %) @todo-list))]
    (reset! todo-list updated)))

(defn remove-todo-by-id [id]
  (reset! todo-list
          (vec (filter #(not= (% "id") id) @todo-list))))

;; UI and handlers

(declare refresh-data)

(defn delete-click-handler [ev]
  (let [id (.getAttribute (.-target ev) "data-todo-id")]
    (remove-todo-by-id id)
    (refresh-data)))

(defn checkbox-change-handler [ev]
  (let [checkbox (.-target ev)
        id       (.getAttribute checkbox "data-todo-id")
        checked  (.-checked checkbox)]
    (update-attr id "completed" checked)
    (refresh-data)))

(defn todo-content-handler [ev]
  (let [id    (.getAttribute (.-target ev) "data-todo-id")
        div   (by-id (str "li_" id))
        input (by-id (str "input_" id))]
    (set! (.-className div) "editing")
    (.focus input)))

(defn input-todo-key-handler [ev]
  (let [input (.-target ev)
        text  (.trim (.-value input))
        id    (apply str (drop 6 (.-id input)))]
    (if (seq text)
      (if (= ENTER_KEY (.-keyCode ev))
        (do
          (update-attr id "title" text)
          (refresh-data)))
      (do
        (remove-todo-by-id id)
        (refresh-data)))))

(defn input-todo-blur-handler [ev]
  (let [input (.-target ev)
        text  (.trim (.-value input))
        id    (apply str (drop 6 (.-id input)))]
    (do
      (update-attr id "title" text)
      (refresh-data))))

(defn redraw-todos-ui []
  (let [ul (by-id "todo-list")]
    (set! (.-innerHTML ul) "")
    (set! (.-value (by-id "new-todo")) "")
    (dorun ;; materialize lazy list returned by map below
     (map
      (fn [todo]
        (let [li              (dom/element :li)
              checkbox        (dom/element :input)
              label           (dom/element :label)
              delete-link     (dom/element :button)
              div-display     (dom/element :div)
              input-edit-todo (dom/element :input)]

          (dom/set-properties checkbox
            {"class" "toggle" "data-todo-id" (todo "id") "type" "checkbox" })
          (ev/listen checkbox "change" checkbox-change-handler)

          (dom/set-properties label {"data-todo-id" (todo "id")})
          (dom/set-text label (todo "title"))
          (ev/listen label "dblclick" todo-content-handler)

          (dom/set-properties delete-link
            {"class" "destroy" "data-todo-id" (todo "id")})
          (ev/listen delete-link "click" delete-click-handler)

          (dom/set-properties div-display
            {"class" "view" "data-todo-id" (todo "id")})
          (dom/append div-display checkbox label delete-link)

          (dom/set-properties input-edit-todo
            {"id" (str "input_" (todo "id")) "class" "edit"})
          (dom/set-value input-edit-todo (todo "title"))
          (ev/listen input-edit-todo "keypress" input-todo-key-handler)
          (ev/listen input-edit-todo "blur" input-todo-blur-handler)

          (dom/set-properties li {"id" (str "li_" (todo "id"))})
          (dom/append li div-display input-edit-todo)

          (if (todo "completed")
            (do
              (dom/set-properties li {"class" "complete"})
              (dom/set-properties checkbox {"checked" true})))

          (dom/append ul li)))
      @todo-list))))

(defn draw-todo-count []
  (let [stat (stats)
        number (dom/element :strong)
        remaining (dom/element :span)
        text (str " " (if (= 1 (:left stat)) "item" "items") " left")
        footer (by-id "footer")]
    (set! (.-innerHTML number) (:left stat))
    (set! (.-id remaining) "todo-count")
    (dom/append remaining number)
    (dom/append remaining (.createTextNode js/document text))
    (dom/append footer remaining)))

(defn clear-click-handler []
  (reset! todo-list (filter #(not (% "completed")) @todo-list))
  (refresh-data))

(defn draw-todo-clear []
  (let [button (dom/element :button)
        footer (by-id "footer")]
    (set! (.-id button) "clear-completed")
    (ev/listen button "click" clear-click-handler)
    (set! (.-innerHTML button)
          (str "Clear completed (" (:completed (stats)) ")"))
    (dom/append footer button)))

(defn redraw-status-ui []
  (let [footer  (by-id "footer")
        display (if (empty? @todo-list) "none" "block")
        stat (stats)]
    (set! (.-innerHTML footer) "")
    (set! (.-display (.-style (by-id "footer"))) display)
    (if (not= 0 (:completed stat)) (draw-todo-clear))
    (if (not= 0 (:total stat)) (draw-todo-count))))

(defn change-toggle-all-checkbox-state []
  (let [toggle-all  (by-id "toggle-all")
        all-checked (every? #(= true (% "completed")) @todo-list)]
    (set! (.-checked toggle-all) all-checked)))

(defn refresh-data []
  (save-todos)
  ;; (compute-stats)
  (redraw-todos-ui)
  (redraw-status-ui)
  (change-toggle-all-checkbox-state))

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

(defn add-todo [text]
  (let [trimmed (.trim text)]
    (if (seq trimmed)
      (do
        (swap! todo-list conj {"id" (get-uuid) "title" trimmed, "completed" false})
        (refresh-data)))))

(defn new-todo-handler [ev]
  (if (= ENTER_KEY (.-keyCode ev))
    (add-todo (.-value (by-id "new-todo")))))

(defn toggle-all-handler [ev]
  (let [checked (.-checked (.-target ev))
        toggled (map #(assoc % "completed" checked) @todo-list)]
    (reset! todo-list toggled)
    (refresh-data)))

(defn add-event-listeners []
  (ev/listen (by-id "new-todo") "keypress" new-todo-handler)
  (ev/listen (by-id "toggle-all") "change" toggle-all-handler)
)

(defn window-load-handler []
  (load-todos)
  (refresh-data)
  (add-event-listeners))

;; Launch window-load-handler when window loads
;; -- not sure why (ev/listen js/window "load" fn) does not work
(goog.events/listen js/window "load" window-load-handler)

;; To connect a browser-attached repl:
;; (repl/connect "http://localhost:9000/repl")

;; Debugging:
;; (in-ns 'todo-cljs.todos)
;; (add-todo "one")
;; (add-todo "two")
;; (add-todo "three")
;; (map #(js/alert %) @todo-list)
