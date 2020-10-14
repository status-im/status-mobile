(ns status-im.extensions.core
  (:require [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]))

(defn send-command [ext]
  (fn [params]
    (re-frame/dispatch [:send-extension-command {:id     (:id ext)
                                                 :params (.stringify js/JSON ^js params)}])))

(defn send-text-message [_]
  (fn [value]
    (re-frame/dispatch [:send-plain-text-message value])))

(re-frame/reg-fx
 ::init-extension
 (fn [ext]
   (doseq [hook (:hooks ext)]
     ((:init hook) #js {:sendCommand (send-command ext)
                        :sendTextMessage (send-text-message ext)
                        :close #(re-frame/dispatch [:bottom-sheet/hide])}))))

(fx/defn join-time-messages-checked-for-chats
  {:events [:switch-extension]}
  [{:keys [db]} {:keys [id] :as ext}]
  (merge
   {:db (update-in db [:extensions id] #(when-not % ext))}
   (when-not (get-in db [:extensions id])
     {::init-extension ext})))