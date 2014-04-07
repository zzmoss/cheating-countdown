(ns cheating-countdown.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def ms-in-a-second 1000)
(def seconds-in-a-minute 60)
(def seconds-in-an-hour (* 60 seconds-in-a-minute))
(def seconds-in-a-day (* 24 seconds-in-an-hour))

(defn date-gen 
  ([] (js/Date.))
  ([date] (js/Date. date)))

(def deadline (date-gen "Mon Apr 27 2014 16:05:17 GMT-0400 (EDT)"))

(defn compare-dates [date-end date-start]
  (- (.getTime date-end) (.getTime date-start)))

(def units [(* 60 60 24) (* 60 60) 60 1])

(defn date-vec
  [millis]
  (loop [units units seconds (/ millis 1000) out []]
    (if (empty? units)
      out
      (let [unit (first units)
            unit-val (quot seconds unit)
            seconds (rem seconds unit)]
        (recur (rest units) seconds (conj out unit-val))))))

(def app-state (atom {:remaining nil}))

(defn display [app]
  (dom/div nil
    (apply dom/ul nil
      (map dom/li (repeat nil) (date-vec (:remaining app))))))

(defn count-view [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (js/setInterval
        #(om/update! app :remaining(compare-dates deadline (date-gen))) 1000)) 

    om/IRender
    (render[_]
      (display app))))

(om/root count-view app-state
  {:target (. js/document (getElementById "app"))})
