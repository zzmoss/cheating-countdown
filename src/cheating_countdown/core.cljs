(ns cheating-countdown.core
  (:require-macros  [cljs.core.async.macros :refer  [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer  [put! chan <!]]
            [goog.Uri :as uri]) 
  (:import [goog Uri]))

(enable-console-print!)

(defn date-gen 
  ([] (js/Date.))
  ([date] (js/Date. date)))

(def deadline (.getParameterValue (Uri. (.-location js/window)) "date"))

(defn compare-dates [date-end date-start]
  (- (.getTime date-end) (.getTime date-start)))

(defn remaining [deadline]
  (compare-dates (date-gen deadline) (date-gen)))

(def units [(* 60 60 24) (* 60 60) 60 1])

(defn date-vec
  [millis]
  (loop [units units seconds (/ millis 1000) out []]
    (if (empty? units)
      (if (every? #(>= % 0) out) out (vec (repeat 4 0)))
      (let [unit (first units)
            unit-val (quot seconds unit)
            seconds (rem seconds unit)]
        (recur (rest units) seconds (conj out unit-val))))))

(defn update-deadline
  [owner new-deadline]
  (om/set-state! owner :deadline new-deadline))

(def app-state (atom {:remaining nil}))

(defn time-display [app]
  (dom/div #js {:className "app"} 
    (apply dom/ul #js {:className "timer"}
      (map #(dom/li #js {:className "time-box"} %) (date-vec (:remaining app))))))

(defn count-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      ;;If URL has a deadline update state to that deadline
      (when (not= deadline nil) (update-deadline owner deadline))
      
      ;;Infinite go loop that reads from deadline-chan when there's a 
      ;; click event and updates deadline 
      (let [deadline-chan (om/get-state owner :deadline-chan)]
        (go (while true (let [new-deadline (<! deadline-chan) ] 
                            (when (not= nil new-deadline)
                               (update-deadline owner new-deadline))))))
      
      ;;Update remaining time in the app state every one second.
      (om/set-state! owner :interval
        (js/setInterval
            #(om/update! app :remaining (remaining (om/get-state owner :deadline)))
          1000))) 

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :interval)))

    om/IRenderState
    (render-state[this {:keys [deadline-chan]}]
      (time-display app))))


(defn form-display [deadline-chan owner]
  (dom/div #js {:className "app"}
           (dom/input #js {:type "text" 
                           :id "datetimepicker" 
                           :ref "datetime" })
           (dom/button #js {:ref "datetime-submit"
                            :id "datetime-submit" 
                            :onClick (fn [] 
                                       (put! deadline-chan 
                                         (om/get-state owner :deadline-selected)))
                            } "Countdown")))


(defn datepicker-view [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      ( -> (om/get-node owner "datetime")
           js/jQuery
           (.datetimepicker 
             (clj->js {:onChangeDateTime 
                        (fn [datetime input]
                          (om/set-state! owner :deadline-selected datetime))}))))

    om/IRenderState
    (render-state [this {:keys [deadline-chan]}]
      (form-display deadline-chan owner))))

(defn countdown-timer [app owner]
  (reify

    om/IInitState
    (init-state [_]
      {:deadline-chan (chan)})
 
    om/IRenderState
    (render-state [this state]
      (dom/div nil 
               (dom/div nil (om/build datepicker-view app {:init-state state}))
               (dom/div nil (om/build count-view app {:init-state state}))))))

(om/root countdown-timer app-state
  {:target (. js/document (getElementById "app"))})
