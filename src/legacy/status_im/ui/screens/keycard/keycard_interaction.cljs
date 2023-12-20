(ns legacy.status-im.ui.screens.keycard.keycard-interaction
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.keycard.components.description :as description]
    [legacy.status-im.ui.screens.keycard.components.keycard-animation :refer [animated-circles]]
    [legacy.status-im.ui.screens.keycard.components.style :as styles]
    [legacy.status-im.ui.screens.keycard.components.turn-nfc :as turn-nfc]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]))

(def state->translations
  {:init       {:title       :t/keycard-init-title
                :description :t/keycard-init-description}
   :awaiting   {:title       :t/keycard-awaiting-title
                :description :t/keycard-awaiting-description}
   :processing {:title       :t/keycard-processing-title
                :description :t/keycard-processing-description}
   :connected  {:title       :t/keycard-connected-title
                :description :t/keycard-connected-description}
   :error      {:title       :t/keycard-error-title
                :description :t/keycard-error-description}
   :success    {:title       :t/keycard-success-title
                :description :t/keycard-success-description}})

(defn card-sync-flow
  []
  (let [state (reagent/atom nil)]
    (fn [{:keys [on-card-connected connected? on-card-disconnected params]}]
      (let [translation (or (get-in params [:state-translations @state])
                            (get state->translations @state))]
        [react/view
         {:style  styles/container-style
          :height 286}
         [react/view
          {:height        200
           :margin-bottom 20}
          [animated-circles
           {:state                state
            :connected?           connected?
            :on-card-disconnected on-card-disconnected
            :on-card-connected    on-card-connected}]]
         (when translation
           [description/animated-description
            {:title       (i18n/label (:title translation))
             :description (i18n/label (:description translation))}])]))))

(defn connect-keycard
  [{:keys [on-connect on-cancel
           connected? on-disconnect
           params]}]
  [react/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}
   (when on-cancel
     [react/touchable-highlight
      {:on-press on-cancel
       :style    {:position :absolute
                  :top      0
                  :right    0}}
      [react/text
       {:style {:line-height        22
                :padding-horizontal 16
                :color              colors/blue
                :text-align         :center}}
       (i18n/label :t/cancel)]])
   (when (:title params)
     [react/view
      {:style
       {:align-self :flex-start :padding-left 16 :margin-bottom 24 :position :absolute :top 0 :left 0}}
      [react/text {:style {:font-size (if (:small-screen? params) 15 17) :font-weight "700"}}
       (:title params)]])
   (when (:header params)
     [(:header params)])
   (if @(re-frame/subscribe [:keycard/nfc-enabled?])
     [card-sync-flow
      {:connected? connected?
       :params (select-keys params [:state-translations])
       :on-card-disconnected
       #(re-frame/dispatch [on-disconnect])
       :on-card-connected
       #(re-frame/dispatch [on-connect])}]
     [turn-nfc/turn-nfc-on])
   (when (:footer params)
     [(:footer params)])])
