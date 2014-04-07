(ns cheating-countdown.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def ms-in-a-second 1000)
(def seconds-in-a-minute 60)
(def seconds-in-an-hour (* 60 seconds-in-a-minute))
(def seconds-in-a-day (* 24 seconds-in-an-hour))

(def total-time-to-elapse 108440)
(def days (quot total-time-to-elapse seconds-in-a-day))
(def hours (quot (rem total-time-to-elapse seconds-in-a-day) seconds-in-an-hour))
(def minutes (quot (rem total-time-to-elapse seconds-in-an-hour) seconds-in-a-minute))
(def seconds (rem total-time-to-elapse seconds-in-a-minute))

(println days hours minutes seconds)
(def app-state (atom  {:days days 
                       :hours hours 
                       :minutes minutes 
                       :seconds seconds}))

(defn display [app]
  (dom/div nil
           (dom/ul nil 
                   (dom/li nil (:days app))
                   (dom/li nil (:hours app))
                   (dom/li nil (:minutes app))
                   (dom/li nil (:seconds app)))))

(defn count-view [app owner]
  (reify
    om/IWillMount
    (will_mount[_]
      (js/setInterval
        (fn [] (om/transact! app :seconds dec))
        ms-in-a-second)
      (js/setInterval
        (fn [] (om/transact! app :minutes dec))
        (* ms-in-a-second seconds-in-a-minute))
      (js/setInterval
        (fn [] (om/transact! app :hours dec))
        (* ms-in-a-second seconds-in-an-hour))
      (js/setInterval
        (fn [] (om/transact! app :days dec))
        (* ms-in-a-second seconds-in-a-day)))
      
    om/IRender
    (render[_]
      (display app))))

(om/root count-view app-state
  {:target (. js/document (getElementById "app"))})
