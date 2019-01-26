(ns data-explorer.core
  (:require [reagent.core :as r :refer [atom]]))

(def app-dom-element (.getElementById js/document "app"))

(defn hello [name]
  [:div (str "hello " name)])

(r/render
  [hello "antoine"]
  app-dom-element)
