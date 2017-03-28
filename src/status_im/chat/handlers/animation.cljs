(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [after dispatch subscribe debug path]]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(handlers/register-handler
  :set-expandable-height
  (fn [{:keys [current-chat-id] :as db} [_ key value]]
    (-> db
        (assoc-in [:chat-animations current-chat-id key :height] value)
        (update-in [:chat-animations current-chat-id key :changes-counter] inc))))

(handlers/register-handler
  :hide-expandable
  (handlers/side-effect!
    (fn [_ [_ key]]
      (dispatch [:set-expandable-height key 1]))))

(handlers/register-handler
  :choose-predefined-expandable-height
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chat-ui-props layout-height] :as db} [_ key preset]]
      (let [input-height      (get-in chat-ui-props [current-chat-id :input-height])
            chat-input-margin (if platform/ios?
                                (get db :keyboard-height)
                                0)
            bottom            (+ input-height chat-input-margin)
            height            (case preset
                                :min input-utils/min-height
                                :max (input-utils/max-container-area-height bottom layout-height)
                                (input-utils/default-container-area-height bottom layout-height))]
        (dispatch [:set-expandable-height key height])))))

(handlers/register-handler
  :fix-expandable-height
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chats chat-ui-props layout-height] :as db} [_ vy current key]]
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
        (dispatch [:set-expandable-height key height])))))

