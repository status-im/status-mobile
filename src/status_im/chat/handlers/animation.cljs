(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [status-im.components.animation :as anim]
            [status-im.components.drag-drop :as drag]
            [status-im.models.commands :as commands]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.chat.styles.plain-input :refer [input-height]]
            [status-im.chat.styles.response :refer [request-info-height response-height-normal]]
            [status-im.chat.styles.response-suggestions :as response-suggestions-styles]
            [status-im.constants :refer [response-input-hiding-duration]]))

(def zero-height input-height)

(register-handler :finish-animate-cancel-command
  (fn [db _]
    (assoc-in db [:animations :commands-input-is-switching?] false)))

(defn animate-cancel-command! [{{:keys [response-height-anim-value
                                        message-input-buttons-scale
                                        message-input-offset
                                        messages-offset-anim-value]} :animations}
                               [_ on-animation-stop]]
  (let [height-to-value zero-height]
    (anim/start (anim/spring response-height-anim-value {:toValue  height-to-value})
                (fn []
                  (dispatch [:finish-animate-cancel-command])
                  (on-animation-stop)))
    (anim/start (anim/timing message-input-buttons-scale {:toValue  1
                                                          :duration response-input-hiding-duration}))
    (anim/start (anim/timing message-input-offset {:toValue  0
                                                   :duration response-input-hiding-duration}))
    (anim/start (anim/spring messages-offset-anim-value {:toValue 0}))))

(register-handler :animate-cancel-command
    (after animate-cancel-command!)
    (fn [db _]
      (let [hiding? (get-in db [:animations :commands-input-is-switching?])]
        (if-not hiding?
          (assoc-in db [:animations :commands-input-is-switching?] true)
          db))))

(register-handler :finish-animate-response-resize
  (fn [db _]
    (let [fixed (get-in db [:animations :response-height-fixed])]
      (-> db
          (assoc-in [:animations :response-height] fixed)
          (assoc-in [:animations :response-resize?] false)))))

(register-handler :set-response-height
  (fn [db [_ value]]
    (assoc-in db [:animations :response-height] value)))

(defn animate-response-resize! [{{height-anim-value :response-height-anim-value
                                  from              :response-height
                                  to                :response-height-fixed} :animations}]
  (anim/remove-all-listeners height-anim-value)
  (anim/set-value height-anim-value from)
  (anim/add-listener height-anim-value
                     (fn [val]
                       (dispatch [:set-response-height (anim/value val)])))
  (anim/start (anim/spring height-anim-value {:toValue to})
              (fn []
                (anim/remove-all-listeners height-anim-value)
                (dispatch [:finish-animate-response-resize]))))

(register-handler :animate-response-resize
  (after animate-response-resize!)
  (fn [db _]
    (assoc-in db [:animations :response-resize?] true)))

(defn get-response-height [db]
  (let [command (commands/get-chat-command db)
        text (commands/get-chat-command-content db)
        suggestions (get-content-suggestions command text)
        suggestions-height (reduce + 0 (map #(if (:header %)
                                              response-suggestions-styles/header-height
                                              response-suggestions-styles/suggestion-height)
                                            suggestions))]
    (+ zero-height
       (min response-height-normal (+ suggestions-height request-info-height)))))

(defn update-response-height [db]
  (assoc-in db [:animations :response-height-fixed] (get-response-height db)))

(register-handler :finish-show-response!
  (fn [db _]
    (assoc-in db [:animations :commands-input-is-switching?] false)))

(defn animate-show-response! [{{scale-anim-value           :message-input-buttons-scale
                                input-offset-anim-value    :message-input-offset
                                messages-offset-anim-value :messages-offset-anim-value} :animations}]
  (let [to-value 0.1]
    (anim/start (anim/timing scale-anim-value {:toValue  to-value
                                               :duration response-input-hiding-duration})
                #(dispatch [:finish-show-response!]))
    (anim/start (anim/timing input-offset-anim-value {:toValue  -40
                                                      :duration response-input-hiding-duration}))
    (anim/start (anim/spring messages-offset-anim-value {:toValue request-info-height}))))

(register-handler :animate-show-response
  (after animate-show-response!)
  (fn [db _]
    (dispatch [:animate-response-resize])
    (-> db
        (assoc-in [:animations :commands-input-is-switching?] true)
        (assoc-in [:animations :response-height] zero-height)
        (update-response-height))))

(register-handler :set-response-max-height
  (fn [db [_ height]]
    (assoc-in db [:animations :response-height-max] height)))

(register-handler :on-drag-response
  (fn [db [_ dy]]
    (let [fixed (get-in db [:animations :response-height-fixed])]
      (assoc-in db [:animations :response-height] (- fixed dy)))))

(register-handler :fix-response-height
  (fn [db _]
    (let [current (get-in db [:animations :response-height])
          normal-height response-height-normal
          max-height (get-in db [:animations :response-height-max])
          delta (/ normal-height 2)
          new-fixed (cond
                      (<= current (+ zero-height delta)) (+ zero-height request-info-height)
                      (<= current (+ zero-height normal-height delta)) (get-response-height db)
                      :else max-height)]
      (dispatch [:animate-response-resize])
      (assoc-in db [:animations :response-height-fixed] new-fixed))))

(defn create-response-pan-responder []
  (drag/create-pan-responder
    {:on-move    (fn [e gesture]
                   (dispatch [:on-drag-response (.-dy gesture)]))
     :on-release (fn [e gesture]
                   (dispatch [:fix-response-height]))}))

(defn init-response-dragging [db]
  (assoc-in db [:animations :response-pan-responder] (create-response-pan-responder)))
