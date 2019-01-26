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
  - display the type of data on success"
  (:require [reagent.core :as r :refer [atom]]
            [goog.object :refer [get] :rename {get oget}]
            [cljs.reader :refer [read-string]]
            [cljs.test :as t]))

(def not-nil? (complement nil?))

(defonce app-state (atom {:raw nil
                          :data nil}))

(def app-dom-element (.getElementById js/document "app"))

(defn update-data [data]
  (let [cljs-data (try
                    (hash-map :value (read-string data))
                    (catch js/Error error
                      {:error error}))]
    (swap! app-state assoc
           :raw data
           :data cljs-data)))

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
    (list? data) :list
    (set? data) :set
    (or (true? data)
        (false? data)) :boolean))

(t/deftest test-get-data-type
  (t/is (= :map (get-data-type {})))
  (t/is (= :vector (get-data-type [])))
  (t/is (= :list (get-data-type ())))
  (t/is (= :set (get-data-type #{})))
  (t/is (= :number (get-data-type 1)))
  (t/is (= :string (get-data-type "1")))
  (t/is (= :boolean (get-data-type true)))
  (t/is (= :boolean (get-data-type false)))
  (t/is (= :nil (get-data-type nil))))

(defn visualizer [data]
  (let [t (get-data-type data)]
    [:article
     [:h1 "Visualizer"]
     [:p (str "Data is of type " (name t))]]))

(defn data-explorer []
  [:p (let [{:keys [error value]} (:data @app-state)]
        (cond
          (not-nil? value) [visualizer value]
          (not-nil? error) "Error on parsing"
          :default "Wut wut"))])

(defn display-data []
  [:section
   [:h1 "Visualizer"]
   [display-raw-data]
   [data-explorer]])

(defn data-input-form []
  [:form {:onSubmit #(do
                       (.preventDefault %)
                       (-> %
                           (oget "target")
                           (oget "elements")
                           (oget "data-input")
                           (oget "value")
                           update-data))}
   [:h1 "Enter your data here"]
   [:textarea {:style {:border "solid black"
                       :width "500px"
                       :height "100px"
                       :display "block"}
               :name "data-input"}]
   [:input {:type "submit"
            :value "Update data!"}]])


(defn app []
  [:section
   [data-input-form]
   [display-data]])

(r/render
  [app]
  app-dom-element)
