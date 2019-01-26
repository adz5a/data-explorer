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

(defonce app-state (atom {:data-str nil}))

(def app-dom-element (.getElementById js/document "app"))

(defn current-data-str []
  (let [current-value (get @app-state :data-str)]
    (if (string? current-value)
      [:p current-value]
      [:p "Insert some data in the text area."])))

(defn data-input []
  [:textarea {:style {:border "solid black"
                      :width "500px"
                      :height "100px"}
              :name "data-input"}])

(defn update-data [data]
  (swap! app-state assoc :data-str data))

(defn app []
  [:form {:onSubmit #(do
                       (.preventDefault %)
                       (-> %
                           (oget "target")
                           (oget "elements")
                           (oget "data-input")
                           (oget "value")
                           update-data))}
   [:h1 "Enter your data here"]
   [data-input]
   [current-data-str]
   [:input {:type "submit"
            :value "Update data!"}]])

(r/render
  [app]
  app-dom-element)
