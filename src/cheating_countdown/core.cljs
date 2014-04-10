(ns cheating-countdown.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.Uri :as uri]) 
  (:import [goog Uri]))

(enable-console-print!)

(defn date-gen 
  ([] (js/Date.))
  ([date] (js/Date. date)))

(def deadline (date-gen (.getParameterValue (Uri. (.-location js/window)) "date")))

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

(defn time-display [app]
  (dom/div nil
    (apply dom/ul nil
      (map dom/li (repeat nil) (date-vec (:remaining app))))))

(defn count-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (om/update! app :remaining (compare-dates deadline (date-gen)))
      (om/set-state! owner :interval
        (js/setInterval
          #(om/update! app :remaining (compare-dates deadline (date-gen)))
          1000))) 

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :interval)))

    om/IRender
    (render[_]
      (time-display app))))

(defn testtest 
  [dp input]
  (println dp))

(defn form-display [app]
  (dom/div nil
           (dom/input #js {:type "text" 
                           :id "datetimepicker" 
                           :ref "datetime" })))


(defn datepicker-view [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      ( -> (om/get-node owner "datetime")
           js/jQuery
           (.datetimepicker (clj->js {:onChangeDateTime testtest}))))

    om/IRender
    (render [_]
      (form-display app))))

(om/root count-view app-state
  {:target (. js/document (getElementById "app1"))})

(om/root datepicker-view app-state
  {:target (. js/document (getElementById "app0"))})
