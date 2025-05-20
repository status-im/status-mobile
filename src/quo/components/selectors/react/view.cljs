(ns quo.components.selectors.react.view
  (:require
    [quo.components.selectors.react-selector.view :as react-selector]
    [quo.components.selectors.react.style :as style]
    [react-native.core :as rn]))

(defn view
  [{:keys [reactions on-press on-long-press hide-new-reaction-button? on-press-add use-case
           container-style]}]
  [rn/view {:style (merge style/container container-style)}
   (for [emoji-reaction reactions
         :let           [{:keys [emoji emoji-id emoji-reaction-id quantity own]} emoji-reaction]]
     [react-selector/view
      {:key                 emoji-reaction-id
       :emoji               emoji
       :state               (if own :pressed :not-pressed)
       :use-case            use-case
       :container-style     style/reaction-container
       :clicks              quantity
       :on-press            #(on-press emoji-reaction)
       :on-long-press       #(on-long-press emoji-reaction)
       :accessibility-label (str "emoji-reaction-" emoji-id)}])
   (when-not hide-new-reaction-button?
     [react-selector/view
      {:on-press            on-press-add
       :state               :add-reaction
       :use-case            use-case
       :accessibility-label "emoji-add-reaction"}])])
