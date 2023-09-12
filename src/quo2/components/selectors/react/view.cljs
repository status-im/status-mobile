(ns quo2.components.selectors.react.view
  (:require [status-im2.constants :as constants]
            [quo2.components.selectors.react-selector.view :as react-selector]
            [quo2.components.selectors.react-selector-add.view :as react-selector-add]
            [quo2.components.selectors.react.style :as style]
            [react-native.core :as rn]))

(defn view
  [{:keys [reactions on-press on-long-press add-reaction? on-press-add pinned? container-style]}]
  [rn/view {:style (merge container-style style/container)}
   (for [emoji-reaction reactions
         :let           [{:keys [emoji-id emoji-reaction-id quantity own]} emoji-reaction]]
     [react-selector/view
      {:key                 emoji-reaction-id
       :emoji               (get constants/reactions emoji-id)
       :neutral?            own
       :pinned?             pinned?
       :container-style     style/reaction-container
       :clicks              quantity
       :on-press            #(on-press emoji-reaction)
       :on-long-press       #(on-long-press emoji-reaction)
       :accessibility-label (str "emoji-reaction-" emoji-id)}])
   (when add-reaction?
     [react-selector-add/view
      {:on-press on-press-add
       :pinned?  pinned?}])])
