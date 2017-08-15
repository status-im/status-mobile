(ns status-im.chat.events.animation
  (:require [re-frame.core :as re-frame]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

;;;; Helper fns

(defn set-expandable-height
  [{:keys [current-chat-id] :as db} key value]
  (-> db
      (assoc-in [:chat-animations current-chat-id key :height] value)
      (update-in [:chat-animations current-chat-id key :changes-counter] inc)))

(defn choose-predefined-expandable-height
  [{:keys [current-chat-id chat-ui-props layout-height] :as db} key preset]
  (if (= preset :max)
    (set-expandable-height db key :max)
    (let [input-height      (get-in chat-ui-props [current-chat-id :input-height])
          chat-input-margin (if platform/ios?
                              (get db :keyboard-height)
                              0)
          bottom            (+ input-height chat-input-margin)
          height            (case preset
                              :min input-utils/min-height
                              (input-utils/default-container-area-height bottom layout-height))]
      (set-expandable-height db key height))))

;;;; Handlers

(handlers/register-handler-db
  :set-expandable-height
  [re-frame/trim-v]
  (fn [db [key value]]
    (set-expandable-height db key value)))

(handlers/register-handler-db
  :choose-predefined-expandable-height
  [re-frame/trim-v]
  (fn [db [key preset]]
    (choose-predefined-expandable-height db key preset)))

(handlers/register-handler-db
  :fix-expandable-height
  [re-frame/trim-v]
  (fn [{:keys [current-chat-id chats chat-ui-props layout-height] :as db} [vy current key]]
    (let [input-height      (get-in chat-ui-props [current-chat-id :input-height])
          chat-input-margin (if platform/ios?
                              (get db :keyboard-height)
                              0)
          bottom            (+ input-height chat-input-margin)

          min-height        input-utils/min-height
          max-height        (input-utils/max-container-area-height bottom layout-height)
          default-height    (input-utils/default-container-area-height bottom layout-height)
          possible-values   [min-height default-height max-height]

          moving-down?      (pos? vy)
          closest-index     (->> possible-values
                                 (map-indexed vector)
                                 (sort-by (fn [[i v]] (Math/abs (- v current))))
                                 (ffirst))
          height            (cond (and moving-down? (not= closest-index 0))
                                  (get possible-values (dec closest-index))

                                  (and (not moving-down?) (not= closest-index 2))
                                  (get possible-values (inc closest-index))

                                  moving-down?
                                  min-height

                                  (not moving-down?)
                                  max-height)]
      (set-expandable-height db key height))))
