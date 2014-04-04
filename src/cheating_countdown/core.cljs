(ns cheating-countdown.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {:count 1}))

(defn count-view [app owner]
  (reify
    om/IWillMount
    (will_mount[_]
      (js/setInterval
        (fn [] (om/transact! app :count inc))
        1000))
    om/IRender
    (render[_]
      (dom/div nil (:count app)))))

(om/root count-view app-state
  {:target (. js/document (getElementById "app"))})
