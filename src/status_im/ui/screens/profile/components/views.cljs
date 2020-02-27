(ns status-im.ui.screens.profile.components.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.screens.profile.user.sheet.views :as sheets]
            [status-im.ui.screens.profile.components.styles :as styles]))

;; profile header elements

(defn- profile-name-input [name on-change-text-event & [props]]
  [react/view
   [react/text-input
    (merge {:style               styles/profile-name-input-text
            :placeholder         ""
            :default-value       name
            :auto-focus          true
            :on-change-text      #(when on-change-text-event
                                    (re-frame/dispatch [on-change-text-event %]))
            :accessibility-label :username-input}
           props)]])

(defn- names [{:keys [usernames name public-key] :as contact}]
  (let [generated-name (when public-key (gfy/generate-gfy public-key))
        with-subtitle? (seq usernames)]
    [react/view (if with-subtitle? styles/profile-header-name-container-with-subtitle
                    styles/profile-header-name-container)
     [react/text {:style (if with-subtitle? styles/profile-name-text-with-subtitle
                             styles/profile-name-text)
                  :number-of-lines 2
                  :ellipsize-mode  :tail}

      (multiaccounts/displayed-name contact)]
     (when with-subtitle?
       [react/text {:style           styles/profile-three-words
                    :number-of-lines 1}
        generated-name])]))

(defn- profile-header-display [{:keys [name public-key] :as contact}
                               allow-icon-change? include-remove-action?]
  [react/view (merge styles/profile-header-display {:padding-horizontal 16})
   (if allow-icon-change?
     [react/view {:align-items     :center
                  :align-self      :stretch
                  :justify-content :center}
      [react/touchable-highlight
       {:accessibility-label :edit-profile-photo-button
        :on-press
        #(re-frame/dispatch
          [:bottom-sheet/show-sheet
           {:content        (sheets/profile-icon-actions include-remove-action?)
            :content-height (if include-remove-action? 192 128)}])}
       [react/view
        [react/view {:background-color colors/white
                     :border-radius    15
                     :width            30
                     :height           30
                     :justify-content  :center
                     :align-items      :center
                     :position         :absolute
                     :z-index          1
                     :top              -5
                     :right            -5}
         [react/view {:background-color colors/blue
                      :border-radius    12
                      :width            24
                      :height           24
                      :justify-content  :center
                      :align-items      :center}
          [vector-icons/icon :tiny-edit {:color  colors/white
                                         :width  16
                                         :height 16}]]]
        [chat-icon.screen/my-profile-icon {:multiaccount contact
                                           :edit?        false}]]]]
     ;; else
     [chat-icon.screen/my-profile-icon {:multiaccount contact
                                        :edit?        false}])
   [names contact]])

(defn group-header-display [{:keys [chat-name color contacts]}]
  [react/view (merge styles/profile-header-display {:padding-horizontal 16})
   [chat-icon.screen/profile-icon-view nil chat-name color nil 64 nil]
   [react/view styles/profile-header-name-container
    [react/text {:style           styles/profile-name-text
                 :number-of-lines 2
                 :ellipsize-mode  :tail}
     chat-name]
    [react/view {:style {:flex-direction :row
                         :align-items    :center}}
     [vector-icons/icon :icons/tiny-group {:color           colors/gray
                                           :width           16
                                           :height          16
                                           :container-style {:margin-right 4}}]
     [react/text {:style {:line-height 22
                          :color       colors/gray}}
      (i18n/label :t/members-count {:count (count contacts)})]]]])

(defn profile-header
  [{:keys [contact allow-icon-change? include-remove-action?]}]
  [profile-header-display contact allow-icon-change? include-remove-action?])

;; settings items elements

(defn settings-item-separator []
  [common/separator styles/settings-item-separator])

(defn settings-title [title]
  [react/text {:style styles/settings-title}
   title])

(defn settings-item
  [{:keys [item-text label-kw value action-fn active? destructive? hide-arrow?
           accessibility-label icon icon-content]
    :or   {value "" active? true}}]
  [react/touchable-highlight
   (cond-> {:on-press action-fn
            :disabled (not active?)}
     accessibility-label
     (assoc :accessibility-label accessibility-label))
   [react/view styles/settings-item
    (when icon
      [react/view styles/settings-item-icon
       [vector-icons/icon icon {:color colors/blue}]])
    [react/view styles/settings-item-text-wrapper
     [react/text {:style (merge styles/settings-item-text
                                (when destructive?
                                  styles/settings-item-destructive)
                                (when-not active?
                                  styles/settings-item-disabled)
                                (when icon
                                  {:font-size 17}))
                  :number-of-lines 1}
      (or item-text (i18n/label label-kw))]
     (when-not (string/blank? value)
       [react/text {:style           styles/settings-item-value
                    :number-of-lines 1}
        value])]
    (if icon-content
      icon-content
      (when (and active? (not hide-arrow?))
        [vector-icons/icon :main-icons/next {:color colors/gray-transparent-40}]))]])

(defn settings-switch-item
  [{:keys [label-kw value action-fn active?]
    :or {active? true}}]
  [react/view styles/settings-item
   [react/view styles/settings-item-text-wrapper
    [react/i18n-text {:style styles/settings-item-text :key label-kw}]]
   [react/switch {:track-color     #js {:true colors/blue :false nil}
                  :value           (boolean value)
                  :on-value-change action-fn
                  :disabled        (not active?)}]])
