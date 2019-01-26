(ns data-explorer.core
  "This namespace is the entry point for the 'data-explorer' app.

  This app is pretty simple and works this way: the page has a <textearea> on
  which you can paste whatever you want. The app will then try to call
  read-string on it and parse it as a clojure data structure. If it succeeds it
  will then offer a nice view of the data structure with controls to open/close
  nested structures and edit functionality.

  TODO:
  - load a clojure data structure from the page - OK
    - handle the failing case
    - handle the success case
  - display the data structure when success - OK
  - display the type of data on success - OK
  - display the data structure recursively - OK
  - create a load-as-json mode and a load-as-edn mode"
  (:require [reagent.core :as r :refer [atom]]
            [goog.object :refer [get] :rename {get oget}]
            [cljs.reader :refer [read-string]]
            [cljs.test :as t]
            [clojure.walk :refer [keywordize-keys]]))

(def not-nil? (complement nil?))

(defonce app-state (atom {:raw nil
                          :data nil
                          :display-path :all}))

(def app-dom-element (.getElementById js/document "app"))

(defn display-raw-data []
  [:article
   [:h1 "Raw data"]
   [:p (get @app-state :raw)]])

(defn get-data-type
  "Returns a symbol for each data type. Supports a subset of data types, see
  tests."
  [data]
  (cond
    (map? data) :map
    (vector? data) :vector
    (nil? data) :nil
    (number? data) :number
    (string? data) :string
    (keyword? data) :keyword
    (list? data) :list
    (set? data) :set
    (or (true? data)
        (false? data)) :boolean
    (symbol? data) :symbol
    :default :undefined))

(t/deftest test-get-data-type
  (t/is (= :map (get-data-type {})))
  (t/is (= :vector (get-data-type [])))
  (t/is (= :list (get-data-type ())))
  (t/is (= :set (get-data-type #{})))
  (t/is (= :number (get-data-type 1)))
  (t/is (= :keyword (get-data-type :yolo)))
  (t/is (= :string (get-data-type "1")))
  (t/is (= :boolean (get-data-type true)))
  (t/is (= :boolean (get-data-type false)))
  (t/is (= :nil (get-data-type nil))))

(comment
  (t/run-tests))

(defmulti display-data get-data-type)

(defn toggle-mode! [state]
  (swap! state update :edit-mode not))

(defn toggle-visible! [state]
  (swap! state update :visible not))

(defn edit-tooltip
  [state]
  [:span {:className "edit-tooltip"
          :onClick (partial toggle-mode! state)} "edit"])

(defn collapse-button
  [state]
  [:span {:className "collapse-button"
          :onClick (partial toggle-visible! state)} "v"])

(defn edit-node
  [data path state]
  [:form {:style {:display "inline-block"}}
   [:input {:type "text"
            :name "data-input"
            :defaultValue (str data)}]
   [:input {:type "button"
            :value "Edit!"
            :onClick #(let [raw-data (-> %
                                         (oget "target")
                                         (oget "form")
                                         (oget "elements")
                                         (oget "data-input")
                                         (oget "value"))
                            data (try
                                   (hash-map :value (->> raw-data
                                                         read-string))
                                   (catch js/Error error
                                     {:error (oget error "message")}))
                            update-path (apply conj [:data :value] path)]
                        (if (:value data)
                          (do
                            (swap! app-state assoc-in update-path (:value data))
                            (toggle-mode! state))
                          (js/alert (str "Error " (:error data)))))}]])

(defn display
  [data path]
  (let [local-state (atom {:edit-mode false
                           :visible true})]
    (fn [data path]
      [:div {:className "data-wrapper"}
       (if (:edit-mode @local-state)
         [edit-node data path local-state]
         [display-data data path local-state])
       [edit-tooltip local-state]
       [collapse-button local-state]])))

(defmethod display-data :map
  [map-data path local-state]
  (if (:visible @local-state)
    [:ol {:className "data-map"}
     (->> map-data
          (sort-by first)
          (map (fn [[k v]]
                 ^{:key k}[:li
                           [:span {:className "data-map-key"} (name k)]
                           [display v (conj path k)]])))]
    [:ol {:className "data-map data-map-collapsed"}]))

(defmethod display-data :string
  [data path]
  [:span {:className "data-string"} data])

(defmethod display-data :vector
  [data path local-state]
  (if (:visible @local-state)
    [:ul {:className "data-vector"}
     (map (fn [data index]
            ^{:key index}[:li
                          [:span {:className "data-vector-index"} index]
                          [display data (conj path index)]])
          data
          (range))]
    [:ul {:className "data-vector data-vector-collapsed"}]))

(defmethod display-data :boolean
  [data path]
  [:span {:className "data-boolean"} (str data)])

(defmethod display-data :keyword
  [data path]
  [:span {:className "data-keyword"} (name data)])

(defmethod display-data :symbol
  [data path]
  [:span {:className "data-symbol"} (str data)])

(defmethod display-data :number
  [data path]
  [:span {:className "data-number"} (str data)])

(defmethod display-data :nil
  [data path]
  [:span {:className "data-nil"} "nil"])

(defmethod display-data :default
  [data path]
  [:span {:style {:padding "0.5rem"
                  :font-weight "bold"}}
   (str "Not implemented yet : " (name (get-data-type data)))])

(defn collapse-all [_click-event]
  (swap! app-state assoc :display-path :hide))

(defn expand-all [_click-event]
  (swap! app-state assoc :display-path :all))

(defn visualizer [data]
  (let [t (get-data-type data)]
    [:section
     [:h1 "Visualizer"]
     [display
      data
      []]]))

(defn data-explorer []
  [:section (let [{:keys [error value]} (:data @app-state)]
        (cond
          (not-nil? value) [visualizer value]
          (not-nil? error) "Error on parsing"
          :default "Wut wut"))])

(defn load-json [click-event]
  (let [raw-data (-> click-event
                     (oget "target")
                     (oget "form")
                     (oget "elements")
                     (oget "data-input")
                     (oget "value"))
        data (try
               (hash-map :value (->> raw-data
                                    (.parse js/JSON)
                                    js->clj
                                    keywordize-keys))
               (catch js/Error error
                 {:error (oget error "message")}))]
    (swap! app-state assoc
           :raw raw-data
           :data data)))

(defn load-edn [click-event]
  (let [raw-data (-> click-event
                     (oget "target")
                     (oget "form")
                     (oget "elements")
                     (oget "data-input")
                     (oget "value"))
        data (try
               (hash-map :value (->> raw-data
                                     read-string))
               (catch js/Error error
                 {:error (oget error "message")}))]
    (swap! app-state assoc
           :raw raw-data
           :data data)))

(defn data-input-form []
  [:form
   [:h1 "Enter your data here"]
   [:textarea {:style {:border "solid black"
                       :width "500px"
                       :height "100px"
                       :display "block"}
               :name "data-input"}]
   [:input {:type "button"
            :value "Load as JSON"
            :onClick load-json}]
   [:input {:type "button"
            :value "Load as EDN"
            :onClick load-edn}]])

(defn app []
  [:section
   [data-input-form]
   [:section
    [data-explorer]
    [display-raw-data]]])

(r/render
  [app]
  app-dom-element)
