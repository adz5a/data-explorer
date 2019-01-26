(ns data-explorer.core
  "This namespace is the entry point for the 'data-explorer' app.

  This app is pretty simple and works this way: the page has a <textearea> on
  which you can paste whatever you want. The app will then try to call
  read-string on it and parse it as a clojure data structure. If it succeeds it
  will then offer a nice view of the data structure with controls to open/close
  nested structures and edit functionality.
  
  TODO:
  - load a clojure data structure from the page
    - handle the failing case
    - handle the success case
  - display the data structure when success"
  (:require [reagent.core :as r :refer [atom]]
            [goog.object :refer [get] :rename {get oget}]))

(defonce app-state (atom {:raw nil}))

(def app-dom-element (.getElementById js/document "app"))

(defn update-data [data]
  (swap! app-state assoc :raw data))

(defn display-raw-data []
  [:p (get @app-state :raw)])

(defn data-explorer []
  [:p "here goes the visualizer"])

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
