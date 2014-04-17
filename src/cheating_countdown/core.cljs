(ns cheating-countdown.core 
  (:require-macros  [cljs.core.async.macros :refer  [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer  [put! chan <!]]
            [goog.Uri :as uri])
  (:import [goog Uri]))

(enable-console-print!)

;;Defining app state
(def app-state (atom {:deadline nil
                      :drunk false }))

;;Date related helpers
(defn date-gen 
  ([] (js/Date.))
  ([date] (js/Date. date)))

(defn random-date
  [deadline]
  (let [start (date-gen)
        end (date-gen deadline)]
    (date-gen (+  (.getTime start) (* (Math/random) (- (.getTime end) (.getTime start)) )))))

(defn compare-dates 
  [date-end date-start]
  (- (.getTime date-end) (.getTime date-start)))

(defn remaining
  [deadline random]
  (if random 
    (compare-dates (date-gen deadline) (random-date deadline))
    (compare-dates (date-gen deadline) (date-gen))))

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

;;DOM related helpers

(defn reset-dom-elements 
  [app]
  (do
    (-> (js/jQuery "#check-drunk") (.prop "checked" (:drunk app)))
    (-> (js/jQuery "#datetimepicker") (.val (:deadline app)))))

(defn is-checked []
  (-> (js/jQuery "#check-drunk") (.prop "checked")))

;;Undo related helpers

(def app-history (atom [@app-state]))

(add-watch app-state :history
  (fn [_ _ _ n]
    (when-not (= (last @app-history) n)
      (swap! app-history conj n))))

(defn undo
  [event]
  (when (> (count @app-history) 1)
    (swap! app-history pop)
    (reset! app-state (last @app-history))
    (reset-dom-elements (last @app-history))))
        
;;Read deadline from URL
(def deadline (.getParameterValue (Uri. (.-location js/window)) "date"))

;;Helper to update deadline with new value
(defn update-deadline
  [app deadline new-deadline]
  (do
    (-> (js/jQuery "#deadline-display") (.html deadline))
    (om/update! app :deadline new-deadline)))

;;Display code for timer component
(defn time-display 
  [app owner]
  (dom/div #js {:className "component"} 
    (apply dom/ul #js {:className "timer"}
      (map #(dom/li #js {:className "time-box"} %) (date-vec (om/get-state owner :remaining))))))

;;Timer component
(defn count-view 
  [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      ;;If URL has a deadline update state to that deadline
      (when (not= deadline nil) (update-deadline app (:deadline app) deadline))
      
      ;;Infinite go loop that reads from deadline-chan when there's a 
      ;; click event and updates deadline 
      (let [deadline-chan (om/get-state owner :deadline-chan)]
        (go (while true (let [new-deadline (<! deadline-chan) ] 
                            (when (not= nil new-deadline)
                               (update-deadline app (:deadline @app) new-deadline))))))
      
      ;;Update remaining time in the app state every one second.
      (om/set-state! owner :interval
        (js/setInterval
            #(om/set-state! owner :remaining (remaining (:deadline @app) (:drunk @app)))
          1000))) 

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :interval)))

    om/IRenderState
    (render-state[this {:keys [deadline-chan]}]
      (time-display app owner))))

;;Display code for datetime chooser component
(defn form-display 
  [deadline-chan owner]
  (dom/div #js {:className "component"}
           (dom/input #js {:type "text" 
                           :id "datetimepicker" 
                           :ref "datetime" })
           (dom/button #js {:ref "datetime-submit"
                            :id "datetime-submit" 
                            :onClick (fn [] 
                                       (put! deadline-chan 
                                         (om/get-state owner :deadline-selected)))
                            } "Countdown")))

;;DatePicker form component
(defn datepicker-view 
  [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      ( -> (om/get-node owner "datetime")
           js/jQuery
           (.datetimepicker 
             (clj->js {:step 30 
                       :onChangeDateTime 
                        (fn [datetime input]
                          (om/set-state! owner :deadline-selected (-> input js/jQuery .val)))}))))

    om/IRenderState
    (render-state [this {:keys [deadline-chan]}]
      (form-display deadline-chan owner))))


;;Root component, composed of timer and form
(defn countdown-timer 
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:deadline-chan (chan)})
 
    om/IRenderState
    (render-state [this state]
      (dom/div nil 
         (dom/div nil (om/build count-view app {:init-state state}))
         (dom/div nil (om/build datepicker-view app {:init-state state}))
         (dom/div #js {:className "component spacer"} 
            (dom/span #js {} "Counting down to: ")
            (dom/span #js {:id "deadline-display"}
              (when (not= nil (:deadline app)) (str (date-gen (:deadline app)))))
            (dom/span nil (dom/button #js {:onClick undo} "Undo"))
            (dom/span nil 
              (dom/input #js {:type "checkbox" 
                              :id "check-drunk" 
                              :onClick 
                                (fn []
                                  (om/update! app :drunk (is-checked))) } "Drunk")))))))

(om/root countdown-timer app-state
  {:target (. js/document (getElementById "app"))})
